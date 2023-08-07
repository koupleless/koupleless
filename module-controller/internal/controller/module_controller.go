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
	"encoding/json"
	"fmt"
	moduledeploymentv1alpha1 "github.com/sofastack/sofa-serverless/api/v1alpha1"
	"k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/runtime"
	"net/http"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/log"
	"sigs.k8s.io/controller-runtime/pkg/reconcile"
)

// ModuleReconciler reconciles a Module object
type ModuleReconciler struct {
	client.Client
	Scheme *runtime.Scheme
}

//+kubebuilder:rbac:groups=module-deployment.serverless.alipay.com,resources=modules,verbs=get;list;watch;create;update;patch;delete
//+kubebuilder:rbac:groups=module-deployment.serverless.alipay.com,resources=modules/status,verbs=get;update;patch
//+kubebuilder:rbac:groups=module-deployment.serverless.alipay.com,resources=modules/finalizers,verbs=update

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

	// TODO(user): your logic here
	module := &moduledeploymentv1alpha1.Module{}
	err := r.Client.Get(ctx, req.NamespacedName, module)

	if err != nil {
		if errors.IsNotFound(err) {
			log.Log.Info("module is deleted", "moduleName", module.Name)
			return reconcile.Result{}, nil
		}
		log.Log.Error(err, "Failed to get module", "moduleName", module.Name)
		return ctrl.Result{}, nil
	}

	ip := module.Labels[moduledeploymentv1alpha1.PodIpLabel]
	if ip == "" {
		return ctrl.Result{}, nil
	}

	url := fmt.Sprintf("http://%s:1238/installBiz", ip)
	body := fmt.Sprintf(`{"bizName": "%s", "bizVersion": "%s", "arkBizFilePath": "%s"}`, module.Spec.Module.Name, module.Spec.Module.Version, module.Spec.Module.Url)
	postResult, err := http.Post(url, "application/json", bytes.NewReader([]byte(body)))
	if err != nil {
		log.Log.Error(err, "Failed post module", "moduleName", module.Name, "url", url, "body", body)
		return ctrl.Result{}, err
	}
	postJson, _ := json.Marshal(postResult)
	log.Log.Info("post success", "ip", ip, "result", postJson)

	return ctrl.Result{}, nil
}

// SetupWithManager sets up the controller with the Manager.
func (r *ModuleReconciler) SetupWithManager(mgr ctrl.Manager) error {
	return ctrl.NewControllerManagedBy(mgr).
		For(&moduledeploymentv1alpha1.Module{}).
		Complete(r)
}
