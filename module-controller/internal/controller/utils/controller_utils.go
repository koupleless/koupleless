package utils

import (
	"fmt"
	moduledeploymentv1alpha1 "github.com/sofastack/sofa-serverless/api/v1alpha1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	ctrl "sigs.k8s.io/controller-runtime"
)

type ModuleReplicaSetsByCreationTimestamp []*moduledeploymentv1alpha1.ModuleReplicaSet

func (m ModuleReplicaSetsByCreationTimestamp) Len() int {
	return len(m)
}

func (m ModuleReplicaSetsByCreationTimestamp) Less(i, j int) bool {
	if m[i].CreationTimestamp.Equal(&m[j].CreationTimestamp) {
		return m[i].Name < m[j].Name
	}
	return m[i].CreationTimestamp.Before(&m[j].CreationTimestamp)
}

func (m ModuleReplicaSetsByCreationTimestamp) Swap(i, j int) {
	m[i], m[j] = m[j], m[i]
}

func AddFinalizer(meta *metav1.ObjectMeta, finalizer string) bool {
	if meta.Finalizers == nil {
		meta.Finalizers = []string{}
	}
	for _, s := range meta.Finalizers {
		if s == finalizer {
			return false
		}
	}
	meta.Finalizers = append(meta.Finalizers, finalizer)
	return true
}

func RemoveFinalizer(meta *metav1.ObjectMeta, needle string) bool {
	finalizers := make([]string, 0)
	found := false
	if meta.Finalizers != nil {
		for _, finalizer := range meta.Finalizers {
			if finalizer != needle {
				finalizers = append(finalizers, finalizer)
			} else {
				found = true
			}
		}
		meta.Finalizers = finalizers
	}

	return found
}

func HasFinalizer(meta *metav1.ObjectMeta, needle string) bool {
	if meta.Finalizers != nil {
		for _, finalizer := range meta.Finalizers {
			if finalizer == needle {
				return true
			}
		}
	}

	return false
}

func Key(req ctrl.Request) string {
	return fmt.Sprintf("%s/%s", req.Namespace, req.Name)
}
