package controller

import (
	"fmt"
	"strconv"
	"testing"

	"github.com/stretchr/testify/assert"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"

	"github.com/sofastack/sofa-serverless/api/v1alpha1"
	"github.com/sofastack/sofa-serverless/internal/constants/label"
)

type podWithModules struct {
	podName string
	modules []fakeModule
}
type fakeModule struct {
	name string
}

/*
pod 01: moduleCount=0
pod 02: moduleCount=1
pod 03: moduleCount=2
pod 04: moduleCount=3
*/

// mock selected pods
var pods = []podWithModules{
	{
		podName: "pod-01",
		modules: []fakeModule{},
	},
	{
		podName: "pod-02",
		modules: []fakeModule{
			{name: "module-01"},
		},
	},
	{
		podName: "pod-03",
		modules: []fakeModule{
			{name: "module-02"},
			{name: "module-03"},
		},
	},
	{
		podName: "pod-04",
		modules: []fakeModule{
			{name: "module-02"},
			{name: "module-03"},
			{name: "module-04"},
		},
	},
}

// test for fn getScaleUpCandidatePods
func TestModuleReplicaSetReconciler_getScaleUpCandidatePods(t *testing.T) {
	var targetModuleName = "module-01"

	podList := generatePodList(pods)
	moduleList := generateModuleList(targetModuleName, pods)

	testCases := []struct {
		targetModuleName string
		podList          *corev1.PodList
		moduleList       *v1alpha1.ModuleList
		moduleReplicaSet *v1alpha1.ModuleReplicaSet
		expectedPodNames []string
	}{
		{
			targetModuleName: targetModuleName,
			moduleReplicaSet: generateModuleReplicaSet(v1alpha1.Scatter, 2),
			podList:          podList,
			moduleList:       moduleList,
			expectedPodNames: []string{"pod-01"},
		},
		{
			targetModuleName: targetModuleName,
			moduleReplicaSet: generateModuleReplicaSet(v1alpha1.Stacking, 2),
			podList:          podList,
			moduleList:       moduleList,
			expectedPodNames: []string{"pod-03"},
		},
	}

	r := &ModuleReplicaSetReconciler{}
	cmp := func(expectedPodNames []string, actualPods []corev1.Pod) error {
		if n1, n2 := len(expectedPodNames), len(actualPods); n1 != n2 {
			return fmt.Errorf("expect pod Count is %d, but got %d", n1, n2)
		}
		for i := 0; i < len(actualPods); i++ {
			if actualPods[i].Name != expectedPodNames[i] {
				return fmt.Errorf("expect pod name is %v, but got %v", expectedPodNames[i], actualPods[i].Name)
			}
		}
		return nil
	}

	for _, tt := range testCases {
		t.Run("", func(t *testing.T) {
			actualPods, err := r.getScaleUpCandidatePods(tt.moduleList.Items, tt.podList, tt.moduleReplicaSet)
			assert.NoError(t, err)
			if err := cmp(tt.expectedPodNames, actualPods); err != nil {
				t.Error(err)
			}
		})
	}
}

// test for fn getScaleDownCandidateModules
func TestModuleReplicaSetReconciler_getScaleDownCandidateModules(t *testing.T) {
	var targetModuleName = "module-03"

	podList := generatePodList(pods)
	moduleList := generateModuleList(targetModuleName, pods)

	testCases := []struct {
		targetModuleName string
		podList          *corev1.PodList
		moduleList       *v1alpha1.ModuleList
		moduleReplicaSet *v1alpha1.ModuleReplicaSet
		expectedModules  []string
	}{
		{
			targetModuleName: targetModuleName,
			moduleReplicaSet: generateModuleReplicaSet(v1alpha1.Scatter, 1),
			podList:          podList,
			moduleList:       moduleList,
			expectedModules:  []string{targetModuleName + "/" + "pod-04"},
		},
		{
			targetModuleName: targetModuleName,
			moduleReplicaSet: generateModuleReplicaSet(v1alpha1.Stacking, 1),
			podList:          podList,
			moduleList:       moduleList,
			expectedModules:  []string{targetModuleName + "/" + "pod-03"},
		},
	}

	r := &ModuleReplicaSetReconciler{}
	cmp := func(expectedModules []string, actualModules []v1alpha1.Module) error {
		if n1, n2 := len(expectedModules), len(actualModules); n1 != n2 {
			return fmt.Errorf("expect module Count is %d, but got %d", n1, n2)
		}
		for i := 0; i < len(actualModules); i++ {
			if actualModules[i].Name != expectedModules[i] {
				return fmt.Errorf("expect module name is %v, but got %v", expectedModules[i], actualModules[i].Name)
			}
		}
		return nil
	}

	for _, tt := range testCases {
		t.Run("", func(t *testing.T) {
			actualModules, _ := r.getScaleDownCandidateModules(tt.moduleList.Items, tt.podList, tt.moduleReplicaSet)
			if err := cmp(tt.expectedModules, actualModules); err != nil {
				t.Error(err)
			}
		})
	}
}

// generate pod list
func generatePodList(pods []podWithModules) *corev1.PodList {
	podList := &corev1.PodList{}

	for i := 0; i < len(pods); i++ {
		pod := pods[i]
		podList.Items = append(podList.Items, corev1.Pod{
			ObjectMeta: metav1.ObjectMeta{
				Name: pod.podName,
				Labels: map[string]string{
					label.ModuleInstanceCount: strconv.FormatInt(int64(len(pod.modules)), 10),
					label.MaxModuleCount:      "3",
				},
			},
		})
	}
	return podList
}

// generate module list
func generateModuleList(moduleName string, pods []podWithModules) *v1alpha1.ModuleList {
	moduleList := &v1alpha1.ModuleList{}

	for i := 0; i < len(pods); i++ {
		for j := 0; j < len(pods[i].modules); j++ {
			if module := pods[i].modules[j]; module.name == moduleName {
				moduleList.Items = append(moduleList.Items, v1alpha1.Module{
					ObjectMeta: metav1.ObjectMeta{
						Name: module.name + "/" + pods[i].podName,
						Labels: map[string]string{
							label.BaseInstanceNameLabel: pods[i].podName,
						},
					},
				})
			}

		}
	}
	return moduleList
}

// generate moduleReplicaSet
func generateModuleReplicaSet(strategy v1alpha1.ModuleSchedulingType, replicas int) *v1alpha1.ModuleReplicaSet {
	return &v1alpha1.ModuleReplicaSet{
		ObjectMeta: metav1.ObjectMeta{
			Labels: map[string]string{
				label.ModuleSchedulingStrategy: string(strategy),
			},
		},
		Spec: v1alpha1.ModuleReplicaSetSpec{
			Replicas: int32(replicas),
			Template: v1alpha1.ModuleTemplateSpec{
				Spec: v1alpha1.ModuleSpec{},
			},
		},
	}
}
