package controller

import (
	"context"
	"time"

	. "github.com/onsi/ginkgo/v2"
	. "github.com/onsi/gomega"
	"github.com/sofastack/sofa-serverless/api/v1alpha1"
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

	namespace := "default"
	moduleDeploymentName := "module-deployment-test-demo"
	moduleDeployment := prepareModuleDeployment(namespace, moduleDeploymentName)
	Context("create module deployment", func() {
		It("create module replicaset", func() {
			Expect(k8sClient.Create(context.TODO(), &moduleDeployment)).Should(Succeed())

			moduleReplicaSet := &v1alpha1.ModuleReplicaSet{}

			key := types.NamespacedName{
				Name:      getModuleReplicasName(moduleDeploymentName),
				Namespace: namespace,
			}

			Eventually(func() bool {
				k8sClient.Get(context.TODO(), key, moduleReplicaSet)
				return len(moduleReplicaSet.GetOwnerReferences()) > 0
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("update module deployment", func() {
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
				return replicaSetList.Items[0].Spec.Template.Spec.Module.Version == "1.0.1"
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

			moduleReplicaSet := &v1alpha1.ModuleReplicaSet{}
			moduleReplicaSetKey := types.NamespacedName{
				Name:      getModuleReplicasName(moduleDeploymentName),
				Namespace: namespace,
			}

			Eventually(func() bool {
				replicaSetErr := k8sClient.Get(context.TODO(), moduleReplicaSetKey, moduleReplicaSet)
				if replicaSetErr != nil && errors.IsNotFound(replicaSetErr) {
					moduleDeploymentErr := k8sClient.Get(context.TODO(), key, &newModuleDeployment)
					if moduleDeploymentErr != nil && errors.IsNotFound(moduleDeploymentErr) {
						return true
					}
				}
				return false
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
