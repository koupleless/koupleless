package handler

import (
	"fmt"
	"github.com/sofastack/sofa-serverless/internal/constants/label"
	"github.com/sofastack/sofa-serverless/internal/event"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/log"
)

type ServiceCreateHandler struct {
}

func (h ServiceCreateHandler) Async() bool {
	return false
}

func (h ServiceCreateHandler) Handle(e event.Event) error {
	moduleDeploymentCreateEvent := e.(event.ModuleDeploymentCreateEvent)

	serviceStrategy := moduleDeploymentCreateEvent.ModuleDeployment.Spec.OperationStrategy.ServiceStrategy
	if !serviceStrategy.EnableModuleService {
		return nil
	}
	log.Log.Info("start to create service", "event", e)
	moduleName := moduleDeploymentCreateEvent.ModuleDeployment.Spec.Template.Spec.Module.Name

	serviceName := fmt.Sprintf("%s-%s", moduleName, "service")

	service := &corev1.Service{}
	err := moduleDeploymentCreateEvent.Client.Get(moduleDeploymentCreateEvent.Context, types.NamespacedName{Namespace: moduleDeploymentCreateEvent.ModuleDeployment.Namespace, Name: serviceName}, service)
	if err == nil && service.Name != "" {
		// already create service
		return nil
	}
	// create service
	service = &corev1.Service{
		ObjectMeta: metav1.ObjectMeta{
			Name:      fmt.Sprintf("%s-%s", moduleName, "service"),
			Namespace: moduleDeploymentCreateEvent.ModuleDeployment.Namespace,
		},
		Spec: corev1.ServiceSpec{
			Selector: map[string]string{
				label.ModuleLabelPrefix + moduleName: "true",
			},
			Ports: []corev1.ServicePort{
				{
					Name:       "http",
					Protocol:   corev1.ProtocolTCP,
					Port:       serviceStrategy.Port,
					TargetPort: serviceStrategy.TargetPort,
				},
			},
			Type: corev1.ServiceTypeNodePort,
		},
	}

	err = moduleDeploymentCreateEvent.Client.Create(moduleDeploymentCreateEvent.Context, service)
	if err != nil {
		return err
	}
	log.Log.Info("finish create service", "serviceName", serviceName)
	return nil
}

func (h ServiceCreateHandler) InterestIn(e event.Event) bool {
	return e.GetEventType() == event.ModuleDeploymentCreate
}

func init() {
	event.Handlers = append(event.Handlers, ServiceCreateHandler{})
}
