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
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/utils/pointer"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"

	"k8s.io/apimachinery/pkg/runtime"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/log"

	moduledeploymentv1alpha1 "github.com/sofastack/sofa-serverless/api/v1alpha1"
)

// ModuleReplicaSetReconciler reconciles a ModuleReplicaSet object
type ModuleReplicaSetReconciler struct {
	client.Client
	Scheme *runtime.Scheme
}

//+kubebuilder:rbac:groups=serverless.alipay.com,resources=modulereplicasets,verbs=get;list;watch;create;update;patch;delete
//+kubebuilder:rbac:groups=serverless.alipay.com,resources=modulereplicasets/status,verbs=get;update;patch
//+kubebuilder:rbac:groups=serverless.alipay.com,resources=modulereplicasets/finalizers,verbs=update

// Reconcile is part of the main kubernetes reconciliation loop which aims to
// move the current state of the cluster closer to the desired state.
// TODO(user): Modify the Reconcile function to compare the state specified by
// the ModuleReplicaSet object against the actual cluster state, and then
// perform operations to make the cluster state reflect the state specified by
// the user.
//
// For more details, check Reconcile and its Result here:
// - https://pkg.go.dev/sigs.k8s.io/controller-runtime@v0.15.0/pkg/reconcile
func (r *ModuleReplicaSetReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	_ = log.FromContext(ctx)

	// get the moduleReplicaSet
	moduleReplicaSet := &moduledeploymentv1alpha1.ModuleReplicaSet{}
	err := r.Client.Get(ctx, req.NamespacedName, moduleReplicaSet)
	if err != nil {
		log.Log.Error(err, "Failed to get moduleReplicaSet", "moduleReplicaSetName", moduleReplicaSet.Name)
		return ctrl.Result{}, nil
	}

	// get the existed module
	existedModuleList := &moduledeploymentv1alpha1.ModuleList{}
	err = r.Client.List(ctx, existedModuleList, &client.ListOptions{Namespace: req.Namespace, LabelSelector: labels.SelectorFromSet(map[string]string{
		moduledeploymentv1alpha1.ModuleReplicasetLabel: moduleReplicaSet.Name,
	})})
	if err != nil {
		log.Log.Error(err, "Failed to list existedModuleList", "moduleReplicaSetName", moduleReplicaSet.Name)
		return ctrl.Result{}, nil
	}

	if moduleReplicaSet.DeletionTimestamp != nil {
		for _, existedModule := range existedModuleList.Items {
			err := r.Client.Delete(ctx, &existedModule)
			if err != nil {
				log.Log.Error(err, "Failed to delete module", "moduleName", existedModule.Name)
				return ctrl.Result{}, err
			}
		}
	}

	// compare replicas
	if int(moduleReplicaSet.Spec.Replicas) != len(existedModuleList.Items) {
		// replicas change
		selector, err := metav1.LabelSelectorAsSelector(&moduleReplicaSet.Spec.Selector)
		selectedPods := &corev1.PodList{}
		if err = r.List(ctx, selectedPods, &client.ListOptions{Namespace: req.Namespace, LabelSelector: selector}); err != nil {
			log.Log.Error(err, "Failed to list pod", "moduleReplicaSetName", moduleReplicaSet.Name)
			return reconcile.Result{}, nil
		}

		deltaReplicas := int(moduleReplicaSet.Spec.Replicas) - len(existedModuleList.Items)
		if deltaReplicas > 0 {
			// scale up

			// get allocated pod
			usedPodNames := make(map[string]bool)
			for _, module := range existedModuleList.Items {
				usedPodNames[module.Labels[moduledeploymentv1alpha1.BaseInstanceNameLabel]] = true
			}

			// allocate pod
			var toAllocatePod []corev1.Pod
			count := deltaReplicas
			for _, pod := range selectedPods.Items {
				if _, ok := usedPodNames[pod.Name]; !ok {
					toAllocatePod = append(toAllocatePod, pod)
					if count--; count == 0 {
						break
					}
				}
			}

			for _, pod := range toAllocatePod {
				pod.Labels[fmt.Sprintf("%s/%s", moduledeploymentv1alpha1.ModuleNameLabel, moduleReplicaSet.Spec.Template.Spec.Module.Name)] = moduleReplicaSet.Spec.Template.Spec.Module.Version
				err := r.Client.Update(ctx, &pod)
				// TODO add pod finalizer
				if err != nil {
					// update pod label
					return ctrl.Result{}, err
				}
				// create module
				module := r.generateModule(moduleReplicaSet, pod)
				if err = r.Client.Create(ctx, module); err != nil {
					log.Log.Error(err, "Failed to create module", "moduleName", module.Name)
					return reconcile.Result{}, nil
				}
			}
		} else {
			// scale down
			count := -deltaReplicas
			log.Log.Info("scale down replicas", "deltaReplicas", deltaReplicas)
			for _, existedModule := range existedModuleList.Items {
				err := r.Client.Delete(ctx, &existedModule)
				if err != nil {
					log.Log.Error(err, "Failed to delete module", "moduleName", existedModule.Name)
					return ctrl.Result{}, err
				}
				if count--; count == 0 {
					break
				}
			}
		}
	}

	// compare module
	desiredModule := moduleReplicaSet.Spec.Template.Spec.Module
	for _, existedModule := range existedModuleList.Items {

		needUpgradeModule := existedModule.Spec.Module.Name != desiredModule.Name || existedModule.Spec.Module.Version != desiredModule.Version || existedModule.Spec.Module.Url != desiredModule.Url
		needUninstallModule := existedModule.Spec.Module.Name != desiredModule.Name
		if needUpgradeModule {
			existedModule.Spec.Module.Name = desiredModule.Name
			existedModule.Spec.Module.Version = desiredModule.Version
			existedModule.Spec.Module.Url = desiredModule.Url
			err := r.Client.Update(ctx, &existedModule)
			if err != nil {
				log.Log.Error(err, "Failed to update module", "moduleName", existedModule.Name)
				return ctrl.Result{}, err
			}
		}
		if needUninstallModule {
			err := r.Client.Delete(ctx, &existedModule)
			if err != nil {
				log.Log.Error(err, "Failed to delete module", "moduleName", existedModule.Name)
				return ctrl.Result{}, err
			}
		}
	}

	return ctrl.Result{}, nil
}

