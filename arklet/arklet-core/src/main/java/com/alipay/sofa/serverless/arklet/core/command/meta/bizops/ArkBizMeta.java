package com.alipay.sofa.serverless.arklet.core.command.meta.bizops;

import com.alipay.sofa.serverless.arklet.core.command.meta.InputMeta;

/**
 * @author mingmen
 * @date 2023/8/21
 */
public class ArkBizMeta extends InputMeta {
    private String  bizName;
    private String  bizVersion;
    private String  requestId;
    private boolean async;

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

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }
}
