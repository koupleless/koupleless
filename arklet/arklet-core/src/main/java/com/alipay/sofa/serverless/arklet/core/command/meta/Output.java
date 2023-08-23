package com.alipay.sofa.serverless.arklet.core.command.meta;

import com.alipay.sofa.serverless.arklet.core.api.model.ResponseCode;

/**
 * @author mingmen
 * @date 2023/6/8
 */
public class Output<T> {

    private ResponseCode code;
    private String message;
    private T data;


    private Output() {
    }

    public static <T> Output<T> ofSuccess(T data) {
        Output<T> output = new Output<>();
        output.code = ResponseCode.SUCCESS;
        output.data = data;
        return output;
    }


    public static <T> Output<T> ofFailed(String message) {
        Output<T> output = new Output<>();
        output.code = ResponseCode.FAILED;
        output.message = message;
        return output;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ResponseCode getCode() {
        return code;
    }

    public void setCode(ResponseCode code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
