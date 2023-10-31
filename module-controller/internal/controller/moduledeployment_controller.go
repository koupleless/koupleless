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

package controller

import (
	"context"
	"fmt"
	"math"
	"strconv"
	"time"

	v1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/utils/pointer"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/log"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"

	"github.com/sofastack/sofa-serverless/api/v1alpha1"
	"github.com/sofastack/sofa-serverless/internal/constants/finalizer"
	"github.com/sofastack/sofa-serverless/internal/constants/label"
	"github.com/sofastack/sofa-serverless/internal/event"
	"github.com/sofastack/sofa-serverless/internal/utils"
)

// ModuleDeploymentReconciler reconciles a ModuleDeployment object
type ModuleDeploymentReconciler struct {
	client.Client
	Scheme *runtime.Scheme
}

//+kubebuilder:rbac:groups=serverless.alipay.com,resources=moduledeployments,verbs=get;list;watch;create;update;patch;delete
//+kubebuilder:rbac:groups=serverless.alipay.com,resources=moduledeployments/status,verbs=get;update;patch
//+kubebuilder:rbac:groups=serverless.alipay.com,resources=moduledeployments/finalizers,verbs=update

//+kubebuilder:rbac:groups=apps,resources=deployments,verbs=get;list;watch
//+kubebuilder:rbac:groups="",resources=pods,verbs=create;delete;get;list;patch;update;watch
//+kubebuilder:rbac:groups="",resources=services,verbs=create;delete;get;list;patch;update;watch

