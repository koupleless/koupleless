package event

import (
	"context"
	"sigs.k8s.io/controller-runtime/pkg/log"

	"github.com/sofastack/sofa-serverless/api/v1alpha1"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

type ModuleDeploymentCreateEvent struct {
	client.Client
	Context          context.Context
	ModuleDeployment *v1alpha1.ModuleDeployment
}

func PublishModuleDeploymentCreateEvent(client client.Client, ctx context.Context, moduleDeployment *v1alpha1.ModuleDeployment) error {
	log.Log.Info("publish ModuleDeploymentCreateEvent", "moduleDeploymentName", moduleDeployment.Name)
	return PublishEvent(ModuleDeploymentCreateEvent{ModuleDeployment: moduleDeployment, Client: client, Context: ctx})
}

func (e ModuleDeploymentCreateEvent) GetEventType() EventType {
	return ModuleDeploymentCreate
}
