package controller

import (
	moduledeploymentv1alpha1 "github.com/sofastack/sofa-serverless/api/v1alpha1"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestIsModuleChanges(t *testing.T) {
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
	assert.False(t, isModuleChanges(module1, module1))
	assert.True(t, isModuleChanges(module1, module2))
	assert.True(t, isModuleChanges(module2, module3))
}

func TestGetModuleReplicasName(t *testing.T) {
	moduleDeploymentName := "module-deployment"
	assert.Equal(t, moduleDeploymentName+"-replicas", getModuleReplicasName(moduleDeploymentName))
}
