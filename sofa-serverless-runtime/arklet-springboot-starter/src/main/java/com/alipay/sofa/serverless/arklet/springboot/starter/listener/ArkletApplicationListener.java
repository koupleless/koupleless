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
package com.alipay.sofa.serverless.arklet.springboot.starter.listener;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.serverless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.common.log.ArkletLogger;
import com.alipay.sofa.serverless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.serverless.arklet.core.common.model.CombineInstallRequest;
import com.alipay.sofa.serverless.arklet.core.common.model.CombineInstallResponse;
import com.alipay.sofa.serverless.arklet.core.ops.UnifiedOperationService;
import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * @author mingmen
 * @date 2023/6/14
 */
@SuppressWarnings("rawtypes")
public class ArkletApplicationListener implements ApplicationListener<ApplicationContextEvent> {

    private static ArkletLogger LOGGER = ArkletLoggerFactory.getDefaultLogger();

    @SneakyThrows
    public void combineDeployFromLocalDir() {
        String absolutePath = System.getProperty("deploy.combine.biz.dir.absolute.path");
        if (StringUtils.isBlank(absolutePath)) {
            return;
        }
        LOGGER.info("start to combine deploy from local dir:{}", absolutePath);
        UnifiedOperationService operationServiceInstance = ArkletComponentRegistry.getOperationServiceInstance();
        CombineInstallResponse combineInstallResponse = operationServiceInstance.combineInstall(CombineInstallRequest.builder().
                bizDirAbsolutePath(absolutePath).
                build());
        LOGGER.info("combine deploy result:{}", combineInstallResponse);
        Preconditions.checkState(combineInstallResponse.getCode() == ResponseCode.SUCCESS,
                "combine deploy failed!"
        );
    }

    @Override
    public void onApplicationEvent(ApplicationContextEvent event) {
        // 非基座应用直接跳过
        if (!Objects.equals(this.getClass().getClassLoader(), Thread.currentThread().getContextClassLoader()) || event.getApplicationContext().getParent() != null) {
            return;
        }
        if (event instanceof ContextRefreshedEvent) {
            List<AbstractCommandHandler> handlers = ArkletComponentRegistry
                    .getCommandServiceInstance().listAllHandlers();
            String commands = handlers.stream().map(s -> s.command().getId()).collect(Collectors.joining(", "));
            LOGGER.info("total supported commands:{}", commands);

            combineDeployFromLocalDir();
        }
    }
}
