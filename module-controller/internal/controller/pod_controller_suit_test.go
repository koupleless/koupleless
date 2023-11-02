package controller

import (
	"context"
	. "github.com/onsi/ginkgo/v2"
	. "github.com/onsi/gomega"
	"github.com/sofastack/sofa-serverless/api/v1alpha1"
	"github.com/sofastack/sofa-serverless/internal/constants/finalizer"
	"github.com/sofastack/sofa-serverless/internal/constants/label"
	"github.com/sofastack/sofa-serverless/internal/utils"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"time"
)

var _ = Describe("Pod Controller", func() {

	const timeout = time.Second * 30
	const interval = time.Second * 3
	namespaceName := "pod-controller-namespace"

	namespace := prepareNamespace(namespaceName)

	Context("delete pod", func() {
		It("all modules installed on the pod are all deleted", func() {
			Expect(k8sClient.Create(context.TODO(), &namespace)).Should(Succeed())
			pod := preparePod(namespaceName, "fake-pod-for-pod-controller")
			module1 := PrepareModule(namespaceName, "fake-module-for-pod-controller-1")
			module1.Labels[label.BaseInstanceNameLabel] = pod.Name
			module2 := PrepareModule(namespaceName, "fake-module-for-pod-controller-2")
			module2.Labels[label.BaseInstanceNameLabel] = pod.Name
			Expect(k8sClient.Create(context.TODO(), &module1)).Should(Succeed())
			Expect(k8sClient.Create(context.TODO(), &module2)).Should(Succeed())
			Expect(k8sClient.Create(context.TODO(), &pod)).Should(Succeed())
			Expect(k8sClient.Delete(context.TODO(), &pod)).Should(Succeed())

			Eventually(func() bool {
				moduleList := &v1alpha1.ModuleList{}
				k8sClient.List(context.TODO(), moduleList, &client.ListOptions{Namespace: pod.Namespace, LabelSelector: labels.SelectorFromSet(map[string]string{
					label.BaseInstanceNameLabel: pod.Name,
				})})
				return len(moduleList.Items) == 0
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("delete pod with pod is already deleted", func() {
		It("pod not exist", func() {
			pod := preparePod(namespaceName, "fake-pod-for-pod-controller-2")
			Expect(k8sClient.Create(context.TODO(), &pod)).Should(Succeed())
			pod.Labels[label.DeletePodLabel] = "true"
			Expect(k8sClient.Update(context.TODO(), &pod)).Should(Succeed())
			Eventually(func() bool {
				key := types.NamespacedName{
					Name:      "fake-pod-for-pod-controller-2",
					Namespace: namespaceName,
				}
				newPod := &corev1.Pod{}
				err := k8sClient.Get(context.TODO(), key, newPod)
				if err != nil {
					return errors.IsNotFound(err)
				}
				return false
			}, timeout, interval).Should(BeTrue())
		})
	})

	Context("delete pod by update delete label", func() {
		It("all modules are updated by delete label", func() {
			pod := preparePod(namespaceName, "fake-pod-for-pod-controller-3")
			module1 := PrepareModule(namespaceName, "fake-module1-for-pod-controller-3")
			module1.Labels[label.BaseInstanceNameLabel] = pod.Name
			module2 := PrepareModule(namespaceName, "fake-module2-for-pod-controller-3")
			module2.Labels[label.BaseInstanceNameLabel] = pod.Name
			Expect(k8sClient.Create(context.TODO(), &module1)).Should(Succeed())
			Expect(k8sClient.Create(context.TODO(), &module2)).Should(Succeed())
			utils.AddFinalizer(&pod.ObjectMeta, finalizer.AllocatePodFinalizer)
			Expect(k8sClient.Create(context.TODO(), &pod)).Should(Succeed())
			pod.Labels[label.DeletePodLabel] = "true"
			Expect(k8sClient.Update(context.TODO(), &pod)).Should(Succeed())
			Eventually(func() bool {
				moduleList := &v1alpha1.ModuleList{}
				k8sClient.List(context.TODO(), moduleList, &client.ListOptions{Namespace: pod.Namespace, LabelSelector: labels.SelectorFromSet(map[string]string{
					label.BaseInstanceNameLabel: pod.Name,
				})})
				return len(moduleList.Items) == 0
			}, timeout, interval).Should(BeTrue())
		})
	})
})
