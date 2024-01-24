/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package deploy

import (
	"context"
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"serverless.alipay.com/sofa-serverless/arkctl/common/osutil"
	"strings"

	"serverless.alipay.com/sofa-serverless/arkctl/common/cmdutil"
	"serverless.alipay.com/sofa-serverless/arkctl/common/contextutil"
	"serverless.alipay.com/sofa-serverless/arkctl/common/fileutil"
	"serverless.alipay.com/sofa-serverless/arkctl/common/runtime"
	"serverless.alipay.com/sofa-serverless/arkctl/common/style"
	"serverless.alipay.com/sofa-serverless/arkctl/v1/cmd/root"
	"serverless.alipay.com/sofa-serverless/arkctl/v1/service/ark"

	"github.com/google/uuid"
	"github.com/pterm/pterm"
	"github.com/spf13/cobra"
)

var (
	portFlag int

	subBundlePath string

	defaultArg string
	doBuild    bool

	podFlag      string
	podNamespace string // pre parsed pod namespace
	podName      string // pre parsed pod name
)

const (
	ctxKeyArkBizBundlePathInSidePod = "arkBizBundlePathInSidePod"
	ctxKeyArkService                = "ark.Service"
	ctxKeyBizModel                  = "ark.BizModel"
	ctxKeyArkContainerRuntimeInfo   = "ark.ContainerRuntimeInfo"
)

var DeployCommand = &cobra.Command{
	Use:   "deploy [flags] [path/to/your/project/or/bundle]",
	Short: "deploy your biz module to running containers",
	Long: `
The arkctl deploy subcommand can help you quickly deploy your biz module to running ark container.
We advice you to use this in local dev phase.
`,
	Example: `
Scenario 0: Build bundle at current workspace and deploy it to a local running ark container with default port:
    arkctl deploy

Scenario 1: Build bundle at given path and deploy it to local running ark container with given port:
	arkctl deploy --port ${your ark container portFlag} ${path/to/your/project}

Scenario 2: Deploy a local pre-built bundle to local running ark container:
	arkctl deploy ${path/to/your/pre/built/bundle.jar}

Scenario 3: Build and deploy a bundle at current dir to a remote running ark container in k8s cluster with default port:
	arkctl deploy --pod ${namespace}/${name}

Scenario 4: Build an maven multi module project and deploy a sub module to a running ark container:
	arkctl deploy --sub ${path/to/your/sub/module}
`,
	Args: func(cmd *cobra.Command, args []string) error {
		if len(args) == 0 {
			defaultArg = runtime.Must(os.Getwd())
		} else {
			defaultArg = args[len(args)-1]
			if !filepath.IsAbs(defaultArg) {
				defaultArg = filepath.Join(runtime.Must(os.Getwd()), defaultArg)
			}
		}
		doBuild = !strings.HasSuffix(defaultArg, ".jar")

		if podFlag != "" && strings.Contains(podFlag, "/") {
			podNamespace, podName = strings.Split(podFlag, "/")[0], strings.Split(podFlag, "/")[1]
		} else {
			podNamespace, podName = "default", podFlag
		}

		return nil
	},
	Run: executeDeploy,
}

func execMavenBuild(ctx *contextutil.Context) bool {
	if !doBuild {
		return true
	}

	style.InfoPrefix("Stage").Println("BuildBundle")
	style.InfoPrefix("BuildDirectory").Println(defaultArg)

	mvn := cmdutil.BuildCommandWithWorkDir(
		ctx,
		defaultArg,
		"mvn",
		"clean", "package", "-Dmaven.test.skip=true")
	style.InfoPrefix("Command").Println(mvn.String())

	if err := mvn.Exec(); err != nil {
		pterm.Error.PrintOnErrorf("Build bundle failed: %s!", err)
		return false
	}

	go func() {
		for line := range mvn.Output() {
			pterm.Println(line)
		}
	}()

	if err := <-mvn.Wait(); err != nil {
		pterm.Error.PrintOnErrorf("Build bundle failed: %s!", err)
		return false
	}

	if err := mvn.GetExitError(); err != nil {
		pterm.Error.PrintOnErrorf("Build bundle failed: %s!", err)
		return false
	}

	pterm.Info.Printfln(pterm.Green("build bundle success!"))
	pterm.Println()
	return true
}

