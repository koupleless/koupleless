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

	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/selection"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/utils/pointer"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/log"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"

	"github.com/sofastack/sofa-serverless/api/v1alpha1"
	"github.com/sofastack/sofa-serverless/internal/arklet"
	"github.com/sofastack/sofa-serverless/internal/constants/finalizer"
	"github.com/sofastack/sofa-serverless/internal/constants/label"
	"github.com/sofastack/sofa-serverless/internal/event"
	"github.com/sofastack/sofa-serverless/internal/utils"
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

//+kubebuilder:rbac:groups=apps,resources=deployments,verbs=get;list;watch
//+kubebuilder:rbac:groups="",resources=pods,verbs=create;delete;get;list;patch;update;watch

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
	log.Log.Info("start reconcile for module", "request", req)
	defer log.Log.Info("finish reconcile for module", "request", req)
	// get module
	module := &v1alpha1.Module{}
	err := r.Client.Get(ctx, req.NamespacedName, module)

	if err != nil {
		if errors.IsNotFound(err) {
			log.Log.Info("module is deleted", "moduleName", module.Spec.Module.Name)
			return reconcile.Result{}, nil
		}
		log.Log.Error(err, "Failed to get module", "moduleName", module.Spec.Module.Name)
		return ctrl.Result{}, nil
	}

	moduleInstanceStatus := module.Status.Status

	if module.Status.Status == "" {
		// status is unknown, parse module instance status
		return r.parseModuleInstanceStatus(ctx, module)
	}

	if module.Labels[label.DeleteModuleLabel] != "" || module.Labels[label.DeleteModuleDirectlyLabel] != "" || module.DeletionTimestamp != nil {
		// module is deleting, set module instance status
		moduleInstanceStatus = v1alpha1.ModuleInstanceStatusTerminating
	}

	// handle by the status
	switch moduleInstanceStatus {
	case v1alpha1.ModuleInstanceStatusTerminating:
		return r.handleTerminatingModuleInstance(ctx, module)
	case v1alpha1.ModuleInstanceStatusPending:
		return r.handlePendingModuleInstance(ctx, module)
	case v1alpha1.ModuleInstanceStatusPrepare:
		return r.handlePrepareModuleInstance(ctx, module)
	case v1alpha1.ModuleInstanceStatusUpgrading:
		return r.handleUpgradingModuleInstance(ctx, module)
	case v1alpha1.ModuleInstanceStatusCompleting:
		return r.handleCompletingModuleInstance(ctx, module)
	case v1alpha1.ModuleInstanceStatusAvailable:
		return r.handleAvailableModuleInstance(ctx, module)
	default:
		return ctrl.Result{}, fmt.Errorf("invalid status %s, key=%s", moduleInstanceStatus, utils.Key(req))
	}
}

// module instance status is null, parse it
func (r *ModuleReconciler) parseModuleInstanceStatus(ctx context.Context, module *v1alpha1.Module) (ctrl.Result, error) {
	ip := module.Labels[label.BaseInstanceIpLabel]
	moduleInstanceStatus := module.Status.Status
	if ip == "" {
		moduleInstanceStatus = v1alpha1.ModuleInstanceStatusPending
	} else {
		moduleInstanceStatus = v1alpha1.ModuleInstanceStatusPrepare
	}
	module.Status.Status = moduleInstanceStatus
	module.Status.LastTransitionTime = metav1.Now()
	log.Log.Info(fmt.Sprintf("%s%s", "module status change to ", moduleInstanceStatus), "moduleName", module.Name)
	err := utils.UpdateStatus(r.Client, ctx, module)
	if err != nil {
		return ctrl.Result{}, utils.Error(err, "update module status failed")
	}
	return ctrl.Result{}, nil
}

