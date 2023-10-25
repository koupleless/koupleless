package utils

import (
	"github.com/sofastack/sofa-serverless/api/v1alpha1"
	"github.com/sofastack/sofa-serverless/internal/constants/label"
	"golang.org/x/net/context"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/meta"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/runtime/schema"
	"reflect"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"strconv"
)

func PrepareModuleReplicaSet(namespace, moduleReplicaSetName string) v1alpha1.ModuleReplicaSet {

	moduleReplicaSet := v1alpha1.ModuleReplicaSet{
		Spec: v1alpha1.ModuleReplicaSetSpec{
			Replicas: 1,
			Template: v1alpha1.ModuleTemplateSpec{
				Spec: v1alpha1.ModuleSpec{
					Module: v1alpha1.ModuleInfo{
						Name:    "dynamic-provider",
						Version: "1.0.0",
						Url:     "http://serverless-opensource.oss-cn-shanghai.aliyuncs.com/module-packages/stable/dynamic-provider-1.0.0-ark-biz.jar",
					},
				},
			},
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      moduleReplicaSetName,
			Namespace: namespace,
			Labels: map[string]string{
				"app":                          "dynamic-stock",
				label.MaxModuleCount:           "10",
				label.ModuleSchedulingStrategy: string(v1alpha1.Scatter),
				label.DeploymentNameLabel:      "test-deployment-name",
			},
			Annotations: map[string]string{},
		},
	}
	return moduleReplicaSet
}

type MockOnBaseClient struct {
}

func (m MockOnBaseClient) Scheme() *runtime.Scheme {
	return nil
}

func (m MockOnBaseClient) RESTMapper() meta.RESTMapper {
	return nil
}

func (m MockOnBaseClient) GroupVersionKindFor(obj runtime.Object) (schema.GroupVersionKind, error) {
	return schema.GroupVersionKind{}, nil
}

func (m MockOnBaseClient) IsObjectNamespaced(obj runtime.Object) (bool, error) {
	return true, nil
}

func (m MockOnBaseClient) Get(ctx context.Context, key client.ObjectKey, obj client.Object, opts ...client.GetOption) error {
	return nil
}

func (m MockOnBaseClient) List(ctx context.Context, list client.ObjectList, opts ...client.ListOption) error {
	var mockPodList []corev1.Pod
	for i := 3; i > 0; i-- {
		podName := "mock-pod-" + strconv.Itoa(i)
		pod := &corev1.Pod{
			TypeMeta: metav1.TypeMeta{},
			ObjectMeta: metav1.ObjectMeta{
				Name: podName,
				Labels: map[string]string{
					label.ModuleInstanceCount: strconv.Itoa(i),
				},
			},
			Spec:   corev1.PodSpec{},
			Status: corev1.PodStatus{},
		}
		mockPodList = append(mockPodList, *pod)
	}

	// 使用反射设置 list 的 Items 字段
	listValue := reflect.ValueOf(list)
	if listValue.Kind() == reflect.Ptr && listValue.Elem().Kind() == reflect.Struct {
		itemsField := listValue.Elem().FieldByName("Items")
		if itemsField.IsValid() && itemsField.CanSet() {
			itemsField.Set(reflect.ValueOf(mockPodList))
		}
	}
	return nil
}

func (m MockOnBaseClient) Create(ctx context.Context, obj client.Object, opts ...client.CreateOption) error {
	return nil
}

func (m MockOnBaseClient) Delete(ctx context.Context, obj client.Object, opts ...client.DeleteOption) error {
	return nil
}

func (m MockOnBaseClient) Update(ctx context.Context, obj client.Object, opts ...client.UpdateOption) error {
	return nil
}

func (m MockOnBaseClient) Patch(ctx context.Context, obj client.Object, patch client.Patch, opts ...client.PatchOption) error {
	return nil
}

func (m MockOnBaseClient) DeleteAllOf(ctx context.Context, obj client.Object, opts ...client.DeleteAllOfOption) error {
	return nil
}

func (m MockOnBaseClient) Status() client.SubResourceWriter {
	return nil
}

func (m MockOnBaseClient) SubResource(subResource string) client.SubResourceClient {
	return nil
}
