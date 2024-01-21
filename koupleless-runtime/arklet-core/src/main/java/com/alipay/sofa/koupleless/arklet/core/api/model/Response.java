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
package com.alipay.sofa.koupleless.arklet.core.api.model;

import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;

/**
 * @author mingmen
 * @date 2023/6/26
 */
public class Response {

    /**
     * code
     */
    private ResponseCode code;

    /**
     * message
     */
    private String       message;

    /**
     * data
     */
    private Object       data;

    /**
     * error stack trace
     */
    private String       errorStackTrace;

    public static Response fromCommandOutput(Output output) {
        Response response = new Response();
        response.code = output.getCode();
        response.data = output.getData();
        response.message = output.getMessage();
        return response;
    }

    public static Response success(Object data) {
        Response response = new Response();
        response.code = ResponseCode.SUCCESS;
        response.data = data;
        return response;
    }

    public static Response failed(String message) {
        Response response = new Response();
        response.code = ResponseCode.FAILED;
        response.message = message;
        return response;
    }

    public static Response notFound() {
        Response response = new Response();
        response.code = ResponseCode.CMD_NOT_FOUND;
        response.message = "please follow the doc";
        return response;
    }

    public static Response internalError(String message, String errorStackTrace) {
        Response response = new Response();
        response.code = ResponseCode.CMD_PROCESS_INTERNAL_ERROR;
        response.message = message;
        response.errorStackTrace = errorStackTrace;
        return response;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public ResponseCode getCode() {
        return code;
    }

    public void setCode(ResponseCode code) {
        this.code = code;
    }

    public String getErrorStackTrace() {
        return errorStackTrace;
    }

    public void setErrorStackTrace(String errorStackTrace) {
        this.errorStackTrace = errorStackTrace;
    }
}