// Reconcile is part of the main kubernetes reconciliation loop which aims to
// move the current state of the cluster closer to the desired state.
// TODO(user): Modify the Reconcile function to compare the state specified by
// the ModuleDeployment object against the actual cluster state, and then
// perform operations to make the cluster state reflect the state specified by
// the user.
//
// For more details, check Reconcile and its Result here:
// - https://pkg.go.dev/sigs.k8s.io/controller-runtime@v0.15.0/pkg/reconcile
func (r *ModuleDeploymentReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	log.Log.Info("start reconcile for moduleDeployment", "request", req)
	defer log.Log.Info("finish reconcile for moduleDeployment", "request", req)

	// get the moduleDeployment
	moduleDeployment := &v1alpha1.ModuleDeployment{}
	err := r.Client.Get(ctx, req.NamespacedName, moduleDeployment)
	if err != nil {
		if errors.IsNotFound(err) {
			log.Log.Info("moduleDeployment is deleted", "moduleDeploymentName", moduleDeployment.Name)
			return reconcile.Result{}, nil
		}
		return ctrl.Result{}, utils.Error(err, "Failed to get moduleDeployment", "moduleDeploymentName", moduleDeployment.Name)
	}

	if moduleDeployment.DeletionTimestamp != nil {
		// delete moduleDeployment
		event.PublishModuleDeploymentDeleteEvent(r.Client, ctx, moduleDeployment)
		return r.handleDeletingModuleDeployment(ctx, moduleDeployment)
	}

	if moduleDeployment.Generation == 1 {
		event.PublishModuleDeploymentCreateEvent(r.Client, ctx, moduleDeployment)
	}

	if moduleDeployment.Spec.Pause {
		return ctrl.Result{}, nil
	}

	// update moduleDeployment owner reference
	if !utils.HasOwnerReference(&moduleDeployment.ObjectMeta, moduleDeployment.Spec.BaseDeploymentName) {
		err = r.updateOwnerReference(ctx, moduleDeployment)
		if err != nil {
			return ctrl.Result{}, err
		}
		return ctrl.Result{}, nil
	}

	// create moduleReplicaSet
	newRS, _, moduleVersionChanged, err := r.createOrGetModuleReplicas(ctx, moduleDeployment)
	if err != nil {
		return ctrl.Result{}, err
	}

	if moduleDeployment.Status.ReleaseStatus == nil || moduleVersionChanged {
		moduleDeployment.Status.ReleaseStatus = &v1alpha1.ReleaseStatus{
			CurrentBatch:       1,
			Progress:           v1alpha1.ModuleDeploymentReleaseProgressInit,
			LastTransitionTime: metav1.Now(),
		}
	}

	releaseStatus := moduleDeployment.Status.ReleaseStatus
	switch releaseStatus.Progress {
	case v1alpha1.ModuleDeploymentReleaseProgressInit:
		handleInitModuleDeployment(moduleDeployment, newRS)
		log.Log.Info("update moduleDeployment release status", "moduleDeploymentName", moduleDeployment.Name)
		if err := utils.UpdateStatus(r.Client, ctx, moduleDeployment); err != nil {
			return ctrl.Result{}, utils.Error(err, "update release status failed when init moduleDeployment")
		}
	case v1alpha1.ModuleDeploymentReleaseProgressExecuting:
		return r.updateModuleReplicaSet(ctx, moduleDeployment, newRS)
	case v1alpha1.ModuleDeploymentReleaseProgressCompleted:
		if moduleDeployment.Spec.Replicas != newRS.Spec.Replicas {
			moduleDeployment.Status.ReleaseStatus.Progress = v1alpha1.ModuleDeploymentReleaseProgressInit
			log.Log.Info("update release status progress to init when complete moduleDeployment", "moduleDeploymentName", moduleDeployment.Name)
			if err := utils.UpdateStatus(r.Client, ctx, moduleDeployment); err != nil {
				return ctrl.Result{}, utils.Error(err, "update release status progress to init failed when complete moduleDeployment")
			}
		}
		if !moduleVersionChanged && isUrlChange(moduleDeployment.Spec.Template.Spec.Module, newRS.Spec.Template.Spec.Module) {
			newRS.Spec.Template.Spec.Module = moduleDeployment.Spec.Template.Spec.Module
			if err := utils.UpdateResource(r.Client, ctx, newRS); err != nil {
				return ctrl.Result{}, err
			}
		}
	case v1alpha1.ModuleDeploymentReleaseProgressWaitingForConfirmation:
		moduleDeployment.Spec.Pause = true
		if err := r.Update(ctx, moduleDeployment); err != nil {
			return ctrl.Result{}, err
		}

		moduleDeployment.Status.ReleaseStatus.Progress = v1alpha1.ModuleDeploymentReleaseProgressPaused
		log.Log.Info("update moduleDeployment releaseStatus progress to paused", "moduleDeploymentName", moduleDeployment.Name)
		if err := utils.UpdateStatus(r.Client, ctx, moduleDeployment); err != nil {
			return ctrl.Result{}, utils.Error(err, "update moduleDeployment releaseStatus progress to paused failed")
		}
	case v1alpha1.ModuleDeploymentReleaseProgressPaused:
		if !moduleDeployment.Spec.Pause && time.Since(moduleDeployment.Status.ReleaseStatus.NextReconcileTime.Time) >= 0 {
			moduleDeployment.Status.ReleaseStatus.Progress = v1alpha1.ModuleDeploymentReleaseProgressExecuting
			log.Log.Info("update moduleDeployment progress from paused to executing", "moduleDeploymentName", moduleDeployment.Name)
			if err := utils.UpdateStatus(r.Client, ctx, moduleDeployment); err != nil {
				return ctrl.Result{}, utils.Error(err, "update moduleDeployment progress from paused to executing failed")
			}
		}
	}
	return ctrl.Result{}, nil
}

func handleInitModuleDeployment(moduleDeployment *v1alpha1.ModuleDeployment, newRS *v1alpha1.ModuleReplicaSet) {
	var (
		expReplicas    = moduleDeployment.Spec.Replicas
		deltaReplicas  = expReplicas - newRS.Spec.Replicas
		realBatchCount = moduleDeployment.Status.ReleaseStatus.RealBatchCount
	)

	batchCount := moduleDeployment.Spec.OperationStrategy.BatchCount
	if batchCount <= 0 {
		realBatchCount = 1
	} else if int32(math.Abs(float64(deltaReplicas))) < batchCount {
		realBatchCount = int32(math.Abs(float64(deltaReplicas)))
	} else {
		realBatchCount = batchCount
	}
	moduleDeployment.Status.ReleaseStatus.RealBatchCount = realBatchCount

	moduleDeployment.Status.ReleaseStatus.OriginalDeltaReplicas = deltaReplicas

	moduleDeployment.Status.ReleaseStatus.CurrentBatch = 1
	moduleDeployment.Status.ReleaseStatus.Progress = v1alpha1.ModuleDeploymentReleaseProgressExecuting
}

