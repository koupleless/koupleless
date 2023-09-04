package utils

import (
	"fmt"
	"sort"
	"strings"
	"time"

	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	ctrl "sigs.k8s.io/controller-runtime"

	moduledeploymentv1alpha1 "github.com/sofastack/sofa-serverless/api/v1alpha1"
)

type ModuleReplicaSetsByCreationTimestamp []*moduledeploymentv1alpha1.ModuleReplicaSet

func (m ModuleReplicaSetsByCreationTimestamp) Len() int {
	return len(m)
}

func (m ModuleReplicaSetsByCreationTimestamp) Less(i, j int) bool {
	if m[i].CreationTimestamp.Equal(&m[j].CreationTimestamp) {
		return m[i].Name < m[j].Name
	}
	return m[i].CreationTimestamp.Before(&m[j].CreationTimestamp)
}

func (m ModuleReplicaSetsByCreationTimestamp) Swap(i, j int) {
	m[i], m[j] = m[j], m[i]
}

func AddFinalizer(meta *metav1.ObjectMeta, finalizer string) bool {
	if meta.Finalizers == nil {
		meta.Finalizers = []string{}
	}
	for _, s := range meta.Finalizers {
		if s == finalizer {
			return false
		}
	}
	meta.Finalizers = append(meta.Finalizers, finalizer)
	return true
}

func RemoveFinalizer(meta *metav1.ObjectMeta, needle string) bool {
	finalizers := make([]string, 0)
	found := false
	if meta.Finalizers != nil {
		for _, finalizer := range meta.Finalizers {
			if finalizer != needle {
				finalizers = append(finalizers, finalizer)
			} else {
				found = true
			}
		}
		meta.Finalizers = finalizers
	}

	return found
}

func HasFinalizer(meta *metav1.ObjectMeta, needle string) bool {
	if meta.Finalizers != nil {
		for _, finalizer := range meta.Finalizers {
			if finalizer == needle {
				return true
			}
		}
	}

	return false
}

func Key(req ctrl.Request) string {
	return fmt.Sprintf("%s/%s", req.Namespace, req.Name)
}

func GetNextReconcileTime(currentTime time.Time) time.Duration {
	timeDuration := time.Now().Sub(currentTime)
	var nextDuration time.Duration
	if timeDuration.Hours() > 1 {
		nextDuration = time.Minute * 10
	} else if timeDuration.Minutes() > 30 {
		nextDuration = time.Minute * 5
	} else if timeDuration.Minutes() > 10 {
		nextDuration = time.Minute * 1
	} else {
		nextDuration = time.Second * 10
	}
	return nextDuration
}

type PodWithModuleCount struct {
	Pod   *corev1.Pod
	Count int
}

func ScaleUp(pods *corev1.PodList, existedModuleList *moduledeploymentv1alpha1.ModuleList,
	delta int, limit int, strategy moduledeploymentv1alpha1.ModuleSchedulingStrategy) []corev1.Pod {

	// get allocated pod
	usedPodNames := make(map[string]bool)
	for _, module := range existedModuleList.Items {
		usedPodNames[module.Labels[moduledeploymentv1alpha1.BaseInstanceNameLabel]] = true
	}

	// allocate pod
	var toAllocatePod []PodWithModuleCount

	for i := 0; i < len(pods.Items); i++ {
		pod := pods.Items[i]
		if _, ok := usedPodNames[pod.Name]; !ok {
			if count := GetModuleCountFromPod(&pod); count < limit {
				toAllocatePod = append(toAllocatePod, PodWithModuleCount{
					Pod:   &pod,
					Count: count,
				})
			}
		}
	}

	if strategy == moduledeploymentv1alpha1.Scatter {
		sort.Slice(toAllocatePod, func(i, j int) bool {
			return toAllocatePod[i].Count < toAllocatePod[j].Count
		})
	} else if strategy == moduledeploymentv1alpha1.Stacking {
		sort.Slice(toAllocatePod, func(i, j int) bool {
			return toAllocatePod[i].Count > toAllocatePod[j].Count
		})
	}

	var res []corev1.Pod
	for i := 0; i < len(toAllocatePod) && i < delta; i++ {
		res = append(res, *toAllocatePod[i].Pod)
	}
	return res
}

func ScaleDown(pods *corev1.PodList, existedModuleList *moduledeploymentv1alpha1.ModuleList, delta int, strategy moduledeploymentv1alpha1.ModuleSchedulingStrategy) []moduledeploymentv1alpha1.Module {
	usedPodNames := make(map[string]int)
	for idx, module := range existedModuleList.Items {
		usedPodNames[module.Labels[moduledeploymentv1alpha1.BaseInstanceNameLabel]] = idx
	}

	var filteredPods []PodWithModuleCount
	for i := 0; i < len(pods.Items); i++ {
		pod := pods.Items[i]
		if _, ok := usedPodNames[pod.Name]; !ok {
			filteredPods = append(filteredPods, PodWithModuleCount{
				Pod:   &pod,
				Count: GetModuleCountFromPod(&pod),
			})
		}
	}

	if strategy == moduledeploymentv1alpha1.Scatter {
		sort.Slice(filteredPods, func(i, j int) bool {
			return filteredPods[i].Count > filteredPods[j].Count
		})
	} else if strategy == moduledeploymentv1alpha1.Stacking {
		sort.Slice(filteredPods, func(i, j int) bool {
			return filteredPods[i].Count < filteredPods[j].Count
		})
	}

	var res []moduledeploymentv1alpha1.Module
	for i := 0; i < len(filteredPods) && i < delta; i++ {
		res = append(res, existedModuleList.Items[usedPodNames[filteredPods[i].Pod.Name]])
	}
	return res
}

func GetModuleCountFromPod(pod *corev1.Pod) (count int) {
	for k, _ := range pod.Labels {
		if strings.HasPrefix(k, moduledeploymentv1alpha1.ModuleNameLabel) {
			count += 1
		}
	}
	return count
}
