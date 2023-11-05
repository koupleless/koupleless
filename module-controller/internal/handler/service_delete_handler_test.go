package handler

import (
	"github.com/sofastack/sofa-serverless/internal/event"
	"github.com/sofastack/sofa-serverless/internal/utils"
	"golang.org/x/net/context"
	"k8s.io/apimachinery/pkg/util/intstr"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"testing"
)

func TestServiceDeleteHandler(t *testing.T) {
	moduleDeployment := utils.PrepareModuleDeployment("default", "test-module-deployment-name")
	moduleDeployment.Spec.OperationStrategy.ServiceStrategy.EnableModuleService = true
	moduleDeployment.Spec.OperationStrategy.ServiceStrategy.Port = 8080
	moduleDeployment.Spec.OperationStrategy.ServiceStrategy.TargetPort = intstr.FromInt(8080)
	event.PublishModuleDeploymentDeleteEvent(TestServiceDeleteClient{}, nil, &moduleDeployment)
}

type TestServiceDeleteClient struct {
	utils.MockClient
}

func (m TestServiceDeleteClient) Get(ctx context.Context, key client.ObjectKey, obj client.Object, opts ...client.GetOption) error {
	obj.SetName("test-service")
	return nil
}
