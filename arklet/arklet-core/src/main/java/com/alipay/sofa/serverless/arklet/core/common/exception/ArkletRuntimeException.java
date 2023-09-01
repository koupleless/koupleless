package com.alipay.sofa.serverless.arklet.core.common.exception;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public class ArkletRuntimeException extends ArkletException {
    public ArkletRuntimeException() {
    }

    public ArkletRuntimeException(String message) {
        super(message);
    }

    public ArkletRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArkletRuntimeException(Throwable cause) {
        super(cause);
    }

    public ArkletRuntimeException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ArkletRuntimeException(String format, Object... args) {
        super(format, args);
    }

    public ArkletRuntimeException(Throwable cause, String format, Object... args) {
        super(cause, format, args);
    }
}
