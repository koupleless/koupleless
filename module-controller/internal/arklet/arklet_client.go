package arklet

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"net/http/httptest"
	"strconv"
	"sync"

	"k8s.io/utils/env"
	"sigs.k8s.io/controller-runtime/pkg/log"

	"github.com/sofastack/sofa-serverless/api/v1alpha1"
)

const (
	DefaultPort          = 1238
	DefaultInstallPath   = "installBiz"
	DefaultUninstallPath = "uninstallBiz"
	DefaultSwitchPath    = "switchBiz"
)

// ArkletClient support install / uninstall /switch biz
type ArkletClient struct {
	installPath string

	uninstallPath string

	switchPath string

	port int
}

type ArkletResponseCode string

const (
	Success ArkletResponseCode = "SUCCESS"
	Failed  ArkletResponseCode = "FAILED"
)

type ArkletResponse struct {
	// TODO 根据 arklet response 定义再调整
	Code ArkletResponseCode

	Message string
}

var instance *ArkletClient
var installPath, uninstallPath, switchPath string
var port int
var mockUrl string

var once sync.Once

func init() {
	installPath = DefaultInstallPath
	uninstallPath = DefaultUninstallPath
	switchPath = DefaultSwitchPath
	port = DefaultPort

	testEnv, _ := env.GetBool("test", false)
	if testEnv {
		MockClient()
	}
}

func MockClient() {
	svr := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		data := ArkletResponse{
			Code: Success,
		}
		w.Header().Set("Content-Type", "application/json")
		json.NewEncoder(w).Encode(data)
	}))
	mockUrl = svr.URL
}

func Client() *ArkletClient {
	if instance == nil {
		once.Do(func() {
			instance = &ArkletClient{installPath: installPath, uninstallPath: uninstallPath, switchPath: switchPath, port: port}
		})
	}
	return instance
}

func (client *ArkletClient) InstallBiz(ip string, moduleInfo v1alpha1.ModuleInfo) (*ArkletResponse, error) {
	log.Log.Info("start to install module", "ip", ip, "moduleInfo", moduleInfo)
	var url string
	if mockUrl != "" {
		url = mockUrl
	} else {
		url = fmt.Sprintf("http://%s:%s/%s", ip, strconv.Itoa(client.port), client.installPath)
	}
	data := []byte(fmt.Sprintf(`{"bizName": "%s", "bizVersion": "%s", "bizUrl": "%s"}`, moduleInfo.Name, moduleInfo.Version, moduleInfo.Url))
	req, err := http.NewRequest("POST", url, bytes.NewBuffer(data))
	if err != nil {
		fmt.Println("Error creating install HTTP request:", err)
		return nil, err
	}
	req.Header.Set("Content-Type", "application/json")

	httpClient := &http.Client{}
	resp, err := httpClient.Do(req)
	if err != nil {
		fmt.Println("Error sending install HTTP request:", err)
		return nil, err
	}
	defer resp.Body.Close()
	body, err := io.ReadAll(resp.Body)
	if err != nil {
		fmt.Println("Error reading install response body:", err)
		return nil, err
	}
	var result ArkletResponse
	err = json.Unmarshal(body, &result)
	log.Log.Info("install biz success", "result", result, "moduleName", moduleInfo.Name, "url", url, "body", body)
	return &result, nil
}

func (client *ArkletClient) UninstallBiz(ip string, moduleName string, moduleVersion string) (*ArkletResponse, error) {
	log.Log.Info("start to uninstall module", "ip", ip, "moduleName", moduleName, "moduleVersion", moduleVersion)
	var url string
	if mockUrl != "" {
		url = mockUrl
	} else {
		url = fmt.Sprintf("http://%s:%s/%s", ip, strconv.Itoa(client.port), client.uninstallPath)
	}

	data := []byte(fmt.Sprintf(`{"bizName": "%s", "bizVersion": "%s"}`, moduleName, moduleVersion))
	req, err := http.NewRequest("POST", url, bytes.NewBuffer(data))
	if err != nil {
		fmt.Println("Error creating uninstall HTTP request:", err)
		return nil, err
	}
	req.Header.Set("Content-Type", "application/json")

	httpClient := &http.Client{}
	resp, err := httpClient.Do(req)
	if err != nil {
		fmt.Println("Error sending uninstall HTTP request:", err)
		return nil, err
	}
	defer resp.Body.Close()
	body, err := io.ReadAll(resp.Body)
	if err != nil {
		fmt.Println("Error reading uninstall response body:", err)
		return nil, err
	}
	var result ArkletResponse
	err = json.Unmarshal(body, &result)
	log.Log.Info("uninstall biz success", "result", result, "moduleName", moduleName, "moduleVersion", moduleVersion, "url", url, "body", body)
	return &result, nil
}
