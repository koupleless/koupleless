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

type ModuleInstanceStatus string

const (
	ModuleInstanceStatusPending     ModuleInstanceStatus = "Pending"
	ModuleInstanceStatusPrepare     ModuleInstanceStatus = "Prepare"
	ModuleInstanceStatusUpgrading   ModuleInstanceStatus = "Upgrading"
	ModuleInstanceStatusCompleting  ModuleInstanceStatus = "Completing"
	ModuleInstanceStatusAvailable   ModuleInstanceStatus = "Available"
	ModuleInstanceStatusTerminating ModuleInstanceStatus = "Terminating"
)

// ModuleTemplate describes a template for creating copies of a predefined module.
type ModuleTemplate struct {
	metav1.TypeMeta `json:",inline"`
	// Standard object's metadata.
	// More info: https://git.k8s.io/community/contributors/devel/sig-architecture/api-conventions.md#metadata
	// +optional
	metav1.ObjectMeta `json:"metadata,omitempty" protobuf:"bytes,1,opt,name=metadata"`

	// Template defines the pods that will be created from this module template.
	// +optional
	Template ModuleTemplateSpec `json:"template,omitempty" protobuf:"bytes,2,opt,name=template"`
}

type ModuleTemplateSpec struct {
	// Standard object's metadata.
	// More info: https://git.k8s.io/community/contributors/devel/sig-architecture/api-conventions.md#metadata
	// +optional
	metav1.ObjectMeta `json:"metadata,omitempty" protobuf:"bytes,1,opt,name=metadata"`

	// Specification of the desired behavior of the module
	// +optional
	Spec ModuleSpec `json:"spec,omitempty" protobuf:"bytes,2,opt,name=spec"`
}

type ModuleInfo struct {
	// +kubebuilder:validation:MinLength=1
	Name string `json:"name"`
	// +kubebuilder:validation:MinLength=1
	Version string `json:"version"`
	// +kubebuilder:validation:Format=uri
	Url  string `json:"url"`
	Type string `json:"type,omitempty"`
	Md5  string `json:"md5,omitempty"`
}

// ModuleSpec defines the desired state of Module
type ModuleSpec struct {
	Selector metav1.LabelSelector `json:"selector,omitempty"`
	Module   ModuleInfo           `json:"module"`
}

// ModuleStatus defines the observed state of Module
type ModuleStatus struct {
	// INSERT ADDITIONAL STATUS FIELD - define observed state of cluster
	// Important: Run "make" to regenerate code after modifying this file

	Status ModuleInstanceStatus `json:"status,omitempty"`
	// Last time the ModuleStatus transitioned from one status to another.
	LastTransitionTime metav1.Time `json:"last_transition_time,omitempty"`
}

//+kubebuilder:object:root=true
//+kubebuilder:subresource:status

// Module is the Schema for the modules API
type Module struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   ModuleSpec   `json:"spec,omitempty"`
	Status ModuleStatus `json:"status,omitempty"`
}

//+kubebuilder:object:root=true

// ModuleList contains a list of Module
type ModuleList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []Module `json:"items"`
}

func init() {
	SchemeBuilder.Register(&Module{}, &ModuleList{})
}
