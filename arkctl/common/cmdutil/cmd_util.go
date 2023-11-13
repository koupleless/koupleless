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
	"bufio"
	"context"
	"errors"
	"os/exec"
	"strings"
	"sync/atomic"
)

// Command is a wrapper for executing cmd command
type Command interface {
	// Exec execute the command
	Exec() error

	// GetCommand return the command
	GetCommand() string

	// GetArgs return the args
	GetArgs() []string

	// Output return the output of command
	Output() <-chan string

	// Wait wait for command to finish
	// is stderr is not empty, send an error
	Wait() <-chan error

	// GetExitState return the exit state of command
	GetExitError() error

	// Kill the command
	Kill() error

	// String return the string representation of command
	String() string
}

// BuildCommand return a new Command
func BuildCommand(
	ctx context.Context,
	cmd string, args ...string) Command {
	return BuildCommandWithWorkDir(ctx, "", cmd, args...)
}

func BuildCommandWithWorkDir(
	ctx context.Context,
	workdir string,
	cmd string,
	args ...string) Command {
	cancableContext, cancelFunc := context.WithCancel(ctx)
	return &command{
		ctx:            cancableContext,
		cmd:            cmd,
		workdir:        workdir,
		args:           args,
		output:         make(chan string, 1),
		completeSignal: make(chan error, 1),
		cancel:         cancelFunc,
	}
}

type command struct {
	started *atomic.Bool
	ctx     context.Context
	workdir string
	cmd     string
	args    []string

	cancel         context.CancelFunc
	output         chan string
	completeSignal chan error
	exitState      error
}

func (c *command) String() string {
	return c.cmd + " " + strings.Join(c.args, " ")
}

func (c *command) GetCommand() string {
	return c.cmd
}

func (c *command) GetArgs() []string {
	return c.args
}

func (c *command) GetExitError() error {
	return c.exitState
}

func (c *command) Exec() error {
	execCmd := exec.CommandContext(c.ctx, c.cmd, c.args...)
	execCmd.Dir = c.workdir

	stdoutpipeline, err := execCmd.StdoutPipe()

	if err != nil {
		return err
	}

	stderrorPipeline, err := execCmd.StderrPipe()
	if err != nil {
		return err
	}

	closed := &atomic.Bool{}
	closed.Store(false)
	closeCompleteSignal := func(err error) {
		if closed.CompareAndSwap(false, true) {
			if err != nil {
				c.completeSignal <- err
			}
			close(c.completeSignal)
		}
	}

	go func() {
		scanner := bufio.NewScanner(stdoutpipeline)
		for scanner.Scan() {
			c.output <- scanner.Text()
		}
		close(c.output)
	}()

	go func() {
		scanner := bufio.NewScanner(stderrorPipeline)
		sb := &strings.Builder{}
		for scanner.Scan() {
			sb.WriteString(scanner.Text())
			sb.WriteString("\n")
		}

		if len(sb.String()) != 0 {
			closeCompleteSignal(errors.New(sb.String()))
		}
	}()

	if err := execCmd.Start(); err != nil {
		return err
	}

	go func() {
		c.exitState = execCmd.Wait()
		closeCompleteSignal(nil)
	}()

	return nil
}

func (c *command) Wait() <-chan error {
	return c.completeSignal
}

func (c *command) Kill() error {
	c.cancel()
	return nil
}

func (c *command) Output() <-chan string {
	return c.output
}
