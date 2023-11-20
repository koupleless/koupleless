/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.serverless.arklet.core.common.model;

import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 合并部署响应。
 * @author gouzhendong.gzd
 * @version $Id: CombineInstallResponse, v 0.1 2023-11-20 15:19 gouzhendong.gzd Exp $
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CombineInstallResponse {
    /**
     * 响应码。
     */
    private ResponseCode code;

    /**
     * 响应消息。
     */
    private String message;

    /**
     * 业务文件对应的响应。
     */
    private Map<String, ClientResponse> bizUrlToResponse;
}
