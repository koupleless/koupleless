package ark

import (
	"context"
	"encoding/json"
	"fmt"

	"serverless.alipay.com/sofa-serverless/arkctl/common/contextutil"

	"github.com/go-resty/resty/v2"
)

// Service is responsible for interacting with ark container.
type Service interface {
	// InstallBiz call the remote ark container to install biz.
	// The precondition is that the biz file is already uploaded to the ark container or file hosting service (e.g. oss).
	InstallBiz(ctx context.Context, req InstallBizRequest) (*InstallBizResponse, error)
}

// BuildClient return a new Service.
func BuildClient(_ context.Context) Service {
	return &service{
		client: resty.New(),
	}
}

var (
	_ Service = &service{}
)

type service struct {
	client *resty.Client
}

// Use http client to install biz on local
// The implementation is simple, just copy file to local dir.
func (h *service) installBizOnLocal(_ context.Context, req InstallBizRequest) (*InstallBizResponse, error) {
	resp, err := h.client.R().
		SetBody(req.BizModel).
		Post(fmt.Sprintf("http://127.0.0.1:%d/installBiz", req.TargetContainer.GetPort()))

	if err != nil {
		return nil, err
	}

	if !resp.IsSuccess() {
		return nil, fmt.Errorf("install biz http failed with code %d", resp.StatusCode())
	}

	installResponse := &InstallBizResponse{}
	if err := json.Unmarshal(resp.Body(), installResponse); err != nil {
		return nil, err
	}

	if installResponse.Code != "SUCCESS" {
		return nil, fmt.Errorf("install biz failed: %s", installResponse.Message)
	}

	return installResponse, nil
}

// Use kubectl exec to install biz in pod
// In this way, the implementation won't be overwhelmed with complicated 7 layers of k8s service
// The constraint is that user requires with CA or token to access k8s cluster exec.
// However, this is not a big problem, because this command is using in local DEV phase, not in production.
func (h *service) installBizInPod(_ context.Context, _ InstallBizRequest) (*InstallBizResponse, error) {
	panic("not implemented")
}

func (h *service) InstallBiz(ctx context.Context, req InstallBizRequest) (resp *InstallBizResponse, err error) {
	logger := contextutil.GetLogger(ctx)
	logger.WithField("req", req).Info()
	defer func() {
		if err != nil {
			logger.Error(err)
		} else {
			logger.WithField("resp", resp).Info()
		}
	}()

	switch req.TargetContainer.RunType {
	case ArkContainerRunTypeLocal:
		resp, err = h.installBizOnLocal(ctx, req)
	case ArkContainerRunTypeK8s:
		resp, err = h.installBizInPod(ctx, req)
	default:
		err = fmt.Errorf("unknown run type: %s", req.TargetContainer.RunType)
	}
	return
}
