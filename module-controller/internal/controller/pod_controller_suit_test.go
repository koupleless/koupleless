package controller

import (
	"context"
	. "github.com/onsi/ginkgo/v2"
	. "github.com/onsi/gomega"
	"github.com/sofastack/sofa-serverless/api/v1alpha1"
	"github.com/sofastack/sofa-serverless/internal/constants/label"
	"k8s.io/apimachinery/pkg/labels"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"time"
)

var _ = Describe("Pod Controller", func() {
	const timeout = time.Second * 30
	const interval = time.Second * 3
	namespaceName := "pod-controller-namespace"

	Context("delete pod", func() {
		It("all modules installed on the pod are all deleted", func() {
			namespace := prepareNamespace(namespaceName)
			Expect(k8sClient.Create(context.TODO(), &namespace)).Should(Succeed())
			pod := preparePod(namespaceName, "fake-pod-for-pod-controller")
			module1 := prepareModule(namespaceName, "fake-module-for-pod-controller-1")
			module1.Labels[label.BaseInstanceNameLabel] = pod.Name
			module2 := prepareModule(namespaceName, "fake-module-for-pod-controller-2")
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
})
