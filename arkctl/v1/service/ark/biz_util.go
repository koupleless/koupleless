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
	"github.com/koupleless/arkctl/common/osutil"
	"github.com/koupleless/arkctl/common/runtime"
	"io"
	"strings"

	"github.com/koupleless/arkctl/common/fileutil"
)

// isJarFile return true if fileUrl provides a jar file.
func isJarFile(fileUrl fileutil.FileUrl) bool {
	// end with .jar
	return strings.HasSuffix(string(fileUrl), ".jar")
}

// parseJarBizModel parse jar file to BizModel.
func parseJarBizModel(ctx context.Context, bizUrl fileutil.FileUrl) (model *BizModel, err error) {
	defer runtime.RecoverFromError(&err)
	fileUtil := fileutil.DefaultFileUtil()
	localPath := runtime.MustReturnResult(fileUtil.Download(ctx, bizUrl))

	zipReader := runtime.MustReturnResult(zip.OpenReader(localPath[len(osutil.GetLocalFileProtocol()):]))
	defer zipReader.Close()

	bizName := ""
	bizVersion := ""

	for _, fileInfo := range zipReader.File {
		if fileInfo.Name == "META-INF/MANIFEST.MF" {
			// open this file
			file := runtime.MustReturnResult(fileInfo.Open())

			// read all to string
			buf := make([]byte, fileInfo.UncompressedSize64)
			_, err = file.Read(buf)
			runtime.Assert(err == nil || err == io.EOF, "failed to read file %s", fileInfo.Name)
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
	runtime.Assert(isJarFile(bizUrl), "unknown biz bundle type %s", bizUrl)
	return parseJarBizModel(ctx, bizUrl)
}
