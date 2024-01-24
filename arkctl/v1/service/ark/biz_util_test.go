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
	"io"
	"os"
	"path/filepath"
	"serverless.alipay.com/sofa-serverless/arkctl/common/osutil"
	"strings"
	"testing"

	"serverless.alipay.com/sofa-serverless/arkctl/common/fileutil"

	"github.com/google/uuid"
	"github.com/magiconair/properties/assert"
)

func TestParseBizModel_LocalJar(t *testing.T) {
	// creating a mock jar file
	tmpDir := os.TempDir()
	newUUID, err := uuid.NewUUID()
	if err != nil {
		panic(err)
	}

	zipFilePath := filepath.Join(tmpDir, newUUID.String()+".jar")
	defer os.Remove(zipFilePath) // Schedule removal of zip file after program execution

	// Create the zip file.
	zipFile, err := os.Create(zipFilePath)
	if err != nil {
		fmt.Println("Failed to create zip file:", err)
		return
	}
	defer zipFile.Close()

	zipWriter := zip.NewWriter(zipFile)

	// Add 'META-INF/MANIFEST.MF' to the zip file.
	manifestFile, err := zipWriter.Create("META-INF/MANIFEST.MF")
	if err != nil {
		fmt.Println("Failed to create manifest file in zip:", err)
		return
	}

	// Write the specified lines to the 'META-INF/MANIFEST.MF' file.
	io.Copy(manifestFile, strings.NewReader("Ark-Biz-Name: testName\nArk-Biz-Version: version\n"))
	zipWriter.Close()

	fmt.Println("Zip file created and manifest added at:", zipFilePath)

	model, err := parseJarBizModel(
		context.Background(),
		fileutil.FileUrl("file://"+zipFilePath),
	)
	if err != nil {
		panic(err)
	}

	assert.Equal(t, model.BizName, "testName")
	assert.Equal(t, model.BizVersion, "version")
	assert.Equal(t, model.BizUrl, fileutil.FileUrl(osutil.GetLocalFileProtocol()+zipFilePath))
}