// handle terminating module instance
func (r *ModuleReconciler) handleTerminatingModuleInstance(ctx context.Context, module *v1alpha1.Module) (ctrl.Result, error) {
	log.Log.Info("start to terminate module", "moduleName", module.Spec.Module.Name, "module", module.Name)
	if module.DeletionTimestamp != nil {
		event.PublishModuleDeleteEvent(*module)
		// deletionTimestamp is not null
		if module.Labels[label.DeleteModuleDirectlyLabel] != "" {
			log.Log.Info("delete module with DeleteModuleDirectlyLabel", "module", module.Name)
			// remove finalizer
			err := r.cleanLabelAndFinalizer(ctx, module)
			if err != nil {
				return ctrl.Result{}, err
			}
			return ctrl.Result{}, nil
		} else if module.Labels[label.DeleteModuleLabel] != "" {
			log.Log.Info("delete module with DeleteModuleLabel", "module", module.Name)
			// delete module label exists
			// uninstall module
			err := r.doUninstallModuleWhenTerminating(module)
			if err != nil {
				return ctrl.Result{}, err
			}
			// remove finalizer
			err = r.cleanLabelAndFinalizer(ctx, module)
			if err != nil {
				return ctrl.Result{}, err
			}
			return ctrl.Result{}, nil
		} else if v1alpha1.ScaleUpThenScaleDownUpgradePolicy == module.Spec.UpgradePolicy {
			log.Log.Info("delete module with ScaleUpThenScaleDownUpgradePolicy", "module", module.Name)
			// create a new module before deleting
			return r.doScaleUpThenScaleDownWhenTerminating(ctx, module)
		} else {
			log.Log.Info("delete module with DeleteModuleLabel is null", "module", module.Name)
			// uninstall module
			err := r.doUninstallModuleWhenTerminating(module)
			if err != nil {
				return ctrl.Result{}, err
			}
			// remove finalizer
			err = r.cleanLabelAndFinalizer(ctx, module)
			// create a new module
			log.Log.Info("start to create a new module", "moduleName", module.Spec.Module.Name, "module", module.Name)
			_, err = r.createNewModule(ctx, module)
			if err != nil {
				return ctrl.Result{}, err
			}
		}
	} else if module.Labels[label.DeleteModuleLabel] != "" || module.Labels[label.DeleteModuleDirectlyLabel] != "" {
		// do delete module with deleteModuleLabel
		log.Log.Info("delete module with DeleteModuleLabel", "moduleName", module.Name)
		err := r.Client.Delete(ctx, module)
		if err != nil {
			return ctrl.Result{}, err
		}
	}
	return ctrl.Result{}, nil
}
func (r *ModuleReconciler) doUninstallModuleWhenTerminating(module *v1alpha1.Module) error {
	ip := module.Labels[label.BaseInstanceIpLabel]
	if ip == "" {
		return nil
	}
	// uninstall module
	uninstallResult, err := arklet.Client().UninstallBiz(ip, module.Spec.Module.Name, module.Spec.Module.Version)
	if err != nil || uninstallResult.Code != arklet.Success {
		return utils.Error(err, "Failed uninstall module", "result", uninstallResult, "ip", ip, "moduleName", module.Spec.Module.Name, "moduleVersion", module.Spec.Module.Version)
	}
	return nil
}

func (r *ModuleReconciler) doScaleUpThenScaleDownWhenTerminating(ctx context.Context, module *v1alpha1.Module) (ctrl.Result, error) {
	// scale up then scale down
	if module.Labels[label.NewReplicatedModuleLabel] != "" {
		newModule := &v1alpha1.Module{}
		err := r.Client.Get(ctx, types.NamespacedName{Namespace: module.Namespace, Name: module.Labels[label.NewReplicatedModuleLabel]}, newModule)
		if err != nil {
			return ctrl.Result{}, err
		}
		if v1alpha1.ModuleInstanceStatusAvailable == newModule.Status.Status {
			// new module is available, remove old one
			err := r.cleanLabelAndFinalizer(ctx, module)
			if err != nil {
				return ctrl.Result{}, err
			}
		} else {
			// new module is not available, wait
			requeueAfter := utils.GetNextReconcileTime(module.ObjectMeta.CreationTimestamp.Time)
			return ctrl.Result{RequeueAfter: requeueAfter}, nil
		}
	} else {
		// create a new module
		log.Log.Info("start to create a new module", "moduleName", module.Spec.Module.Name, "module", module.Name)
		newModule, err := r.createNewModule(ctx, module)
		if err != nil {
			return ctrl.Result{}, err
		}

		// update old module label
		module.Labels[label.NewReplicatedModuleLabel] = newModule.Name
		err = r.Update(ctx, module)
		if err != nil {
			return ctrl.Result{}, err
		}
	}
	return ctrl.Result{}, nil
}

