package controller

import (
	"context"
	"k8s.io/apimachinery/pkg/labels"
	"time"

	. "github.com/onsi/ginkgo/v2"
	. "github.com/onsi/gomega"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/client"

	"github.com/sofastack/sofa-serverless/api/v1alpha1"
	"github.com/sofastack/sofa-serverless/internal/constants/label"
	"github.com/sofastack/sofa-serverless/internal/utils"
)

var _ = Describe("ModuleReplicaSet Controller Scale Test", func() {

	const timeout = time.Second * 30
	const interval = time.Second * 3

	moduleDeploymentName := "test-module-deployment"
	var moduleReplicaSetName string
	namespaceName := "scale-test-namespace"
	podName1 := "test-pod"

	namespaceObj := prepareNamespace(namespaceName)
	deployment := prepareDeployment(namespaceName)
	moduleDeployment := utils.PrepareModuleDeployment(namespaceName, moduleDeploymentName)
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
				if err != nil {
					return false
				}
				if len(replicaSetList.Items) != 1 {
					return false
				}
				moduleReplicaSetName = replicaSetList.Items[0].Name
				k8sClient.Get(context.TODO(), types.NamespacedName{Name: moduleDeploymentName, Namespace: namespaceName}, &moduleDeployment)
				if moduleDeployment.Status.ReleaseStatus.Progress != v1alpha1.ModuleDeploymentReleaseProgressCompleted {
					return false
				}
				return true
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("scale up", func() {
		It("replica is 1 and create one module", func() {
			var newModuleDeployment v1alpha1.ModuleDeployment
			k8sClient.Get(context.TODO(), types.NamespacedName{Name: moduleDeploymentName, Namespace: namespaceName}, &newModuleDeployment)
			newModuleDeployment.Spec.Replicas = 1
			Expect(k8sClient.Update(context.TODO(), &newModuleDeployment)).Should(Succeed())
			Eventually(func() bool {
				// replicas is 1
				key := types.NamespacedName{
					Name:      moduleReplicaSetName,
					Namespace: namespaceName,
				}
				moduleReplicaSet := v1alpha1.ModuleReplicaSet{}
				err := k8sClient.Get(context.TODO(), key, &moduleReplicaSet)
				if err != nil && moduleReplicaSet.Spec.Replicas != 1 {
					return false
				}

				// module is 1 and allocate to pod1
				selector, err := metav1.LabelSelectorAsSelector(&moduleReplicaSet.Spec.Selector)
				modules := &v1alpha1.ModuleList{}
				err = k8sClient.List(context.TODO(), modules, &client.ListOptions{Namespace: moduleReplicaSet.Namespace, LabelSelector: selector})
				if err == nil && len(modules.Items) == 1 && modules.Items[0].Labels[label.BaseInstanceIpLabel] == pod.Status.PodIP {
					return true
				}
				return false
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("scale down", func() {
		It("replica is 0 and delete all module", func() {
			var newModuleDeployment v1alpha1.ModuleDeployment
			k8sClient.Get(context.TODO(), types.NamespacedName{Name: moduleDeploymentName, Namespace: namespaceName}, &newModuleDeployment)
			newModuleDeployment.Spec.Replicas = 0
			Expect(k8sClient.Update(context.TODO(), &newModuleDeployment)).Should(Succeed())
			Eventually(func() bool {

				// replicas is 0
				key := types.NamespacedName{
					Name:      moduleReplicaSetName,
					Namespace: namespaceName,
				}
				moduleReplicaSet := v1alpha1.ModuleReplicaSet{}
				err := k8sClient.Get(context.TODO(), key, &moduleReplicaSet)
				if err != nil && moduleReplicaSet.Spec.Replicas != 0 {
					return false
				}

				// modules are all deleted
				selector, err := metav1.LabelSelectorAsSelector(&moduleReplicaSet.Spec.Selector)
				modules := &v1alpha1.ModuleList{}
				err = k8sClient.List(context.TODO(), modules, &client.ListOptions{Namespace: moduleReplicaSet.Namespace, LabelSelector: selector})
				if err == nil && len(modules.Items) == 0 {
					return true
				}
				return false
			}, timeout, interval).Should(BeTrue())
		})
	})
})

func prepareModuleReplicaSet(namespace, moduleReplicaSetName, moduleDeploymentName string) v1alpha1.ModuleReplicaSet {

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
				"app":                               "dynamic-stock",
				label.MaxModuleCount:                "10",
				label.ModuleSchedulingStrategy:      string(v1alpha1.Scatter),
				label.ModuleDeploymentLabel:         moduleDeploymentName,
				label.ModuleReplicasetRevisionLabel: "1",
			},
			Annotations: map[string]string{},
		},
	}
	return moduleReplicaSet
}
