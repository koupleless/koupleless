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
	"strconv"

	moduledeploymentv1alpha1 "github.com/sofastack/sofa-serverless/api/v1alpha1"
	"github.com/sofastack/sofa-serverless/internal/constants/finalizer"
	"github.com/sofastack/sofa-serverless/internal/constants/label"
	"github.com/sofastack/sofa-serverless/internal/utils"

	v1 "k8s.io/api/apps/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/utils/pointer"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/log"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"
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
//+kubebuilder:rbac:groups=,resources=pods,verbs=create;delete;get;list;patch;update;watch

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
	moduleDeployment := &moduledeploymentv1alpha1.ModuleDeployment{}
	err := r.Client.Get(ctx, req.NamespacedName, moduleDeployment)
	if err != nil {
		if errors.IsNotFound(err) {
			log.Log.Info("moduleDeployment is deleted", "moduleDeploymentName", moduleDeployment.Name)
			return reconcile.Result{}, nil
		}
		log.Log.Error(err, "Failed to get moduleDeployment", "moduleDeploymentName", moduleDeployment.Name)
		return ctrl.Result{}, nil
	}

	if moduleDeployment.DeletionTimestamp != nil {
		// delete moduleDeployment
		return r.handleDeletingModuleDeployment(ctx, moduleDeployment)
	}

	// create moduleReplicaSet
	moduleReplicaSet, err := r.createOrGetModuleReplicas(ctx, moduleDeployment)
	if err != nil {
		return ctrl.Result{}, err
	}

	// update moduleReplicaSet
	err = r.updateModuleReplicas(ctx, moduleDeployment, moduleReplicaSet)
	if err != nil {
		return ctrl.Result{}, err
	}

	// update moduleDeployment owner reference
	err = r.updateOwnerReference(ctx, moduleDeployment)
	if err != nil {
		return ctrl.Result{}, err
	}
	return ctrl.Result{}, nil
}

// handle deleting module deployment
func (r *ModuleDeploymentReconciler) handleDeletingModuleDeployment(ctx context.Context, moduleDeployment *moduledeploymentv1alpha1.ModuleDeployment) (ctrl.Result, error) {
	if !utils.HasFinalizer(&moduleDeployment.ObjectMeta, finalizer.ModuleReplicaSetExistedFinalizer) {
		return ctrl.Result{}, nil
	}

	moduleReplicaSet := &moduledeploymentv1alpha1.ModuleReplicaSet{}
	moduleReplicaSetName := getModuleReplicasName(moduleDeployment.Name)
	err := r.Client.Get(ctx, types.NamespacedName{Namespace: moduleDeployment.Namespace, Name: moduleReplicaSetName}, moduleReplicaSet)
	existReplicaset := true
	if err != nil {
		if errors.IsNotFound(err) {
			existReplicaset = false
		} else {
			log.Log.Error(err, "Failed to get moduleReplicaSet", "moduleReplicaSetName", moduleReplicaSetName)
			return ctrl.Result{}, err
		}
	}
	if existReplicaset {
		err := r.Client.Delete(ctx, moduleReplicaSet)
		if err != nil {
			log.Log.Error(err, "Failed to delete moduleReplicaSet", "moduleReplicaSetName", moduleReplicaSetName)
			return ctrl.Result{}, err
		}
		requeueAfter := utils.GetNextReconcileTime(moduleDeployment.DeletionTimestamp.Time)
		return ctrl.Result{RequeueAfter: requeueAfter}, nil
	} else {
		log.Log.Info("moduleReplicaSet is deleted, remove moduleDeployment finalizer", "moduleDeploymentName", moduleDeployment.Name)
		utils.RemoveFinalizer(&moduleDeployment.ObjectMeta, finalizer.ModuleReplicaSetExistedFinalizer)
		err := r.Client.Update(ctx, moduleDeployment)
		if err != nil {
			return ctrl.Result{}, err
		}
	}
	return ctrl.Result{}, nil
}

