package controller

import (
	"context"
	. "github.com/onsi/ginkgo/v2"
	. "github.com/onsi/gomega"
	"github.com/sofastack/sofa-serverless/api/v1alpha1"
)

var _ = Describe("ModuleDeployment Controller", func() {
	baseAppName := "testAppName"
	Context("create module deployment", func() {
		It("create success", func() {
			moduleSpec := v1alpha1.ModuleSpec{
				Module: v1alpha1.ModuleInfo{
					Name:    "dynamic-provider",
					Version: "1.0.0",
					Url:     "http://serverless-opensource.oss-cn-shanghai.aliyuncs.com/module-packages/stable/dynamic-provider-1.0.0-ark-biz.jar",
				},
			}

			moduleTemplate := v1alpha1.ModuleTemplateSpec{
				Spec: moduleSpec,
			}

			spec := v1alpha1.ModuleDeploymentSpec{
				BaseAppName: baseAppName,
				Template:    moduleTemplate,
			}
			mockModuleDeployment := v1alpha1.ModuleDeployment{
				Spec: spec,
			}

			Expect(k8sClient.Create(context.TODO(), &mockModuleDeployment)).Should(Succeed())
		})
	})
})