// handle deleting module deployment
func (r *ModuleDeploymentReconciler) handleDeletingModuleDeployment(ctx context.Context, moduleDeployment *v1alpha1.ModuleDeployment) (ctrl.Result, error) {
	if !utils.HasFinalizer(&moduleDeployment.ObjectMeta, finalizer.ModuleReplicaSetExistedFinalizer) {
		return ctrl.Result{}, nil
	}

	existReplicaset := true
	set := map[string]string{
		label.ModuleDeploymentLabel: moduleDeployment.Name,
	}
	replicaSetList := &v1alpha1.ModuleReplicaSetList{}
	err := r.Client.List(ctx, replicaSetList, &client.ListOptions{LabelSelector: labels.SelectorFromSet(set)}, client.InNamespace(moduleDeployment.Namespace))
	if err != nil {
		if !errors.IsNotFound(err) {
			return ctrl.Result{}, utils.Error(err, "Failed to get moduleReplicaSetList")
		}
		existReplicaset = false
	}

	if len(replicaSetList.Items) == 0 {
		existReplicaset = false
	}

	if existReplicaset {
		for i := 0; i < len(replicaSetList.Items); i++ {
			err := r.Client.Delete(ctx, &replicaSetList.Items[i])
			if err != nil {
				return ctrl.Result{}, utils.Error(err, "Failed to delete moduleReplicaSet", "moduleReplicaSetName", replicaSetList.Items[i].Name)
			}
		}
		requeueAfter := utils.GetNextReconcileTime(moduleDeployment.DeletionTimestamp.Time)
		return ctrl.Result{RequeueAfter: requeueAfter}, nil
	} else {
		log.Log.Info("moduleReplicaSet is deleted, remove moduleDeployment finalizer", "moduleDeploymentName", moduleDeployment.Name)
		utils.RemoveFinalizer(&moduleDeployment.ObjectMeta, finalizer.ModuleReplicaSetExistedFinalizer)
		err := utils.UpdateResource(r.Client, ctx, moduleDeployment)
		if err != nil {
			return ctrl.Result{}, err
		}
	}
	return ctrl.Result{}, nil
}

// update moduleDeployment owner reference
func (r *ModuleDeploymentReconciler) updateOwnerReference(ctx context.Context, moduleDeployment *v1alpha1.ModuleDeployment) error {
	deployment := &v1.Deployment{}
	err := r.Client.Get(ctx, types.NamespacedName{Namespace: moduleDeployment.Namespace, Name: moduleDeployment.Spec.BaseDeploymentName}, deployment)
	if err != nil {
		return utils.Error(err, "Failed to get deployment", "deploymentName", deployment.Name)
	}
	ownerReference := moduleDeployment.GetOwnerReferences()
	ownerReference = append(ownerReference, metav1.OwnerReference{
		APIVersion:         deployment.APIVersion,
		Kind:               deployment.Kind,
		UID:                deployment.UID,
		Name:               deployment.Name,
		BlockOwnerDeletion: pointer.Bool(true),
		Controller:         pointer.Bool(true),
	})
	moduleDeployment.SetOwnerReferences(ownerReference)
	utils.AddFinalizer(&moduleDeployment.ObjectMeta, finalizer.ModuleReplicaSetExistedFinalizer)
	err = utils.UpdateResource(r.Client, ctx, moduleDeployment)
	if err != nil {
		return utils.Error(err, "Failed to update moduleDeployment", "moduleDeploymentName", moduleDeployment.Name)
	}
	return nil
}

