package utils

import (
	"fmt"
	"github.com/sofastack/sofa-serverless/api/v1alpha1"
	"golang.org/x/net/context"
	"k8s.io/apimachinery/pkg/api/errors"
	"strconv"
	"testing"
	"time"

	"github.com/stretchr/testify/assert"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/types"
	ctrl "sigs.k8s.io/controller-runtime"

	"github.com/sofastack/sofa-serverless/internal/constants/finalizer"
	"github.com/sofastack/sofa-serverless/internal/constants/label"
)

func TestAddNotExistedFinalizer(t *testing.T) {
	meta := &metav1.ObjectMeta{}
	assert.True(t, AddFinalizer(meta, finalizer.ModuleReplicaSetExistedFinalizer))
}

func TestHasNotExistedFinalizer(t *testing.T) {
	meta := &metav1.ObjectMeta{}
	assert.False(t, HasFinalizer(meta, finalizer.ModuleReplicaSetExistedFinalizer))
}

func TestRemoveNotExistedFinalizer(t *testing.T) {
	meta := &metav1.ObjectMeta{}
	assert.False(t, RemoveFinalizer(meta, finalizer.ModuleReplicaSetExistedFinalizer))
}

func TestAddExistedFinalizer(t *testing.T) {
	meta := &metav1.ObjectMeta{}
	meta.Finalizers = []string{finalizer.ModuleReplicaSetExistedFinalizer}
	assert.False(t, AddFinalizer(meta, finalizer.ModuleReplicaSetExistedFinalizer))
}

func TestHasExistedFinalizer(t *testing.T) {
	meta := &metav1.ObjectMeta{}
	meta.Finalizers = []string{finalizer.ModuleReplicaSetExistedFinalizer}
	assert.True(t, HasFinalizer(meta, finalizer.ModuleReplicaSetExistedFinalizer))
}

func TestRemoveExistedFinalizer(t *testing.T) {
	meta := &metav1.ObjectMeta{}
	meta.Finalizers = []string{finalizer.ModuleReplicaSetExistedFinalizer}
	assert.True(t, RemoveFinalizer(meta, finalizer.ModuleReplicaSetExistedFinalizer))
}

func TestKeyEqual(t *testing.T) {
	request := ctrl.Request{NamespacedName: types.NamespacedName{Namespace: "default", Name: "test"}}
	key := Key(request)
	assert.Equal(t, "default/test", key)
}

func TestGetNextReconcileTime(t *testing.T) {
	now := time.Now()
	reconcileTime := GetNextReconcileTime(now)
	assert.Equal(t, "10s", reconcileTime.String())

	m, _ := time.ParseDuration("-11m")
	reconcileTime2 := GetNextReconcileTime(now.Add(m))
	assert.Equal(t, "1m0s", reconcileTime2.String())

	m31, _ := time.ParseDuration("-31m")
	reconcileTime3 := GetNextReconcileTime(now.Add(m31))
	assert.Equal(t, "5m0s", reconcileTime3.String())

	h1, _ := time.ParseDuration("-61m")
	reconcileTime4 := GetNextReconcileTime(now.Add(h1))
	assert.Equal(t, "10m0s", reconcileTime4.String())
}

func TestGetModuleCountFromPod(t *testing.T) {
	pod := &corev1.Pod{
		ObjectMeta: metav1.ObjectMeta{
			Labels: map[string]string{},
		},
	}
	count := 5

	for i := 0; i < count; i++ {
		pod.Labels[fmt.Sprintf(label.ModuleLabelPrefix+"module-"+strconv.Itoa(i))] = "true"
	}

	actual := GetModuleCountFromPod(pod)
	if count != actual {
		t.Errorf("the expected count is %v, but got %v", count, actual)
	}
}

func TestError(t *testing.T) {
	err := Error(errors.NewBadRequest("testBadRequestErr"), "testMsg", "testKey", "testValue")
	assert.True(t, errors.IsBadRequest(err))
}

func TestGetModuleInstanceCount(t *testing.T) {
	pod := &corev1.Pod{
		ObjectMeta: metav1.ObjectMeta{
			Labels: map[string]string{},
		},
	}
	assert.Equal(t, 0, GetModuleInstanceCount(*pod))

	pod.Labels[label.ModuleInstanceCount] = "error"
	assert.Equal(t, 0, GetModuleInstanceCount(*pod))

	pod.Labels[label.ModuleInstanceCount] = "1"
	assert.Equal(t, 1, GetModuleInstanceCount(*pod))
}

func TestAppendModuleDeploymentCondition(t *testing.T) {
	moduleDeployment := PrepareModuleDeployment("namespace", "testModuleDeploymentName")

	i := 0
	for i < 11 {
		condition := v1alpha1.ModuleDeploymentCondition{
			Type:    v1alpha1.DeploymentProgressing,
			Status:  corev1.ConditionTrue,
			Message: "message" + strconv.Itoa(i),
		}
		moduleDeployment.Status.Conditions = AppendModuleDeploymentCondition(moduleDeployment.Status.Conditions, condition)
		i++
	}
	assert.Equal(t, 10, len(moduleDeployment.Status.Conditions))
}

func TestUpdateResource(t *testing.T) {
	moduleDeployment := PrepareModuleDeployment("namespace", "testModuleDeploymentName")
	client := MockClient{}
	err := UpdateResource(client, context.TODO(), &moduleDeployment)
	assert.True(t, err == nil)
}

func TestUpdateStatus(t *testing.T) {
	moduleDeployment := PrepareModuleDeployment("namespace", "testModuleDeploymentName")
	client := MockClient{}
	err := UpdateStatus(client, context.TODO(), &moduleDeployment)
	assert.True(t, err == nil)
}

//
//type TestClient struct {
//	MockClient
//}
//
//func (m MockClient) Update(ctx context.Context, obj client.Object, opts ...client.UpdateOption) error {
//	return nil
//}
