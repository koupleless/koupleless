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

import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.serverless.arklet.core.ArkletComponent;
import com.alipay.sofa.serverless.arklet.springboot.actuator.model.HealthDataModel;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public interface UnifiedOperationService extends ArkletComponent {

    ClientResponse install(String bizPath) throws Throwable;

    ClientResponse uninstall(String bizName, String bizVersion) throws Throwable;

    List<Biz> queryBizList();

    ClientResponse switchBiz(String bizName, String bizVersion) throws Throwable;

    HealthDataModel health();

    HealthDataModel queryAllBizHealth();

    HealthDataModel queryAllPluginHealth();

    HealthDataModel queryBizHealth(String bizName, String bizVersion) ;

    HealthDataModel queryPluginHealth(String pluginName, String pluginVersion) ;

}
