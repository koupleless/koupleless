package handler

import (
	"github.com/koupleless/module-controller/internal/event"
	"github.com/koupleless/module-controller/internal/utils"
	"testing"
)

func TestModuleReplicaSetReplicasChangedHandler(t *testing.T) {
	moduleReplicaSet := utils.PrepareModuleReplicaSet("default", "test-module-replica-set-name")
	event.PublishModuleReplicaSetReplicasChangedEvent(TestModuleReplicaSetReplicasChangedClient{}, nil, &moduleReplicaSet)
}

type TestModuleReplicaSetReplicasChangedClient struct {
	utils.MockClient
}
