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
		assert.Equal(t, "ls: /not/exist/path: No such file or directory", err.Error())
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
	assert.Equal(t, "exec: \"not_exist_command\": executable file not found in $PATH", err.Error())
}
