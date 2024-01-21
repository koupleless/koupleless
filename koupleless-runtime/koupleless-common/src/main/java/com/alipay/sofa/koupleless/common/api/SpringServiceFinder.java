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
package com.alipay.sofa.koupleless.common.api;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.koupleless.common.service.ServiceProxyFactory;

import java.util.Map;

import static com.alipay.sofa.koupleless.common.service.ServiceProxyFactory.determineMostSuitableBiz;

/**
 * @author: yuanyuan
 * @date: 2023/9/21 9:11 下午
 */
public class SpringServiceFinder {

    public static <T> T getBaseService(String name, Class<T> serviceType) {
        Biz masterBiz = ArkClient.getMasterBiz();
        return ServiceProxyFactory.createServiceProxy(masterBiz, name, serviceType, null);
    }

    public static <T> T getBaseService(Class<T> serviceType) {
        Biz masterBiz = ArkClient.getMasterBiz();
        return ServiceProxyFactory.createServiceProxy(masterBiz, serviceType, null);
    }

    public static <T> Map<String, T> listBaseServices(Class<T> serviceType) {
        Biz masterBiz = ArkClient.getMasterBiz();
        return ServiceProxyFactory.batchCreateServiceProxy(masterBiz, serviceType, null);
    }

    public static <T> T getModuleService(String moduleName, String moduleVersion, String name,
                                         Class<T> serviceType) {
        Biz biz = determineMostSuitableBiz(moduleName, moduleVersion);
        return ServiceProxyFactory.createServiceProxy(biz, name, serviceType, null);
    }

    public static <T> T getModuleService(String moduleName, String moduleVersion,
                                         Class<T> serviceType) {
        Biz biz = determineMostSuitableBiz(moduleName, moduleVersion);
        return ServiceProxyFactory.createServiceProxy(biz, serviceType, null);
    }

    public static <T> Map<String, T> listModuleServices(String moduleName, String moduleVersion,
                                                        Class<T> serviceType) {
        Biz biz = determineMostSuitableBiz(moduleName, moduleVersion);
        return ServiceProxyFactory.batchCreateServiceProxy(biz, serviceType, null);
    }

}
