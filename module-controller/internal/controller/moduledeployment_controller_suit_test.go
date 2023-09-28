package controller

import (
	"context"
	"time"

	. "github.com/onsi/ginkgo/v2"
	. "github.com/onsi/gomega"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/client"

	"github.com/sofastack/sofa-serverless/api/v1alpha1"

	moduledeploymentv1alpha1 "github.com/sofastack/sofa-serverless/api/v1alpha1"
	"github.com/sofastack/sofa-serverless/internal/constants/label"
)

var _ = Describe("ModuleDeployment Controller", func() {
	const timeout = time.Second * 30
	const interval = time.Second * 5

	namespace := "module-deployment-namespace"
	namespaceObj := prepareNamespace(namespace)
	deployment := prepareDeployment(namespace)
	moduleDeploymentName := "module-deployment-test-demo"
	moduleDeployment := prepareModuleDeployment(namespace, moduleDeploymentName)
	pod := preparePod(namespace, "fake-pod-1")
	Context("create module deployment", func() {
		It("prepare deployment and pod", func() {
			Expect(k8sClient.Create(context.TODO(), &namespaceObj)).Should(Succeed())

			Expect(k8sClient.Create(context.TODO(), &deployment)).Should(Succeed())

			Expect(k8sClient.Create(context.TODO(), &pod)).Should(Succeed())

			pod.Status.PodIP = "127.0.0.1"
			Expect(k8sClient.Status().Update(context.TODO(), &pod)).Should(Succeed())
		})

		It("create module replicaset", func() {
			Expect(k8sClient.Create(context.TODO(), &moduleDeployment)).Should(Succeed())

			Eventually(func() bool {
				set := map[string]string{
					label.ModuleDeploymentLabel: moduleDeployment.Name,
				}
				replicaSetList := &moduledeploymentv1alpha1.ModuleReplicaSetList{}
				err := k8sClient.List(context.TODO(), replicaSetList, &client.ListOptions{LabelSelector: labels.SelectorFromSet(set)}, client.InNamespace(moduleDeployment.Namespace))
				if err != nil {
					return false
				}
				return len(replicaSetList.Items) == 1
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("update module url for module deployment", func() {
		It("update module replicaset", func() {
			moduleUrl := "https://test.url.com"
			key := types.NamespacedName{
				Name:      moduleDeploymentName,
				Namespace: namespace,
			}
			var newModuleDeployment v1alpha1.ModuleDeployment
			Expect(k8sClient.Get(context.TODO(), key, &newModuleDeployment)).Should(Succeed())
			newModuleDeployment.Spec.Template.Spec.Module.Url = moduleUrl
			Expect(k8sClient.Update(context.TODO(), &newModuleDeployment)).Should(Succeed())

			Eventually(func() bool {
				set := map[string]string{
					label.ModuleDeploymentLabel: moduleDeployment.Name,
				}
				replicaSetList := &moduledeploymentv1alpha1.ModuleReplicaSetList{}
				err := k8sClient.List(context.TODO(), replicaSetList, &client.ListOptions{LabelSelector: labels.SelectorFromSet(set)}, client.InNamespace(moduleDeployment.Namespace))
				if err != nil || len(replicaSetList.Items) > 1 {
					return false
				}

				replicaSet := replicaSetList.Items[0]
				url := replicaSet.Spec.Template.Spec.Module.Url
				return url == moduleUrl
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("update module version for module deployment", func() {
		It("update module replicaset", func() {
			key := types.NamespacedName{
				Name:      moduleDeploymentName,
				Namespace: namespace,
			}
			var newModuleDeployment v1alpha1.ModuleDeployment
			Expect(k8sClient.Get(context.TODO(), key, &newModuleDeployment)).Should(Succeed())
			newModuleDeployment.Spec.Template.Spec.Module.Version = "1.0.1"
			Expect(k8sClient.Update(context.TODO(), &newModuleDeployment)).Should(Succeed())

			Eventually(func() bool {
				set := map[string]string{
					label.ModuleDeploymentLabel: moduleDeployment.Name,
				}
				replicaSetList := &moduledeploymentv1alpha1.ModuleReplicaSetList{}
				err := k8sClient.List(context.TODO(), replicaSetList, &client.ListOptions{LabelSelector: labels.SelectorFromSet(set)}, client.InNamespace(moduleDeployment.Namespace))
				if err != nil || len(replicaSetList.Items) == 0 {
					return false
				}

				maxVersion := 0
				var newRS *moduledeploymentv1alpha1.ModuleReplicaSet
				for i := 0; i < len(replicaSetList.Items); i++ {
					version, err := getRevision(&replicaSetList.Items[i])
					if err != nil {
						return false
					}
					if version > maxVersion {
						maxVersion = version
						newRS = &replicaSetList.Items[i]
					}
				}

				// the replicas of old replicaset must be zero
				for i := 0; i < len(replicaSetList.Items); i++ {
					if version, _ := getRevision(&replicaSetList.Items[i]); version != maxVersion {
						if replicaSetList.Items[i].Spec.Replicas != 0 {
							return false
						}
					}
				}

				// the replicas of new replicaset must be equal to newModuleDeployment
				return newRS != nil &&
					newRS.Spec.Template.Spec.Module.Version == "1.0.1" &&
					newRS.Status.Replicas == newRS.Spec.Replicas &&
					newRS.Status.Replicas == newModuleDeployment.Spec.Replicas
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("update replicas for module deployment", func() {
		It("update module replicas", func() {
			key := types.NamespacedName{
				Name:      moduleDeploymentName,
				Namespace: namespace,
			}
			var newModuleDeployment v1alpha1.ModuleDeployment
			Expect(k8sClient.Get(context.TODO(), key, &newModuleDeployment)).Should(Succeed())
			newModuleDeployment.Spec.Replicas += 1
			Expect(k8sClient.Update(context.TODO(), &newModuleDeployment)).Should(Succeed())

			Eventually(func() bool {
				set := map[string]string{
					label.ModuleDeploymentLabel: moduleDeployment.Name,
				}
				replicaSetList := &moduledeploymentv1alpha1.ModuleReplicaSetList{}
				err := k8sClient.List(context.TODO(), replicaSetList, &client.ListOptions{LabelSelector: labels.SelectorFromSet(set)}, client.InNamespace(moduleDeployment.Namespace))
				if err != nil || len(replicaSetList.Items) == 0 {
					return false
				}

				maxVersion := 0
				var newRS *moduledeploymentv1alpha1.ModuleReplicaSet
				for i := 0; i < len(replicaSetList.Items); i++ {
					version, err := getRevision(&replicaSetList.Items[i])
					if err != nil {
						return false
					}
					if version > maxVersion {
						maxVersion = version
						newRS = &replicaSetList.Items[i]
					}
				}

				// the replicas of old replicaset must be zero
				for i := 0; i < len(replicaSetList.Items); i++ {
					if version, _ := getRevision(&replicaSetList.Items[i]); version != maxVersion {
						if replicaSetList.Items[i].Spec.Replicas != 0 {
							return false
						}
					}
				}

				// the replicas of new replicaset must be equal to newModuleDeployment
				return newRS != nil &&
					newRS.Status.Replicas == newRS.Spec.Replicas &&
					newRS.Status.Replicas == newModuleDeployment.Spec.Replicas
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("delete module deployment", func() {
		It("clean module replicaset and deployment", func() {
			key := types.NamespacedName{
				Name:      moduleDeploymentName,
				Namespace: namespace,
			}
			var newModuleDeployment v1alpha1.ModuleDeployment
			Expect(k8sClient.Get(context.TODO(), key, &newModuleDeployment)).Should(Succeed())
			Expect(k8sClient.Delete(context.TODO(), &newModuleDeployment)).Should(Succeed())

			Eventually(func() bool {
				set := map[string]string{
					label.ModuleDeploymentLabel: moduleDeployment.Name,
				}
				replicaSetList := &moduledeploymentv1alpha1.ModuleReplicaSetList{}
				err := k8sClient.List(context.TODO(), replicaSetList, &client.ListOptions{LabelSelector: labels.SelectorFromSet(set)}, client.InNamespace(moduleDeployment.Namespace))
				if err != nil {
					if errors.IsNotFound(err) {
						return true
					}
					return false
				}
				return len(replicaSetList.Items) == 0
			}, timeout, interval).Should(BeTrue())
		})
	})
})

func prepareModuleDeployment(namespace, moduleDeploymentName string) v1alpha1.ModuleDeployment {
	baseDeploymentName := "dynamic-stock-deployment"

	moduleDeployment := v1alpha1.ModuleDeployment{
		Spec: v1alpha1.ModuleDeploymentSpec{
			BaseDeploymentName: baseDeploymentName,
			Template: v1alpha1.ModuleTemplateSpec{
				Spec: v1alpha1.ModuleSpec{
					Module: v1alpha1.ModuleInfo{
						Name:    "dynamic-provider",
						Version: "1.0.0",
						Url:     "http://serverless-opensource.oss-cn-shanghai.aliyuncs.com/module-packages/stable/dynamic-provider-1.0.0-ark-biz.jar",
					},
				},
			},
			SchedulingStrategy: v1alpha1.ModuleSchedulingStrategy{
				SchedulingPolicy: v1alpha1.Scatter,
			},
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      moduleDeploymentName,
			Namespace: namespace,
			Labels: map[string]string{
				"app": "dynamic-stock",
			},
			Annotations: map[string]string{},
		},
	}
	return moduleDeployment
}
