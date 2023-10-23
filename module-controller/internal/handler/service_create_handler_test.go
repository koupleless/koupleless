package handler

import (
	"github.com/sofastack/sofa-serverless/internal/event"
	"github.com/sofastack/sofa-serverless/internal/utils"
	"k8s.io/apimachinery/pkg/util/intstr"
	"testing"
)

func TestServiceCreateHandler(t *testing.T) {
	moduleDeployment := utils.PrepareModuleDeployment("default", "test-module-deployment-name")
	moduleDeployment.Spec.OperationStrategy.ServiceStrategy.EnableModuleService = true
	moduleDeployment.Spec.OperationStrategy.ServiceStrategy.Port = 8080
	moduleDeployment.Spec.OperationStrategy.ServiceStrategy.TargetPort = intstr.FromInt(8080)
	event.PublishModuleDeploymentCreateEvent(TestServiceCreateClient{}, nil, &moduleDeployment)
}

type TestServiceCreateClient struct {
	utils.MockClient
}
