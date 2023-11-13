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

package status

import (
	"context"
	"encoding/json"
	"fmt"
	"serverless.alipay.com/sofa-serverless/arkctl/common/style"
	"strings"

	"serverless.alipay.com/sofa-serverless/arkctl/common/cmdutil"
	"serverless.alipay.com/sofa-serverless/arkctl/common/runtime"
	"serverless.alipay.com/sofa-serverless/arkctl/v1/cmd/root"
	"serverless.alipay.com/sofa-serverless/arkctl/v1/service/ark"

	"github.com/pterm/pterm"
	"github.com/spf13/cobra"
)

var (
	portFlag int    = 1238
	hostFlag string = "127.0.0.1"

	podFlag      string = ""
	podNamespace string = ""
	podName      string = ""
)

var (
	StatusCommand = cobra.Command{
		Use: "status",
		RunE: func(cmd *cobra.Command, args []string) error {
			if podFlag != "" && strings.Contains(podFlag, "/") {
				podNamespace, podName = strings.Split(podFlag, "/")[0], strings.Split(podFlag, "/")[1]
			} else {
				podNamespace, podName = "default", podFlag
			}

			return execStatus(context.Background())
		},
	}
)

func execStatusLocal(ctx context.Context) error {
	arkService := ark.BuildService(ctx)
	biz, err := arkService.QueryAllBiz(ctx, ark.QueryAllArkBizRequest{
		HostName: hostFlag,
		Port:     portFlag,
	})
	if err != nil {
		return err
	}
	style.InfoPrefix("QueryAllBiz").Println(string(runtime.Must(json.Marshal(*biz))))
	return nil
}

func execStatusKubePod(ctx context.Context) error {
	kubeQueryCmd := cmdutil.BuildCommand(
		ctx,
		"kubectl",
		"-n", podNamespace,
		"exec", podName, "--",
		"curl",
		"-X",
		"POST",
		fmt.Sprintf("http://127.0.0.1:%v/queryAllBiz", portFlag),
	)

	if err := kubeQueryCmd.Exec(); err != nil {
		pterm.Error.PrintOnError(err)
		return err
	}

	if stderroutput := <-kubeQueryCmd.Wait(); stderroutput != nil {
		stderrlines := stderroutput.Error()
		stdoutlines := &strings.Builder{}
		for line := range kubeQueryCmd.Output() {
			stdoutlines.WriteString(line)
		}

		if !strings.Contains(stdoutlines.String(), "SUCCESS") {
			pterm.Println(stderrlines)
			pterm.Println(stdoutlines)
			pterm.Error.Println("query all biz failed")
			return fmt.Errorf("query all biz failed")
		}
		style.InfoPrefix("QueryAllBiz").Println(stdoutlines)
	}
	return nil
}

func execStatus(ctx context.Context) error {
	switch {
	case podFlag != "":
		return execStatusKubePod(ctx)
	default:
		return execStatusLocal(ctx)
	}
}

func init() {
	root.RootCmd.AddCommand(&StatusCommand)
	StatusCommand.Flags().IntVar(&portFlag, "port", portFlag, "ark container's port")
	StatusCommand.Flags().StringVar(&hostFlag, "host", hostFlag, "ark container's host")
	StatusCommand.Flags().StringVar(&podFlag, "pod", podFlag, "ark container's running pod")
}
