package com.alipay.sofa.serverless.arklet.springboot.actuator.endpoint.model;

/**
 * @author Lunarscave
 */
public class EndpointResponse<T> {

    private int code;
    private EndpointResponseCode codeType;
    private T data;

    private EndpointResponse() {
    }

    public static <T> EndpointResponse<T> ofSuccess(T data) {
        EndpointResponse<T> endpointResponse = new EndpointResponse<>();
        endpointResponse.code = EndpointResponseCode.ENDPOINT_HEALTHY.getCode();
        endpointResponse.codeType = EndpointResponseCode.ENDPOINT_HEALTHY;
        endpointResponse.data = data;
        return endpointResponse;
    }


    public static <T> EndpointResponse<T> ofFailed(EndpointResponseCode codeType, T data) {
        EndpointResponse<T> endpointResponse = new EndpointResponse<>();
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
}