// create or get moduleReplicaset
func (r *ModuleDeploymentReconciler) createOrGetModuleReplicas(ctx context.Context, moduleDeployment *v1alpha1.ModuleDeployment) (*v1alpha1.ModuleReplicaSet, []*v1alpha1.ModuleReplicaSet, bool, error) {
	var err error
	for i := 0; i < 3; i++ {
		var (
			newRS  *v1alpha1.ModuleReplicaSet
			oldRSs []*v1alpha1.ModuleReplicaSet
		)

		set := map[string]string{
			label.ModuleDeploymentLabel: moduleDeployment.Name,
		}

		replicaSetList := &v1alpha1.ModuleReplicaSetList{}
		err = r.Client.List(ctx, replicaSetList, &client.ListOptions{LabelSelector: labels.SelectorFromSet(set)}, client.InNamespace(moduleDeployment.Namespace))
		maxVersion := 0
		if err != nil {
			log.Log.Info("get module replicaSet failed", "error", err)
			if !errors.IsNotFound(err) {
				return nil, nil, false, err
			}
		} else if len(replicaSetList.Items) != 0 {
			var rsList []*v1alpha1.ModuleReplicaSet
			for j := 0; j < len(replicaSetList.Items); j++ {
				rsList = append(rsList, &replicaSetList.Items[j])
				version, err := getRevision(&replicaSetList.Items[j])
				if err != nil {
					return nil, nil, false, err
				}
				if maxVersion < version {
					maxVersion = version
					newRS = &replicaSetList.Items[j]
				}
			}
			for j := 0; j < len(rsList); j++ {
				if version, _ := getRevision(rsList[j]); version != maxVersion {
					oldRSs = append(oldRSs, rsList[j])
				}
			}
			// todo: 批发发布没有完成时不允许改变 module 版本，需要webhook支持限制在批次发布过程中 module 版本变更
			// 此处默认当 Module 发生版本变化时，已经完成上个版本的批次发布
			if !isModuleChange(moduleDeployment.Spec.Template.Spec.Module, newRS.Spec.Template.Spec.Module) {
				return newRS, oldRSs, false, nil
			}
			oldRSs = append(oldRSs, newRS)
			log.Log.Info("module has changed, need create a new replicaset")
		}

		// create a new moduleReplicaset
		moduleReplicaSet, err := r.createNewReplicaSet(ctx, moduleDeployment, maxVersion+1)
		if err != nil {
			continue
		}
		return moduleReplicaSet, oldRSs, true, nil

	}
	return nil, nil, false, fmt.Errorf("create or get modulereplicaset error")
}

// update module replicas
func (r *ModuleDeploymentReconciler) updateModuleReplicas(
	ctx context.Context, replicas int32,
	moduleDeployment *v1alpha1.ModuleDeployment,
	newRS *v1alpha1.ModuleReplicaSet) error {
	moduleSpec := moduleDeployment.Spec.Template.Spec
	if replicas != newRS.Spec.Replicas || isModuleChange(moduleSpec.Module, newRS.Spec.Template.Spec.Module) ||
		isUrlChange(moduleSpec.Module, newRS.Spec.Template.Spec.Module) {
		log.Log.Info("prepare to update newRS", "moduleReplicaSetName", newRS.Name)
		newRS.Spec.Replicas = replicas
		newRS.Spec.Template.Spec.Module = moduleSpec.Module
		err := utils.UpdateResource(r.Client, ctx, newRS)
		if err != nil {
			return utils.Error(err, "Failed to update newRS", "moduleReplicaSetName", newRS.Name)
		}
		log.Log.Info("finish to update newRS", "moduleReplicaSetName", newRS.Name)
	}
	return nil
}

