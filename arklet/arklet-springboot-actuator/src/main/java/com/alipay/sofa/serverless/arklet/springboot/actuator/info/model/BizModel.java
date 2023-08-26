package com.alipay.sofa.serverless.arklet.springboot.actuator.info.model;

import com.alipay.sofa.ark.spi.model.BizState;

public class BizModel {
    private String  bizName;

    private String  bizVersion;

    private BizState bizState;

    public String getBizName() {
        return bizName;
    }

    public void setBizName(String bizName) {
        this.bizName = bizName;
    }

    public String getBizVersion() {
        return bizVersion;
    }

    public void setBizVersion(String bizVersion) {
        this.bizVersion = bizVersion;
    }

    public BizState getBizState() {
        return bizState;
    }

    public void setBizState(BizState bizState) {
        this.bizState = bizState;
    }

}
