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

	"github.com/go-resty/resty/v2"
	"github.com/koupleless/arkctl/common/contextutil"
	"github.com/koupleless/arkctl/common/fileutil"
	"github.com/koupleless/arkctl/common/runtime"
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

func (h *service) InstallBiz(ctx context.Context, req InstallBizRequest) (err error) {
	logger := contextutil.GetLogger(ctx)
	logger.WithField("req", string(runtime.MustReturnResult(json.Marshal(req)))).Info("install biz started")
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
	default:
		err = fmt.Errorf("unknown run type: %s", req.TargetContainer.RunType)
	}
	return
}

// Use http client to uninstall biz on local
func (h *service) unInstallBizOnLocal(_ context.Context, req UnInstallBizRequest) (err error) {
	defer runtime.RecoverFromError(&err)()

	resp := runtime.MustReturnResult(h.client.R().
		SetContext(context.Background()).
		SetBody(req.BizModel).
		Post(fmt.Sprintf("http://127.0.0.1:%d/uninstallBiz", req.TargetContainer.GetPort())))

	runtime.Assert(resp.IsSuccess(), "uninstall biz http failed with code %d", resp.StatusCode())
	uninstallResponse := &UnInstallBizResponse{}
	runtime.Must(json.Unmarshal(resp.Body(), uninstallResponse))

	isBizNotFound := uninstallResponse.Code == "FAILED" && uninstallResponse.Data.Code == "NOT_FOUND_BIZ"
	isInstallSuccess := uninstallResponse.Code == "SUCCESS"
	runtime.Assert(isBizNotFound || isInstallSuccess, "uninstall biz failed: %v", *uninstallResponse)
	return
}

func (h *service) UnInstallBiz(ctx context.Context, req UnInstallBizRequest) (err error) {
	logger := contextutil.GetLogger(ctx)
	logger.WithField("req", string(runtime.MustReturnResult(json.Marshal(req)))).Info("uninstall biz started")
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
	default:
		err = fmt.Errorf("unknown run type: %s", req.TargetContainer.RunType)
	}
	return
}

func (h *service) QueryAllBiz(ctx context.Context, req QueryAllArkBizRequest) (resp *QueryAllArkBizResponse, err error) {
	logger := contextutil.GetLogger(ctx)
	logger.WithField("req", string(runtime.MustReturnResult(json.Marshal(req)))).Info("query all biz started")
	defer runtime.RecoverFromErrorWithHandler(func(recover error) {
		err = recover
		logger.Error(err)
	})()

	httpResp := runtime.MustReturnResult(h.client.R().
		SetContext(context.Background()).
		SetBody(req).
		Post(fmt.Sprintf("http://%s:%d/queryAllBiz", req.HostName, req.Port)))
	queryAllBizResponse := &QueryAllArkBizResponse{}

	runtime.Assert(httpResp.IsSuccess(), "query all biz http failed with code %d", httpResp.StatusCode())
	runtime.Must(json.Unmarshal(httpResp.Body(), queryAllBizResponse))
	runtime.Must(IsSuccessResponse(&queryAllBizResponse.GenericArkResponseBase))
	logger.Info("query all biz completed")

	return queryAllBizResponse, nil
}

// IsSuccessResponse checks if the response is successful
func IsSuccessResponse[T any](resp *GenericArkResponseBase[T]) error {
	if resp.Code == "SUCCESS" {
		return nil
	}
	return fmt.Errorf("sofa-ark failed response: %s", resp.Message)
}
