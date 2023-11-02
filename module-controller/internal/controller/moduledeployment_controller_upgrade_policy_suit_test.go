package controller

import (
	"context"
	"k8s.io/apimachinery/pkg/labels"
	"time"

	. "github.com/onsi/ginkgo/v2"
	. "github.com/onsi/gomega"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/client"

	"github.com/sofastack/sofa-serverless/api/v1alpha1"
	"github.com/sofastack/sofa-serverless/internal/constants/label"
	"github.com/sofastack/sofa-serverless/internal/utils"
)

var _ = Describe("ModuleReplicaSet Controller Scale Test", func() {

	const timeout = time.Second * 30
	const interval = time.Second * 3

	moduleDeploymentName := "test-module-deployment-for-upgrade-policy"
	var moduleReplicaSetName string
	namespaceName := "upgrade-policy-test-namespace"
	podName1 := "test-pod-for-upgrade-policy-1"
	podName2 := "test-pod-for-upgrade-policy-2"

	namespaceObj := prepareNamespace(namespaceName)
	deployment := prepareDeployment(namespaceName)
	moduleDeployment := utils.PrepareModuleDeployment(namespaceName, moduleDeploymentName)
	moduleDeployment.Spec.Replicas = 1
	moduleDeployment.Spec.OperationStrategy.UpgradePolicy = v1alpha1.ScaleUpThenScaleDownUpgradePolicy
	pod := preparePod(namespaceName, podName1)
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
				if err != nil || len(replicaSetList.Items) != 1 {
					return false
				}
				moduleReplicaSetName = replicaSetList.Items[0].Name
				// module is 1 and allocate to pod1
				modules := &v1alpha1.ModuleList{}
				err = k8sClient.List(context.TODO(), modules, &client.ListOptions{Namespace: namespaceName, LabelSelector: labels.SelectorFromSet(map[string]string{
					label.ModuleReplicasetLabel: moduleReplicaSetName,
				})})
				if err == nil && len(modules.Items) == 1 && modules.Items[0].Labels[label.BaseInstanceIpLabel] == pod.Status.PodIP {
					return true
				}
				return false
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("create module replicaset with scaleup_then_scaledown upgradePolicy", func() {
		It("create module", func() {
			pod2 := preparePod(namespaceName, podName2)
			Expect(k8sClient.Create(context.TODO(), &pod2)).Should(Succeed())
			pod2.Status.PodIP = "127.0.0.2"
			Expect(k8sClient.Status().Update(context.TODO(), &pod2))

			k8sClient.Get(context.TODO(), types.NamespacedName{Name: moduleDeploymentName, Namespace: namespaceName}, &moduleDeployment)
			moduleDeployment.Spec.OperationStrategy.UpgradePolicy = v1alpha1.ScaleUpThenScaleDownUpgradePolicy
			moduleDeployment.Spec.Template.Spec.Module.Version = "1.0.1"
			k8sClient.Update(context.TODO(), &moduleDeployment)

			Eventually(func() bool {

				// old replicaSet replicas is 0
				key := types.NamespacedName{
					Name:      moduleReplicaSetName,
					Namespace: namespaceName,
				}
				var oldModuleReplicaSet v1alpha1.ModuleReplicaSet
				k8sClient.Get(context.TODO(), key, &oldModuleReplicaSet)

				replicaSetList := &v1alpha1.ModuleReplicaSetList{}
				err := k8sClient.List(context.TODO(), replicaSetList, &client.ListOptions{LabelSelector: labels.SelectorFromSet(map[string]string{label.ModuleDeploymentLabel: moduleDeployment.Name})}, client.InNamespace(moduleDeployment.Namespace))

				newModuleReplicaSet := v1alpha1.ModuleReplicaSet{}
				for _, moduleReplicaSet := range replicaSetList.Items {
					if moduleReplicaSet.Name != oldModuleReplicaSet.Name {
						newModuleReplicaSet = moduleReplicaSet
					}
				}
				if newModuleReplicaSet.Name == "" {
					return false
				}

				// module is 1
				modules := &v1alpha1.ModuleList{}
				err = k8sClient.List(context.TODO(), modules, &client.ListOptions{Namespace: namespaceName, LabelSelector: labels.SelectorFromSet(map[string]string{
					label.ModuleReplicasetLabel: newModuleReplicaSet.Name,
				})})
				if err != nil || len(modules.Items) != 1 {
					return false
				}

				// pod is available and reallocate to pod2
				if v1alpha1.ModuleInstanceStatusAvailable != modules.Items[0].Status.Status || modules.Items[0].Labels[label.BaseInstanceIpLabel] != pod2.Status.PodIP {
					return false
				}

				// old moduleReplicaSet replicas is 0
				if oldModuleReplicaSet.Spec.Replicas != 0 {
					return false
				}

				return true
			}, timeout, interval).Should(BeTrue())
		})
	})

})
