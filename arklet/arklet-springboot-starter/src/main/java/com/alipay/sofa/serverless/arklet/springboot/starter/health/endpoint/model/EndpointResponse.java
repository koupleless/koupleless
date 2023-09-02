package com.alipay.sofa.serverless.arklet.springboot.starter.health.endpoint.model;

/**
 * @author Lunarscave
 */
public class EndpointResponse<T> {

    private boolean healthy;
    private int code;
    private EndpointResponseCode codeType;
    private T data;

    private EndpointResponse() {
    }

    public static <T> EndpointResponse<T> ofSuccess(T data) {
        EndpointResponse<T> endpointResponse = new EndpointResponse<>();
        endpointResponse.healthy = true;
        endpointResponse.code = EndpointResponseCode.HEALTHY.getCode();
        endpointResponse.codeType = EndpointResponseCode.HEALTHY;
        endpointResponse.data = data;
        return endpointResponse;
    }


    public static <T> EndpointResponse<T> ofFailed(EndpointResponseCode codeType, T data) {
        EndpointResponse<T> endpointResponse = new EndpointResponse<>();
        endpointResponse.healthy = false;
        endpointResponse.codeType = codeType;
        endpointResponse.code = codeType.getCode();
        endpointResponse.data = data;
        return endpointResponse;
    }

    public EndpointResponseCode getCodeType() {
        return codeType;
    }

    public void setCodeType(EndpointResponseCode codeType) {
        this.codeType = codeType;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }
}
