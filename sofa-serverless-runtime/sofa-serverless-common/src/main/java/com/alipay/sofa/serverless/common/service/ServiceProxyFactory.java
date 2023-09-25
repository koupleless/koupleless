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
package com.alipay.sofa.serverless.common.service;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.serverless.common.BizRuntimeContext;
import com.alipay.sofa.serverless.common.BizRuntimeContextRegistry;
import com.alipay.sofa.serverless.common.exception.BizRuntimeException;
import com.alipay.sofa.serverless.common.util.ReflectionUtils;
import org.springframework.aop.framework.ProxyFactory;
import sun.reflect.CallerSensitive;

/**
 * @author: yuanyuan
 * @date: 2023/9/21 9:55 下午
 */
public class ServiceProxyFactory {

    @CallerSensitive
    public static <T> T createServiceProxy(Biz biz, String name) {
        T service = getService(biz, name);
        return (T) doCreateServiceProxy(biz, service);
    }

    @CallerSensitive
    public static <T> T createServiceProxy(Biz biz, Class<T> serviceType) {
        T service = getService(biz, serviceType);
        return (T) doCreateServiceProxy(biz, service);
    }

    @CallerSensitive
    private static <T> T getService(Biz biz, String name) {
        BizRuntimeContext bizRuntimeContext = getBizRuntimeContext(biz);
        return (T) bizRuntimeContext.getRootApplicationContext().getBean(name);
    }

    @CallerSensitive
    private static <T> T getService(Biz biz, Class<T> serviceType) {
        BizRuntimeContext bizRuntimeContext = getBizRuntimeContext(biz);
        return bizRuntimeContext.getRootApplicationContext().getBean(serviceType);
    }

    @CallerSensitive
    private static BizRuntimeContext getBizRuntimeContext(Biz biz) {
        if (biz == null) {
            throw new BizRuntimeException("", "biz is null");
        }
        if (biz.getBizState() != BizState.ACTIVATED && biz.getBizState() != BizState.DEACTIVATED) {
            throw new BizRuntimeException("", "biz state is not valid");
        }
        BizRuntimeContext bizRuntimeContext = BizRuntimeContextRegistry.getBizRuntimeContext(biz);
        if (bizRuntimeContext == null) {
            throw new BizRuntimeException("", "biz runtime context is null");
        }
        return bizRuntimeContext;
    }

    @CallerSensitive
    private static Object doCreateServiceProxy(Biz biz, Object service) {
        Class<?> callerClass = ReflectionUtils.getCallerClass(5);
        SpringServiceInvoker serviceInvoker = new SpringServiceInvoker(service, biz.getBizName(),
            biz.getBizVersion(), biz.getIdentity(), callerClass.getClassLoader(), service
                .getClass().getClassLoader());
        ProxyFactory factory = new ProxyFactory();
        Class<?> targetClass = service.getClass();
        if (targetClass.isInterface()) {
            factory.addInterface(targetClass);
            //            factory.addInterface(JvmBindingInterface.class);
        } else {
            factory.setTargetClass(targetClass);
            factory.setProxyTargetClass(true);
        }
        factory.addAdvice(serviceInvoker);
        return factory.getProxy(callerClass.getClassLoader());
    }
}
