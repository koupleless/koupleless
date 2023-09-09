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
	namespace := "default"

	Context("create module replicaset", func() {
		It("create success and replica is 1", func() {
			moduleReplicaSet := prepareModuleReplicaSet(namespace, moduleReplicaSetName)
			utils.AddFinalizer(&moduleReplicaSet.ObjectMeta, finalizer.ModuleExistedFinalizer)
			Expect(k8sClient.Create(context.TODO(), &moduleReplicaSet)).Should(Succeed())
			key := types.NamespacedName{
				Name:      moduleReplicaSetName,
				Namespace: namespace,
			}
			var newModuleReplicaSet v1alpha1.ModuleReplicaSet
			Eventually(func() bool {
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
				Namespace: namespace,
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

	//Context("update module name in replicaset", func() {
	//	It("module is deleted and wait to recreate", func() {
	//		key := types.NamespacedName{
	//			Name:      moduleReplicaSetName,
	//			Namespace: namespace,
	//		}
	//		var newModuleReplicaSet v1alpha1.ModuleReplicaSet
	//		k8sClient.Get(context.TODO(), key, &newModuleReplicaSet)
	//		newModuleReplicaSet.Spec.Template.Spec.Module.Name = "new-module-name"
	//		Expect(k8sClient.Update(context.TODO(), &newModuleReplicaSet)).Should(Succeed())
	//		Eventually(func() bool {
	//			selector, err := metav1.LabelSelectorAsSelector(&newModuleReplicaSet.Spec.Selector)
	//			modules := &v1alpha1.ModuleList{}
	//			err = k8sClient.List(context.TODO(), modules, &client.ListOptions{Namespace: newModuleReplicaSet.Namespace, LabelSelector: selector})
	//			if err == nil && len(modules.Items) == 0 {
	//				return true
	//			}
	//			return false
	//		}, timeout, interval).Should(BeTrue())
	//	})
	//})

	Context("scale down replicaset", func() {
		It("replica is 0 and delete all module", func() {
			key := types.NamespacedName{
				Name:      moduleReplicaSetName,
				Namespace: namespace,
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
				Namespace: namespace,
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
				Namespace: namespace,
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