// update moduleDeployment owner reference
func (r *ModuleDeploymentReconciler) updateOwnerReference(ctx context.Context, moduleDeployment *moduledeploymentv1alpha1.ModuleDeployment) error {
	moduleDeploymentOwnerReferenceExist := false
	for _, ownerReference := range moduleDeployment.GetOwnerReferences() {
		if moduleDeployment.Spec.BaseDeploymentName == ownerReference.Name {
			moduleDeploymentOwnerReferenceExist = true
		}
	}

	if !moduleDeploymentOwnerReferenceExist {
		deployment := &v1.Deployment{}
		err := r.Client.Get(ctx, types.NamespacedName{Namespace: moduleDeployment.Namespace, Name: moduleDeployment.Spec.BaseDeploymentName}, deployment)
		if err != nil {
			log.Log.Error(err, "Failed to get deployment", "deploymentName", deployment.Name)
			return err
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
		err = r.Client.Update(ctx, moduleDeployment)
		if err != nil {
			log.Log.Error(err, "Failed to update moduleDeployment", "moduleDeploymentName", moduleDeployment.Name)
			return err
		}
	}
	return nil
}

// create or get module replicas
func (r *ModuleDeploymentReconciler) createOrGetModuleReplicas(ctx context.Context, moduleDeployment *moduledeploymentv1alpha1.ModuleDeployment) (*moduledeploymentv1alpha1.ModuleReplicaSet, error) {
	var err error
	moduleReplicaSet := &moduledeploymentv1alpha1.ModuleReplicaSet{}
	moduleReplicaSetName := getModuleReplicasName(moduleDeployment.Name)
	for i := 0; i < 3; i++ {
		err = r.Client.Get(ctx, types.NamespacedName{Namespace: moduleDeployment.Namespace, Name: moduleReplicaSetName}, moduleReplicaSet)
		if err != nil {
			log.Log.Info("get module replicaSet failed", "name", moduleReplicaSetName, "error", err)
			if errors.IsNotFound(err) {
				log.Log.Info("moduleReplicaSet is not exist", "name", moduleReplicaSetName)
				deployment := &v1.Deployment{}
				err := r.Client.Get(ctx, types.NamespacedName{Namespace: moduleDeployment.Namespace, Name: moduleDeployment.Spec.BaseDeploymentName}, deployment)
				if err != nil {
					log.Log.Error(err, "Failed to get deployment", "deploymentName", deployment.Name)
					continue
				}
				moduleReplicaSet := r.generateModuleReplicas(moduleDeployment, deployment)
				err = r.Client.Create(ctx, moduleReplicaSet)
				if err != nil {
					log.Log.Error(err, "Failed to create moduleReplicaSet", "moduleReplicaSetName", moduleReplicaSet.Name)
					continue
				}
				log.Log.Info("finish to create a new one", "moduleReplicaSetName", moduleReplicaSet.Name)
				return moduleReplicaSet, nil
			}
		} else {
			return moduleReplicaSet, nil
		}
	}
	return moduleReplicaSet, err
}

// update module replicas
func (r *ModuleDeploymentReconciler) updateModuleReplicas(ctx context.Context, moduleDeployment *moduledeploymentv1alpha1.ModuleDeployment, moduleReplicaSet *moduledeploymentv1alpha1.ModuleReplicaSet) error {
	moduleSpec := moduleDeployment.Spec.Template.Spec
	if moduleDeployment.Spec.Replicas != moduleReplicaSet.Spec.Replicas || isModuleChanges(moduleSpec.Module, moduleReplicaSet.Spec.Template.Spec.Module) {
		log.Log.Info("prepare to update moduleReplicaSet", "moduleReplicaSetName", moduleReplicaSet.Name)
		moduleReplicaSet.Spec.Replicas = moduleDeployment.Spec.Replicas
		moduleReplicaSet.Spec.Template.Spec.Module = moduleSpec.Module
		err := r.Client.Update(ctx, moduleReplicaSet)
		if err != nil {
			log.Log.Error(err, "Failed to update moduleReplicaSet", "moduleReplicaSetName", moduleReplicaSet.Name)
			return err
		}
		log.Log.Info("finish to update moduleReplicaSet", "moduleReplicaSetName", moduleReplicaSet.Name)
	}
	return nil
}

// generate module replicas
func (r *ModuleDeploymentReconciler) generateModuleReplicas(moduleDeployment *moduledeploymentv1alpha1.ModuleDeployment, deployment *v1.Deployment) *moduledeploymentv1alpha1.ModuleReplicaSet {
	newLabels := moduleDeployment.Labels
	newLabels[label.ModuleNameLabel] = moduleDeployment.Spec.Template.Spec.Module.Name
	newLabels[label.ModuleDeploymentLabel] = moduleDeployment.Name
	newLabels[label.ModuleSchedulingStrategy] = string(moduleDeployment.Spec.SchedulingStrategy.SchedulingType)
	newLabels[label.MaxModuleCount] = strconv.Itoa(moduleDeployment.Spec.SchedulingStrategy.MaxModuleCount)
	moduleReplicaSet := &moduledeploymentv1alpha1.ModuleReplicaSet{
		ObjectMeta: metav1.ObjectMeta{
			Annotations: map[string]string{},
			Labels:      newLabels,
			Name:        getModuleReplicasName(moduleDeployment.Name),
			Namespace:   moduleDeployment.Namespace,
		},
		Spec: moduledeploymentv1alpha1.ModuleReplicaSetSpec{
			Selector:        *deployment.Spec.Selector,
			Replicas:        moduleDeployment.Spec.Replicas,
			Template:        moduleDeployment.Spec.Template,
			MinReadySeconds: moduleDeployment.Spec.MinReadySeconds,
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

// SetupWithManager sets up the controller with the Manager.
func (r *ModuleDeploymentReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&moduledeploymentv1alpha1.ModuleDeployment{}).
		Complete(r)
}

func isModuleChanges(module1, module2 moduledeploymentv1alpha1.ModuleInfo) bool {
	return module1.Name != module2.Name || module1.Version != module2.Version
}

func getModuleReplicasName(moduleDeploymentName string) string {
	return fmt.Sprintf(`%s-%s`, moduleDeploymentName, "replicas")
}
