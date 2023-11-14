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

package cmdutil

import (
	"context"
	"fmt"
	"strings"
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestCommand_HappyPath(t *testing.T) {
	// ls command is available in most platform
	cmd := BuildCommand(context.Background(), "ls", "-l")

	err := cmd.Exec()
	assert.Nil(t, err)

	output := cmd.Output()
	containsCurFile := false
	for line := range output {
		fmt.Println(line)
		containsCurFile = containsCurFile || strings.Contains(line, "cmd_util_test.go")
	}
	assert.True(t, containsCurFile)

	for err := range cmd.Wait() {
		assert.Nil(t, err)
	}

	err = cmd.Kill()
	assert.Nil(t, err)
}

func TestCommand_StdErr(t *testing.T) {
	cmd := BuildCommand(context.Background(), "ls", "-l", "/not/exist/path")

	err := cmd.Exec()
	assert.Nil(t, err)

	output := cmd.Output()
	hasStdout := false
	for _ = range output {
		hasStdout = true
	}
	assert.False(t, hasStdout)

	for err := range cmd.Wait() {
		assert.NotNil(t, err)
		assert.True(t, len(err.Error()) != 0)
	}

	err = cmd.Kill()
	assert.Nil(t, err)

	err = cmd.GetExitError()
	assert.Nil(t, err)
}

func TestCommand_WrongCommannd(t *testing.T) {
	cmd := BuildCommand(context.Background(), "not_exist_command")
	err := cmd.Exec()
	assert.NotNil(t, err)
	assert.True(t, len(err.Error()) != 0)
}
