package com.alipay.sofa.serverless.arklet.core.command.coordinate;

import com.alipay.sofa.serverless.arklet.core.command.meta.Command;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public class ExecutionLock {
    private Command command;

    public static ExecutionLock newInstance(Command command) {
        ExecutionLock executionLock = new ExecutionLock();
        executionLock.command = command;
        return executionLock;
    }

    public Command getCommand() {
        return command;
    }
}
