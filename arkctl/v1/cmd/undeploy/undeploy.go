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

package undeploy

import (
	"context"
	"fmt"
	"strings"

	"github.com/koupleless/arkctl/common/contextutil"
	"github.com/koupleless/arkctl/common/style"
	"github.com/koupleless/arkctl/v1/cmd/root"
	"github.com/koupleless/arkctl/v1/service/ark"
	"github.com/manifoldco/promptui"
	"github.com/pterm/pterm"
	"github.com/spf13/cobra"
)

var (
	hostFlag          string = "127.0.0.1"
	portFlag          int
	bizNameAndVersion string // in the format of bizName:bizVersion
)

var (
	ctxKeyArkService = "ctxKeyArkService"
)

// UnDeployCmd is the command to uninstall arkctl
var UnDeployCmd = &cobra.Command{
	Use:   "undeploy [bizName:bizVersion]",
	Short: "this command can help you uninstall biz in ark container",
	Args: func(cmd *cobra.Command, args []string) error {
		if len(args) != 0 && strings.Contains(args[len(args)-1], ":") {
			bizNameAndVersion = args[len(args)-1]
		}
		return nil
	},

	RunE: unInstall,
}

func generateContext() *contextutil.Context {
	ctx := contextutil.NewContext(context.Background())
	arkService := ark.BuildService(ctx)
	ctx.Put(ctxKeyArkService, arkService)
	return ctx
}

func unInstall(_ *cobra.Command, _ []string) error {
	ctx := generateContext()
	if bizNameAndVersion == "" {
		return execUnInstallLocalWithPrompt(ctx)
	}
	return execUnInstallLocal(ctx)
}

func execUnInstallLocal(ctx *contextutil.Context) error {
	arkService := ctx.Value(ctxKeyArkService).(ark.Service)
	style.InfoPrefix("UnInstallBiz").Println(bizNameAndVersion)
	if err := arkService.UnInstallBiz(ctx, ark.UnInstallBizRequest{
		TargetContainer: ark.ArkContainerRuntimeInfo{
			RunType: ark.ArkContainerRunTypeLocal,
			Port:    &portFlag,
		},
		BizModel: ark.BizModel{
			BizName:    strings.Split(bizNameAndVersion, ":")[0],
			BizVersion: strings.Split(bizNameAndVersion, ":")[1],
		},
	}); err != nil {
		pterm.Error.Printfln("uninstall %s failed: %s", bizNameAndVersion, err)
		return err
	}
	pterm.Info.Printfln(pterm.Green(fmt.Sprintf("uninstall %s success", bizNameAndVersion)))
	return nil
}

func execUnInstallLocalWithPrompt(ctx *contextutil.Context) error {
	arkService := ctx.Value(ctxKeyArkService).(ark.Service)

	response, err := arkService.QueryAllBiz(ctx, ark.QueryAllArkBizRequest{
		HostName: hostFlag,
		Port:     portFlag,
	})

	if err != nil {
		return err
	}

	if err := ark.IsSuccessResponse(&response.GenericArkResponseBase); err != nil {
		return err
	}

	arkbizInfos := response.Data
	arkbizInfosStr := make([]string, 0, len(arkbizInfos))
	for _, biz := range arkbizInfos {
		arkbizInfosStr = append(arkbizInfosStr, biz.BizName+":"+biz.BizVersion+"|"+biz.BizState)
	}

	p := &promptui.Select{
		Label: "InstalledBiz",
		Items: arkbizInfosStr,
	}
	idx, _, err := p.Run()
	if err != nil {
		return err
	}

	bizNameAndVersion = arkbizInfos[idx].BizName + ":" + arkbizInfos[idx].BizVersion
	return execUnInstallLocal(ctx)
}

func init() {
	UnDeployCmd.Flags().IntVar(&portFlag, "port", 1238, "the port of ark container")

	root.RootCmd.AddCommand(UnDeployCmd)
}
