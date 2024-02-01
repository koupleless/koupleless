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
package com.alipay.sofa.koupleless.plugin.manager.listener;

import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.koupleless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLogger;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallRequest;
import com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallResponse;
import com.alipay.sofa.koupleless.arklet.core.ops.UnifiedOperationService;
import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * @author CodeNoobKingKc2
 * @version $Id: ApplicationContextEventListener, v 0.1 2023-11-21 11:26 CodeNoobKingKc2 Exp $
 */
public class StaticBatchInstallEventListener implements
                                            ApplicationListener<ApplicationContextEvent> {

    // 合并部署是否已经完成，防止重复执行。
    private AtomicBoolean isBatchdDeployed = new AtomicBoolean(false);

    @SneakyThrows
    public void batchDeployFromLocalDir() {
        String absolutePath = System.getProperty("com.alipay.sofa.ark.static.biz.dir");
        if (StringUtils.isEmpty(absolutePath) || isBatchdDeployed.get()) {
            return;
        }
        ArkletLoggerFactory.getDefaultLogger().info("start to batch deploy from local dir:{}",
            absolutePath);
        UnifiedOperationService operationServiceInstance = ArkletComponentRegistry
            .getOperationServiceInstance();

        BatchInstallResponse batchInstallResponse = operationServiceInstance
            .batchInstall(BatchInstallRequest.builder().bizDirAbsolutePath(absolutePath).build());
        for (Map.Entry<String, ClientResponse> entry : batchInstallResponse.getBizUrlToResponse()
            .entrySet()) {
            ArkletLoggerFactory.getDefaultLogger().info("{}, {}, {}, BatchDeployResult",
                entry.getKey(), entry.getValue().getCode().toString(),
                entry.getValue().getMessage());
        }
        isBatchdDeployed.set(true);
        Preconditions.checkState(batchInstallResponse.getCode() == ResponseCode.SUCCESS,
            "batch deploy failed!");
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        // 非基座应用直接跳过
        if (!Objects.equals(this.getClass().getClassLoader(), Thread.currentThread()
            .getContextClassLoader())
            || event.getApplicationContext().getParent() != null) {
            return;
        }

        // 基座应用启动完成后，执行合并部署。
        if (event instanceof ContextRefreshedEvent) {
            batchDeployFromLocalDir();
        }
    }
}
