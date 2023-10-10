package com.alipay.sofa.biz.model;

/**
 * @author: yuanyuan
 * @date: 2023/9/25 8:47 下午
 */
public class Result {

    private boolean success;

    private com.alipay.sofa.biz.model.People people;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public com.alipay.sofa.biz.model.People getPeople() {
        return people;
    }

    public void setPeople(People people) {
        this.people = people;
    }
}
