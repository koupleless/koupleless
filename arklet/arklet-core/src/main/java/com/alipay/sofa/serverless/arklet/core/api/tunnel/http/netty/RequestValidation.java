package com.alipay.sofa.serverless.arklet.core.api.tunnel.http.netty;

import java.util.Map;

/**
 * @author mingmen
 * @date 2023/6/19
 */
public class RequestValidation {
    private boolean pass;
    private String message;
    private boolean cmdSupported;
    private String cmd;
    private Map<String, Object> cmdContent;

    public RequestValidation() {
    }

    public static RequestValidation notPass(String message) {
        RequestValidation validation = new RequestValidation();
        validation.pass = false;
        validation.message = message;
        return validation;
    }

    public static RequestValidation passed(boolean cmdSupported, String cmd, Map<String, Object> cmdContent) {
        RequestValidation validation = new RequestValidation();
        validation.pass = true;
        validation.cmdSupported = cmdSupported;
        validation.cmd = cmd;
        validation.cmdContent = cmdContent;
        return validation;
    }

    public boolean isPass() {
        return pass;
    }

    public String getMessage() {
        return message;
    }

    public boolean isCmdSupported() {
        return cmdSupported;
    }

    public String getCmd() {
        return cmd;
    }

    public Map<String, Object> getCmdContent() {
        return cmdContent;
    }
}
