package event

import (
	"github.com/sofastack/sofa-serverless/internal/utils"
	"sigs.k8s.io/controller-runtime/pkg/log"
)

type EventType string

const (
	ModuleDeploymentCreate          EventType = "moduledeployment_create"
	ModuleDeploymentDelete          EventType = "moduledeployment_delete"
	ModuleReplicaSetCreate          EventType = "modulereplicaset_create"
	ModuleReplicaSetDelete          EventType = "modulereplicaset_delete"
	ModuleReplicaSetReplicasChanged EventType = "modulereplicaset_replicas_changed"
	ModuleCreate                    EventType = "module_create"
	ModuleDelete                    EventType = "module_delete"
)

type Event interface {
	GetEventType() EventType
}

var Handlers []Handler

type Handler interface {
	Handle(e Event) error

	InterestIn(e Event) bool

	Async() bool
}

func PublishEvent(e Event) error {
	for _, h := range Handlers {
		if h.InterestIn(e) {
			if h.Async() {
				h := h
				go func() {
					err := h.Handle(e)
					if err != nil {
						log.Log.Error(err, "handle event error", "event", e)
					}
				}()
			} else {
				err := h.Handle(e)
				if err != nil {
					return utils.Error(err, "handle event error", "event", e)
				}
			}
		}
	}
	return nil
}
