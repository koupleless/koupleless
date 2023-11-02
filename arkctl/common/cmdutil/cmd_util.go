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

	// Output return the output of command
	Output() <-chan string

	// Wait wait for command to finish
	// is stderr is not empty, send an error
	Wait() <-chan error

	// GetExitState return the exit state of command
	GetExitError() error

	// Kill the command
	Kill() error
}

// BuildCommand return a new Command
func BuildCommand(ctx context.Context, cmd string, args ...string) Command {
	cancableContext, cancelFunc := context.WithCancel(ctx)
	return &command{
		ctx:            cancableContext,
		cmd:            cmd,
		args:           args,
		output:         make(chan string, 1),
		completeSignal: make(chan error, 1),
		cancel:         cancelFunc,
	}
}

type command struct {
	started *atomic.Bool
	ctx     context.Context
	cmd     string
	args    []string

	cancel         context.CancelFunc
	output         chan string
	completeSignal chan error
	exitState      error
}

func (c *command) GetExitError() error {
	return c.exitState
}

func (c *command) Exec() error {
	execCmd := exec.CommandContext(c.ctx, c.cmd, c.args...)

	stdoutpipeline, err := execCmd.StdoutPipe()

	if err != nil {
		return err
	}

	stderrorPipeline, err := execCmd.StderrPipe()
	if err != nil {
		return err
	}

	closed := false
	closeCompleteSignal := func(err error) {
		if closed {
			return
		}
		closed = true
		if err != nil {
			c.completeSignal <- err
		}
		close(c.completeSignal)
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
