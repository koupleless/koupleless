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

package fileutil

import (
	"context"
	"fmt"
	"strings"
)

// FileUrl is the url of file
type FileUrl string

func (url FileUrl) GetFileUrlType() FileUrlType {
	switch {
	// start with file:// then it's a local file
	case strings.HasPrefix(string(url), "file://"):
		return FileUrlTypeLocal

	default:
		panic(fmt.Sprintf("unknown file url type %s", url))
	}
}

type FileUrlType string

const (
	FileUrlTypeLocal FileUrlType = "local"
)

// FileUtils is an interface for all fileutil
type FileUtils interface {
	// Download file from fileUrl to local file system.
	Download(ctx context.Context, fileUrl FileUrl) (string, error)
}

var (
	defaultFileUtil FileUtils = &fileUtil{}
)

// DefaultFileUtil return a default FileUtils.
func DefaultFileUtil() FileUtils {
	return defaultFileUtil
}

type fileUtil struct {
}

func (f fileUtil) Download(_ context.Context, fileUrl FileUrl) (string, error) {
	switch fileUrl.GetFileUrlType() {
	case FileUrlTypeLocal:
		return (string)(fileUrl), nil
	default:
		panic(fmt.Sprintf("unknown download operation for file url type %s", fileUrl))
	}
}
