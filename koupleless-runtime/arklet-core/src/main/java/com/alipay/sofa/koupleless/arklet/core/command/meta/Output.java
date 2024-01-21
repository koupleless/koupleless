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
package com.alipay.sofa.koupleless.arklet.core.command.meta;

import com.alipay.sofa.koupleless.arklet.core.api.model.ResponseCode;

/**
 * @author mingmen
 * @date 2023/6/8
 */
public class Output<T> {

    private ResponseCode code;
    private String       message;
    private T            data;

    private Output() {
    }

    public static <T> Output<T> ofSuccess(T data) {
        Output<T> output = new Output<>();
        output.code = ResponseCode.SUCCESS;
        output.data = data;
        return output;
    }

    public static <T> Output<T> ofFailed(String message) {
        Output<T> output = new Output<>();
        output.code = ResponseCode.FAILED;
        output.message = message;
        return output;
    }

    public boolean success() {
        return ResponseCode.SUCCESS.equals(code);
    }

    public boolean failed() {
        return ResponseCode.FAILED.equals(code);
    }

    public static <T> Output<T> ofFailed(T data, String message) {
        Output<T> output = new Output<>();
        output.code = ResponseCode.FAILED;
        output.data = data;
        output.message = message;
        return output;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ResponseCode getCode() {
        return code;
    }

    public void setCode(ResponseCode code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
