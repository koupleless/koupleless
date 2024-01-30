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
package com.alipay.sofa.koupleless.arklet.core.common.model;

import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 合并部署响应。
 * @author CodeNoobKingKc2
 * @version $Id: BatchInstallResponse, v 0.1 2023-11-20 15:19 CodeNoobKingKc2 Exp $
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class BatchInstallResponse {
    /**
     * 响应码。
     */
    private ResponseCode                code;

    /**
     * 响应消息。
     */
    private String                      message;

    /**
     * 业务文件对应的响应。
     */
    private Map<String, ClientResponse> bizUrlToResponse;
}
