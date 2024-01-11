package com.alipay.sofa.biz.facade;

/**
 * @author: yuanyuan
 * @date: 2023/10/16 9:34 下午
 */
public class Result {

    private boolean success;

    private People people;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public People getPeople() {
        return people;
    }

    public void setPeople(People people) {
        this.people = people;
    }
}
