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
	"sort"
	"strconv"

	"k8s.io/apimachinery/pkg/selection"

	"golang.org/x/tools/container/intsets"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/types"
	"k8s.io/utils/pointer"
	"sigs.k8s.io/controller-runtime/pkg/handler"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"

	"github.com/sofastack/sofa-serverless/internal/constants/finalizer"
	"github.com/sofastack/sofa-serverless/internal/constants/label"
	"github.com/sofastack/sofa-serverless/internal/utils"

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

//+kubebuilder:rbac:groups=apps,resources=deployments,verbs=get;list;watch
//+kubebuilder:rbac:groups="",resources=pods,verbs=create;delete;get;list;patch;update;watch

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
	log.Log.Info("start reconcile for moduleReplicaSet", "request", req)
	defer log.Log.Info("finish reconcile for moduleReplicaSet", "request", req)
	// get the moduleReplicaSet
	moduleReplicaSet := &moduledeploymentv1alpha1.ModuleReplicaSet{}
	err := r.Client.Get(ctx, req.NamespacedName, moduleReplicaSet)
	if err != nil {
		if errors.IsNotFound(err) {
			log.Log.Info("moduleReplicaSet is deleted", "moduleReplicaSetName", moduleReplicaSet.Name)
			return reconcile.Result{}, nil
		}
		utils.Error(err, "Failed to get moduleReplicaSet", "moduleReplicaSetName", moduleReplicaSet.Name)
		return ctrl.Result{}, nil
	}

	// get all modules
	moduleList := &moduledeploymentv1alpha1.ModuleList{}
	err = r.Client.List(ctx, moduleList, &client.ListOptions{Namespace: req.Namespace, LabelSelector: labels.SelectorFromSet(map[string]string{
		label.ModuleNameLabel: moduleReplicaSet.Spec.Template.Spec.Module.Name,
	})})
	if err != nil {
		return ctrl.Result{}, utils.Error(err, "Failed to list existedModuleList", "moduleReplicaSetName", moduleReplicaSet.Name)
	}

	var sameReplicaSetModules []moduledeploymentv1alpha1.Module
	var otherReplicaSetModules []moduledeploymentv1alpha1.Module

	for _, module := range moduleList.Items {
		if module.Labels[label.ModuleReplicasetLabel] == moduleReplicaSet.Name {
			sameReplicaSetModules = append(sameReplicaSetModules, module)
		} else {
			otherReplicaSetModules = append(otherReplicaSetModules, module)
		}
	}

	// update status.replicas
	currentReplicas := int32(0)
	// calculate the modules that have been installed successfully
	for i := 0; i < len(sameReplicaSetModules); i++ {
		status := sameReplicaSetModules[i].Status.Status
		if status == moduledeploymentv1alpha1.ModuleInstanceStatusAvailable {
			currentReplicas += 1
		}
	}
	// if current replicas isn't equal to status.replicas, then we need update status
	if currentReplicas != moduleReplicaSet.Status.Replicas {
		moduleReplicaSet.Status.Replicas = currentReplicas
		return ctrl.Result{}, r.Status().Update(ctx, moduleReplicaSet)
	}

	if moduleReplicaSet.DeletionTimestamp != nil {
		return r.handleDeletingModuleReplicaSet(ctx, sameReplicaSetModules, moduleReplicaSet)
	}

	// compare replicas and scale up or scale down
	if int(moduleReplicaSet.Spec.Replicas) != len(sameReplicaSetModules) {
		// replicas change
		deltaReplicas := int(moduleReplicaSet.Spec.Replicas) - len(sameReplicaSetModules)
		if deltaReplicas > 0 {
			selector, err := metav1.LabelSelectorAsSelector(&moduleReplicaSet.Spec.Selector)
			noAllocatedPod, _ := labels.NewRequirement(fmt.Sprintf("%s-%s", label.ModuleNameLabel, moduleReplicaSet.Spec.Template.Spec.Module.Name), selection.DoesNotExist, nil)
			selector = selector.Add(*noAllocatedPod)
			availablePods := &corev1.PodList{}
			if err = r.List(ctx, availablePods, &client.ListOptions{Namespace: moduleReplicaSet.Namespace, LabelSelector: selector}); err != nil {
				return reconcile.Result{}, utils.Error(err, "Failed to list pod", "moduleReplicaSetName", moduleReplicaSet.Name)
			}

			if len(availablePods.Items) == 0 {
				// no pod to scale up
				requeueAfter := utils.GetNextReconcileTime(moduleReplicaSet.ObjectMeta.CreationTimestamp.Time)
				return ctrl.Result{RequeueAfter: requeueAfter}, nil
			}
			// scale up
			result, err := r.scaleup(ctx, availablePods, sameReplicaSetModules, otherReplicaSetModules, moduleReplicaSet)
			if err != nil {
				return result, err
			}
		} else {
			// scale down
			err = r.scaledown(ctx, sameReplicaSetModules, moduleReplicaSet)
			if err != nil {
				return reconcile.Result{}, err
			}
		}
	} else {
		// replicas not change, directly update module
		err = r.compareAndUpdateModule(ctx, sameReplicaSetModules, moduleReplicaSet)
		if err != nil {
			return reconcile.Result{}, err
		}
	}

	return ctrl.Result{}, nil
}

