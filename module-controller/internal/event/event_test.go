package event

import (
	"github.com/sofastack/sofa-serverless/api/v1alpha1"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestPublishModuleCreateEvent(t *testing.T) {
	Handlers = append(Handlers, TestPublishModuleCreateEventHandler{})
	assert.Equal(t, ModuleCreate, ModuleCreateEvent{}.GetEventType())
	module := v1alpha1.Module{}
	PublishModuleCreateEvent(module)
}

type TestPublishModuleCreateEventHandler struct {
}

func (t TestPublishModuleCreateEventHandler) Handle(e Event) error {
	return nil
}

func (t TestPublishModuleCreateEventHandler) InterestIn(e Event) bool {
	return e.GetEventType() == ModuleCreate
}

func (t TestPublishModuleCreateEventHandler) Async() bool {
	return false
}

func TestPublishModuleDeleteEvent(t *testing.T) {
	Handlers = append(Handlers, TestPublishModuleDeleteEventHandler{})
	assert.Equal(t, ModuleDelete, ModuleDeleteEvent{}.GetEventType())
	module := v1alpha1.Module{}
	PublishModuleDeleteEvent(module)
}

type TestPublishModuleDeleteEventHandler struct {
}

func (t TestPublishModuleDeleteEventHandler) Handle(e Event) error {
	return nil
}

func (t TestPublishModuleDeleteEventHandler) InterestIn(e Event) bool {
	return e.GetEventType() == ModuleDelete
}

func (t TestPublishModuleDeleteEventHandler) Async() bool {
	return true
}

func TestPublishModuleDeploymentCreateEvent(t *testing.T) {
	assert.Equal(t, ModuleDeploymentCreate, ModuleDeploymentCreateEvent{}.GetEventType())
	moduleDeployment := v1alpha1.ModuleDeployment{}
	PublishModuleDeploymentCreateEvent(nil, nil, &moduleDeployment)
}

func TestPublishModuleDeploymentDeleteEvent(t *testing.T) {
	assert.Equal(t, ModuleDeploymentDelete, ModuleDeploymentDeleteEvent{}.GetEventType())
	moduleDeployment := v1alpha1.ModuleDeployment{}
	PublishModuleDeploymentDeleteEvent(nil, nil, &moduleDeployment)
}

func TestPublishModuleReplicaSetCreateEvent(t *testing.T) {
	assert.Equal(t, ModuleReplicaSetCreate, ModuleReplicaSetCreateEvent{}.GetEventType())
	moduleReplicaSet := v1alpha1.ModuleReplicaSet{}
	PublishModuleReplicaSetCreateEvent(nil, nil, &moduleReplicaSet)
}

func TestPublishModuleReplicaSetDeleteEvent(t *testing.T) {
	assert.Equal(t, ModuleReplicaSetDelete, ModuleReplicaSetDeleteEvent{}.GetEventType())
	moduleReplicaSet := v1alpha1.ModuleReplicaSet{}
	PublishModuleReplicaSetDeleteEvent(nil, nil, &moduleReplicaSet)
}

func TestPublishModuleReplicaSetReplicasChangedEvent(t *testing.T) {
	assert.Equal(t, ModuleReplicaSetReplicasChanged, ModuleReplicaSetReplicasChangedEvent{}.GetEventType())
	moduleReplicaSet := v1alpha1.ModuleReplicaSet{}
	PublishModuleReplicaSetReplicasChangedEvent(nil, nil, &moduleReplicaSet)
}
