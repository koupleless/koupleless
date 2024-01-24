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
	"archive/zip"
	"context"
	"fmt"
	"serverless.alipay.com/sofa-serverless/arkctl/common/osutil"
	"strings"

	"serverless.alipay.com/sofa-serverless/arkctl/common/fileutil"
)

// isJarFile return true if fileUrl provides a jar file.
func isJarFile(fileUrl fileutil.FileUrl) bool {
	// end with .jar
	return strings.HasSuffix(string(fileUrl), ".jar")
}

// parseJarBizModel parse jar file to BizModel.
func parseJarBizModel(ctx context.Context, bizUrl fileutil.FileUrl) (*BizModel, error) {
	fileUtil := fileutil.DefaultFileUtil()
	localPath, err := fileUtil.Download(ctx, bizUrl)
	if err != nil {
		return nil, err
	}

	zipReader, err := zip.OpenReader(localPath[len(osutil.GetLocalFileProtocol()):])
	if err != nil {
		return nil, err
	}
	defer zipReader.Close()

	bizName := ""
	bizVersion := ""

	for _, fileInfo := range zipReader.File {
		if fileInfo.Name == "META-INF/MANIFEST.MF" {
			// open this file
			file, err := fileInfo.Open()
			if err != nil {
				return nil, err
			}
			// read all to string
			buf := make([]byte, fileInfo.UncompressedSize64)
			_, err = file.Read(buf)
			if err != nil && err.Error() != "EOF" {
				return nil, err
			}
			// find bizName and bizVersion
			for _, line := range strings.Split(string(buf), "\n") {
				// if line contains "Ark-Biz-Name:" then it's bizName
				if strings.Contains(line, "Ark-Biz-Name:") {
					bizName = strings.TrimSpace(strings.Split(line, ":")[1])
				}

				// if line contains "Ark-Biz-Version:" then it's bizVersion
				if strings.Contains(line, "Ark-Biz-Version:") {
					bizVersion = strings.TrimSpace(strings.Split(line, ":")[1])
				}
			}
			break
		}
	}

	return &BizModel{
		BizName:    bizName,
		BizVersion: bizVersion,
		BizUrl:     bizUrl,
	}, nil
}

// ParseBizModel parse biz bundle given by bizUrl to BizModel.
func ParseBizModel(ctx context.Context, bizUrl fileutil.FileUrl) (*BizModel, error) {
	switch {
	case isJarFile(bizUrl):
		return parseJarBizModel(ctx, bizUrl)
	default:
		return nil, fmt.Errorf("unknown biz bundle type %s", bizUrl)
	}
}
