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
	"bytes"
	"context"
	"fmt"
	moduledeploymentv1alpha1 "github.com/sofastack/sofa-serverless/api/v1alpha1"
	"github.com/sofastack/sofa-serverless/internal/controller/utils"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/selection"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/apimachinery/pkg/util/json"
	"k8s.io/utils/pointer"
	"net/http"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/log"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"
)

const (
	ProtectModuleFinalizer = "module-installed"
)

// ModuleReconciler reconciles a Module object
type ModuleReconciler struct {
	client.Client
	Scheme *runtime.Scheme
}

//+kubebuilder:rbac:groups=serverless.alipay.com,resources=modules,verbs=get;list;watch;create;update;patch;delete
//+kubebuilder:rbac:groups=serverless.alipay.com,resources=modules/status,verbs=get;update;patch
//+kubebuilder:rbac:groups=serverless.alipay.com,resources=modules/finalizers,verbs=update

// Reconcile is part of the main kubernetes reconciliation loop which aims to
// move the current state of the cluster closer to the desired state.
// TODO(user): Modify the Reconcile function to compare the state specified by
// the Module object against the actual cluster state, and then
// perform operations to make the cluster state reflect the state specified by
// the user.
//
// For more details, check Reconcile and its Result here:
// - https://pkg.go.dev/sigs.k8s.io/controller-runtime@v0.15.0/pkg/reconcile
func (r *ModuleReconciler) Reconcile(ctx context.Context, req ctrl.Request) (ctrl.Result, error) {
	_ = log.FromContext(ctx)

	module := &moduledeploymentv1alpha1.Module{}
	err := r.Client.Get(ctx, req.NamespacedName, module)

	if err != nil {
		if errors.IsNotFound(err) {
			log.Log.Info("module is deleted", "moduleName", module.Spec.Module.Name)
			return reconcile.Result{}, nil
		}
		log.Log.Error(err, "Failed to get module", "moduleName", module.Spec.Module.Name)
		return ctrl.Result{}, nil
	}

	ip := module.Labels[moduledeploymentv1alpha1.BaseInstanceIpLabel]
	moduleInstanceStatus := module.Status.Status

	// new module, resolve module status by ip
	if moduleInstanceStatus == "" {
		if ip == "" {
			moduleInstanceStatus = moduledeploymentv1alpha1.ModuleInstanceStatusPending
		} else {
			moduleInstanceStatus = moduledeploymentv1alpha1.ModuleInstanceStatusPrepare
		}
		module.Status.Status = moduleInstanceStatus
		err = r.Status().Update(ctx, module)
		if err != nil {
			return ctrl.Result{}, err
		}
		return ctrl.Result{}, nil
	}

	// uninstall module label
	if module.Labels[moduledeploymentv1alpha1.DeleteModuleLabel] != "" {
		err := r.Client.Delete(ctx, module)
		if err != nil {
			return ctrl.Result{}, err
		}
	}

	if module.DeletionTimestamp != nil {
		log.Log.Info("start to delete module", "moduleName", module.Spec.Module.Name, "module", module.Name)
		if ip != "" {
			podName := module.Labels[moduledeploymentv1alpha1.BaseInstanceNameLabel]

			targetPod := &corev1.Pod{}
			err := r.Client.Get(ctx, types.NamespacedName{Name: podName, Namespace: req.Namespace}, targetPod)
			if err != nil {
				if errors.IsNotFound(err) {
					log.Log.Info("pod is deleted", "podName", podName)
				} else {
					log.Log.Error(err, "Failed to get pod", "podName", podName)
					return ctrl.Result{}, nil
				}
			}

			if targetPod != nil && targetPod.Name != "" {
				// clean module label
				delete(targetPod.Labels, fmt.Sprintf("%s/%s", moduledeploymentv1alpha1.ModuleNameLabel, module.Spec.Module.Name))
				err = r.Client.Update(ctx, targetPod)
				if err != nil {
					log.Log.Error(err, "Failed remove module label in pod", "moduleName", module.Spec.Module.Name)
					return ctrl.Result{}, err
				}

				// uninstall module
				log.Log.Info("start to uninstall module", "moduleName", module.Spec.Module.Name, "module", module.Name)
				url := fmt.Sprintf("http://%s:1238/uninstallBiz", ip)
				body := fmt.Sprintf(`{"bizName": "%s", "bizVersion": "%s"}`, module.Spec.Module.Name, module.Spec.Module.Version)
				uninstallResult, err := http.Post(url, "application/json", bytes.NewReader([]byte(body)))
				if err != nil {
					log.Log.Error(err, "Failed post module", "moduleName", module.Spec.Module.Name, "url", url, "body", body)
					return ctrl.Result{}, err
				}
				uninstallResultJson, _ := json.Marshal(uninstallResult)
				log.Log.Info("uninstall module success", "ip", ip, "result", uninstallResultJson)
			} else {
				log.Log.Info("pod not exist", "moduleName", module.Spec.Module.Name, "module", module.Name)
			}
		}

		if module.Labels[moduledeploymentv1alpha1.DeleteModuleLabel] == "" {
			// create a new module
			log.Log.Info("start to create a new module", "moduleName", module.Spec.Module.Name, "module", module.Name)
			err := r.createNewModule(module)
			if err != nil {
				return ctrl.Result{}, err
			}
		}

		// remove finalizer
		log.Log.Info("start clean module install finalizer", "moduleName", module.Spec.Module.Name, "module", module.Name)
		utils.RemoveFinalizer(&module.ObjectMeta, ProtectModuleFinalizer)
		err = r.Client.Update(ctx, module)

		if err != nil {
			log.Log.Error(err, "Failed remove module installed finalizer", "moduleName", module.Spec.Module.Name, "module", module.Name)
			return ctrl.Result{}, err
		}
		log.Log.Info("finish clean module install finalizer", "moduleName", module.Spec.Module.Name, "module", module.Name)
		return ctrl.Result{}, nil
	}

	if moduleInstanceStatus == moduledeploymentv1alpha1.ModuleInstanceStatusPending {
		log.Log.Info("module is pending", "moduleName", module.Spec.Module.Name, "module", module.Name)
		if module.Labels[moduledeploymentv1alpha1.BaseInstanceIpLabel] != "" {
			// already schedule ip
			log.Log.Info("module is already schedule ip", "moduleName", module.Spec.Module.Name, "module", module.Name, "ip", module.Labels[moduledeploymentv1alpha1.BaseInstanceIpLabel])
			module.Status.Status = moduledeploymentv1alpha1.ModuleInstanceStatusPrepare
			err := r.Status().Update(ctx, module)
			if err != nil {
				return ctrl.Result{}, err
			}
		}

		log.Log.Info("module wait to schedule ip", "moduleName", module.Spec.Module.Name, "module", module.Name)

		// find a new pod to schedule
		selector, err := metav1.LabelSelectorAsSelector(&module.Spec.Selector)

		noAllocatePod, _ := labels.NewRequirement(fmt.Sprintf("%s/%s", moduledeploymentv1alpha1.ModuleNameLabel, module.Spec.Module.Name), selection.DoesNotExist, nil)
		selector = selector.Add(*noAllocatePod)

		selectedPods := &corev1.PodList{}
		if err = r.List(ctx, selectedPods, &client.ListOptions{Namespace: req.Namespace, LabelSelector: selector}); err != nil {
			log.Log.Error(err, "Failed to list not allocated pod", "moduleName", module.Name)
		}

		var pod corev1.Pod
		existSchedulingPod := false
		for _, podItr := range selectedPods.Items {
			if podItr.Status.PodIP != "" {
				// pod with ip to schedule module
				pod = podItr
				existSchedulingPod = true
				break
			}
		}

		if err != nil || !existSchedulingPod {
			// not find a new pod, delay reconcile to wait schedule again
			requeueAfter := utils.GetNextReconcileTime(module.ObjectMeta.CreationTimestamp.Time)
			return ctrl.Result{RequeueAfter: requeueAfter}, nil
		}

		// find a pod to schedule
		// TODO 调度策略

		// lock the pod, update the label
		pod.Labels[fmt.Sprintf("%s/%s", moduledeploymentv1alpha1.ModuleNameLabel, module.Spec.Module.Name)] = module.Spec.Module.Version
		err = r.Client.Update(ctx, &pod)
		// TODO add pod finalizer
		if err != nil {
			return ctrl.Result{}, err
		}

		// schedule the ip to the module
		module.Labels[moduledeploymentv1alpha1.BaseInstanceIpLabel] = pod.Status.PodIP
		module.Labels[moduledeploymentv1alpha1.BaseInstanceNameLabel] = pod.Name
		owner := []metav1.OwnerReference{
			{
				APIVersion:         pod.APIVersion,
				Kind:               pod.Kind,
				UID:                pod.UID,
				Name:               pod.Name,
				BlockOwnerDeletion: pointer.Bool(true),
			},
		}
		module.SetOwnerReferences(owner)

		err = r.Client.Update(ctx, module)
		if err != nil {
			return ctrl.Result{}, err
		}
		return ctrl.Result{}, nil
	}

	if moduleInstanceStatus == moduledeploymentv1alpha1.ModuleInstanceStatusPrepare {
		// TODO pre hook

		module.Status.Status = moduledeploymentv1alpha1.ModuleInstanceStatusUpgrading
		err := r.Status().Update(ctx, module)
		if err != nil {
			return ctrl.Result{}, err
		}
		return ctrl.Result{}, nil
	}

	if moduleInstanceStatus == moduledeploymentv1alpha1.ModuleInstanceStatusUpgrading {
		url := fmt.Sprintf("http://%s:1238/installBiz", ip)
		body := fmt.Sprintf(`{"bizName": "%s", "bizVersion": "%s", "arkBizFilePath": "%s"}`, module.Spec.Module.Name, module.Spec.Module.Version, module.Spec.Module.Url)
		installResult, err := http.Post(url, "application/json", bytes.NewReader([]byte(body)))
		if err != nil {
			log.Log.Error(err, "Failed post module", "moduleName", module.Spec.Module.Name, "url", url, "body", body)
			return ctrl.Result{}, err
		}
		installResultJson, _ := json.Marshal(installResult)
		log.Log.Info("install module success", "ip", ip, "result", installResultJson)

		// update status
		module.Status.Status = moduledeploymentv1alpha1.ModuleInstanceStatusCompleting
		err = r.Status().Update(ctx, module)
		if err != nil {
			return ctrl.Result{}, err
		}
		return ctrl.Result{}, nil
	}

	if moduleInstanceStatus == moduledeploymentv1alpha1.ModuleInstanceStatusCompleting {
		// TODO post hook

		if !utils.HasFinalizer(&module.ObjectMeta, ProtectModuleFinalizer) {
			// add installed module finalizer
			utils.AddFinalizer(&module.ObjectMeta, ProtectModuleFinalizer)
			err := r.Client.Update(ctx, module)
			if err != nil {
				return ctrl.Result{}, err
			}
		} else {
			// update available
			module.Status.Status = moduledeploymentv1alpha1.ModuleInstanceStatusAvailable
			err = r.Status().Update(ctx, module)
			if err != nil {
				log.Log.Error(err, "Failed add module installed finalizer", "moduleName", module.Spec.Module.Name, "module", module.Name)
				return ctrl.Result{}, err
			}
		}
		return ctrl.Result{}, nil
	}

	if moduleInstanceStatus == moduledeploymentv1alpha1.ModuleInstanceStatusAvailable {
		// do nothing
		return ctrl.Result{}, nil
	}

	return ctrl.Result{}, fmt.Errorf("invalid status %s, key=%s", moduleInstanceStatus, utils.Key(req))

}

func (r *ModuleReconciler) createNewModule(module *moduledeploymentv1alpha1.Module) error {
	moduleLabels := module.Labels
	delete(moduleLabels, moduledeploymentv1alpha1.BaseInstanceIpLabel)
	delete(moduleLabels, moduledeploymentv1alpha1.BaseInstanceNameLabel)

	newModule := &moduledeploymentv1alpha1.Module{
		ObjectMeta: metav1.ObjectMeta{
			Annotations:  map[string]string{},
			Labels:       moduleLabels,
			GenerateName: fmt.Sprintf("%s-%s-", moduleLabels[moduledeploymentv1alpha1.ModuleReplicasetLabel], moduleLabels[moduledeploymentv1alpha1.ModuleNameLabel]),
			Namespace:    module.Namespace,
		},
		Spec: moduledeploymentv1alpha1.ModuleSpec{
			Module:   module.Spec.Module,
			Selector: module.Spec.Selector,
		},
	}
	return r.Create(context.TODO(), newModule)
}

// SetupWithManager sets up the controller with the Manager.
func (r *ModuleReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&moduledeploymentv1alpha1.Module{}).
		Complete(r)
}