func (r *ModuleReconciler) cleanLabelAndFinalizer(ctx context.Context, module *v1alpha1.Module) error {
	// remove finalizer
	log.Log.Info("start clean module install finalizer", "moduleName", module.Spec.Module.Name, "module", module.Name)
	utils.RemoveFinalizer(&module.ObjectMeta, finalizer.AllocatePodFinalizer)
	err := utils.UpdateResource(r.Client, ctx, module)
	if err != nil {
		return utils.Error(err, "failed to clean module install finalizer", "moduleName", module.Spec.Module.Name, "module", module.Name)
	}
	log.Log.Info("finish clean module install finalizer", "moduleName", module.Spec.Module.Name, "module", module.Name)
	podName := module.Labels[label.BaseInstanceNameLabel]
	if podName != "" {
		pod := &corev1.Pod{}
		err = r.Client.Get(ctx, types.NamespacedName{Name: podName, Namespace: module.Namespace}, pod)
		if err != nil {
			return utils.Error(err, "Failed to get pod when removeFinalizer", "moduleName", module.Name, "podName", podName)
		}
		delete(pod.Labels, label.ModuleLabelPrefix+module.Spec.Module.Name)
		utils.RemoveFinalizer(&pod.ObjectMeta, fmt.Sprintf("%s-%s", finalizer.ModuleNameFinalizer, module.Spec.Module.Name))
		if _, exist := pod.Labels[label.ModuleInstanceCount]; exist {
			count, err := strconv.Atoi(pod.Labels[label.ModuleInstanceCount])
			if err != nil {
				log.Log.Error(err, "failed to update module count")
			} else {
				pod.Labels[label.ModuleInstanceCount] = strconv.Itoa(count - 1)
			}
		}
		err = utils.UpdateResource(r.Client, ctx, pod)
		if err != nil {
			return err
		}
	}
	return nil
}

