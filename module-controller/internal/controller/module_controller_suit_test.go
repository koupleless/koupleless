package controller

import (
	"context"
	"time"

	. "github.com/onsi/ginkgo/v2"
	. "github.com/onsi/gomega"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"

	"github.com/koupleless/module-controller/api/v1alpha1"
	"github.com/koupleless/module-controller/internal/constants/finalizer"
	"github.com/koupleless/module-controller/internal/constants/label"
	"github.com/koupleless/module-controller/internal/utils"

	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/log"
)

var _ = Describe("Module Controller", func() {

	const timeout = time.Second * 30
	const interval = time.Second * 3
	moduleName := "test-module-name"
	namespaceName := "module-controller-namespace"
	podNamePrefix := "test-pod-name"
	moduleReplicaSetName := "test-modulereplicaset"

	Context("create module deployment without pod", func() {
		It("should be pending status", func() {
			namespace := prepareNamespace(namespaceName)
			Expect(k8sClient.Create(context.TODO(), &namespace)).Should(Succeed())
			module := PrepareModule(namespaceName, moduleName)
			Expect(k8sClient.Create(context.TODO(), &module)).Should(Succeed())

			key := types.NamespacedName{
				Name:      moduleName,
				Namespace: namespaceName,
			}
			Eventually(func() bool {
				k8sClient.Get(context.TODO(), key, &module)
				log.Log.Info("module status", "status", module.Status.Status)
				return module.Status.Status == v1alpha1.ModuleInstanceStatusPending
			}, timeout, interval).Should(BeTrue())

			Expect(k8sClient.Delete(context.TODO(), &module)).Should(Succeed())
		})
	})

	module := PrepareModule(namespaceName, moduleName)
	Context("create module deployment with pod", func() {
		It("should be available status", func() {
			podName := podNamePrefix + "-1"
			pod := preparePod(namespaceName, podName)
			k8sClient.Create(context.TODO(), &pod)
			pod.Status.PodIP = "127.0.0.1"
			k8sClient.Status().Update(context.TODO(), &pod)

			Expect(k8sClient.Create(context.TODO(), &module)).Should(Succeed())

			key := types.NamespacedName{
				Name:      moduleName,
				Namespace: namespaceName,
			}
			Eventually(func() bool {
				k8sClient.Get(context.TODO(), key, &module)
				log.Log.Info("module status", "status", module.Status.Status)
				return module.Status.Status == v1alpha1.ModuleInstanceStatusAvailable
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("delete module deployment with ip by deleting module", func() {

		updateModuleUrl := "https://module-test-url"
		It("should be deleted and recreate a new one", func() {
			module.Labels[label.ModuleReplicasetLabel] = moduleReplicaSetName
			module.Labels[label.ModuleNameLabel] = "test-module"
			utils.AddFinalizer(&module.ObjectMeta, finalizer.AllocatePodFinalizer)
			moduleReplicaSet := prepareModuleReplicaSet(namespaceName, moduleReplicaSetName, "")
			moduleReplicaSet.Spec.Template.Spec.Module.Url = updateModuleUrl
			Expect(k8sClient.Create(context.TODO(), &moduleReplicaSet)).Should(Succeed())
			Expect(k8sClient.Update(context.TODO(), &module)).Should(Succeed())
			Expect(k8sClient.Delete(context.TODO(), &module)).Should(Succeed())
			key := types.NamespacedName{
				Name:      moduleName,
				Namespace: namespaceName,
			}
			Eventually(func() bool {
				err := k8sClient.Get(context.TODO(), key, &module)
				if err != nil && errors.IsNotFound(err) {
					selector, err := metav1.LabelSelectorAsSelector(&module.Spec.Selector)
					modules := &v1alpha1.ModuleList{}
					err = k8sClient.List(context.TODO(), modules, &client.ListOptions{Namespace: module.Namespace, LabelSelector: selector})
					if err == nil && len(modules.Items) > 0 {
						module = modules.Items[0]
						return module.Spec.Module.Url == updateModuleUrl
					}
				}
				return false
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("delete module deployment with ip by patching delete label", func() {
		It("should be deleted", func() {
			module.Labels[label.DeleteModuleLabel] = "true"
			utils.AddFinalizer(&module.ObjectMeta, finalizer.AllocatePodFinalizer)
			Expect(k8sClient.Update(context.TODO(), &module)).Should(Succeed())
			key := types.NamespacedName{
				Name:      module.Name,
				Namespace: namespaceName,
			}
			Eventually(func() bool {
				err := k8sClient.Get(context.TODO(), key, &module)
				if err != nil && errors.IsNotFound(err) {
					return true
				}
				return false
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("delete module with ScaleUpThenScaleDownUpgradePolicy", func() {
		It("should create a new module with available status", func() {
			moduleWithScaleUpThenScaleDownUpgradePolicy := PrepareModule(namespaceName, moduleName)
			moduleWithScaleUpThenScaleDownUpgradePolicy.Labels[label.ModuleReplicasetLabel] = moduleReplicaSetName
			utils.AddFinalizer(&moduleWithScaleUpThenScaleDownUpgradePolicy.ObjectMeta, finalizer.AllocatePodFinalizer)
			moduleWithScaleUpThenScaleDownUpgradePolicy.Spec.UpgradePolicy = v1alpha1.ScaleUpThenScaleDownUpgradePolicy
			podName := podNamePrefix + "-2"
			pod := preparePod(namespaceName, podName)
			Expect(k8sClient.Create(context.TODO(), &pod)).Should(Succeed())
			pod.Status.PodIP = "127.0.0.2"
			k8sClient.Status().Update(context.TODO(), &pod)
			Expect(k8sClient.Create(context.TODO(), &moduleWithScaleUpThenScaleDownUpgradePolicy)).Should(Succeed())
			Expect(k8sClient.Delete(context.TODO(), &moduleWithScaleUpThenScaleDownUpgradePolicy)).Should(Succeed())
			Eventually(func() bool {
				k8sClient.Get(context.TODO(), types.NamespacedName{Namespace: namespaceName, Name: moduleName}, &moduleWithScaleUpThenScaleDownUpgradePolicy)
				if moduleWithScaleUpThenScaleDownUpgradePolicy.Labels[label.NewReplicatedModuleLabel] != "" {
					newModule := &v1alpha1.Module{}
					err := k8sClient.Get(context.TODO(), types.NamespacedName{Namespace: namespaceName, Name: moduleName}, newModule)
					if err != nil {
						return false
					}
					return newModule != nil
				}
				return false
			}, timeout, interval).Should(BeTrue())
		})
	})

})

func PrepareModule(namespace string, moduleName string) v1alpha1.Module {
	module := v1alpha1.Module{
		Spec: v1alpha1.ModuleSpec{
			Selector: metav1.LabelSelector{
				MatchLabels: map[string]string{
					"app": "dynamic-stock",
				},
			},
			Module: v1alpha1.ModuleInfo{
				Name:    "dynamic-provider",
				Version: "1.0.0",
				Url:     "http://serverless-opensource.oss-cn-shanghai.aliyuncs.com/module-packages/stable/dynamic-provider-1.0.0-ark-biz.jar",
			},
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      moduleName,
			Namespace: namespace,
			Labels: map[string]string{
				"app": "dynamic-stock",
			},
			Annotations: map[string]string{},
		},
	}
	return module
}
