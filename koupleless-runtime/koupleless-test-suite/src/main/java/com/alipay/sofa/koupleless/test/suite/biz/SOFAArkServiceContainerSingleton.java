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
package com.alipay.sofa.koupleless.test.suite.biz;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.model.PluginModel;
import com.alipay.sofa.ark.container.service.ArkServiceContainer;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.alipay.sofa.ark.spi.constant.Constants.MASTER_BIZ;

public class SOFAArkServiceContainerSingleton {

    private static ArkServiceContainer INSTANCE = new ArkServiceContainer(new String[0]);
    private static AtomicBoolean       started  = new AtomicBoolean();

    public static void init(ClassLoader baseClassLoader) {
        if (started.compareAndSet(false, true)) {
            INSTANCE.start();
            ArkClient.setMasterBiz(new BizModel().setBizName("master biz")
                    .setBizVersion("VERSION").setClassLoader(baseClassLoader));
            BizRuntimeContext bizRuntimeContext = new BizRuntimeContext(ArkClient.getMasterBiz());
            BizRuntimeContextRegistry.registerBizRuntimeManager(bizRuntimeContext);
            ArkConfigs.setSystemProperty(MASTER_BIZ, "master biz");
        }
    }

    public static ArkServiceContainer instance() {
        return INSTANCE;
    }
}