func (r *ModuleReplicaSetReconciler) generateModule(moduleReplicaSet *moduledeploymentv1alpha1.ModuleReplicaSet, pod corev1.Pod) *moduledeploymentv1alpha1.Module {

	moduleLabels := moduleReplicaSet.Labels
	moduleLabels[moduledeploymentv1alpha1.ModuleNameLabel] = moduleReplicaSet.Spec.Template.Spec.Module.Name
	moduleLabels[moduledeploymentv1alpha1.ModuleVersionLabel] = moduleReplicaSet.Spec.Template.Spec.Module.Version
	moduleLabels[moduledeploymentv1alpha1.BaseInstanceIpLabel] = pod.Status.PodIP
	moduleLabels[moduledeploymentv1alpha1.BaseInstanceNameLabel] = pod.Name
	moduleLabels[moduledeploymentv1alpha1.ModuleReplicasetLabel] = moduleReplicaSet.Name

	module := &moduledeploymentv1alpha1.Module{
		ObjectMeta: metav1.ObjectMeta{
			Annotations:  map[string]string{},
			Labels:       moduleLabels,
			GenerateName: fmt.Sprintf("%s-%s-", moduleReplicaSet.Name, moduleReplicaSet.Spec.Template.Spec.Module.Name),
			Namespace:    moduleReplicaSet.Namespace,
		},
		Spec: moduledeploymentv1alpha1.ModuleSpec{
			Selector: moduleReplicaSet.Spec.Selector,
			Module:   moduleReplicaSet.Spec.Template.Spec.Module,
		},
	}
	// OwnerReference to moduleReplicaSet and Pod
	owner := []metav1.OwnerReference{
		//{
		//	APIVersion:         moduleReplicaSet.APIVersion,
		//	Kind:               moduleReplicaSet.Kind,
		//	UID:                moduleReplicaSet.UID,
		//	Name:               moduleReplicaSet.Name,
		//	BlockOwnerDeletion: pointer.Bool(true),
		//	Controller:         pointer.Bool(true),
		//},
		{
			APIVersion:         pod.APIVersion,
			Kind:               pod.Kind,
			UID:                pod.UID,
			Name:               pod.Name,
			BlockOwnerDeletion: pointer.Bool(true),
		},
	}
	module.SetOwnerReferences(owner)
	return module
}

// SetupWithManager sets up the controller with the Manager.
func (r *ModuleReplicaSetReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&moduledeploymentv1alpha1.ModuleReplicaSet{}).
		Complete(r)
}
