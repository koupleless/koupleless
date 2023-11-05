package event

import (
	"context"
	"sigs.k8s.io/controller-runtime/pkg/log"

	"github.com/sofastack/sofa-serverless/api/v1alpha1"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

type ModuleDeploymentDeleteEvent struct {
	client.Client
	Context          context.Context
	ModuleDeployment *v1alpha1.ModuleDeployment
}

func PublishModuleDeploymentDeleteEvent(client client.Client, ctx context.Context, moduleDeployment *v1alpha1.ModuleDeployment) error {
	log.Log.Info("publish ModuleDeploymentDeleteEvent", "moduleDeploymentName", moduleDeployment.Name)
	return PublishEvent(ModuleDeploymentDeleteEvent{ModuleDeployment: moduleDeployment, Client: client, Context: ctx})
}

func (e ModuleDeploymentDeleteEvent) GetEventType() EventType {
	return ModuleDeploymentDelete
}