// compare and update module
func (r *ModuleReplicaSetReconciler) compareAndUpdateModule(ctx context.Context, existedModuleList []moduledeploymentv1alpha1.Module, moduleReplicaSet *moduledeploymentv1alpha1.ModuleReplicaSet) error {
	desiredModule := moduleReplicaSet.Spec.Template.Spec.Module
	for _, existedModule := range existedModuleList {

		needUpgradeModule := existedModule.Spec.Module.Name != desiredModule.Name || existedModule.Spec.Module.Version != desiredModule.Version
		needUninstallModule := existedModule.Spec.Module.Name != desiredModule.Name
		if needUpgradeModule {
			existedModule.Spec.Module.Name = desiredModule.Name
			existedModule.Spec.Module.Version = desiredModule.Version
			existedModule.Spec.Module.Url = desiredModule.Url
			err := r.Client.Update(ctx, &existedModule)
			if err != nil {
				return utils.Error(err, "Failed to update module", "moduleName", existedModule.Name)
			}
		}
		if needUninstallModule {
			err := r.Client.Delete(ctx, &existedModule)
			if err != nil {
				return utils.Error(err, "Failed to delete module", "moduleName", existedModule.Name)
			}
		}
	}
	return nil
}

// handle deleting moduleReplicaSet
func (r *ModuleReplicaSetReconciler) handleDeletingModuleReplicaSet(ctx context.Context, existedModuleList []moduledeploymentv1alpha1.Module, moduleReplicaSet *moduledeploymentv1alpha1.ModuleReplicaSet) (ctrl.Result, error) {
	if len(existedModuleList) == 0 {
		if utils.HasFinalizer(&moduleReplicaSet.ObjectMeta, finalizer.ModuleExistedFinalizer) {
			// all module is removed, remove module replicaset finalizer
			log.Log.Info("all modules are deleted, remove moduleReplicaSet finalizer", "moduleReplicaSetName", moduleReplicaSet.Name)
			utils.RemoveFinalizer(&moduleReplicaSet.ObjectMeta, finalizer.ModuleExistedFinalizer)
			err := r.Client.Update(ctx, moduleReplicaSet)
			if err != nil {
				return ctrl.Result{}, err
			}
		}
		return ctrl.Result{}, nil
	} else {
		var err error
		for _, existedModule := range existedModuleList {
			log.Log.Info("moduleReplicaSet is deleting, delete module", "moduleReplicaSetName", moduleReplicaSet.Name, "module", existedModule.Name)
			existedModule.Labels[label.DeleteModuleLabel] = "true"
			err = r.Client.Update(ctx, &existedModule)
		}
		if err != nil {
			return ctrl.Result{}, utils.Error(err, "Failed to update uninstall module label")
		}

		// wait all module deleting
		log.Log.Info("moduleReplicaSet wait module deleting", "moduleReplicaSetName", moduleReplicaSet.Name, "existedModuleSize", len(existedModuleList))
		requeueAfter := utils.GetNextReconcileTime(moduleReplicaSet.DeletionTimestamp.Time)
		return ctrl.Result{RequeueAfter: requeueAfter}, nil
	}
}

