/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.serverless.arklet.core.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 合并部署请求。
 * @author gouzhendong.gzd
 * @version $Id: CombineInstallRequest, v 0.1 2023-11-20 15:21 gouzhendong.gzd Exp $
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class CombineInstallRequest {
    /**
     * 合并部署类型。
     *    LOCAL_FILE_SYSTEM: 从本地文件系统。
     */
    private String type;

    /**
     * 本地文件系统目录。
     */
    private String bizDirAbsolutePath;
}
