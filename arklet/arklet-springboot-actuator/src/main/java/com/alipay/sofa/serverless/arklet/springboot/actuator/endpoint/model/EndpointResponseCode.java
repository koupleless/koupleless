package com.alipay.sofa.serverless.arklet.springboot.actuator.endpoint.model;

/**
 * @author Lunarscave
 */
public enum EndpointResponseCode {
    ENDPOINT_HEALTHY(200),
    ENDPOINT_UNHEALTHY(400),
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
