/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.koupleless.arklet.core.api.tunnel.http.netty;

import java.util.Map;

/**
 * @author mingmen
 * @date 2023/6/19
 */
public class RequestValidation {
    private boolean             pass;
    private String              message;
    private boolean             cmdSupported;
    private String              cmd;
    private Map<String, Object> cmdContent;

    public RequestValidation() {
    }

    public static RequestValidation notPass(String message) {
        RequestValidation validation = new RequestValidation();
        validation.pass = false;
        validation.message = message;
        return validation;
    }

    public static RequestValidation passed(boolean cmdSupported, String cmd,
                                           Map<String, Object> cmdContent) {
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
