package controller

import (
	"fmt"
	"testing"

	"github.com/stretchr/testify/assert"

	moduledeploymentv1alpha1 "github.com/sofastack/sofa-serverless/api/v1alpha1"
)

func TestIsModuleChange(t *testing.T) {
	module1 := moduledeploymentv1alpha1.ModuleInfo{
		Name:    "testModule1",
		Version: "v1",
	}
	module2 := moduledeploymentv1alpha1.ModuleInfo{
		Name:    "testModule1",
		Version: "v2",
	}
	module3 := moduledeploymentv1alpha1.ModuleInfo{
		Name:    "testModule2",
		Version: "v2",
	}
	assert.False(t, isModuleChange(module1, module1))
	assert.True(t, isModuleChange(module1, module2))
	assert.True(t, isModuleChange(module2, module3))
}

func TestGetModuleReplicasName(t *testing.T) {
	moduleDeploymentName := "module-deployment"
	revision := 1
	assert.Equal(t, fmt.Sprintf("%v-%v-%v", moduleDeploymentName, "replicas", revision), getModuleReplicasName(moduleDeploymentName, revision))
}

func TestIsOnlyModuleUrlChange(t *testing.T) {
	module1 := moduledeploymentv1alpha1.ModuleInfo{
		Name:    "testModule1",
		Version: "v1",
		Url:     "http://serverless-opensource.oss-cn-shanghai.aliyuncs.com/module-packages/stable/dynamic-provider-1.0.0-ark-biz.jar",
	}
	module2 := moduledeploymentv1alpha1.ModuleInfo{
		Name:    "testModule1",
		Version: "v1",
		Url:     "http://serverless-opensource.oss-cn-shanghai.aliyuncs.com/module-packages/stable/dynamic-provider-1.0.1-ark-biz.jar",
	}
	assert.False(t, isOnlyModuleUrlChange(module1, module1))
	assert.True(t, isOnlyModuleUrlChange(module1, module2))
}
