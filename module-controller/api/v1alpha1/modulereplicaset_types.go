/*
Copyright 2023.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package v1alpha1

import (
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// EDIT THIS FILE!  THIS IS SCAFFOLDING FOR YOU TO OWN!
// NOTE: json tags are required.  Any new fields you add must have json tags for the fields to be serialized.

// ModuleReplicaSetSpec defines the desired state of ModuleReplicaSet
type ModuleReplicaSetSpec struct {
	// INSERT ADDITIONAL SPEC FIELDS - desired state of cluster
	// Important: Run "make" to regenerate code after modifying this file
	Selector metav1.LabelSelector `json:"selector,omitempty"`

	Replicas int32 `json:"replicas,omitempty"`

	Template ModuleTemplateSpec `json:"template,omitempty"`

	OperationStrategy ModuleOperationStrategy `json:"operationStrategy,omitempty"`

	SchedulingStrategy ModuleSchedulingStrategy `json:"schedulingStrategy,omitempty"`

	MinReadySeconds int32 `json:"minReadySeconds,omitempty"`
}

// ModuleReplicaSetStatus defines the observed state of ModuleReplicaSet
type ModuleReplicaSetStatus struct {
	// INSERT ADDITIONAL STATUS FIELD - define observed state of cluster
	// Important: Run "make" to regenerate code after modifying this file
	Replicas int32 `json:"replicas,omitempty"`

	CurrentReplicas int32 `json:"currentReplicas,omitempty"`

	AvailableReplicas int32 `json:"availableReplicas,omitempty"`

	ReadyReplicas int32 `json:"readyReplicas,omitempty"`

	UnAvailableReplicas int32 `json:"unavailableReplicas,omitempty"`

	UpdatedReplicas int32 `json:"updatedReplicas,omitempty"`

	UpgradingReplicas int32 `json:"upgradingReplicas,omitempty"`

	UpdatedReadyReplicas int32 `json:"updatedReadyReplicas,omitempty"`

	UpdatedAvailableReplicas int32 `json:"updatedAvailableReplicas,omitempty"`

	CollisionCount *int32 `json:"collisionCount,omitempty"`

	Conditions []ModuleDeploymentCondition `json:"conditions,omitempty"`

	ObservedGeneration int64 `json:"observedGeneration,omitempty"`
}

//+kubebuilder:object:root=true
//+kubebuilder:subresource:status
//+kubebuilder:resource:shortName=mdrs

// ModuleReplicaSet is the Schema for the modulereplicasets API
type ModuleReplicaSet struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   ModuleReplicaSetSpec   `json:"spec,omitempty"`
	Status ModuleReplicaSetStatus `json:"status,omitempty"`
}

//+kubebuilder:object:root=true

// ModuleReplicaSetList contains a list of ModuleReplicaSet
type ModuleReplicaSetList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []ModuleReplicaSet `json:"items"`
}

func init() {
	SchemeBuilder.Register(&ModuleReplicaSet{}, &ModuleReplicaSetList{})
}
