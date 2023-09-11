package controller

import (
	"context"
	"time"

	. "github.com/onsi/ginkgo/v2"
	. "github.com/onsi/gomega"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/client"

	"github.com/sofastack/sofa-serverless/api/v1alpha1"
	"github.com/sofastack/sofa-serverless/internal/constants/finalizer"
	"github.com/sofastack/sofa-serverless/internal/constants/label"
	"github.com/sofastack/sofa-serverless/internal/utils"
)

var _ = Describe("ModuleReplicaSet Controller", func() {
	const timeout = time.Second * 300
	const interval = time.Second * 3

	moduleReplicaSetName := "test-module-replicaset"
	namespaceName := "module-replicaset-controller-namespace"
	podName := "test-pod-for-replicaset"

	Context("create module replicaset", func() {
		It("create success and replica is 1", func() {
			namespace := prepareNamespace(namespaceName)
			Expect(k8sClient.Create(context.TODO(), &namespace)).Should(Succeed())
			pod := preparePod(namespaceName, podName)
			Expect(k8sClient.Create(context.TODO(), &pod)).Should(Succeed())
			moduleReplicaSet := prepareModuleReplicaSet(namespaceName, moduleReplicaSetName)
			utils.AddFinalizer(&moduleReplicaSet.ObjectMeta, finalizer.ModuleExistedFinalizer)
			Expect(k8sClient.Create(context.TODO(), &moduleReplicaSet)).Should(Succeed())
			Eventually(func() bool {
				key := types.NamespacedName{
					Name:      moduleReplicaSetName,
					Namespace: namespaceName,
				}
				var newModuleReplicaSet v1alpha1.ModuleReplicaSet
				k8sClient.Get(context.TODO(), key, &newModuleReplicaSet)
				if newModuleReplicaSet.Spec.Replicas == 1 {
					selector, err := metav1.LabelSelectorAsSelector(&newModuleReplicaSet.Spec.Selector)
					modules := &v1alpha1.ModuleList{}
					err = k8sClient.List(context.TODO(), modules, &client.ListOptions{Namespace: newModuleReplicaSet.Namespace, LabelSelector: selector})
					if err == nil && len(modules.Items) == 1 {
						return true
					}
				}
				return false
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("update module version in replicaset", func() {
		It("module version is updated", func() {
			key := types.NamespacedName{
				Name:      moduleReplicaSetName,
				Namespace: namespaceName,
			}
			var newModuleReplicaSet v1alpha1.ModuleReplicaSet
			k8sClient.Get(context.TODO(), key, &newModuleReplicaSet)
			newModuleReplicaSet.Spec.Template.Spec.Module.Version = "1.0.1"
			Expect(k8sClient.Update(context.TODO(), &newModuleReplicaSet)).Should(Succeed())
			Eventually(func() bool {
				selector, err := metav1.LabelSelectorAsSelector(&newModuleReplicaSet.Spec.Selector)
				modules := &v1alpha1.ModuleList{}
				err = k8sClient.List(context.TODO(), modules, &client.ListOptions{Namespace: newModuleReplicaSet.Namespace, LabelSelector: selector})
				if err == nil && len(modules.Items) == 1 {
					return modules.Items[0].Spec.Module.Version == "1.0.1"
				}
				return false
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("scale down replicaset", func() {
		It("replica is 0 and delete all module", func() {
			key := types.NamespacedName{
				Name:      moduleReplicaSetName,
				Namespace: namespaceName,
			}
			var newModuleReplicaSet v1alpha1.ModuleReplicaSet
			k8sClient.Get(context.TODO(), key, &newModuleReplicaSet)
			newModuleReplicaSet.Spec.Replicas = 0
			Expect(k8sClient.Update(context.TODO(), &newModuleReplicaSet)).Should(Succeed())
			Eventually(func() bool {
				selector, err := metav1.LabelSelectorAsSelector(&newModuleReplicaSet.Spec.Selector)
				modules := &v1alpha1.ModuleList{}
				err = k8sClient.List(context.TODO(), modules, &client.ListOptions{Namespace: newModuleReplicaSet.Namespace, LabelSelector: selector})
				if err == nil && len(modules.Items) == 0 {
					return true
				}
				return false
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("scale up replicaset", func() {
		It("replica is 1 and create one module", func() {
			key := types.NamespacedName{
				Name:      moduleReplicaSetName,
				Namespace: namespaceName,
			}
			var newModuleReplicaSet v1alpha1.ModuleReplicaSet
			k8sClient.Get(context.TODO(), key, &newModuleReplicaSet)
			newModuleReplicaSet.Spec.Replicas = 1
			Expect(k8sClient.Update(context.TODO(), &newModuleReplicaSet)).Should(Succeed())
			Eventually(func() bool {
				selector, err := metav1.LabelSelectorAsSelector(&newModuleReplicaSet.Spec.Selector)
				modules := &v1alpha1.ModuleList{}
				err = k8sClient.List(context.TODO(), modules, &client.ListOptions{Namespace: newModuleReplicaSet.Namespace, LabelSelector: selector})
				if err == nil && len(modules.Items) == 1 {
					return true
				}
				return false
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("delete replicaset", func() {
		It("delete one module and clean replicaset", func() {
			key := types.NamespacedName{
				Name:      moduleReplicaSetName,
				Namespace: namespaceName,
			}
			var newModuleReplicaSet v1alpha1.ModuleReplicaSet
			k8sClient.Get(context.TODO(), key, &newModuleReplicaSet)
			Expect(k8sClient.Delete(context.TODO(), &newModuleReplicaSet)).Should(Succeed())
			Eventually(func() bool {
				selector, err := metav1.LabelSelectorAsSelector(&newModuleReplicaSet.Spec.Selector)
				modules := &v1alpha1.ModuleList{}
				err = k8sClient.List(context.TODO(), modules, &client.ListOptions{Namespace: newModuleReplicaSet.Namespace, LabelSelector: selector})
				if err == nil && len(modules.Items) == 0 {
					err = k8sClient.Get(context.TODO(), key, &newModuleReplicaSet)
					if err != nil && errors.IsNotFound(err) {
						return true
					}
				}
				return false
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("create module replicaset with scaleup_then_scaledown upgradePolicy", func() {
		It("create module", func() {
			podName2 := "test-pod-for-replicaset-2"
			pod2 := preparePod(namespaceName, podName2)
			Expect(k8sClient.Create(context.TODO(), &pod2)).Should(Succeed())
			pod2.Status.PodIP = "127.0.0.2"
			k8sClient.Status().Update(context.TODO(), &pod2)
			moduleReplicaSet := prepareModuleReplicaSet(namespaceName, moduleReplicaSetName)
			moduleReplicaSet.Spec.SchedulingStrategy.UpgradePolicy = v1alpha1.ScaleUpThenScaleDownUpgradePolicy
			utils.AddFinalizer(&moduleReplicaSet.ObjectMeta, finalizer.ModuleExistedFinalizer)
			Expect(k8sClient.Create(context.TODO(), &moduleReplicaSet)).Should(Succeed())
			key := types.NamespacedName{
				Name:      moduleReplicaSetName,
				Namespace: namespaceName,
			}
			Eventually(func() bool {
				var newModuleReplicaSet v1alpha1.ModuleReplicaSet
				k8sClient.Get(context.TODO(), key, &newModuleReplicaSet)
				if newModuleReplicaSet.Spec.Replicas == 1 {
					selector, err := metav1.LabelSelectorAsSelector(&newModuleReplicaSet.Spec.Selector)
					modules := &v1alpha1.ModuleList{}
					err = k8sClient.List(context.TODO(), modules, &client.ListOptions{Namespace: newModuleReplicaSet.Namespace, LabelSelector: selector})
					if err == nil && len(modules.Items) == 1 && v1alpha1.ModuleInstanceStatusAvailable == modules.Items[0].Status.Status {
						return true
					}
				}
				return false
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("update module version with scaleup_then_scaledown upgradePolicy", func() {
		It("pod and module will scale up then scale down", func() {
			podName3 := "test-pod-for-replicaset-3"
			pod3 := preparePod(namespaceName, podName3)
			Expect(k8sClient.Create(context.TODO(), &pod3)).Should(Succeed())
			pod3.Status.PodIP = "127.0.0.3"
			k8sClient.Status().Update(context.TODO(), &pod3)
			key := types.NamespacedName{
				Name:      moduleReplicaSetName,
				Namespace: namespaceName,
			}
			var moduleReplicaSet v1alpha1.ModuleReplicaSet
			k8sClient.Get(context.TODO(), key, &moduleReplicaSet)
			moduleReplicaSet.Spec.Template.Spec.Module.Version = "1.0.1"
			k8sClient.Update(context.TODO(), &moduleReplicaSet)

			selector, _ := metav1.LabelSelectorAsSelector(&moduleReplicaSet.Spec.Selector)
			modules := &v1alpha1.ModuleList{}
			k8sClient.List(context.TODO(), modules, &client.ListOptions{Namespace: moduleReplicaSet.Namespace, LabelSelector: selector})
			var moduleNames []string
			for _, moduleName := range modules.Items {
				moduleNames = append(moduleNames, moduleName.Name)
			}

			Eventually(func() bool {
				for _, moduleName := range moduleNames {
					module := &v1alpha1.Module{}
					err := k8sClient.Get(context.TODO(), types.NamespacedName{Name: moduleName, Namespace: namespaceName}, module)
					if !errors.IsNotFound(err) {
						return false
					}
				}
				k8sClient.List(context.TODO(), modules, &client.ListOptions{Namespace: moduleReplicaSet.Namespace, LabelSelector: selector})
				return len(modules.Items) == len(moduleNames)
			}, timeout, interval).Should(BeTrue())
		})
	})

})

func prepareModuleReplicaSet(namespace, moduleReplicaSetName string) v1alpha1.ModuleReplicaSet {

	moduleReplicaSet := v1alpha1.ModuleReplicaSet{
		Spec: v1alpha1.ModuleReplicaSetSpec{
			Replicas: 1,
			Template: v1alpha1.ModuleTemplateSpec{
				Spec: v1alpha1.ModuleSpec{
					Module: v1alpha1.ModuleInfo{
						Name:    "dynamic-provider",
						Version: "1.0.0",
						Url:     "http://serverless-opensource.oss-cn-shanghai.aliyuncs.com/module-packages/stable/dynamic-provider-1.0.0-ark-biz.jar",
					},
				},
			},
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      moduleReplicaSetName,
			Namespace: namespace,
			Labels: map[string]string{
				"app":                          "dynamic-stock",
				label.MaxModuleCount:           "10",
				label.ModuleSchedulingStrategy: string(v1alpha1.Scatter),
			},
			Annotations: map[string]string{},
		},
	}
	return moduleReplicaSet
}
