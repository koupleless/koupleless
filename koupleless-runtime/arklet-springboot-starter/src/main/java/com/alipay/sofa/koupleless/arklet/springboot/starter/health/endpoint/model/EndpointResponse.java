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
package com.alipay.sofa.koupleless.arklet.springboot.starter.health.endpoint.model;

/**
 * @author Lunarscave
 */
public class EndpointResponse<T> {

    private boolean              healthy;
    private int                  code;
    private EndpointResponseCode codeType;
    private T                    data;

    private EndpointResponse() {
    }

    public static <T> EndpointResponse<T> ofSuccess(T data) {
        EndpointResponse<T> endpointResponse = new EndpointResponse<>();
        endpointResponse.healthy = true;
        endpointResponse.code = EndpointResponseCode.HEALTHY.getCode();
        endpointResponse.codeType = EndpointResponseCode.HEALTHY;
        endpointResponse.data = data;
        return endpointResponse;
    }

    public static <T> EndpointResponse<T> ofFailed(EndpointResponseCode codeType, T data) {
        EndpointResponse<T> endpointResponse = new EndpointResponse<>();
        endpointResponse.healthy = false;
        endpointResponse.codeType = codeType;
        endpointResponse.code = codeType.getCode();
        endpointResponse.data = data;
        return endpointResponse;
    }

    public EndpointResponseCode getCodeType() {
        return codeType;
    }

    public void setCodeType(EndpointResponseCode codeType) {
        this.codeType = codeType;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
    }
}
