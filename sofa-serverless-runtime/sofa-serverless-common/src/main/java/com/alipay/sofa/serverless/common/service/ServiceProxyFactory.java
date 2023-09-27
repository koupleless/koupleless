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

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.serverless.common.BizRuntimeContext;
import com.alipay.sofa.serverless.common.BizRuntimeContextRegistry;
import com.alipay.sofa.serverless.common.exception.BizRuntimeException;
import com.alipay.sofa.serverless.common.util.ReflectionUtils;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.alipay.sofa.serverless.common.exception.ErrorCodes.SpringContextManager.E100002;
import static com.alipay.sofa.serverless.common.exception.ErrorCodes.SpringContextManager.E100003;
import static com.alipay.sofa.serverless.common.exception.ErrorCodes.SpringContextManager.E100004;

/**
 * @author: yuanyuan
 * @date: 2023/9/21 9:55 下午
 */
public class ServiceProxyFactory {

    public static <T> T createServiceProxy(Biz biz, String name, ClassLoader clientClassLoader) {
        T service = getService(biz, name);
        return (T) doCreateServiceProxy(biz, service, clientClassLoader);
    }

    public static <T> T createServiceProxy(Biz biz, Class<T> serviceType,
                                           ClassLoader clientClassLoader) {
        Class<?> serviceClass;
        try {
            serviceClass = biz.getBizClassLoader().loadClass(serviceType.getName());
        } catch (ClassNotFoundException e) {
            throw new BizRuntimeException("", "Cannot found class " + serviceType.getName()
                                              + " from the base");
        }
        T service = (T) getService(biz, serviceClass);
        return (T) doCreateServiceProxy(biz, service, clientClassLoader);
    }

    public static <T> Map<String, T> batchCreateServiceProxy(Biz biz, Class<T> serviceType,
                                                             ClassLoader clientClassLoader) {
        Class<?> serviceClass;
        try {
            serviceClass = biz.getBizClassLoader().loadClass(serviceType.getName());
        } catch (ClassNotFoundException e) {
            throw new BizRuntimeException("", "Cannot found class " + serviceType.getName()
                                              + " from the base");
        }
        Map<String, ?> serviceMap = listService(biz, serviceClass);
        Map<String, T> proxyMap = new HashMap<>();
        for (String beanName : serviceMap.keySet()) {
            proxyMap.put(beanName,
                (T) doCreateServiceProxy(biz, serviceMap.get(beanName), clientClassLoader));
        }
        return proxyMap;
    }

    private static <T> T getService(Biz biz, String name) {
        BizRuntimeContext bizRuntimeContext = checkBizStateAndGetBizRuntimeContext(biz);
        return (T) bizRuntimeContext.getRootApplicationContext().getBean(name);
    }

    private static <T> T getService(Biz biz, Class<T> serviceType) {
        BizRuntimeContext bizRuntimeContext = checkBizStateAndGetBizRuntimeContext(biz);
        return bizRuntimeContext.getRootApplicationContext().getBean(serviceType);
    }

    private static <T> Map<String, T> listService(Biz biz, Class<T> serviceType) {
        BizRuntimeContext bizRuntimeContext = checkBizStateAndGetBizRuntimeContext(biz);
        ApplicationContext rootApplicationContext = bizRuntimeContext.getRootApplicationContext();
        if (rootApplicationContext instanceof AbstractApplicationContext) {
            ConfigurableListableBeanFactory beanFactory = ((AbstractApplicationContext) rootApplicationContext)
                .getBeanFactory();
            return beanFactory.getBeansOfType(serviceType);
        }
        return new HashMap<>();
    }

    private static BizRuntimeContext checkBizStateAndGetBizRuntimeContext(Biz biz) {
        if (biz == null) {
            throw new BizRuntimeException(E100003, "biz is null");
        }
        if (biz.getBizState() != BizState.ACTIVATED && biz.getBizState() != BizState.DEACTIVATED) {
            throw new BizRuntimeException(E100004, "biz state is not valid");
        }
        BizRuntimeContext bizRuntimeContext = BizRuntimeContextRegistry.getBizRuntimeContext(biz);
        if (bizRuntimeContext == null) {
            throw new BizRuntimeException(E100002, "biz runtime context is null");
        }
        if (bizRuntimeContext.getRootApplicationContext() == null) {
            throw new BizRuntimeException(E100002, "biz spring context is null");
        }
        return bizRuntimeContext;
    }

    private static Object doCreateServiceProxy(Biz biz, Object service, ClassLoader clientClassLoader) {
        if (clientClassLoader == null) {
            Class<?> callerClass = ReflectionUtils.getCallerClass(5);
            clientClassLoader = callerClass.getClassLoader();
        }

        BizRuntimeContext bizRuntimeContext = BizRuntimeContextRegistry.getBizRuntimeContextByClassLoader(clientClassLoader);
        Map<ClassLoader, Map<String, ServiceProxyCache>> serviceProxyCaches =
                bizRuntimeContext.getServiceProxyCaches();
        Map<String, ServiceProxyCache> cacheMap = serviceProxyCaches.computeIfAbsent(service.getClass().getClassLoader(), o -> new ConcurrentHashMap<>());
        // 服务端模块被卸载时，cacheMap会被清空，需要重新生成proxy并缓存
        if (cacheMap.containsKey(service.getClass().getName())) {
            ServiceProxyCache serviceProxyCache = cacheMap.get(service.getClass().getName());
            return serviceProxyCache.getProxy();
        }

        SpringServiceInvoker serviceInvoker = new SpringServiceInvoker(service, biz.getBizName(),
            biz.getBizVersion(), biz.getIdentity(), clientClassLoader, service
                .getClass().getClassLoader());
        ProxyFactory factory = new ProxyFactory();
        Class<?> targetClass = service.getClass();
        if (targetClass.isInterface()) {
            factory.addInterface(targetClass);
        } else {
            factory.setTargetClass(targetClass);
            factory.setProxyTargetClass(true);
        }
        factory.addAdvice(serviceInvoker);
        Object proxy = factory.getProxy(clientClassLoader);

        cacheMap.put(service.getClass().getName(), new ServiceProxyCache(proxy, serviceInvoker));

        return proxy;
    }

    public static Biz determineMostSuitableBiz(String moduleName, String moduleVersion) {
        Biz biz;
        if (StringUtils.isEmpty(moduleVersion)) {
            List<Biz> bizList = ArkClient.getBizManagerService().getBiz(moduleName);
            biz = bizList.stream().filter(it -> BizState.ACTIVATED == it.getBizState()).findFirst().get();
        } else {
            biz = ArkClient.getBizManagerService().getBiz(moduleName, moduleVersion);
        }
        return biz;
    }
}
