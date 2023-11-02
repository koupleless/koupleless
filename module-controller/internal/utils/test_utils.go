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

func PrepareModuleDeployment(namespace, moduleDeploymentName string) v1alpha1.ModuleDeployment {
	baseDeploymentName := "dynamic-stock-deployment"

	moduleDeployment := v1alpha1.ModuleDeployment{
		Spec: v1alpha1.ModuleDeploymentSpec{
			BaseDeploymentName: baseDeploymentName,
			Template: v1alpha1.ModuleTemplateSpec{
				Spec: v1alpha1.ModuleSpec{
					Module: v1alpha1.ModuleInfo{
						Name:    "dynamic-provider",
						Version: "1.0.0",
						Url:     "http://serverless-opensource.oss-cn-shanghai.aliyuncs.com/module-packages/stable/dynamic-provider-1.0.0-ark-biz.jar",
					},
				},
			},
			SchedulingStrategy: v1alpha1.ModuleSchedulingStrategy{
				SchedulingPolicy: v1alpha1.Scatter,
			},
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      moduleDeploymentName,
			Namespace: namespace,
			Labels: map[string]string{
				"app": "dynamic-stock",
			},
			Annotations: map[string]string{},
		},
	}
	return moduleDeployment
}

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

type MockClient struct {
}

func (m MockClient) Scheme() *runtime.Scheme {
	return nil
}

func (m MockClient) RESTMapper() meta.RESTMapper {
	return nil
}

func (m MockClient) GroupVersionKindFor(obj runtime.Object) (schema.GroupVersionKind, error) {
	return schema.GroupVersionKind{}, nil
}

func (m MockClient) IsObjectNamespaced(obj runtime.Object) (bool, error) {
	return true, nil
}

func (m MockClient) Get(ctx context.Context, key client.ObjectKey, obj client.Object, opts ...client.GetOption) error {
	return nil
}

func (m MockClient) List(ctx context.Context, list client.ObjectList, opts ...client.ListOption) error {
	if list == nil {
		return nil
	}

	listValue := reflect.ValueOf(list)

	itemsType, itemsField := getListType(listValue)
	if itemsType == nil {
		return nil
	}

	// 检查切片中的元素类型
	switch itemsType {
	case reflect.TypeOf(corev1.Pod{}):
		var mockPodList []corev1.Pod

		for i := 3; i > 0; i-- {
			mockLabel := map[string]string{}
			if i == 3 {
				mockLabel = map[string]string{
					label.ModuleInstanceCount: strconv.Itoa(i),
				}
			}
			podName := "mock-pod-" + strconv.Itoa(i)
			podIp := "127.0.0." + strconv.Itoa(i)
			pod := &corev1.Pod{
				TypeMeta: metav1.TypeMeta{},
				ObjectMeta: metav1.ObjectMeta{
					Name:   podName,
					Labels: mockLabel,
				},
				Spec: corev1.PodSpec{},
				Status: corev1.PodStatus{
					PodIP: podIp,
				},
			}
			mockPodList = append(mockPodList, *pod)
		}
		itemsField.Set(reflect.ValueOf(mockPodList))
	case reflect.TypeOf(v1alpha1.Module{}):
		var mockModuleList []v1alpha1.Module
		moduleName := "mock-module-name"
		module := &v1alpha1.Module{
			ObjectMeta: metav1.ObjectMeta{
				Name: moduleName,
			},
		}
		mockModuleList = append(mockModuleList, *module)
		itemsField.Set(reflect.ValueOf(mockModuleList))
	}
	return nil
}

func getListType(listValue reflect.Value) (reflect.Type, reflect.Value) {

	itemsField := listValue.Elem().FieldByName("Items")
	if !itemsField.IsValid() {
		return nil, reflect.Value{}
	}
	itemsType := itemsField.Type()
	// 列表的类型是切片
	if itemsType.Kind() != reflect.Slice {
		return nil, reflect.Value{}
	}
	return itemsType.Elem(), itemsField
}

func (m MockClient) Create(ctx context.Context, obj client.Object, opts ...client.CreateOption) error {
	return nil
}

func (m MockClient) Delete(ctx context.Context, obj client.Object, opts ...client.DeleteOption) error {
	return nil
}

func (m MockClient) Update(ctx context.Context, obj client.Object, opts ...client.UpdateOption) error {
	return nil
}

func (m MockClient) Patch(ctx context.Context, obj client.Object, patch client.Patch, opts ...client.PatchOption) error {
	return nil
}

func (m MockClient) DeleteAllOf(ctx context.Context, obj client.Object, opts ...client.DeleteAllOfOption) error {
	return nil
}

func (m MockClient) Status() client.SubResourceWriter {
	return MockSubResourceWriter{}
}

func (m MockClient) SubResource(subResource string) client.SubResourceClient {
	return nil
}

type MockSubResourceWriter struct {
}

func (m MockSubResourceWriter) Create(ctx context.Context, obj client.Object, subResource client.Object, opts ...client.SubResourceCreateOption) error {
	return nil
}

func (m MockSubResourceWriter) Update(ctx context.Context, obj client.Object, opts ...client.SubResourceUpdateOption) error {
	return nil
}

func (m MockSubResourceWriter) Patch(ctx context.Context, obj client.Object, patch client.Patch, opts ...client.SubResourcePatchOption) error {
	return nil
}
