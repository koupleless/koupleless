package controller

import (
	"context"
	"fmt"
	"time"

	"k8s.io/apimachinery/pkg/api/errors"
	"sigs.k8s.io/controller-runtime/pkg/log"

	. "github.com/onsi/ginkgo/v2"
	. "github.com/onsi/gomega"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/client"

	"github.com/sofastack/sofa-serverless/api/v1alpha1"
	"github.com/sofastack/sofa-serverless/internal/constants/label"
	"github.com/sofastack/sofa-serverless/internal/utils"
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

			Eventually(func() error {
				return checkModuleDeploymentReplicas(
					types.NamespacedName{Name: moduleDeploymentName, Namespace: namespace},
					newModuleDeployment.Spec.Replicas)
			}, timeout, interval).Should(Succeed())
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

	Context("test symmetric deployment", func() {
		namespace := "module-symmetric-deployment-namespace"
		namespaceObj := prepareNamespace(namespace)
		deployment := prepareDeployment(namespace)
		moduleDeploymentName := "module-symmetric-deployment-test"
		moduleDeployment := utils.PrepareModuleDeployment(namespace, moduleDeploymentName)
		nn := types.NamespacedName{Namespace: namespace, Name: moduleDeploymentName}
		// personal params
		moduleDeployment.Spec.Replicas = -1
		moduleDeployment.Spec.OperationStrategy.NeedConfirm = false
		moduleDeployment.Spec.OperationStrategy.BatchCount = 1
		It("1 prepare  namespace", func() {
			Eventually(func() error {
				err := k8sClient.Create(context.TODO(), &namespaceObj)
				if err != nil {
					return err
				}

				return nil
			}, timeout, interval).Should(Succeed())
		})

		It("2 prepare deployment", func() {
			Eventually(func() error {
				derr := k8sClient.Create(context.TODO(), &deployment)
				if derr != nil {
					return derr
				}

				// mock
				i := int32(3)
				deployment.Spec.Replicas = &i
				umderr2 := k8sClient.Update(context.TODO(), &deployment)
				if umderr2 != nil {
					return umderr2
				}

				deployment.Status.Replicas = 3
				deployment.Status.ReadyReplicas = 3
				deployment.Status.AvailableReplicas = 3
				umderr := k8sClient.Status().Update(context.TODO(), &deployment)
				if umderr != nil {
					return umderr
				}

				return nil
			}, timeout, interval).Should(Succeed())
		})

		It("3 prepare moduleDeployment", func() {
			Eventually(func() error {
				mderr := k8sClient.Create(context.TODO(), &moduleDeployment)
				if mderr != nil {
					return mderr
				}

				return nil
			}, timeout, interval).Should(Succeed())

		})

		It("4 prepare pod 1", func() {
			Eventually(func() error {
				pod1 := preparePod(namespace, "fake-pod-sym-1")
				if err := k8sClient.Create(context.TODO(), &pod1); err != nil {
					return err
				}

				// when install module, the podIP is necessary
				pod1.Status.PodIP = "127.0.0.1"
				if perr := k8sClient.Status().Update(context.TODO(), &pod1); perr != nil {
					return perr
				}

				return nil
			}, timeout, interval).Should(Succeed())
		})
		It("5 prepare pod 2", func() {
			Eventually(func() error {
				pod2 := preparePod(namespace, "fake-pod-sym-2")
				if err := k8sClient.Create(context.TODO(), &pod2); err != nil {
					return err
				}

				// when install module, the podIP is necessary
				pod2.Status.PodIP = "127.0.0.1"
				if perr := k8sClient.Status().Update(context.TODO(), &pod2); perr != nil {
					return perr
				}

				return nil
			}, timeout, interval).Should(Succeed())
		})
		It("6 prepare pod 3", func() {
			Eventually(func() error {
				pod3 := preparePod(namespace, "fake-pod-sym-3")
				if err := k8sClient.Create(context.TODO(), &pod3); err != nil {
					return err
				}

				// when install module, the podIP is necessary
				pod3.Status.PodIP = "127.0.0.1"
				if perr := k8sClient.Status().Update(context.TODO(), &pod3); perr != nil {
					return perr
				}

				return nil
			}, timeout, interval).Should(Succeed())
		})

		It("7 wait replicaset created", func() {
			Eventually(func() bool {
				set := map[string]string{label.ModuleDeploymentLabel: moduleDeployment.Name}
				replicaSetList := &v1alpha1.ModuleReplicaSetList{}
				err := k8sClient.List(context.TODO(), replicaSetList, &client.ListOptions{LabelSelector: labels.SelectorFromSet(set)}, client.InNamespace(moduleDeployment.Namespace))
				if err != nil {
					return false
				}

				return len(replicaSetList.Items) == 1
			}, timeout, interval).Should(BeTrue())
		})

		It("8 wait the moduleDeployment is completed", func() {
			Eventually(func() bool {
				if k8sClient.Get(context.TODO(), nn, &moduleDeployment) != nil {
					return false
				}

				status := moduleDeployment.Status.ReleaseStatus
				if status == nil {
					return false
				}

				return status.Progress == v1alpha1.ModuleDeploymentReleaseProgressCompleted
			}, timeout, interval).Should(BeTrue())
		})

		It("9 check the moduleDeployment replicas", func() {
			Eventually(func() bool {
				if err := k8sClient.Get(context.TODO(), nn, &moduleDeployment); err != nil {
					return false
				}

				return moduleDeployment.Spec.Replicas == 3
			}, timeout, interval).Should(BeTrue())
		})

		It("10 check replicaSet replicas", func() {
			Eventually(func() error {
				if err := k8sClient.Get(context.TODO(), nn, &moduleDeployment); err != nil {
					return err
				}

				return checkModuleDeploymentReplicas(types.NamespacedName{Namespace: moduleDeployment.Namespace, Name: moduleDeploymentName}, 3)
			}, timeout, interval).Should(Succeed())
		})

	})

	Context("test symmetric deployment err", func() {
		namespace := "module-symmetric-deployment-namespace"
		moduleDeploymentName := "module-symmetric-deployment-test-2"
		moduleDeployment := utils.PrepareModuleDeployment(namespace, moduleDeploymentName)
		nn := types.NamespacedName{Namespace: namespace, Name: moduleDeploymentName}

		It("0 prepare moduleDeployment", func() {
			Eventually(func() error {
				mderr := k8sClient.Create(context.TODO(), &moduleDeployment)
				if mderr != nil {
					return mderr
				}

				return nil
			}, timeout, interval).Should(Succeed())

		})

		It("1 test symmetric deployment err", func() {
			Eventually(func() error {
				if err := k8sClient.Get(context.TODO(), nn, &moduleDeployment); err != nil {
					return err
				}

				moduleDeployment.Spec.BaseDeploymentName = "test-err"
				// personal params
				moduleDeployment.Spec.Replicas = -1
				moduleDeployment.Spec.OperationStrategy.NeedConfirm = false
				moduleDeployment.Spec.OperationStrategy.BatchCount = 1
				if err := k8sClient.Update(context.TODO(), &moduleDeployment); err != nil {
					return err
				}

				return nil
			}, timeout, interval).Should(Succeed())
		})

		It("2 delete moduleDeployment", func() {
			Expect(k8sClient.Delete(context.TODO(), &moduleDeployment)).Should(Succeed())
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
				if k8sClient.Status().Update(context.TODO(), &pod) != nil {
					return false
				}

				pod2 := preparePod(namespace, "fake-pod-4")
				if err := k8sClient.Create(context.TODO(), &pod2); err != nil {
					return false
				}
				// when install module, the podIP is necessary
				pod.Status.PodIP = "127.0.0.1"
				return k8sClient.Status().Update(context.TODO(), &pod2) == nil
			}, timeout, interval).Should(BeTrue())
		})

		It("1. create a new moduleDeployment", func() {
			Expect(k8sClient.Create(context.TODO(), &moduleDeployment)).Should(Succeed())
		})

		It("2. check if the replicas is 1", func() {
			// todo: we just check deployment.status.replicas rather than modulereplicaset
			Eventually(func() error {
				if err := k8sClient.Get(context.TODO(), nn, &moduleDeployment); err != nil {
					return err
				}

				if !moduleDeployment.Spec.Pause {
					return fmt.Errorf("the deployment is not paused")
				}

				return checkModuleDeploymentReplicas(
					types.NamespacedName{
						Name:      moduleDeploymentName,
						Namespace: moduleDeployment.Namespace}, 1)
			}, timeout, interval).Should(Succeed())
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

		It("5. add another finalizer to prevent module-deployment from being deleted ", func() {
			utils.AddFinalizer(&moduleDeployment.ObjectMeta, "test")
			Expect(k8sClient.Update(context.TODO(), &moduleDeployment)).Should(Succeed())
		})

		It("6. delete moduleDeployment", func() {
			Expect(k8sClient.Delete(context.TODO(), &moduleDeployment)).Should(Succeed())
		})

		It("7. check if the replicas is 1", func() {
			// todo: we just check deployment.status.replicas rather than modulereplicaset
			Eventually(func() error {
				if err := k8sClient.Get(context.TODO(), nn, &moduleDeployment); err != nil {
					return err
				}

				if !moduleDeployment.Spec.Pause {
					return fmt.Errorf("the deployment is not paused")
				}

				return checkModuleDeploymentReplicas(
					types.NamespacedName{
						Name:      moduleDeploymentName,
						Namespace: moduleDeployment.Namespace}, 1)
			}, timeout, interval).Should(Succeed())
		})

		It("8. resume", func() {
			Eventually(func() bool {
				Expect(k8sClient.Get(context.TODO(), nn, &moduleDeployment)).Should(Succeed())

				moduleDeployment.Spec.Pause = false
				return Expect(k8sClient.Update(context.TODO(), &moduleDeployment)).Should(Succeed())
			}, timeout, interval).Should(BeTrue())
		})

		It("9. check if the moduleDeployment status is Terminated", func() {
			Eventually(func() error {
				if err := k8sClient.Get(context.TODO(), nn, &moduleDeployment); err != nil {
					return err
				}

				if moduleDeployment.Spec.Pause != false {
					return fmt.Errorf("the module-deployment is paused")
				}

				if moduleDeployment.Status.ReleaseStatus == nil {
					return fmt.Errorf("release status is nil")
				}

				if moduleDeployment.Status.ReleaseStatus.Progress != v1alpha1.ModuleDeploymentReleaseProgressTerminated {
					return fmt.Errorf("expect status %v, but got %v",
						v1alpha1.ModuleDeploymentReleaseProgressTerminated, moduleDeployment.Status.ReleaseStatus.Progress)
				}
				return nil
			}, timeout, interval).Should(Succeed())
		})

		It("10. clean module-deployment", func() {
			utils.RemoveFinalizer(&moduleDeployment.ObjectMeta, "test")
			Expect(k8sClient.Update(context.TODO(), &moduleDeployment))
		})
	})

	Context("test symmetric batchConfirm strategy", func() {
		moduleDeploymentName := "module-deployment-test-for-symmetric-batch-confirm"
		nn := types.NamespacedName{Namespace: namespace, Name: moduleDeploymentName}
		moduleDeployment := utils.PrepareModuleDeployment(namespace, moduleDeploymentName)
		moduleDeployment.Spec.Replicas = -1
		moduleDeployment.Spec.OperationStrategy.NeedConfirm = true
		moduleDeployment.Spec.OperationStrategy.BatchCount = 2

		It("0 prepare deployment", func() {
			Eventually(func() error {
				deployment.Status.Replicas = 2
				deployment.Status.ReadyReplicas = 2
				deployment.Status.AvailableReplicas = 2
				umderr := k8sClient.Status().Update(context.TODO(), &deployment)
				if umderr != nil {
					return umderr
				}

				return nil
			}, timeout, interval).Should(Succeed())
		})

		It("0. prepare 2 pods", func() {
			Eventually(func() bool {
				pod := preparePod(namespace, "fake-pod-5")
				if err := k8sClient.Create(context.TODO(), &pod); err != nil {
					return false
				}
				pod.Status.PodIP = "127.0.0.1"
				if k8sClient.Status().Update(context.TODO(), &pod) != nil {
					return false
				}

				pod2 := preparePod(namespace, "fake-pod-6")
				if err := k8sClient.Create(context.TODO(), &pod2); err != nil {
					return false
				}
				pod2.Status.PodIP = "127.0.0.1"
				return k8sClient.Status().Update(context.TODO(), &pod2) == nil
			}, timeout, interval).Should(BeTrue())
		})

		It("1. create a new moduleDeployment", func() {
			Expect(k8sClient.Create(context.TODO(), &moduleDeployment)).Should(Succeed())
		})

		It("2. check if the replicas is 1", func() {
			Eventually(func() error {
				if err := k8sClient.Get(context.TODO(), nn, &moduleDeployment); err != nil {
					return err
				}

				if !moduleDeployment.Spec.Pause {
					return fmt.Errorf("the deployment is not paused")
				}

				return checkModuleDeploymentReplicas(
					types.NamespacedName{
						Name:      moduleDeploymentName,
						Namespace: moduleDeployment.Namespace}, 1)
			}, timeout, interval).Should(Succeed())
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

		It("5. add another finalizer to prevent module-deployment from being deleted ", func() {
			utils.AddFinalizer(&moduleDeployment.ObjectMeta, "test")
			Expect(k8sClient.Update(context.TODO(), &moduleDeployment)).Should(Succeed())
		})

		It("6. delete moduleDeployment", func() {
			Expect(k8sClient.Delete(context.TODO(), &moduleDeployment)).Should(Succeed())
		})

		It("7. check if the replicas is 1", func() {
			Eventually(func() error {
				if err := k8sClient.Get(context.TODO(), nn, &moduleDeployment); err != nil {
					return err
				}

				if !moduleDeployment.Spec.Pause {
					return fmt.Errorf("the deployment is not paused")
				}

				return checkModuleDeploymentReplicas(
					types.NamespacedName{
						Name:      moduleDeploymentName,
						Namespace: moduleDeployment.Namespace}, 1)
			}, timeout, interval).Should(Succeed())
		})

		It("8. resume", func() {
			Eventually(func() bool {
				Expect(k8sClient.Get(context.TODO(), nn, &moduleDeployment)).Should(Succeed())

				moduleDeployment.Spec.Pause = false
				return Expect(k8sClient.Update(context.TODO(), &moduleDeployment)).Should(Succeed())
			}, timeout, interval).Should(BeTrue())
		})

		It("9. check if the moduleDeployment status is Terminated", func() {
			Eventually(func() error {
				if err := k8sClient.Get(context.TODO(), nn, &moduleDeployment); err != nil {
					return err
				}

				if moduleDeployment.Spec.Pause != false {
					return fmt.Errorf("the module-deployment is paused")
				}

				if moduleDeployment.Status.ReleaseStatus == nil {
					return fmt.Errorf("release status is nil")
				}

				if moduleDeployment.Status.ReleaseStatus.Progress != v1alpha1.ModuleDeploymentReleaseProgressTerminated {
					return fmt.Errorf("expect status %v, but got %v",
						v1alpha1.ModuleDeploymentReleaseProgressTerminated, moduleDeployment.Status.ReleaseStatus.Progress)
				}
				return nil
			}, timeout, interval).Should(Succeed())
		})

		It("10. clean module-deployment", func() {
			utils.RemoveFinalizer(&moduleDeployment.ObjectMeta, "test")
			Expect(k8sClient.Update(context.TODO(), &moduleDeployment))
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
			Eventually(func() error {
				return checkModuleDeploymentReplicas(nn, 1)
			})
		})

		It("3. clean environment", func() {
			Expect(k8sClient.Delete(context.TODO(), &moduleDeployment)).Should(Succeed())
		})
	})

	Context("test symmetric useBeta strategy", func() {
		moduleDeploymentName := "module-deployment-test-for-symmetric-use-beta"
		nn := types.NamespacedName{Namespace: namespace, Name: moduleDeploymentName}
		moduleDeployment := utils.PrepareModuleDeployment(namespace, moduleDeploymentName)
		moduleDeployment.Spec.Replicas = -1
		moduleDeployment.Spec.OperationStrategy.UseBeta = true
		moduleDeployment.Spec.OperationStrategy.NeedConfirm = true
		moduleDeployment.Spec.OperationStrategy.BatchCount = 2

		It("0 prepare deployment", func() {
			Eventually(func() error {
				deployment.Status.Replicas = 4
				deployment.Status.ReadyReplicas = 4
				deployment.Status.AvailableReplicas = 4
				umderr := k8sClient.Status().Update(context.TODO(), &deployment)
				if umderr != nil {
					return umderr
				}

				return nil
			}, timeout, interval).Should(Succeed())
		})

		It("0. prepare pods", func() {
			Eventually(func() bool {
				pod := preparePod(namespace, "fake-pod-symmetric-use-beta")
				pod.Labels[label.ModuleLabelPrefix+"dynamic-provider"] = "true"
				if err := k8sClient.Create(context.TODO(), &pod); err != nil {
					return false
				}
				pod.Status.PodIP = "127.0.0.1"
				return k8sClient.Status().Update(context.TODO(), &pod) == nil
			}, timeout, interval).Should(BeTrue())
		})

		It("1. create a new moduleDeployment", func() {
			Expect(k8sClient.Create(context.TODO(), &moduleDeployment)).Should(Succeed())
		})

		It("2. check if use Beta strategy", func() {
			Eventually(func() error {
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

func checkModuleDeploymentReplicas(nn types.NamespacedName, replicas int32) error {
	set := map[string]string{
		label.ModuleDeploymentLabel: nn.Name,
	}
	replicaSetList := &v1alpha1.ModuleReplicaSetList{}
	err := k8sClient.List(context.TODO(), replicaSetList, &client.ListOptions{LabelSelector: labels.SelectorFromSet(set)}, client.InNamespace(nn.Namespace))
	if err != nil || len(replicaSetList.Items) == 0 {
		return fmt.Errorf("the replicasetList is empty")
	}

	maxVersion := 0
	var newRS *v1alpha1.ModuleReplicaSet
	for i := 0; i < len(replicaSetList.Items); i++ {
		version, err := getRevision(&replicaSetList.Items[i])
		if err != nil {
			return err
		}
		if version > maxVersion {
			maxVersion = version
			newRS = &replicaSetList.Items[i]
		}
	}

	// the replicas of new replicaset must be equal to newModuleDeployment
	if newRS == nil {
		return fmt.Errorf("the replicaset is nil")
	}
	if newRS.Status.Replicas != newRS.Spec.Replicas {
		return fmt.Errorf("the replicaset is not ready, expect replicas is %v, but got %v",
			newRS.Spec.Replicas, newRS.Status.ReadyReplicas)
	}
	if newRS.Spec.Replicas != replicas {
		return fmt.Errorf("the deployment is not ready, expect replicas is %v, but got %v",
			replicas, newRS.Spec.Replicas)
	}
	return nil
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
