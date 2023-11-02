package controller

import (
	"context"
	"github.com/sofastack/sofa-serverless/internal/utils"
	"sigs.k8s.io/controller-runtime/pkg/log"
	"time"

	. "github.com/onsi/ginkgo/v2"
	. "github.com/onsi/gomega"
	"k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/client"

	"github.com/sofastack/sofa-serverless/api/v1alpha1"
	"github.com/sofastack/sofa-serverless/internal/constants/label"
)

var _ = Describe("ModuleDeployment Controller OperationStrategy Test", func() {

	const timeout = time.Second * 30
	const interval = time.Second * 5

	namespace := "module-deployment-namespace"
	namespaceObj := prepareNamespace(namespace)
	deployment := prepareDeployment(namespace)
	moduleDeploymentName := "module-deployment-test-demo"
	moduleDeployment := utils.PrepareModuleDeployment(namespace, moduleDeploymentName)
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
				replicaSetList := &v1alpha1.ModuleReplicaSetList{}
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
				replicaSetList := &v1alpha1.ModuleReplicaSetList{}
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
				return checkModuleDeploymentReplicas(
					types.NamespacedName{Name: moduleDeploymentName, Namespace: namespace},
					newModuleDeployment.Spec.Replicas)
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("wait moduleDeployment Completed", func() {
		It("wait moduleDeployment Completed", func() {
			waitModuleDeploymentCompleted(moduleDeploymentName, namespace)
		})
	})

	Context("update replicas for module deployment", func() {
		It("update module replicas", func() {
			key := types.NamespacedName{
				Name:      moduleDeploymentName,
				Namespace: namespace,
			}
			var newModuleDeployment v1alpha1.ModuleDeployment
			Eventually(func() bool {
				Expect(k8sClient.Get(context.TODO(), key, &newModuleDeployment)).Should(Succeed())
				newModuleDeployment.Spec.Replicas += 1
				err := k8sClient.Update(context.TODO(), &newModuleDeployment)
				if err == nil {
					return true
				} else {
					log.Log.Error(err, "update module deployment error")
					return false
				}
			}, timeout, interval).Should(BeTrue())

			waitModuleDeploymentCompleted(moduleDeploymentName, namespace)

			Eventually(func() bool {
				set := map[string]string{
					label.ModuleDeploymentLabel: moduleDeployment.Name,
				}
				replicaSetList := &v1alpha1.ModuleReplicaSetList{}
				err := k8sClient.List(context.TODO(), replicaSetList, &client.ListOptions{LabelSelector: labels.SelectorFromSet(set)}, client.InNamespace(moduleDeployment.Namespace))
				if err != nil || len(replicaSetList.Items) == 0 {
					return false
				}

				maxVersion := 0
				var newRS *v1alpha1.ModuleReplicaSet
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

	Context("test batchConfirm strategy", func() {
		moduleDeploymentName := "module-deployment-test-for-batch-confirm"
		nn := types.NamespacedName{Namespace: namespace, Name: moduleDeploymentName}
		moduleDeployment := utils.PrepareModuleDeployment(namespace, moduleDeploymentName)
		moduleDeployment.Spec.Replicas = 2
		moduleDeployment.Spec.OperationStrategy.NeedConfirm = true
		moduleDeployment.Spec.OperationStrategy.BatchCount = 2

		It("0. prepare 2 pods", func() {
			Eventually(func() bool {
				pod := preparePod(namespace, "fake-pod-3")
				//pod.Labels[label.ModuleLabelPrefix+"dynamic-provider"] = "true"
				if err := k8sClient.Create(context.TODO(), &pod); err != nil {
					return false
				}
				// when install module, the podIP is necessary
				pod.Status.PodIP = "127.0.0.1"
				return k8sClient.Status().Update(context.TODO(), &pod) == nil
			}, timeout, interval).Should(BeTrue())

		})

		It("1. create a new moduleDeployment", func() {
			Expect(k8sClient.Create(context.TODO(), &moduleDeployment)).Should(Succeed())
		})

		It("2. check if the replicas is 1", func() {
			// todo: we just check deployment.status.replicas rather than modulereplicaset
			Eventually(func() bool {
				if err := k8sClient.Get(context.TODO(), nn, &moduleDeployment); err != nil {
					return false
				}

				if !moduleDeployment.Spec.Pause {
					return false
				}

				return checkModuleDeploymentReplicas(
					types.NamespacedName{
						Name:      moduleDeploymentName,
						Namespace: moduleDeployment.Namespace}, 1)
			}, timeout, interval).Should(BeTrue())
		})

		It("3. resume", func() {
			Eventually(func() bool {
				Expect(k8sClient.Get(context.TODO(), nn, &moduleDeployment)).Should(Succeed())

				moduleDeployment.Spec.Pause = false
				return Expect(k8sClient.Update(context.TODO(), &moduleDeployment)).Should(Succeed())
			}, timeout, interval).Should(BeTrue())
		})

		It("4. check if the moduleDeployment status is completed", func() {
			Eventually(func() bool {
				if k8sClient.Get(context.TODO(), nn, &moduleDeployment) != nil {
					return false
				}

				if moduleDeployment.Spec.Pause != false {
					return false
				}

				return moduleDeployment.Status.ReleaseStatus.Progress == v1alpha1.ModuleDeploymentReleaseProgressCompleted
			}, timeout, interval).Should(BeTrue())
		})

		It("5. delete moduleDeployment", func() {
			Expect(k8sClient.Delete(context.TODO(), &moduleDeployment)).Should(Succeed())
		})
	})

	Context("test useBeta strategy", func() {
		moduleDeploymentName := "module-deployment-test-for-use-beta"
		nn := types.NamespacedName{Namespace: namespace, Name: moduleDeploymentName}
		moduleDeployment := utils.PrepareModuleDeployment(namespace, moduleDeploymentName)
		moduleDeployment.Spec.Replicas = 4
		moduleDeployment.Spec.OperationStrategy.UseBeta = true
		moduleDeployment.Spec.OperationStrategy.NeedConfirm = true
		moduleDeployment.Spec.OperationStrategy.BatchCount = 2

		It("0. prepare pods", func() {
			Eventually(func() bool {
				pod := preparePod(namespace, "fake-pod-use-beta")
				pod.Labels[label.ModuleLabelPrefix+"dynamic-provider"] = "true"
				if err := k8sClient.Create(context.TODO(), &pod); err != nil {
					return false
				}
				// when install module, the podIP is necessary
				pod.Status.PodIP = "127.0.0.1"
				return k8sClient.Status().Update(context.TODO(), &pod) == nil
			}, timeout, interval).Should(BeTrue())
		})

		It("1. create a new moduleDeployment", func() {
			Expect(k8sClient.Create(context.TODO(), &moduleDeployment)).Should(Succeed())
		})

		It("2. check if use Beta strategy", func() {
			Eventually(func() bool {
				return checkModuleDeploymentReplicas(nn, 1)
			})
		})

		It("3. clean environment", func() {
			Expect(k8sClient.Delete(context.TODO(), &moduleDeployment)).Should(Succeed())
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
				replicaSetList := &v1alpha1.ModuleReplicaSetList{}
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

func checkModuleDeploymentReplicas(nn types.NamespacedName, replicas int32) bool {
	set := map[string]string{
		label.ModuleDeploymentLabel: nn.Name,
	}
	replicaSetList := &v1alpha1.ModuleReplicaSetList{}
	err := k8sClient.List(context.TODO(), replicaSetList, &client.ListOptions{LabelSelector: labels.SelectorFromSet(set)}, client.InNamespace(nn.Namespace))
	if err != nil || len(replicaSetList.Items) == 0 {
		return false
	}

	maxVersion := 0
	var newRS *v1alpha1.ModuleReplicaSet
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

	// the replicas of new replicaset must be equal to newModuleDeployment
	log.Log.Info("checkModuleDeploymentReplicas", "newRS.Status.Replicas", newRS.Status.Replicas, "newRS.Spec.Replicas", newRS.Spec.Replicas, "replicas", replicas)
	return newRS != nil &&
		newRS.Status.Replicas == newRS.Spec.Replicas &&
		newRS.Status.Replicas == replicas
}

func waitModuleDeploymentCompleted(moduleDeploymentName string, namespace string) {
	key := types.NamespacedName{
		Name:      moduleDeploymentName,
		Namespace: namespace,
	}
	newModuleDeployment := &v1alpha1.ModuleDeployment{}
	Expect(k8sClient.Get(context.TODO(), key, newModuleDeployment)).Should(Succeed())
	progress := newModuleDeployment.Status.ReleaseStatus.Progress
	if progress == v1alpha1.ModuleDeploymentReleaseProgressCompleted {
		return
	}
	time.Sleep(5 * time.Second)
	waitModuleDeploymentCompleted(moduleDeploymentName, namespace)
}