func (r *ModuleDeploymentReconciler) updateModuleReplicaSet(ctx context.Context, moduleDeployment *v1alpha1.ModuleDeployment,
	newRS *v1alpha1.ModuleReplicaSet) (ctrl.Result, error) {
	var (
		curBatch              = moduleDeployment.Status.ReleaseStatus.CurrentBatch
		realBatchCount        = moduleDeployment.Status.ReleaseStatus.RealBatchCount
		originalDeltaReplicas = moduleDeployment.Status.ReleaseStatus.OriginalDeltaReplicas

		curReplicas   = newRS.Status.Replicas
		expReplicas   = moduleDeployment.Spec.Replicas
		deltaReplicas = expReplicas - newRS.Spec.Replicas
	)

	if deltaReplicas == 0 {
		moduleDeployment.Status.ReleaseStatus.Progress = v1alpha1.ModuleDeploymentReleaseProgressCompleted
		moduleDeployment.Status.ReleaseStatus.LastTransitionTime = metav1.Now()
		moduleDeployment.Status.Conditions = utils.AppendModuleDeploymentCondition(moduleDeployment.Status.Conditions, v1alpha1.ModuleDeploymentCondition{
			Type:               v1alpha1.DeploymentAvailable,
			Status:             corev1.ConditionTrue,
			LastTransitionTime: metav1.Now(),
			Message:            "deployment release progress completed",
		})
		log.Log.Info("update moduleDeployment to completed", "moduleDeploymentName", moduleDeployment.Name)
		return ctrl.Result{}, utils.Error(utils.UpdateStatus(r.Client, ctx, moduleDeployment), "update moduleDeployment to completed failed")
	}

	// wait moduleReplicaset ready
	// TODO wait batch release completed
	//if newRS.Spec.Replicas != curReplicas {
	if moduleDeployment.Status.ReleaseStatus.BatchProgress == v1alpha1.ModuleDeploymentReleaseProgressExecuting {
		log.Log.Info("module replicaset  is not ready", "expectReplicas", newRS.Spec.Replicas, "curReplicas", curReplicas, "availableReplicas", newRS.Status.AvailableReplicas)
		//return ctrl.Result{Requeue: true, RequeueAfter: utils.GetNextReconcileTime(time.Now())}, nil
		return ctrl.Result{}, nil
	}

	replicas := int32(0)
	// use beta strategy
	useBeta := realBatchCount != 1 && curBatch == 1 && moduleDeployment.Spec.OperationStrategy.UseBeta
	if useBeta {
		replicas = 1
	} else if curBatch == realBatchCount { // if it's the last batch
		replicas = expReplicas
	} else {
		replicas = newRS.Spec.Replicas + int32(math.Ceil(float64(originalDeltaReplicas)/float64(realBatchCount)))
	}

	err := r.updateModuleReplicas(ctx, replicas, moduleDeployment, newRS)
	if err != nil {
		return ctrl.Result{}, err
	}

	var message string
	now := metav1.Now()

	if useBeta {
		message = "deployment release: beta deployment"
	} else {
		message = fmt.Sprintf("deployment release: curbatch %v, batchCount %v", curBatch, realBatchCount)
	}

	moduleDeployment.Status.ReleaseStatus.LastTransitionTime = now
	moduleDeployment.Status.ReleaseStatus.Progress = v1alpha1.ModuleDeploymentReleaseProgressExecuting

	moduleDeployment.Status.Conditions = utils.AppendModuleDeploymentCondition(moduleDeployment.Status.Conditions, v1alpha1.ModuleDeploymentCondition{
		Type:               v1alpha1.DeploymentProgressing,
		Status:             corev1.ConditionTrue,
		LastTransitionTime: now,
		Message:            message,
	})

	var grayTime int
	if curBatch != realBatchCount {
		// needConfirm = true or batch = 1 and useBeta = true
		if moduleDeployment.Spec.OperationStrategy.NeedConfirm || curBatch == 1 && moduleDeployment.Spec.OperationStrategy.UseBeta { // use NeedConfirm Strategy
			moduleDeployment.Status.ReleaseStatus.Progress = v1alpha1.ModuleDeploymentReleaseProgressWaitingForConfirmation
		} else if grayTime = int(moduleDeployment.Spec.OperationStrategy.GrayTimeBetweenBatchSeconds); grayTime != 0 {
			if curBatch == realBatchCount {
				moduleDeployment.Status.ReleaseStatus.Progress = v1alpha1.ModuleDeploymentReleaseProgressExecuting
			} else {
				moduleDeployment.Status.ReleaseStatus.NextReconcileTime = metav1.NewTime(now.Add(time.Duration(grayTime) * time.Second))
				moduleDeployment.Status.ReleaseStatus.Progress = v1alpha1.ModuleDeploymentReleaseProgressPaused
			}
		}
	}
	// TODO update current batch
	moduleDeployment.Status.ReleaseStatus.CurrentBatch += 1
	moduleDeployment.Status.ReleaseStatus.BatchProgress = v1alpha1.ModuleDeploymentReleaseProgressExecuting
	log.Log.Info("update moduleDeployment batch progress to executing", "moduleDeploymentName", moduleDeployment.Name)
	err = utils.UpdateStatus(r.Client, ctx, moduleDeployment)
	if err != nil {
		return ctrl.Result{}, utils.Error(err, "update moduleDeployment batch progress to executing failed")
	}
	return ctrl.Result{Requeue: true, RequeueAfter: time.Duration(grayTime) * time.Second}, nil
}

