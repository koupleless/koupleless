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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Lunarscave
 */
public enum EndpointResponseCode {

    HEALTHY(200), UNHEALTHY(400), ENDPOINT_NOT_FOUND(404), ENDPOINT_PROCESS_INTERNAL_ERROR(500);

    private final int code;

    EndpointResponseCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static EndpointResponseCode getEndpointResponseCode(int code) {
        Set<EndpointResponseCode> codes = new HashSet<>(Arrays.asList(values()));
        EndpointResponseCode endpointResponseCode = null;
        for (EndpointResponseCode codeType : codes) {
            if (codeType.getCode() == code) {
                endpointResponseCode = codeType;
            }
        }
        return endpointResponseCode;
    }

    public static boolean existCode(int code) {
        Set<EndpointResponseCode> codes = new HashSet<>(Arrays.asList(values()));
        boolean exists = false;
        for (EndpointResponseCode codeType : codes) {
            exists |= codeType.getCode() == code;
        }
        return exists;
    }
}
