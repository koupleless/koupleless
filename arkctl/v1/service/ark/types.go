package ark

import "serverless.alipay.com/sofa-serverless/arkctl/common/fileutil"

type ArkContainerRunType string

const (
	ArkContainerRunTypeLocal ArkContainerRunType = "local"
	ArkContainerRunTypeVM    ArkContainerRunType = "vm" // the reason why we need vm is we might use scp to copy file to vm server
	ArkContainerRunTypeK8s   ArkContainerRunType = "pod"
)

// ArkContainerRuntimeInfo contains necessary info of an ark container.
type ArkContainerRuntimeInfo struct {
	// RunType is the type of ark container, like local, vm server, pod, etc.
	RunType ArkContainerRunType `json:"runType"`

	// Coordinate is the exact location of ark container.
	// If the RunType is local, then it's localhost.
	// If the RunType is vm server, then it's ip.
	// If the RunType is pod, then it's the {namespace}/{podName}
	Coordinate string `json:"coordinate"`

	// Port is the ark api port of ark container.
	Port *int `json:"port"`
}

func (info *ArkContainerRuntimeInfo) GetPort() int {
	if info.Port == nil {
		// default port
		return 1238
	}
	return *info.Port
}

// BizModel contains necessary metadata info of an ark biz.
// Usually this BizModel is generated automatically from bundle like jarfile.
type BizModel struct {
	// BizName the biz name
	BizName string `json:"bizName"`

	// BizVersion is the version of biz module.
	BizVersion string `json:"bizVersion"`

	// BizUrl is the location of source code.
	BizUrl fileutil.FileUrl `json:"bizUrl"`
}

// InstallBizRequest is the request for installing biz module to ark container.
type InstallBizRequest struct {
	// BizModel is the metadata a given biz module.
	BizModel BizModel `json:"bizModel"`

	// TargetContainer is the target ark container we want to install a biz module to.
	TargetContainer ArkContainerRuntimeInfo `json:"targetContainer"`

	// InstallType is the type of install.
	// If the InstallType is "filesystem", then it will install the local biz module.
	// If the InstallType is "http", then it will install the remote biz module.
	InstallType string `json:"installType"`

	// BizHomeDir is the location of all biz module.
	// If not given, we will use {tmp}/arkBiz/ dir instead.
	// This will only be used when install biz module from local filesystem.
	BizHomeDir *string `json:"bizHomeDir"`
}

// InstallBizResponse is the response for installing biz module to ark container.
type InstallBizResponse struct {
	// Success is true if install biz successfully.
	Code string `json:"code"`

	// Msg is the error message if install biz failed.
	Data struct {
		Code         string        `json:"code"`
		Message      string        `json:"message"`
		ElapsedSpace int           `json:"elapsedSpace"`
		BizInfos     []interface{} `json:"bizInfos"`
	}

	Message string `json:"message"`
}
