package handler

import (
	"github.com/sofastack/sofa-serverless/internal/event"
	"github.com/sofastack/sofa-serverless/internal/utils"
	"testing"
)

func TestModuleReplicaSetReplicasChangedHandler(t *testing.T) {
	moduleReplicaSet := utils.PrepareModuleReplicaSet("default", "test-module-replica-set-name")
	event.PublishModuleReplicaSetReplicasChangedEvent(TestModuleReplicaSetReplicasChangedClient{}, nil, &moduleReplicaSet)
}

type TestModuleReplicaSetReplicasChangedClient struct {
	utils.MockClient
}
