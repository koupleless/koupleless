package com.alipay.sofa.serverless.arklet.core.common;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public abstract class ArkletException extends RuntimeException {
    public ArkletException() {
    }

    public ArkletException(String message) {
        super(message);
    }

    public ArkletException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArkletException(Throwable cause) {
        super(cause);
    }

    public ArkletException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ArkletException(String format, Object... args) {
        super(String.format(format, args));
    }

    public ArkletException(Throwable cause, String format, Object... args) {
        super(String.format(format, args), cause);
    }
}