func execParseBizModel(ctx *contextutil.Context) bool {
	style.InfoPrefix("Stage").Println("ParseBizModel")
	bundlePath := osutil.GetLocalFileProtocol() + defaultArg
	if doBuild {
		searchdir := defaultArg
		if subBundlePath != "" {
			searchdir = filepath.Join(searchdir, subBundlePath)
		}

		filepath.Walk(searchdir, func(path string, info os.FileInfo, err error) error {
			if !info.IsDir() && strings.HasSuffix(info.Name(), "-ark-biz.jar") {
				bundlePath = path
			}
			return nil
		})

		if !strings.HasSuffix(bundlePath, "-ark-biz.jar") {
			pterm.Error.Println("can not find pre built biz bundle in build dir!")
			return false
		}
		bundlePath = osutil.GetLocalFileProtocol() + bundlePath
	}

	bizModel, err := ark.ParseBizModel(ctx, fileutil.FileUrl(bundlePath))
	if err != nil {
		pterm.Error.PrintOnError(fmt.Errorf("failed to parse bundle: %s", err))
		return false
	}

	ctx.Put(ctxKeyBizModel, bizModel)
	style.InfoPrefix("BizBundleInfo").Println(string(runtime.Must(json.Marshal(*bizModel))))
	pterm.Info.Println(pterm.Green("parse biz bundle success!"))
	pterm.Println()

	return true
}

func execUploadBizBundle(ctx *contextutil.Context) bool {
	bizModel := ctx.Value(ctxKeyBizModel).(*ark.BizModel)

	if podFlag != "" && bizModel.BizUrl.GetFileUrlType() == fileutil.FileUrlTypeLocal {

		targetPath := fmt.Sprintf("/tmp/%s",
			bizModel.BizName+"-"+
				bizModel.BizVersion+"-"+
				runtime.Must(uuid.NewUUID()).String()+"-"+
				"ark-biz.jar",
		)
		kubecpcmd := cmdutil.BuildCommand(ctx,
			"kubectl",
			"-n",
			podNamespace,
			"cp",
			string(bizModel.BizUrl)[7:],
			podName+":"+targetPath,
		)
		style.InfoPrefix("Stage").Println("UploadBizBundle")
		style.InfoPrefix("Command").Println(kubecpcmd.String())

		if err := kubecpcmd.Exec(); err != nil {
			pterm.Error.PrintOnError(err)
			return false
		}

		go func() {
			for line := range kubecpcmd.Output() {
				pterm.DefaultLogger.Print(line)
			}
		}()

		if err := <-kubecpcmd.Wait(); err != nil {
			pterm.Error.PrintOnError(err)
			return false
		}
		ctx.Put(ctxKeyArkBizBundlePathInSidePod, targetPath)
		pterm.Info.Println(pterm.LightGreen("upload biz bundle to pod success!"))
		pterm.Println()

	}
	return true
}

func execInstallInKubePod(ctx *contextutil.Context) bool {
	bizModel := ctx.Value(ctxKeyBizModel).(*ark.BizModel)
	kubeuninstallcmd := cmdutil.BuildCommand(ctx,
		"kubectl",
		"-n", podNamespace,
		"exec", podName, "--",
		"curl",
		"-X",
		"POST",
		"-H",
		"'Content-Type: application/json'",
		"-d",
		string(runtime.Must(json.Marshal(ark.BizModel{
			BizName:    bizModel.BizName,
			BizVersion: bizModel.BizVersion,
		}))),
		fmt.Sprintf("http://127.0.0.1:%v/uninstallBiz", portFlag),
	)

	style.InfoPrefix("Command").Println(kubeuninstallcmd.String())
	if err := kubeuninstallcmd.Exec(); err != nil {
		pterm.Error.PrintOnError(err)
		return false
	}

	// somehow kubectl exec would pipe the pod's realtime output to stderror pipeline
	// so we check command exit state directly, instead of judging by the stderror pipeline output
	if stderrpipe := <-kubeuninstallcmd.Wait(); stderrpipe != nil {
		realtimeoutputlines := stderrpipe.Error()
		stdoutlines := &strings.Builder{}
		for line := range kubeuninstallcmd.Output() {
			stdoutlines.WriteString(line)
		}

		if err := kubeuninstallcmd.GetExitError(); err != nil {
			pterm.Println(realtimeoutputlines)
			pterm.Println(stdoutlines)
			pterm.Error.PrintOnError(err)
			return false
		}

		if strings.Contains(stdoutlines.String(), "\"code\":\"FAILED\"") &&
			!strings.Contains(stdoutlines.String(), "\"code\":\"NOT_FOUND_BIZ\"") {
			pterm.Println(realtimeoutputlines)
			pterm.Println(stdoutlines)
			pterm.Error.Println("uninstall biz failed!")
			return false
		}
	}

	kubeinstallcmd := cmdutil.BuildCommand(ctx,
		"kubectl",
		"-n", podNamespace,
		"exec", podName, "--",
		"curl",
		"-X",
		"POST",
		"-H",
		"'Content-Type: application/json'",
		"-d",
		string(runtime.Must(json.Marshal(ark.BizModel{
			BizName:    bizModel.BizName,
			BizVersion: bizModel.BizVersion,
			BizUrl:     fileutil.FileUrl(osutil.GetLocalFileProtocol() + ctx.Value(ctxKeyArkBizBundlePathInSidePod).(string)),
		}))),
		fmt.Sprintf("http://127.0.0.1:%v/installBiz", portFlag),
	)
	style.InfoPrefix("Command").Println(kubeinstallcmd.String())
	if err := kubeinstallcmd.Exec(); err != nil {
		pterm.Error.PrintOnError(err)
		return false
	}

	// somehow kubectl exec would pipe the pod's realtime output to stderror pipeline
	// so we check command exit state directly, instead of judging by the stderror pipeline output
	if stderrpipe := <-kubeinstallcmd.Wait(); stderrpipe != nil {
		realtimeoutputlines := stderrpipe.Error()
		stdoutlines := &strings.Builder{}
		for line := range kubeinstallcmd.Output() {
			stdoutlines.WriteString(line)
		}

		if err := kubeinstallcmd.GetExitError(); err != nil {
			pterm.Println(realtimeoutputlines)
			pterm.Println(stdoutlines)
			pterm.Error.PrintOnError(err)
			return false
		}

		if strings.Contains(stdoutlines.String(), "\"code\":\"FAILED\"") {
			pterm.Println(realtimeoutputlines)
			pterm.Println(stdoutlines)
			pterm.Error.Println("install biz failed!")
		}
		pterm.Println(stdoutlines)
	}
	pterm.Println()

	return true
}