// generate module
func (r *ModuleReplicaSetReconciler) generateModule(moduleReplicaSet *moduledeploymentv1alpha1.ModuleReplicaSet, pod corev1.Pod) *moduledeploymentv1alpha1.Module {

	moduleLabels := moduleReplicaSet.Labels
	moduleLabels[label.ModuleNameLabel] = moduleReplicaSet.Spec.Template.Spec.Module.Name
	moduleLabels[label.ModuleVersionLabel] = moduleReplicaSet.Spec.Template.Spec.Module.Version
	moduleLabels[label.BaseInstanceIpLabel] = pod.Status.PodIP
	moduleLabels[label.BaseInstanceNameLabel] = pod.Name
	moduleLabels[label.ModuleReplicasetLabel] = moduleReplicaSet.Name

	module := &moduledeploymentv1alpha1.Module{
		ObjectMeta: metav1.ObjectMeta{
			Annotations:  map[string]string{},
			Labels:       moduleLabels,
			GenerateName: fmt.Sprintf("%s-%s-", moduleReplicaSet.Name, moduleReplicaSet.Spec.Template.Spec.Module.Name),
			Namespace:    moduleReplicaSet.Namespace,
		},
		Spec: moduledeploymentv1alpha1.ModuleSpec{
			Selector:      moduleReplicaSet.Spec.Selector,
			Module:        moduleReplicaSet.Spec.Template.Spec.Module,
			UpgradePolicy: moduleReplicaSet.Spec.SchedulingStrategy.UpgradePolicy,
		},
	}
	// OwnerReference to moduleReplicaSet and Pod
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
	return module
}

// SetupWithManager sets up the controller with the Manager.
func (r *ModuleReplicaSetReconciler) SetupWithManager(mgr ctrl.Manager) error {
	filterFn := handler.MapFunc(
		func(_ context.Context, object client.Object) []reconcile.Request {
			module := object.(*moduledeploymentv1alpha1.Module)
			return []reconcile.Request{
				{
					NamespacedName: types.NamespacedName{
						Namespace: object.GetNamespace(),
						Name:      module.Labels[label.ModuleReplicasetLabel],
					},
				},
			}
		})

	return ctrl.NewControllerManagedBy(mgr).
		For(&moduledeploymentv1alpha1.ModuleReplicaSet{}).
		// watch module events
		Watches(&moduledeploymentv1alpha1.Module{}, handler.EnqueueRequestsFromMapFunc(filterFn)).
		Complete(r)
}

// scale up module
func (r *ModuleReplicaSetReconciler) scaleup(ctx context.Context, availablePods *corev1.PodList, sameReplicaSetModules []moduledeploymentv1alpha1.Module,
	otherReplicaSetModules []moduledeploymentv1alpha1.Module, moduleReplicaSet *moduledeploymentv1alpha1.ModuleReplicaSet) (ctrl.Result, error) {
	log.Log.Info("start scale up module", "moduleReplicaSetName", moduleReplicaSet.Name)

	// get candidate pod
	toAllocatePod, err := r.getScaleUpCandidatePods(sameReplicaSetModules, availablePods, moduleReplicaSet)
	if err != nil {
		return reconcile.Result{}, utils.Error(err, "Failed to get the candidate pods for scaling up")
	}

	// scale down old module
	if len(otherReplicaSetModules) > 0 {
		err := r.scaleDownOldPods(ctx, toAllocatePod, otherReplicaSetModules)
		if err != nil {
			return ctrl.Result{}, err
		}
	}

	// allocate pod
	err = r.doAllocatePod(ctx, toAllocatePod, moduleReplicaSet)
	if err != nil {
		return reconcile.Result{}, err
	}

	log.Log.Info("finish scaleup module", "moduleReplicaSetName", moduleReplicaSet.Name)
	return reconcile.Result{}, nil
}

// scale down module
func (r *ModuleReplicaSetReconciler) scaledown(ctx context.Context, existedModuleList []moduledeploymentv1alpha1.Module, moduleReplicaSet *moduledeploymentv1alpha1.ModuleReplicaSet) error {

	var scaleDownModuleList []moduledeploymentv1alpha1.Module
	for _, module := range existedModuleList {
		// filter out module with delete label
		if module.Labels[label.DeleteModuleLabel] != "true" {
			scaleDownModuleList = append(scaleDownModuleList, module)
		}
	}

	deltaReplicas := int(moduleReplicaSet.Spec.Replicas) - len(scaleDownModuleList)
	count := -deltaReplicas
	log.Log.Info("scale down replicas", "deltaReplicas", deltaReplicas)

	selector, err := metav1.LabelSelectorAsSelector(&moduleReplicaSet.Spec.Selector)
	selectedPods := &corev1.PodList{}
	if err = r.List(ctx, selectedPods, &client.ListOptions{Namespace: moduleReplicaSet.Namespace, LabelSelector: selector}); err != nil {
		return utils.Error(err, "Failed to list pod", "moduleReplicaSetName", moduleReplicaSet.Name)
	}
	toDeletedModules, moduleToPod := r.getScaleDownCandidateModules(scaleDownModuleList, selectedPods, moduleReplicaSet)
	for _, module := range toDeletedModules {
		if moduledeploymentv1alpha1.ScaleUpThenScaleDownUpgradePolicy == moduleReplicaSet.Spec.SchedulingStrategy.UpgradePolicy {
			targetPod := moduleToPod[module.Name]
			targetPod.Labels[label.DeletePodLabel] = "true"
			err = r.Client.Update(ctx, targetPod)
			if err != nil {
				log.Log.Error(err, "Failed to update delete pod label", "module", module, "podName", targetPod.Name)
			}
		} else {
			module.Labels[label.DeleteModuleLabel] = "true"
			err = r.Client.Update(ctx, &module)
			if err != nil {
				log.Log.Error(err, "Failed to delete module", "module", module)
			}
		}
		if count--; count == 0 {
			break
		}
	}
	return err
}

