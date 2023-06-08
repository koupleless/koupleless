package com.alipay.sofa.serverless.arklet.core.common;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public class ArkletInitException extends ArkletException {
    public ArkletInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArkletInitException(Throwable cause) {
        super(cause);
    }

    public ArkletInitException(String message) {
        super(message);
    }
}