// install the given package in target ark container
func execInstallInLocal(ctx *contextutil.Context) bool {
	return execUnInstallLocal(ctx) && execInstallLocal(ctx)
}

// install the given package in target ark container
func execInstall(ctx *contextutil.Context) (result bool) {
	style.InfoPrefix("Stage").Println("Install")

	switch {
	case podFlag != "":
		result = execInstallInKubePod(ctx)
	default:
		result = execInstallInLocal(ctx)
	}

	if result {
		pterm.Info.Println(pterm.Green("install biz success!"))
		pterm.Println()
	}

	return

}

// uninstall the given package in target ark container
func execUnInstallLocal(ctx *contextutil.Context) bool {
	var (
		arkService              = ctx.Value(ctxKeyArkService).(ark.Service)
		bizModel                = ctx.Value(ctxKeyBizModel).(*ark.BizModel)
		arkContainerRuntimeInfo = ctx.Value(ctxKeyArkContainerRuntimeInfo).(*ark.ArkContainerRuntimeInfo)
	)
	if err := arkService.UnInstallBiz(ctx, ark.UnInstallBizRequest{
		BizModel:        *bizModel,
		TargetContainer: *arkContainerRuntimeInfo,
	}); err != nil {
		pterm.Error.PrintOnError(err)
		return false
	}

	return true
}

// install the given package in target ark container
func execInstallLocal(ctx *contextutil.Context) bool {
	var (
		arkService              = ctx.Value(ctxKeyArkService).(ark.Service)
		bizModel                = ctx.Value(ctxKeyBizModel).(*ark.BizModel)
		arkContainerRuntimeInfo = ctx.Value(ctxKeyArkContainerRuntimeInfo).(*ark.ArkContainerRuntimeInfo)
	)

	if err := arkService.InstallBiz(ctx, ark.InstallBizRequest{
		BizModel:        *bizModel,
		TargetContainer: *arkContainerRuntimeInfo,
	}); err != nil {
		pterm.Error.PrintOnError(err)
		return false
	}
	return true
}

func generateContext(cmd *cobra.Command) *contextutil.Context {
	ctx := contextutil.NewContext(context.Background())

	arkService := ark.BuildService(ctx)
	ctx.Put(ctxKeyArkService, arkService)

	arkContainerRuntimeInfo := &ark.ArkContainerRuntimeInfo{
		RunType: ark.ArkContainerRunTypeLocal,
		Port:    &portFlag,
	}

	// target is running inside of kubernetes
	if podFlag != "" {
		arkContainerRuntimeInfo.RunType = ark.ArkContainerRunTypeK8s
		arkContainerRuntimeInfo.Coordinate = podFlag
	}
	ctx.Put(ctxKeyArkContainerRuntimeInfo, arkContainerRuntimeInfo)

	return ctx
}

// executeDeploy will execute the deploy command
// 1. build the biz bundle
// 2. parse the biz model for further usage
// 3. uninstall the biz bundle in target ark container to prevent conflict
// 4. install the biz bundle in target ark container
func executeDeploy(cobracmd *cobra.Command, _ []string) {
	c := generateContext(cobracmd)

	todos := []func(context2 *contextutil.Context) bool{
		execMavenBuild,
		execParseBizModel,
		execUploadBizBundle,
		execInstall,
	}

	for _, todo := range todos {
		if !todo(c) {
			return
		}
	}

}

func init() {
	root.RootCmd.AddCommand(DeployCommand)

	DeployCommand.Flags().StringVar(&podFlag, "pod", "", `
If Provided, arkctl will try to deploy the bundle to the ark container running in given pod.
`)
	DeployCommand.Flags().StringVar(&subBundlePath, "sub", "", `
If Provided, arkctl will try to build the project at current dir and deploy the bundle at subBundlePath.
`)

	DeployCommand.Flags().IntVar(&portFlag, "port", 1238, `
The default port of ark container is 1238 if not provided.
`)

}
