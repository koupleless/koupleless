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

	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/labels"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"

	moduledeploymentv1alpha1 "github.com/sofastack/sofa-serverless/api/v1alpha1"
	"github.com/sofastack/sofa-serverless/internal/constants/label"
	"github.com/sofastack/sofa-serverless/internal/utils"

	"k8s.io/apimachinery/pkg/runtime"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/log"
)

// PodReconciler reconciles a Pod object
type PodReconciler struct {
	client.Client
	Scheme *runtime.Scheme
}

//+kubebuilder:rbac:groups=serverless.alipay.com,resources=pods,verbs=get;list;watch;create;update;patch;delete
//+kubebuilder:rbac:groups=serverless.alipay.com,resources=pods/status,verbs=get;update;patch
//+kubebuilder:rbac:groups=serverless.alipay.com,resources=pods/finalizers,verbs=update

// Reconcile is part of the main kubernetes reconciliation loop which aims to
// move the current state of the cluster closer to the desired state.
// TODO(user): Modify the Reconcile function to compare the state specified by
// the Pod object against the actual cluster state, and then
// perform operations to make the cluster state reflect the state specified by
// the user.
//
// For more details, check Reconcile and its Result here:
// - https://pkg.go.dev/sigs.k8s.io/controller-runtime@v0.15.0/pkg/reconcile
func (r *PodReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	// get pod
	pod := &corev1.Pod{}
	err := r.Client.Get(ctx, req.NamespacedName, pod)
	if err != nil {
		if errors.IsNotFound(err) {
			log.Log.Info("pod is deleted", "podName", pod.Name)
			return reconcile.Result{}, nil
		}
		return ctrl.Result{}, utils.Error(err, "Failed to get pod", "podName", pod.Name)
	}

	if pod.DeletionTimestamp != nil {
		// pod is deleting
		log.Log.Info("start delete pod", "podName", pod.Name)
		moduleList := &moduledeploymentv1alpha1.ModuleList{}
		err = r.Client.List(ctx, moduleList, &client.ListOptions{Namespace: req.Namespace, LabelSelector: labels.SelectorFromSet(map[string]string{
			label.BaseInstanceNameLabel: pod.Name,
		})})
		for _, module := range moduleList.Items {
			log.Log.Info("start delete module", "moduleName", module.Name, "podName", pod.Name)
			if pod.Labels[label.DeletePodLabel] == "true" {
				if module.Labels[label.DeleteModuleLabel] != "true" {
					module.Labels[label.DeleteModuleLabel] = "true"
					err = r.Client.Update(ctx, &module)
					if err != nil {
						log.Log.Error(err, "delete module failed when update delete module label", "moduleName", module.Name, "podName", pod.Name)
						return ctrl.Result{}, err
					}
				}
			} else {
				err := r.Client.Delete(ctx, &module)
				if err != nil && errors.IsNotFound(err) {
					log.Log.Error(err, "delete module failed when delete pod", "moduleName", module.Name, "podName", pod.Name)
					return ctrl.Result{}, nil
				}
			}
		}
	} else if pod.Labels[label.DeletePodLabel] == "true" {
		err := r.Client.Delete(ctx, pod)
		if err != nil {
			if errors.IsNotFound(err) {
				log.Log.Info("pod is deleted", "podName", pod.Name)
				return reconcile.Result{}, nil
			}
		}
		return ctrl.Result{}, utils.Error(err, "Failed to get pod", "podName", pod.Name)
	}

	return ctrl.Result{}, nil
}

// SetupWithManager sets up the controller with the Manager.
func (r *PodReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&corev1.Pod{}).
		Complete(r)
}
