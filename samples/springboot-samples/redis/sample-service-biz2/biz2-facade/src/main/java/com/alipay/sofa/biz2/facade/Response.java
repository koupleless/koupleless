package com.alipay.sofa.biz2.facade;

/**
 * @author: yuanyuan
 * @date: 2023/10/16 9:34 下午
 */
public class Response {

    private boolean success;

    private Data date;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Data getDate() {
        return date;
    }

    public void setDate(Data date) {
        this.date = date;
    }
}
