package ark

import (
	"context"
	"fmt"
	"testing"

	"serverless.alipay.com/sofa-serverless/arkctl/common/fileutil"
)

func TestInstallBiz(t *testing.T) {
	ctx := context.Background()
	client := BuildClient(ctx)
	bizModel, err := ParseBizModel(
		ctx,
		fileutil.FileUrl("file:///Users/fengyin/workspace/java/sofa-serverless/samples/springboot-samples/web/tomcat/biz1/target/biz1-web-single-host-0.0.1-SNAPSHOT-ark-biz.jar"),
	)
	if err != nil {
		panic(err)
	}

	resp, err := client.InstallBiz(ctx, InstallBizRequest{
		BizModel:    *bizModel,
		InstallType: "filesystem",
		TargetContainer: ArkContainerRuntimeInfo{
			RunType: ArkContainerRunTypeLocal,
		},
	})

	if err != nil {
		panic(err)
	}

	fmt.Println(resp)
}
