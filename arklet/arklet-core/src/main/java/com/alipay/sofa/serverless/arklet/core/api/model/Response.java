package com.alipay.sofa.serverless.arklet.core.api.model;

import com.alipay.sofa.serverless.arklet.core.command.meta.Output;

/**
 * @author mingmen
 * @date 2023/6/26
 */
public class Response {

    /**
     * code
     */
    private ResponseCode code;

    /**
     * message
     */
    private String message;

    /**
     * data
     */
    private Object data;

    public static Response fromCommandOutput(Output output) {
        Response response = new Response();
        response.code = output.getCode();
        response.data = output.getData();
        response.message = output.getMessage();
        return response;
    }

    public static Response success(Object data) {
        Response response = new Response();
        response.code = ResponseCode.SUCCESS;
        response.data = data;
        return response;
    }

    public static Response failed(String message) {
        Response response = new Response();
        response.code = ResponseCode.FAILED;
        response.message = message;
        return response;
    }

    public static Response notFound() {
        Response response = new Response();
        response.code = ResponseCode.CMD_NOT_FOUND;
        response.message = "please follow the doc";
        return response;
    }

    public static Response internalError(String message) {
        Response response = new Response();
        response.code = ResponseCode.CMD_PROCESS_INTERNAL_ERROR;
        response.message = message;
        return response;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public ResponseCode getCode() {
        return code;
    }

    public void setCode(ResponseCode code) {
        this.code = code;
    }
}
