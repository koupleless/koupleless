package event

import "github.com/sofastack/sofa-serverless/api/v1alpha1"

type ModuleCreateEvent struct {
	Module v1alpha1.Module
}

func PublishModuleCreateEvent(Module v1alpha1.Module) error {
	return PublishEvent(ModuleCreateEvent{Module: Module})
}

func (e ModuleCreateEvent) GetEventType() EventType {
	return ModuleCreate
}
