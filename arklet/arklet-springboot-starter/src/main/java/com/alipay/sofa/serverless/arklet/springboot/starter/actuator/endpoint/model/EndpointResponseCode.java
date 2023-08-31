package com.alipay.sofa.serverless.arklet.springboot.starter.actuator.endpoint.model;

/**
 * @author Lunarscave
 */
public enum EndpointResponseCode {
    HEALTHY(200),
    UNHEALTHY(400),
    ENDPOINT_NOT_FOUND(404),
    ENDPOINT_PROCESS_INTERNAL_ERROR(500);

    private final int code;

    EndpointResponseCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
