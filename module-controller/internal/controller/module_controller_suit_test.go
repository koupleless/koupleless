package controller

import (
	"context"
	"encoding/json"
	. "github.com/onsi/ginkgo/v2"
	. "github.com/onsi/gomega"
	"github.com/sofastack/sofa-serverless/api/v1alpha1"
	"github.com/sofastack/sofa-serverless/internal/arklet"
	"github.com/sofastack/sofa-serverless/internal/constants/finalizer"
	"github.com/sofastack/sofa-serverless/internal/constants/label"
	"github.com/sofastack/sofa-serverless/internal/utils"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"
	"net/http"
	"net/http/httptest"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/log"
	"time"
)

var _ = Describe("Module Controller", func() {
	const timeout = time.Second * 30
	const interval = time.Second * 3
	moduleName := "test-module-name"
	namespace := "default"

	Context("create module deployment without pod", func() {
		It("should be pending status", func() {
			module := prepareModule()
			Expect(k8sClient.Create(context.TODO(), &module)).Should(Succeed())

			key := types.NamespacedName{
				Name:      moduleName,
				Namespace: namespace,
			}
			Eventually(func() bool {
				k8sClient.Get(context.TODO(), key, &module)
				log.Log.Info("module status", "status", module.Status.Status)
				return module.Status.Status == v1alpha1.ModuleInstanceStatusPending
			}, timeout, interval).Should(BeTrue())

			Expect(k8sClient.Delete(context.TODO(), &module)).Should(Succeed())
		})
	})

	module := prepareModule()
	Context("create module deployment with pod", func() {
		svr := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			data := arklet.ArkletResponse{
				Code: arklet.Success,
			}
			w.Header().Set("Content-Type", "application/json")
			json.NewEncoder(w).Encode(data)
		}))
		arklet.MockClient(svr.URL)

		It("should be available status", func() {
			pod := preparePod("fake-pod-2")
			k8sClient.Create(context.TODO(), &pod)
			pod.Status.PodIP = "127.0.0.1"
			k8sClient.Status().Update(context.TODO(), &pod)

			Expect(k8sClient.Create(context.TODO(), &module)).Should(Succeed())

			key := types.NamespacedName{
				Name:      moduleName,
				Namespace: namespace,
			}
			Eventually(func() bool {
				k8sClient.Get(context.TODO(), key, &module)
				log.Log.Info("module status", "status", module.Status.Status)
				return module.Status.Status == v1alpha1.ModuleInstanceStatusAvailable
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("delete module deployment with ip by deleting module", func() {
		moduleReplicaSetName := "test-modulereplicaset"
		updateModuleUrl := "https://module-test-url"
		It("should be deleted and recreate a new one", func() {
			module.Labels[label.ModuleReplicasetLabel] = moduleReplicaSetName
			module.Labels[label.ModuleNameLabel] = "test-module"
			Expect(k8sClient.Update(context.TODO(), &module)).Should(Succeed())
			Expect(k8sClient.Delete(context.TODO(), &module)).Should(Succeed())
			moduleReplicaSet := prepareModuleReplicaSet(namespace, moduleReplicaSetName)
			moduleReplicaSet.Spec.Template.Spec.Module.Url = updateModuleUrl
			Expect(k8sClient.Create(context.TODO(), &moduleReplicaSet)).Should(Succeed())
			key := types.NamespacedName{
				Name:      moduleName,
				Namespace: namespace,
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
			utils.AddFinalizer(&module.ObjectMeta, finalizer.ModuleInstalledFinalizer)
			Expect(k8sClient.Update(context.TODO(), &module)).Should(Succeed())
			key := types.NamespacedName{
				Name:      module.Name,
				Namespace: namespace,
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
})

func prepareModule() v1alpha1.Module {
	moduleName := "test-module-name"
	namespace := "default"
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
