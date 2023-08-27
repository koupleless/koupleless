package com.alipay.sofa.serverless.arklet.springboot.actuator.info.model;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;

/**
 * @author Lunarscave
 */
public class BizModel {

    private String  bizName;

    private String  bizVersion;

    private BizState bizState;

    private String webContextPath;

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

    public String getWebContextPath() {
        return webContextPath;
    }

    public void setWebContextPath(String webContextPath) {
        this.webContextPath = webContextPath;
    }

    public void setBizModel(Biz biz) {
        this.bizName = biz.getBizName();
        this.bizState = biz.getBizState();
        this.bizVersion = biz.getBizVersion();
        this.webContextPath = biz.getWebContextPath();
    }

}
