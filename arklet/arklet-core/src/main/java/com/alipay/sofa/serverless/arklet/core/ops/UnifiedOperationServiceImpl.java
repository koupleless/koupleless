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
package com.alipay.sofa.serverless.arklet.core.ops;

import java.util.List;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizOperation;
import com.alipay.sofa.serverless.arklet.springboot.actuator.api.ActuatorClient;
import com.alipay.sofa.serverless.arklet.springboot.actuator.api.HealthQueryType;
import com.alipay.sofa.serverless.arklet.springboot.actuator.model.HealthDataModel;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.model.BizModel;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.model.PluginModel;
import com.google.inject.Singleton;

/**
 * @author mingmen
 * @date 2023/6/14
 */
@Singleton
public class UnifiedOperationServiceImpl implements UnifiedOperationService {

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    public ClientResponse install(String bizPath) throws Throwable {
        BizOperation bizOperation = new BizOperation()
            .setOperationType(BizOperation.OperationType.INSTALL);
        bizOperation.putParameter(Constants.CONFIG_BIZ_URL, "file://" + bizPath);
        return ArkClient.installOperation(bizOperation);
    }

    @Override
    public ClientResponse uninstall(String bizName, String bizVersion) throws Throwable {
        return ArkClient.uninstallBiz(bizName, bizVersion);
    }

    @Override
    public List<Biz> queryBizList() {
        return ArkClient.getBizManagerService().getBizInOrder();
    }

    @Override
    public ClientResponse switchBiz(String bizName, String bizVersion) throws Throwable {
        return ArkClient.switchBiz(bizName, bizVersion);
    }

    @Override
    public HealthDataModel health() {
        return ActuatorClient.getHealth(HealthQueryType.ALL);
    }

    @Override
    public HealthDataModel queryAllBizHealth() {
        return ActuatorClient.getHealth(HealthQueryType.BIZ_LIST);
    }

    @Override
    public HealthDataModel queryAllPluginHealth() {
        return ActuatorClient.getHealth(HealthQueryType.PLUGIN_LIST);
    }

    @Override
    public HealthDataModel queryBizHealth(String bizName, String bizVersion) {
        return ActuatorClient.getHealth(BizModel.createBizModel(bizName, bizVersion));
    }

    @Override
    public HealthDataModel queryPluginHealth(String pluginName, String pluginVersion) {
        return ActuatorClient.getHealth(PluginModel.createPluginModel(pluginName, pluginVersion));
    }
}
