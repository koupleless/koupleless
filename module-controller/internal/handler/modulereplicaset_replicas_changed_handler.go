package handler

import (
	"fmt"
	"github.com/sofastack/sofa-serverless/api/v1alpha1"
	"github.com/sofastack/sofa-serverless/internal/constants/label"
	"github.com/sofastack/sofa-serverless/internal/event"
	"github.com/sofastack/sofa-serverless/internal/utils"
	v1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/labels"
	"k8s.io/apimachinery/pkg/types"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/log"
	"strconv"
)

type ModuleReplicaSetReplicasChangedHandler struct {
}

func (h ModuleReplicaSetReplicasChangedHandler) Async() bool {
	return true
}

func (h ModuleReplicaSetReplicasChangedHandler) Handle(e event.Event) error {
	moduleReplicaSetReplicasChangedEvent := e.(event.ModuleReplicaSetReplicasChangedEvent)
	moduleReplicaSet := moduleReplicaSetReplicasChangedEvent.ModuleReplicaSet
	ctx := moduleReplicaSetReplicasChangedEvent.Context
	k8sClient := moduleReplicaSetReplicasChangedEvent.Client
	baseDeploymentName := moduleReplicaSet.Labels[label.DeploymentNameLabel]
	if baseDeploymentName == "" {
		return nil
	}
	deployment := &v1.Deployment{}
	err := k8sClient.Get(ctx,
		types.NamespacedName{Namespace: moduleReplicaSet.Namespace, Name: baseDeploymentName}, deployment)
	if err != nil {
		return utils.Error(err, "Failed to get deployment", "deploymentName", baseDeploymentName)
	}
	allPodSelector, err := metav1.LabelSelectorAsSelector(&moduleReplicaSet.Spec.Selector)
	allPods := &corev1.PodList{}
	if err = k8sClient.List(ctx, allPods, &client.ListOptions{Namespace: moduleReplicaSet.Namespace, LabelSelector: allPodSelector}); err != nil {
		return utils.Error(err, "Failed to list pod", "moduleReplicaSetName", moduleReplicaSet.Name)
	}

	if len(allPods.Items) <= 0 {
		return nil
	}
	var minInstanceCount int
	var maxInstanceCount int
	var totalInstanceCount int
	for index, item := range allPods.Items {
		var moduleInstanceCount int
		if cntStr, ok := item.Labels[label.ModuleInstanceCount]; ok {
			moduleInstanceCount, err = strconv.Atoi(cntStr)
			if err != nil {
				log.Log.Error(err, fmt.Sprintf("invalid ModuleInstanceCount in pod %v", item.Name))
				continue
			}
		} else {
			moduleList := &v1alpha1.ModuleList{}
			err := k8sClient.List(ctx, moduleList, &client.ListOptions{LabelSelector: labels.SelectorFromSet(map[string]string{
				label.BaseInstanceIpLabel: item.Status.PodIP,
			})}, client.InNamespace(moduleReplicaSet.Namespace))
			if err != nil {
				log.Log.Error(err, fmt.Sprintf("can't find any module in pod %v", item.Name))
				continue
			}
			moduleInstanceCount = len(moduleList.Items)
		}

		// 赋值第一个pod的安装数量为最小值
		if index == 0 {
			minInstanceCount = moduleInstanceCount
		}
		// 对比获取最小值
		if minInstanceCount > moduleInstanceCount {
			minInstanceCount = moduleInstanceCount
		}
		// 对比获取最大值
		if moduleInstanceCount > maxInstanceCount {
			maxInstanceCount = moduleInstanceCount
		}
		// 获取全部安装数量
		totalInstanceCount += moduleInstanceCount
	}

	if deployment.Labels == nil {
		deployment.Labels = map[string]string{}
	}
	deployment.Labels[label.MaxModuleInstanceCount] = strconv.Itoa(maxInstanceCount)
	deployment.Labels[label.MinModuleInstanceCount] = strconv.Itoa(minInstanceCount)
	avgInstanceCount := float64(totalInstanceCount) / float64(len(allPods.Items))
	deployment.Labels[label.AverageModuleInstanceCount] = fmt.Sprintf("%.2f", avgInstanceCount)

	if err = k8sClient.Update(ctx, deployment); err != nil {
		return utils.Error(err, "Failed to update Deployment", "Deployment", baseDeploymentName)
	}
	return nil
}

func (h ModuleReplicaSetReplicasChangedHandler) InterestIn(e event.Event) bool {
	return e.GetEventType() == event.ModuleReplicaSetReplicasChanged
}

func init() {
	event.Handlers = append(event.Handlers, ModuleReplicaSetReplicasChangedHandler{})
}
