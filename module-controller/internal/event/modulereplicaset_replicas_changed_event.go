package event

import (
	"context"

	"github.com/sofastack/sofa-serverless/api/v1alpha1"
	"sigs.k8s.io/controller-runtime/pkg/client"
)

type ModuleReplicaSetReplicasChangedEvent struct {
	client.Client
	Context          context.Context
	ModuleReplicaSet *v1alpha1.ModuleReplicaSet
}

func PublishModuleReplicaSetReplicasChangedEvent(client client.Client, ctx context.Context, moduleReplicaSet *v1alpha1.ModuleReplicaSet) error {
	return PublishEvent(ModuleReplicaSetReplicasChangedEvent{ModuleReplicaSet: moduleReplicaSet, Client: client, Context: ctx})
}

func (e ModuleReplicaSetReplicasChangedEvent) GetEventType() EventType {
	return ModuleReplicaSetReplicasChanged
}