// generate module replicas
func (r *ModuleDeploymentReconciler) generateModuleReplicas(moduleDeployment *v1alpha1.ModuleDeployment,
	deployment *v1.Deployment, revision int) *v1alpha1.ModuleReplicaSet {
	newLabels := moduleDeployment.Labels
	newLabels[label.ModuleNameLabel] = moduleDeployment.Spec.Template.Spec.Module.Name
	newLabels[label.ModuleDeploymentLabel] = moduleDeployment.Name
	newLabels[label.ModuleSchedulingStrategy] = string(moduleDeployment.Spec.SchedulingStrategy.SchedulingPolicy)
	newLabels[label.ModuleReplicasetRevisionLabel] = strconv.Itoa(revision)
	moduleReplicaSet := &v1alpha1.ModuleReplicaSet{
		ObjectMeta: metav1.ObjectMeta{
			Annotations: map[string]string{},
			Labels:      newLabels,
			Name:        getModuleReplicasName(moduleDeployment.Name, revision),
			Namespace:   moduleDeployment.Namespace,
		},
		Spec: v1alpha1.ModuleReplicaSetSpec{
			Selector:           *deployment.Spec.Selector,
			Template:           moduleDeployment.Spec.Template,
			OperationStrategy:  moduleDeployment.Spec.OperationStrategy,
			SchedulingStrategy: moduleDeployment.Spec.SchedulingStrategy,
			MinReadySeconds:    moduleDeployment.Spec.MinReadySeconds,
		},
	}
	owner := []metav1.OwnerReference{
		{
			APIVersion:         moduleDeployment.APIVersion,
			Kind:               moduleDeployment.Kind,
			UID:                moduleDeployment.UID,
			Name:               moduleDeployment.Name,
			BlockOwnerDeletion: pointer.Bool(true),
			Controller:         pointer.Bool(true),
		},
	}
	moduleReplicaSet.SetOwnerReferences(owner)
	utils.AddFinalizer(&moduleReplicaSet.ObjectMeta, finalizer.ModuleExistedFinalizer)

	return moduleReplicaSet
}

func (r *ModuleDeploymentReconciler) createNewReplicaSet(ctx context.Context, moduleDeployment *v1alpha1.ModuleDeployment, revision int) (*v1alpha1.ModuleReplicaSet, error) {
	deployment := &v1.Deployment{}
	err := r.Client.Get(ctx, types.NamespacedName{Namespace: moduleDeployment.Namespace, Name: moduleDeployment.Spec.BaseDeploymentName}, deployment)
	if err != nil {
		return nil, utils.Error(err, "Failed to get deployment", "deploymentName", deployment.Name)
	}
	moduleReplicaSet := r.generateModuleReplicas(moduleDeployment, deployment, revision)
	// publish module replicaset pre hook event
	event.PublishModuleReplicaSetCreateEvent(r.Client, ctx, moduleReplicaSet)
	err = r.Client.Create(ctx, moduleReplicaSet)
	if err != nil {
		return nil, utils.Error(err, "Failed to create moduleReplicaSet", "moduleReplicaSetName", moduleReplicaSet.Name)
	}
	log.Log.Info("finish to create a new one", "moduleReplicaSetName", moduleReplicaSet.Name)
	return moduleReplicaSet, nil
}

// SetupWithManager sets up the controller with the Manager.
func (r *ModuleDeploymentReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&v1alpha1.ModuleDeployment{}).
		Complete(r)
}

func isModuleChange(module1, module2 v1alpha1.ModuleInfo) bool {
	return module1.Name != module2.Name || module1.Version != module2.Version
}

func isUrlChange(module1, module2 v1alpha1.ModuleInfo) bool {
	return module1.Url != module2.Url
}

func getModuleReplicasName(moduleDeploymentName string, revision int) string {
	return fmt.Sprintf(`%s-%s-%v`, moduleDeploymentName, "replicas", revision)
}

func getRevision(set *v1alpha1.ModuleReplicaSet) (int, error) {
	if versionStr, ok := set.Labels[label.ModuleReplicasetRevisionLabel]; ok {
		version, err := strconv.Atoi(versionStr)
		if err != nil {
			return 0, utils.Error(err, "invalid version for ModuleReplicasetRevisionLabel")
		}
		return version, nil
	}
	return 0, utils.Error(fmt.Errorf("can't get ModuleReplicasetRevisionLabel from ModuleReplicaSet"), "")
}
