/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ark

import (
	"context"
	"encoding/json"
	"fmt"

	"serverless.alipay.com/sofa-serverless/arkctl/common/contextutil"
	"serverless.alipay.com/sofa-serverless/arkctl/common/fileutil"
	"serverless.alipay.com/sofa-serverless/arkctl/common/runtime"

	"github.com/go-resty/resty/v2"
)

// Service is responsible for interacting with ark container.
type Service interface {
	// ParseBizModel parse the biz file and return the biz model.
	ParseBizModel(ctx context.Context, bizUrl fileutil.FileUrl) (*BizModel, error)

	// InstallBiz call the remote ark container to install biz.
	// The precondition is that the biz file is already uploaded to the ark container or file hosting service (e.g. oss).
	InstallBiz(ctx context.Context, req InstallBizRequest) error

	// UnInstallBiz call the remote ark container to install biz.
	// The precondition is that the biz file is already uploaded to the ark container or file hosting service (e.g. oss).
	UnInstallBiz(ctx context.Context, req UnInstallBizRequest) error

	// QueryAllBiz call the remote ark container to query biz.
	QueryAllBiz(ctx context.Context, req QueryAllArkBizRequest) (*QueryAllArkBizResponse, error)
}

// BuildService return a new Service.
func BuildService(_ context.Context) Service {
	return &service{
		client: resty.New(),
	}
}

var (
	_ Service = &service{}
)

type service struct {
	client    *resty.Client
	fileUtils fileutil.FileUtils
}

// ParseBizModel parse the biz file and return the biz model.
func (h *service) ParseBizModel(ctx context.Context, bizUrl fileutil.FileUrl) (*BizModel, error) {
	return ParseBizModel(ctx, bizUrl)
}

// Use http client to install biz on local
// The implementation is simple, just copy file to local dir.
func (h *service) installBizOnLocal(ctx context.Context, req InstallBizRequest) error {
	resp, err := h.client.R().
		SetContext(ctx).
		SetBody(req.BizModel).
		Post(fmt.Sprintf("http://127.0.0.1:%d/installBiz", req.TargetContainer.GetPort()))

	if err != nil {
		return err
	}

	if !resp.IsSuccess() {
		return fmt.Errorf("install biz http failed with code %d", resp.StatusCode())
	}

	installResponse := &InstallBizResponse{}
	if err := json.Unmarshal(resp.Body(), installResponse); err != nil {
		return err
	}

	if installResponse.Code != "SUCCESS" {
		return fmt.Errorf("install biz failed: %s", installResponse.Message)
	}

	return nil
}

// Use kubectl exec to install biz in pod
// In this way, the implementation won't be overwhelmed with complicated 7 layers of k8s service
// The constraint is that user requires with CA or token to access k8s cluster exec.
// However, this is not a big problem, because this command is using in local DEV phase, not in production.
func (h *service) installBizInPod(_ context.Context, _ InstallBizRequest) error {
	panic("not implemented")
}

func (h *service) InstallBiz(ctx context.Context, req InstallBizRequest) (err error) {
	logger := contextutil.GetLogger(ctx)
	logger.WithField("req", string(runtime.Must(json.Marshal(req)))).Info("install biz started")
	defer func() {
		if err != nil {
			logger.Error(err)
		} else {
			logger.Info("install biz completed")
		}
	}()

	switch req.TargetContainer.RunType {
	case ArkContainerRunTypeLocal:
		err = h.installBizOnLocal(ctx, req)
	case ArkContainerRunTypeK8s:
		err = h.installBizInPod(ctx, req)
	default:
		err = fmt.Errorf("unknown run type: %s", req.TargetContainer.RunType)
	}
	return
}

// Use http client to uninstall biz on local
func (h *service) unInstallBizOnLocal(_ context.Context, req UnInstallBizRequest) error {
	resp, err := h.client.R().
		SetContext(context.Background()).
		SetBody(req.BizModel).
		Post(fmt.Sprintf("http://127.0.0.1:%d/uninstallBiz", req.TargetContainer.GetPort()))
	if err != nil {
		return err
	}

	if !resp.IsSuccess() {
		return fmt.Errorf("uninstall biz http failed with code %d", resp.StatusCode())
	}

	uninstallResponse := &UnInstallBizResponse{}
	if err := json.Unmarshal(resp.Body(), uninstallResponse); err != nil {
		return err
	}

	if uninstallResponse.Code == "FAILED" && uninstallResponse.Data.Code == "NOT_FOUND_BIZ" {
		return nil
	}

	if uninstallResponse.Code == "SUCCESS" {
		return nil
	}

	return fmt.Errorf("uninstall biz failed: %v", *uninstallResponse)
}

// Use kubectl exec to uninstall biz in pod
func (h *service) unInstallBizInPod(_ context.Context, _ UnInstallBizRequest) error {
	panic("not implemented")
}

func (h *service) UnInstallBiz(ctx context.Context, req UnInstallBizRequest) (err error) {
	logger := contextutil.GetLogger(ctx)
	logger.WithField("req", string(runtime.Must(json.Marshal(req)))).Info("uninstall biz started")
	defer func() {
		if err != nil {
			logger.Error(err)
		} else {
			logger.Info("uninstall biz completed")
		}
	}()

	switch req.TargetContainer.RunType {
	case ArkContainerRunTypeLocal:
		err = h.unInstallBizOnLocal(ctx, req)
	case ArkContainerRunTypeK8s:
		err = h.unInstallBizInPod(ctx, req)
	default:
		err = fmt.Errorf("unknown run type: %s", req.TargetContainer.RunType)
	}
	return
}

func (h *service) QueryAllBiz(ctx context.Context, req QueryAllArkBizRequest) (*QueryAllArkBizResponse, error) {
	logger := contextutil.GetLogger(ctx)
	logger.WithField("req", string(runtime.Must(json.Marshal(req)))).Info("query all biz started")

	resp, err := h.client.R().
		SetContext(context.Background()).
		SetBody(req).
		Post(fmt.Sprintf("http://%s:%d/queryAllBiz", req.HostName, req.Port))

	if err != nil {
		logger.Error(err)
		return nil, err
	}

	if !resp.IsSuccess() {
		err = fmt.Errorf("query all biz http failed with code %d", resp.StatusCode())
		logger.Error(err)
		return nil, err
	}

	queryAllBizResponse := &QueryAllArkBizResponse{}
	if err := json.Unmarshal(resp.Body(), queryAllBizResponse); err != nil {
		logger.Error(err)
		return nil, err
	}

	if queryAllBizResponse.Code != "SUCCESS" {
		err = fmt.Errorf("query all biz failed: %s", queryAllBizResponse.Message)
		logger.Error(err)
		return nil, err
	}

	logger.Info("query all biz completed")
	return queryAllBizResponse, nil
}