// scale down old pod from old replicaset
func (r *ModuleReplicaSetReconciler) scaleDownOldPods(ctx context.Context, toAllocatePod []corev1.Pod, otherReplicaSetModules []moduledeploymentv1alpha1.Module) error {
	deleteReplicas := int32(len(toAllocatePod))
	// scale down old replicaSet
	otherReplicaSet := make(map[string]string)
	for _, otherModule := range otherReplicaSetModules {
		if otherModule.Labels[label.ModuleReplicasetLabel] != "" {
			otherReplicaSet[otherModule.Labels[label.ModuleReplicasetLabel]] = otherModule.Namespace
		}
	}
	for otherReplicaName := range otherReplicaSet {
		otherModuleReplicaSet := &moduledeploymentv1alpha1.ModuleReplicaSet{}
		err := r.Client.Get(ctx, types.NamespacedName{Namespace: otherReplicaSet[otherReplicaName], Name: otherReplicaName}, otherModuleReplicaSet)
		if err != nil {
			return utils.Error(err, "get otherModuleReplicaSet failed", "otherModuleReplicaSetName", otherModuleReplicaSet.Name)
		}
		if otherModuleReplicaSet.Spec.Replicas > 0 {
			if deleteReplicas <= otherModuleReplicaSet.Spec.Replicas {
				// other replicas >= delete replicas, scale down other replicas
				otherModuleReplicaSet.Spec.Replicas -= deleteReplicas
				deleteReplicas = 0
			} else {
				// other replicas < delete replicas, scale down other replicas to 0 and continue to find next module replicaset
				deleteReplicas -= otherModuleReplicaSet.Spec.Replicas
				otherModuleReplicaSet.Spec.Replicas = 0
			}
			if err := r.Client.Update(ctx, otherModuleReplicaSet); err != nil {
				return utils.Error(err, "Failed to update other replicaset", "moduleReplicaSetName", otherModuleReplicaSet.Name)
			}
		}
	}
	return nil
}

// get the candidate pods used to install modules when scaling up
func (r *ModuleReplicaSetReconciler) getScaleUpCandidatePods(sameReplicaSetModules []moduledeploymentv1alpha1.Module,
	selectedPods *corev1.PodList, moduleReplicaSet *moduledeploymentv1alpha1.ModuleReplicaSet) ([]corev1.Pod, error) {

	deltaReplicas := int(moduleReplicaSet.Spec.Replicas) - len(sameReplicaSetModules)
	usedPodNames := make(map[string]bool)
	for _, module := range sameReplicaSetModules {
		usedPodNames[module.Labels[label.BaseInstanceNameLabel]] = true
	}

	maxModuleCountLabel := selectedPods.Items[0].Labels[label.MaxModuleCount]
	maxModuleCount, err := strconv.Atoi(maxModuleCountLabel)
	if err != nil {
		// if we get MaxModuleCount failed from pod, then set it to max
		maxModuleCount = intsets.MaxInt
	}

	// sort pod by strategy
	strategyLabel := moduleReplicaSet.Labels[label.ModuleSchedulingStrategy]
	strategy := moduledeploymentv1alpha1.ModuleSchedulingType(strategyLabel)
	sortPodByStrategy(strategy, selectedPods.Items, true)

	// allocate pod
	var toAllocatePod []corev1.Pod
	count := deltaReplicas
	for _, pod := range selectedPods.Items {
		if pod.DeletionTimestamp != nil {
			continue
		}
		var instanceCount int
		if cntStr, ok := pod.Labels[label.ModuleInstanceCount]; !ok {
			instanceCount = utils.GetModuleCountFromPod(&pod)
			pod.Labels[label.ModuleInstanceCount] = strconv.Itoa(instanceCount)
			if err = r.Client.Update(context.TODO(), &pod); err != nil {
				log.Log.Error(err, fmt.Sprintf("failed to update pod label"))
				continue
			}
		} else {
			instanceCount, err = strconv.Atoi(cntStr)
			if err != nil {
				log.Log.Error(err, fmt.Sprintf("invalid ModuleInstanceCount in pod %v", pod.Name))
				continue
			}
		}

		if _, ok := usedPodNames[pod.Name]; !ok && instanceCount < maxModuleCount {
			toAllocatePod = append(toAllocatePod, pod)
			if count--; count == 0 {
				break
			}
		}
	}
	return toAllocatePod, nil
}

