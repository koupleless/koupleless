package com.alipay.sofa.serverless.arklet.core.command.builtin.model;

import com.alipay.sofa.ark.spi.model.BizState;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public class BizModel {
    private String  bizName;

    private String  bizVersion;

    private BizState bizState;

    private String mainClass;

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

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public String getWebContextPath() {
        return webContextPath;
    }

    public void setWebContextPath(String webContextPath) {
        this.webContextPath = webContextPath;
    }
}
