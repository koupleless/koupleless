package handler

import (
	"fmt"
	"github.com/sofastack/sofa-serverless/internal/event"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/log"
)

type ServiceDeleteHandler struct {
}

func (h ServiceDeleteHandler) Async() bool {
	return false
}

func (h ServiceDeleteHandler) Handle(e event.Event) error {
	moduleDeploymentDeleteEvent := e.(event.ModuleDeploymentDeleteEvent)

	serviceStrategy := moduleDeploymentDeleteEvent.ModuleDeployment.Spec.OperationStrategy.ServiceStrategy
	if !serviceStrategy.EnableModuleService {
		return nil
	}
	log.Log.Info("start to delete service", "event", e)
	moduleName := moduleDeploymentDeleteEvent.ModuleDeployment.Spec.Template.Spec.Module.Name

	serviceName := fmt.Sprintf("%s-%s", moduleName, "service")

	service := &corev1.Service{}
	err := moduleDeploymentDeleteEvent.Client.Get(moduleDeploymentDeleteEvent.Context, types.NamespacedName{Namespace: moduleDeploymentDeleteEvent.ModuleDeployment.Namespace, Name: serviceName}, service)
	if err == nil && service.Name != "" {
		err := moduleDeploymentDeleteEvent.Client.Delete(moduleDeploymentDeleteEvent.Context, service)
		if err != nil {
			return err
		}
	}
	log.Log.Info("finish delete service", "serviceName", serviceName)
	return nil
}

func (h ServiceDeleteHandler) InterestIn(e event.Event) bool {
	return e.GetEventType() == event.ModuleDeploymentDelete
}

func init() {
	event.Handlers = append(event.Handlers, ServiceDeleteHandler{})
}