func (r *ModuleReplicaSetReconciler) doAllocatePod(ctx context.Context, toAllocatePod []corev1.Pod, moduleReplicaSet *moduledeploymentv1alpha1.ModuleReplicaSet) error {
	for _, pod := range toAllocatePod {
		if _, exist := pod.Labels[label.ModuleInstanceCount]; exist {
			count, err := strconv.Atoi(pod.Labels[label.ModuleInstanceCount])
			if err != nil {
				log.Log.Error(err, "failed to update module count")
			} else {
				pod.Labels[label.ModuleInstanceCount] = strconv.Itoa(count + 1)
			}
		} else {
			pod.Labels[label.ModuleInstanceCount] = "1"
		}
		// add pod finalizer
		utils.AddFinalizer(&pod.ObjectMeta, fmt.Sprintf("%s-%s", finalizer.ModuleNameFinalizer, moduleReplicaSet.Spec.Template.Spec.Module.Name))
		err := r.Client.Update(ctx, &pod)
		// add pod finalizer
		if err != nil {
			return err
		}
		// create module
		module := r.generateModule(moduleReplicaSet, pod)
		if err = r.Client.Create(ctx, module); err != nil {
			return utils.Error(err, "Failed to create module", "moduleName", module.Name)
		}
	}
	return nil
}

// get the candidate modules to be deleted when scaling down
func (r *ModuleReplicaSetReconciler) getScaleDownCandidateModules(
	existedModuleList []moduledeploymentv1alpha1.Module,
	selectedPods *corev1.PodList,
	moduleReplicaSet *moduledeploymentv1alpha1.ModuleReplicaSet,
) ([]moduledeploymentv1alpha1.Module, map[string]*corev1.Pod) {
	deltaReplicas := int(moduleReplicaSet.Spec.Replicas) - len(existedModuleList)
	usedPodNames := make(map[string]int)
	for idx, module := range existedModuleList {
		usedPodNames[module.Labels[label.BaseInstanceNameLabel]] = idx
	}

	var filteredPods []corev1.Pod
	for i := 0; i < len(selectedPods.Items); i++ {
		if _, ok := usedPodNames[selectedPods.Items[i].Name]; ok {
			filteredPods = append(filteredPods, selectedPods.Items[i])
		}
	}

	// get strategy, maxModuleCount from replicaSet Labels
	strategyLabel := moduleReplicaSet.Labels[label.ModuleSchedulingStrategy]
	strategy := moduledeploymentv1alpha1.ModuleSchedulingType(strategyLabel)

	sortPodByStrategy(strategy, filteredPods, false)

	var candidateModules []moduledeploymentv1alpha1.Module
	moduleToPod := make(map[string]*corev1.Pod)
	i := 0
	count := -deltaReplicas
	for count > 0 && i < len(filteredPods) {
		idx := usedPodNames[filteredPods[i].Name]
		candidateModules = append(candidateModules, existedModuleList[idx])
		moduleToPod[existedModuleList[idx].Name] = &filteredPods[i]
		count -= 1
		i += 1
	}
	return candidateModules, moduleToPod
}

// sort pod by the scheduling strategy policy (scatter or stacking)
func sortPodByStrategy(strategy moduledeploymentv1alpha1.ModuleSchedulingType, selectedPods []corev1.Pod, scaleUp bool) {
	if strategy == moduledeploymentv1alpha1.Scatter {
		sort.Slice(selectedPods, func(i, j int) bool {
			if scaleUp {
				return utils.GetModuleInstanceCount(selectedPods[i]) < utils.GetModuleInstanceCount(selectedPods[j])
			} else {
				return utils.GetModuleInstanceCount(selectedPods[i]) > utils.GetModuleInstanceCount(selectedPods[j])
			}
		})
	} else if strategy == moduledeploymentv1alpha1.Stacking {
		sort.Slice(selectedPods, func(i, j int) bool {
			if scaleUp {
				return utils.GetModuleInstanceCount(selectedPods[i]) > utils.GetModuleInstanceCount(selectedPods[j])
			} else {
				return utils.GetModuleInstanceCount(selectedPods[i]) < utils.GetModuleInstanceCount(selectedPods[j])
			}
		})
	}
}
