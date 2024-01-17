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
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/util/intstr"
)

type ModuleDeploymentConditionType string

// These are valid conditions of a deployment.
const (
	// DeploymentAvailable Available means the deployment is available, ie. at least the minimum available
	// replicas required are up and running for at least minReadySeconds.
	DeploymentAvailable ModuleDeploymentConditionType = "Available"
	// DeploymentProgressing Progressing means the deployment is progressing. Progress for a deployment is
	// considered when a new replica set is created or adopted, and when new pods scale
	// up or old pods scale down. Progress is not estimated for paused deployments or
	// when progressDeadlineSeconds is not specified.
	DeploymentProgressing ModuleDeploymentConditionType = "Progressing"
	// DeploymentReplicaFailure ReplicaFailure is added in a deployment when one of its pods fails to be created
	// or deleted.
	DeploymentReplicaFailure ModuleDeploymentConditionType = "ReplicaFailure"
)

type ReleaseProgress string

const (
	ModuleDeploymentReleaseProgressInit                   ReleaseProgress = "Init"
	ModuleDeploymentReleaseProgressWaitingForConfirmation ReleaseProgress = "WaitingForConfirmation"
	ModuleDeploymentReleaseProgressExecuting              ReleaseProgress = "Executing"
	ModuleDeploymentReleaseProgressPaused                 ReleaseProgress = "Paused"
	ModuleDeploymentReleaseProgressCompleted              ReleaseProgress = "Completed"
	ModuleDeploymentReleaseProgressAborted                ReleaseProgress = "Aborted"
	ModuleDeploymentReleaseProgressTerminating            ReleaseProgress = "Terminating"
	ModuleDeploymentReleaseProgressTerminated             ReleaseProgress = "Terminated"
)

type ModuleUpgradeType string

const (
	InstallThenUninstallUpgradePolicy ModuleUpgradeType = "install_then_uninstall"
	UninstallThenInstallUpgradePolicy ModuleUpgradeType = "uninstall_then_install"
	ScaleUpThenScaleDownUpgradePolicy ModuleUpgradeType = "scaleup_then_scaledown"
)

type ModuleSchedulingType string

const (
	Scatter  ModuleSchedulingType = "scatter"
	Stacking ModuleSchedulingType = "stacking"
)

type ReleaseStatus struct {
	// Records the latest revision.
	// +optional
	UpdateRevision string `json:"updateRevision,omitempty"`

	// Records the current batch serial number.
	// +optional
	CurrentBatch int32 `json:"currentBatch,omitempty"`

	// Records the real batch count
	// +optional
	RealBatchCount int32 `json:"realBatchCount,omitempty"`

	// Records the original delta replicas
	// +optional
	OriginalDeltaReplicas int32 `json:"originalDeltaReplicas,omitempty"`

	// The phase current whole release reach
	// +optional
	Progress ReleaseProgress `json:"progress,omitempty"`

	// the phase current batch release reach
	BatchProgress ReleaseProgress `json:"batchProgress,omitempty"`

	// Last time the release transitioned from one status to another.
	LastTransitionTime metav1.Time `json:"lastTransitionTime,omitempty"`

	NextReconcileTime metav1.Time `json:"nextReconcileTime,omitempty"`
}

type ModuleDeploymentCondition struct {
	// Type of in place set condition.
	Type ModuleDeploymentConditionType `json:"type,omitempty"`

	// Status of the condition, one of True, False, Unknown.
	Status corev1.ConditionStatus `json:"status,omitempty"`

	// Last time the condition transitioned from one status to another.
	LastTransitionTime metav1.Time `json:"last_transition_time,omitempty"`

	// The reason for the condition's last transition.
	Reason string `json:"reason,omitempty"`

	// A human readable message indicating details about the transition.
	Message string `json:"message,omitempty"`
}

