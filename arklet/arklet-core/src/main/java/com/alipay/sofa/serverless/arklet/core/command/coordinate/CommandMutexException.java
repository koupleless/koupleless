package com.alipay.sofa.serverless.arklet.core.command.coordinate;

import com.alipay.sofa.serverless.arklet.core.common.ArkletRuntimeException;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public class CommandMutexException extends ArkletRuntimeException {
    public CommandMutexException() {
    }

    public CommandMutexException(String message) {
        super(message);
    }

    public CommandMutexException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommandMutexException(Throwable cause) {
        super(cause);
    }

    public CommandMutexException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CommandMutexException(String format, Object... args) {
        super(format, args);
    }

    public CommandMutexException(Throwable cause, String format, Object... args) {
        super(cause, format, args);
    }
}
