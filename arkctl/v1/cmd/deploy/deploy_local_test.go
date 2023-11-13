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

package deploy

import (
	"context"
	"serverless.alipay.com/sofa-serverless/arkctl/common/contextutil"
	"serverless.alipay.com/sofa-serverless/arkctl/v1/service/ark"
	"testing"
)

func TestExecKubectlUpload(t *testing.T) {
	ctx := contextutil.NewContext(context.Background())
	podFlag = "arkbasecontainer/arkbasecontainer-deploy-66458b676c-4p86l"
	podNamespace = "arkbasecontainer"
	podName = "arkbasecontainer-deploy-66458b676c-4p86l"
	ctx.Put(ctxKeyBizModel, &ark.BizModel{
		BizName:    "biz1",
		BizVersion: "0.0.1-SNAPSHOT",
		BizUrl:     "file:///Users/fengyin/workspace/github/sofa-serverless/samples/springboot-samples/web/tomcat/biz1/target/biz1-web-single-host-0.0.1-SNAPSHOT-ark-biz.jar",
	})

	execUploadBizBundle(ctx)
	execInstallInKubePod(ctx)
}
