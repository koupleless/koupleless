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
package com.alipay.sofa.serverless.arklet.core.command.builtin.handler;

import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.command.meta.bizops.ArkBizMeta;
import com.alipay.sofa.serverless.arklet.core.command.meta.bizops.ArkBizOps;
import com.alipay.sofa.serverless.arklet.core.common.exception.ArkletRuntimeException;
import com.alipay.sofa.serverless.arklet.core.common.exception.CommandValidationException;
import lombok.Getter;
import lombok.Setter;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.List;

/**
 * @author mingmen
 * @date 2023/6/8
 */
public class InstallBizHandler
                              extends
                              AbstractCommandHandler<InstallBizHandler.Input, InstallBizHandler.InstallBizClientResponse>
                                                                                                                         implements
                                                                                                                         ArkBizOps {

    @Override
    public Output<InstallBizClientResponse> handle(Input input) {
        MemoryPoolMXBean metaSpaceMXBean = getMetaSpaceMXBean();
        long startSpace = metaSpaceMXBean.getUsage().getUsed();
        try {
            InstallBizClientResponse installBizClientResponse = convertClientResponse(getOperationService()
                .install(input.getBizUrl()));
            installBizClientResponse.setElapsedSpace(metaSpaceMXBean.getUsage().getUsed()
                                                     - startSpace);
            if (ResponseCode.SUCCESS.equals(installBizClientResponse.getCode())) {
                return Output.ofSuccess(installBizClientResponse);
            } else {
                return Output.ofFailed(installBizClientResponse, "install biz not success!");
            }
        } catch (Throwable e) {
            throw new ArkletRuntimeException(e);
        }
    }

    private MemoryPoolMXBean getMetaSpaceMXBean() {
        MemoryPoolMXBean metaSpaceMXBean = null;
        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans)
            if (memoryPoolMXBean.getName().equals("Metaspace"))
                metaSpaceMXBean = memoryPoolMXBean;
        return metaSpaceMXBean;
    }

    private InstallBizClientResponse convertClientResponse(ClientResponse res) {
        InstallBizClientResponse installBizClientResponse = new InstallBizClientResponse();
        installBizClientResponse.setBizInfos(res.getBizInfos());
        installBizClientResponse.setCode(res.getCode());
        installBizClientResponse.setMessage(res.getMessage());
        return installBizClientResponse;
    }

    @Override
    public Command command() {
        return BuiltinCommand.INSTALL_BIZ;
    }

    @Override
    public void validate(Input input) throws CommandValidationException {
        notBlank(input.getBizName(), "bizName should not be blank");
        notBlank(input.getBizVersion(), "bizVersion should not be blank");
        isTrue(!input.isAsync() || !StringUtils.isEmpty(input.getRequestId()),
            "requestId should not be blank when async is true");
        notBlank(input.getBizUrl(), "bizUrl should not be blank");
    }

    @Getter
    @Setter
    public static class Input extends ArkBizMeta {
        private String bizUrl;
    }

    @Getter
    @Setter
    public static class InstallBizClientResponse extends ClientResponse {
        private long elapsedSpace;
    }

}
