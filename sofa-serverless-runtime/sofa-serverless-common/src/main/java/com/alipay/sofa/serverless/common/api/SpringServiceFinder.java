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
package com.alipay.sofa.serverless.common.api;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.serverless.common.BizRuntimeContext;
import com.alipay.sofa.serverless.common.BizRuntimeContextRegistry;
import com.alipay.sofa.serverless.common.service.ServiceProxyFactory;
import sun.reflect.CallerSensitive;

/**
 * @author: yuanyuan
 * @date: 2023/9/21 9:11 下午
 */
public class SpringServiceFinder {

    @CallerSensitive
    public static <T> T getBaseService(String name) {
        Biz masterBiz = ArkClient.getMasterBiz();
        return getService(masterBiz, name);
    }

    @CallerSensitive
    public static <T> T getBaseService(Class<T> serviceType) {
        Biz masterBiz = ArkClient.getMasterBiz();
        return getService(masterBiz, serviceType);
    }

    //    public static <T> Map<String, T> listBaseServices(Class<T> serviceType) {
    //        // todo spring 只支持 getBean
    //        Biz masterBiz = ArkClient.getMasterBiz();
    //        return getService(masterBiz, serviceType);
    //    }

    @CallerSensitive
    public static <T> T getModuleService(String moduleName, String moduleVersion, String name) {
        // todo if moduleVesion is black
        Biz biz = ArkClient.getBizManagerService().getBiz(moduleName, moduleVersion);
        return ServiceProxyFactory.createServiceProxy(biz, name);
    }

    @CallerSensitive
    public static <T> T getModuleService(String moduleName, String moduleVersion,
                                         Class<T> serviceType) {
        Biz biz = ArkClient.getBizManagerService().getBiz(moduleName, moduleVersion);
        return ServiceProxyFactory.createServiceProxy(biz, serviceType);
    }

    //    public static <T> Map<String, T> listModuleServices(String moduleName, String moduleVersion, Class<T> serviceType) {
    //        // todo spring 只支持 getBean
    //        Biz biz = ArkClient.getBizManagerService().getBiz(moduleName, moduleVersion);
    //        return getService(biz, serviceType);
    //    }

    @CallerSensitive
    private static <T> T getService(Biz biz, String name) {
        BizRuntimeContext bizRuntimeContext = BizRuntimeContextRegistry.getBizRuntimeContext(biz);
        return (T) bizRuntimeContext.getRootApplicationContext().getBean(name);
    }

    @CallerSensitive
    private static <T> T getService(Biz biz, Class<T> serviceType) {
        BizRuntimeContext bizRuntimeContext = BizRuntimeContextRegistry.getBizRuntimeContext(biz);
        return bizRuntimeContext.getRootApplicationContext().getBean(serviceType);
    }

}
