package com.alipay.sofa.serverless.arklet.core.api.model;

/**
 * @author mingmen
 * @date 2023/6/26
 */

public enum ResponseCode {
    SUCCESS(200),
    FAILED(400),
    CMD_NOT_FOUND(404),
    CMD_PROCESS_INTERNAL_ERROR(500);
    private final int code;

    ResponseCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