type ModuleOperationStrategy struct {
	NeedConfirm bool `json:"needConfirm,omitempty"`

	UseBeta bool `json:"useBeta,omitempty"`

	BatchCount int32 `json:"batchCount,omitempty"`

	MaxUnavailable int32 `json:"maxUnavailable,omitempty"`

	GrayTimeBetweenBatchSeconds int32 `json:"grayTimeBetweenBatchSeconds,omitempty"`

	UpgradePolicy ModuleUpgradeType `json:"upgradePolicy,omitempty"`

	ServiceStrategy ModuleServiceStrategy `json:"serviceStrategy,omitempty"`
}

type ModuleSchedulingStrategy struct {

	// +kubebuilder:validation:Enum={"scatter","stacking"}
	// +kubebuilder:default="scatter"
	SchedulingPolicy ModuleSchedulingType `json:"schedulingPolicy,omitempty"`
}

type ModuleServiceStrategy struct {
	EnableModuleService bool `json:"enableModuleService,omitempty"`

	Port int32 `json:"port,omitempty"`

	TargetPort intstr.IntOrString `json:"targetPort,omitempty" protobuf:"bytes,4,opt,name=targetPort"`
}

// ModuleDeploymentSpec defines the desired state of ModuleDeployment
type ModuleDeploymentSpec struct {
	// INSERT ADDITIONAL SPEC FIELDS - desired state of cluster
	// Important: Run "make" to regenerate code after modifying this file
	// +kubebuilder:validation:MinLength=1
	BaseDeploymentName string `json:"baseDeploymentName"`

	Template ModuleTemplateSpec `json:"template,omitempty"`

	// +kubebuilder:validation:Minimum=-1
	Replicas int32 `json:"replicas,omitempty"`

	MinReadySeconds int32 `json:"minReadySeconds,omitempty"`

	RevisionHistoryLimit int32 `json:"revisionHistoryLimit,omitempty"`

	ProgressDeadlineSeconds int32 `json:"progressDeadlineSeconds,omitempty"`

	// +kubebuilder:default:=0
	// +kubebuilder:validation:Minimum=0
	ConfirmBatchNum int32 `json:"confirmBatchNum,omitempty"`

	OperationStrategy ModuleOperationStrategy `json:"operationStrategy,omitempty"`

	SchedulingStrategy ModuleSchedulingStrategy `json:"schedulingStrategy,omitempty"`
}

// ModuleDeploymentStatus defines the observed state of ModuleDeployment
type ModuleDeploymentStatus struct {
	Replicas int32 `json:"replicas,omitempty"`

	AvailableReplicas int32 `json:"availableReplicas,omitempty"`

	ReadyReplicas int32 `json:"readyReplicas,omitempty"`

	UnAvailableReplicas int32 `json:"unavailableReplicas,omitempty"`

	UpdatedReplicas int32 `json:"updatedReplicas,omitempty"`

	UpgradingReplicas int32 `json:"upgradingReplicas,omitempty"`

	UpdatedReadyReplicas int32 `json:"updatedReadyReplicas,omitempty"`

	UpdatedAvailableReplicas int32 `json:"updatedAvailableReplicas,omitempty"`

	CollisionCount *int32 `json:"collisionCount,omitempty"`

	Conditions []ModuleDeploymentCondition `json:"conditions,omitempty"`

	ReleaseStatus *ReleaseStatus `json:"releaseStatus,omitempty"`

	ObservedGeneration int64 `json:"observedGeneration,omitempty"`
}

//+kubebuilder:object:root=true
//+kubebuilder:subresource:status
//+kubebuilder:resource:shortName=mddeploy

// ModuleDeployment is the Schema for the moduledeployments API
type ModuleDeployment struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   ModuleDeploymentSpec   `json:"spec,omitempty"`
	Status ModuleDeploymentStatus `json:"status,omitempty"`
}

//+kubebuilder:object:root=true

// ModuleDeploymentList contains a list of ModuleDeployment
type ModuleDeploymentList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []ModuleDeployment `json:"items"`
}

func init() {
	SchemeBuilder.Register(&ModuleDeployment{}, &ModuleDeploymentList{})
}