// handle pending module instance
func (r *ModuleReconciler) handlePendingModuleInstance(ctx context.Context, module *v1alpha1.Module) (ctrl.Result, error) {
	log.Log.Info("module is pending", "moduleName", module.Spec.Module.Name, "module", module.Name)
	if module.Labels[label.BaseInstanceIpLabel] != "" {
		// already schedule ip
		log.Log.Info("module is already schedule ip", "moduleName", module.Spec.Module.Name, "module", module.Name, "ip", module.Labels[label.BaseInstanceIpLabel])
		module.Status.Status = v1alpha1.ModuleInstanceStatusPrepare
		module.Status.LastTransitionTime = metav1.Now()
		log.Log.Info(fmt.Sprintf("%s%s", "module status change to ", v1alpha1.ModuleInstanceStatusPrepare), "moduleName", module.Name)
		err := utils.UpdateStatus(r.Client, ctx, module)
		if err != nil {
			return ctrl.Result{}, utils.Error(err, "update module status from pending to prepare failed")
		}
		return ctrl.Result{}, nil
	}

	log.Log.Info("module wait to schedule ip", "moduleName", module.Spec.Module.Name, "module", module.Name)

	// find a new pod to schedule
	selector, err := metav1.LabelSelectorAsSelector(&module.Spec.Selector)

	noAllocatePod, _ := labels.NewRequirement(label.ModuleLabelPrefix+module.Spec.Module.Name, selection.DoesNotExist, nil)
	selector = selector.Add(*noAllocatePod)

	selectedPods := &corev1.PodList{}
	if err = r.List(ctx, selectedPods, &client.ListOptions{Namespace: module.Namespace, LabelSelector: selector}); err != nil {
		log.Log.Error(err, "Failed to list unallocated pod", "moduleName", module.Name)
	}

	var pod corev1.Pod
	existSchedulingPod := false
	for _, podItr := range selectedPods.Items {
		if podItr.DeletionTimestamp != nil {
			continue
		}
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
	UpdatePodLabelBeforeInstallModule(pod, module.Spec.Module.Name)
	err = utils.UpdateResource(r.Client, ctx, &pod)
	if err != nil {
		return ctrl.Result{}, err
	}

	// schedule the ip to the module
	module.Labels[label.BaseInstanceIpLabel] = pod.Status.PodIP
	module.Labels[label.BaseInstanceNameLabel] = pod.Name
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
	utils.AddFinalizer(&module.ObjectMeta, finalizer.AllocatePodFinalizer)
	err = utils.UpdateResource(r.Client, ctx, module)
	if err != nil {
		return ctrl.Result{}, err
	}
	return ctrl.Result{}, nil
}

// handle prepare module instance
func (r *ModuleReconciler) handlePrepareModuleInstance(ctx context.Context, module *v1alpha1.Module) (ctrl.Result, error) {
	// pre hook event
	event.PublishModuleCreateEvent(*module)
	module.Status.Status = v1alpha1.ModuleInstanceStatusUpgrading
	module.Status.LastTransitionTime = metav1.Now()
	log.Log.Info(fmt.Sprintf("%s%s", "module status change to ", v1alpha1.ModuleInstanceStatusUpgrading), "moduleName", module.Name)
	err := utils.UpdateStatus(r.Client, ctx, module)
	if err != nil {
		return ctrl.Result{}, utils.Error(err, "update module from prepare to upgrading failed")
	}
	return ctrl.Result{}, nil
}

// handle upgrading module instance
func (r *ModuleReconciler) handleUpgradingModuleInstance(ctx context.Context, module *v1alpha1.Module) (ctrl.Result, error) {
	moduleInfo := v1alpha1.ModuleInfo{Name: module.Spec.Module.Name, Version: module.Spec.Module.Version, Url: module.Spec.Module.Url}
	ip := module.Labels[label.BaseInstanceIpLabel]
	installResult, err := arklet.Client().InstallBiz(ip, moduleInfo)
	if err != nil {
		return ctrl.Result{}, utils.Error(err, "Failed install module", "moduleInfo", moduleInfo)
	}
	if installResult.Code != arklet.Success {
		log.Log.Error(err, "Failed install module", "moduleInfo", moduleInfo, "result", installResult)
		// TODO update install result
		return ctrl.Result{}, nil
	}

	// update module status
	module.Status.Status = v1alpha1.ModuleInstanceStatusCompleting
	module.Status.LastTransitionTime = metav1.Now()
	log.Log.Info(fmt.Sprintf("%s%s", "module status change to ", v1alpha1.ModuleInstanceStatusCompleting), "moduleName", module.Name)
	err = utils.UpdateStatus(r.Client, ctx, module)
	if err != nil {
		return ctrl.Result{}, utils.Error(err, "update module from upgrading to completing failed")
	}
	return ctrl.Result{}, nil
}

// handle completing module instance
func (r *ModuleReconciler) handleCompletingModuleInstance(ctx context.Context, module *v1alpha1.Module) (ctrl.Result, error) {
	// TODO post hook
	// update to available status
	module.Status.Status = v1alpha1.ModuleInstanceStatusAvailable
	module.Status.LastTransitionTime = metav1.Now()
	log.Log.Info(fmt.Sprintf("%s%s", "module status change to ", v1alpha1.ModuleInstanceStatusAvailable), "moduleName", module.Name)
	err := utils.UpdateStatus(r.Client, ctx, module)
	if err != nil {
		return ctrl.Result{}, utils.Error(err, "update module from completing to available failed")
	}
	return ctrl.Result{}, nil
}

// handle available module instance
func (r *ModuleReconciler) handleAvailableModuleInstance(ctx context.Context, module *v1alpha1.Module) (ctrl.Result, error) {
	// do nothing
	return ctrl.Result{}, nil
}

// create a new module
func (r *ModuleReconciler) createNewModule(ctx context.Context, module *v1alpha1.Module) (v1alpha1.Module, error) {
	moduleReplicaSet := &v1alpha1.ModuleReplicaSet{}
	err := r.Client.Get(ctx, types.NamespacedName{Namespace: module.Namespace, Name: module.Labels[label.ModuleReplicasetLabel]}, moduleReplicaSet)
	if err != nil {
		return v1alpha1.Module{}, err
	}

	moduleLabels := make(map[string]string)
	for key, value := range module.Labels {
		if key == label.BaseInstanceIpLabel || key == label.BaseInstanceNameLabel {
			continue
		}
		moduleLabels[key] = value
	}

	newModule := &v1alpha1.Module{
		ObjectMeta: metav1.ObjectMeta{
			Annotations:  map[string]string{},
			Labels:       moduleLabels,
			GenerateName: fmt.Sprintf("%s-%s-", moduleLabels[label.ModuleReplicasetLabel], moduleLabels[label.ModuleNameLabel]),
			Namespace:    module.Namespace,
		},
		Spec: v1alpha1.ModuleSpec{
			Module:        moduleReplicaSet.Spec.Template.Spec.Module,
			Selector:      module.Spec.Selector,
			UpgradePolicy: module.Spec.UpgradePolicy,
		},
	}
	return *newModule, r.Create(ctx, newModule)
}

func UpdatePodLabelBeforeInstallModule(pod corev1.Pod, moduleName string) {
	// add module label
	pod.Labels[label.ModuleLabelPrefix+moduleName] = "true"

	// add module instance count label
	if _, exist := pod.Labels[label.ModuleInstanceCount]; exist {
		count, err := strconv.Atoi(pod.Labels[label.ModuleInstanceCount])
		if err == nil {
			pod.Labels[label.ModuleInstanceCount] = strconv.Itoa(count + 1)
		}
	} else {
		pod.Labels[label.ModuleInstanceCount] = "1"
	}

	// add pod finalizer
	utils.AddFinalizer(&pod.ObjectMeta, finalizer.ModuleNameFinalizer+moduleName)
}

// SetupWithManager sets up the controller with the Manager.
func (r *ModuleReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&v1alpha1.Module{}).
		Complete(r)
}
