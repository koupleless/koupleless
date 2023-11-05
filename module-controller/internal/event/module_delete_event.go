package event

import "github.com/sofastack/sofa-serverless/api/v1alpha1"

type ModuleDeleteEvent struct {
	Module v1alpha1.Module
}

func PublishModuleDeleteEvent(Module v1alpha1.Module) error {
	return PublishEvent(ModuleDeleteEvent{Module: Module})
}

func (e ModuleDeleteEvent) GetEventType() EventType {
	return ModuleDelete
}
