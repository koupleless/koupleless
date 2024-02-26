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
package com.alipay.sofa.koupleless.common.service;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;
import com.alipay.sofa.koupleless.common.exception.BizRuntimeException;
import com.alipay.sofa.koupleless.common.util.ReflectionUtils;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.alipay.sofa.koupleless.common.exception.ErrorCodes.SpringContextManager.E100002;
import static com.alipay.sofa.koupleless.common.exception.ErrorCodes.SpringContextManager.E100003;
import static com.alipay.sofa.koupleless.common.exception.ErrorCodes.SpringContextManager.E100004;
import static com.alipay.sofa.koupleless.common.exception.ErrorCodes.SpringContextManager.E100005;

/**
 * @author: yuanyuan
 * @date: 2023/9/21 9:55 下午
 */
public class ServiceProxyFactory {

    public static <T> T createServiceProxy(String bizName, String bizVersion, String name,
                                           Class<T> clientType, ClassLoader clientClassLoader) {
        Object service = getService(bizName, bizVersion, name, clientType);
        return doCreateServiceProxy(bizName, bizVersion, service, name, clientType,
            clientClassLoader);
    }

    public static <T> Map<String, T> batchCreateServiceProxy(String bizName, String bizVersion,
                                                             Class<T> serviceType,
                                                             ClassLoader clientClassLoader) {
        Biz biz = ArkClient.getBizManagerService().getBiz(bizName, bizVersion);
        Class<?> serviceClass = checkBizStateAndGetTargetClass(biz, serviceType);
        Map<String, ?> serviceMap = listService(biz, serviceClass);
        Map<String, T> proxyMap = new HashMap<>();
        for (String beanName : serviceMap.keySet()) {
            proxyMap.put(
                beanName,
                doCreateServiceProxy(biz.getBizName(), biz.getBizVersion(),
                    serviceMap.get(beanName), null, serviceType, clientClassLoader));
        }
        return proxyMap;
    }

    public static Object getService(String bizName, String bizVersion, String name,
                                    Class<?> clientType) {
        Biz biz = determineMostSuitableBiz(bizName, bizVersion);

        if (biz == null) {
            return null;
        } else if (biz.getBizState() != BizState.ACTIVATED
                   && biz.getBizState() != BizState.DEACTIVATED) {
            throw new BizRuntimeException(E100004, String.format("biz %s:%s state %s is not valid",
                bizName, bizVersion, biz.getBizState()));
        }

        BizRuntimeContext bizRuntimeContext = BizRuntimeContextRegistry.getBizRuntimeContext(biz);
        if (bizRuntimeContext.getRootApplicationContext() == null) {
            throw new BizRuntimeException(E100002, String.format(
                "biz %s:%s spring context is null", bizName, bizVersion));
        }

        if (!StringUtils.isEmpty(name)) {
            return bizRuntimeContext.getRootApplicationContext().getBean(name);
        }

        if (clientType != null) {
            Class<?> serviceType;
            try {
                serviceType = biz.getBizClassLoader().loadClass(clientType.getName());
            } catch (ClassNotFoundException e) {
                throw new BizRuntimeException(E100005,
                    String.format("Cannot find class %s from the biz %s", clientType.getName(),
                        biz.getIdentity()));
            }
            return bizRuntimeContext.getRootApplicationContext().getBean(serviceType);
        }

        throw new BizRuntimeException(E100002, "invalid config");
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

    /**
     * @param bizName 目标 biz 名称
     * @param bizVersion 目标 biz 版本
     * @param service  目标biz中符合条件的bean
     * @param clientType  调用方serviceType
     * @param clientClassLoader  调用方classloader
     * @return 供调用方调用的代理对象
     */
    private static <T> T doCreateServiceProxy(String bizName, String bizVersion, Object service, String name, Class<T> clientType, ClassLoader clientClassLoader) {
        if (clientClassLoader == null) {
            Class<?> callerClass = ReflectionUtils.getCallerClass(6);
            clientClassLoader = callerClass.getClassLoader();
        }

        BizRuntimeContext bizRuntimeContext = BizRuntimeContextRegistry.getBizRuntimeContextByClassLoader(clientClassLoader);
        Map<ClassLoader, Map<String, ServiceProxyCache>> serviceProxyCaches =
                bizRuntimeContext.getServiceProxyCaches();

        Biz biz = determineMostSuitableBiz(bizName, bizVersion);
        if (biz != null) {
            Map<String, ServiceProxyCache> cacheMap = serviceProxyCaches.computeIfAbsent(biz.getBizClassLoader(), o -> new ConcurrentHashMap<>());
            // 服务端模块被卸载时，cacheMap会被清空，需要重新生成proxy并缓存
            String cacheKey = service != null ? service.getClass().getName() : clientType.getName();
            if (cacheMap.containsKey(cacheKey)) {
                ServiceProxyCache serviceProxyCache = cacheMap.get(cacheKey);
                return (T) serviceProxyCache.getProxy();
            }
        }

        SpringServiceInvoker serviceInvoker = new SpringServiceInvoker(service, name, clientType, bizName, bizVersion, clientClassLoader, service != null ? service.getClass().getClassLoader() : null);
        ProxyFactory factory = new ProxyFactory();
        if (clientType.isInterface()) {
            factory.addInterface(clientType);
        } else {
            factory.setTargetClass(clientType);
            factory.setProxyTargetClass(true);
        }
        factory.addAdvice(serviceInvoker);
        Object proxy = factory.getProxy(clientClassLoader);

        if (biz != null) {
            String cacheKey = service != null ? service.getClass().getName() : clientType.getName();
            Map<String, ServiceProxyCache> cacheMap = serviceProxyCaches.computeIfAbsent(biz.getBizClassLoader(), o -> new ConcurrentHashMap<>());
            cacheMap.put(cacheKey, new ServiceProxyCache(proxy, serviceInvoker));
        }

        return (T) proxy;
    }

    public static Biz determineMostSuitableBiz(String moduleName, String moduleVersion) {
        Biz biz;
        if (StringUtils.isEmpty(moduleVersion)) {
            List<Biz> bizList = ArkClient.getBizManagerService().getBiz(moduleName);
            if (bizList.size() == 0) {
                return null;
            }
            biz = bizList.stream().filter(it -> BizState.ACTIVATED == it.getBizState()).findFirst().get();
        } else {
            biz = ArkClient.getBizManagerService().getBiz(moduleName, moduleVersion);
        }
        return biz;
    }

    private static Class<?> checkBizStateAndGetTargetClass(Biz biz, Class<?> sourceClass) {
        checkBizState(biz);
        try {
            return biz.getBizClassLoader().loadClass(sourceClass.getName());
        } catch (ClassNotFoundException e) {
            throw new BizRuntimeException(E100005, "Cannot find class " + sourceClass.getName()
                                                   + " from the biz " + biz.getIdentity());
        }
    }

    private static BizRuntimeContext checkBizStateAndGetBizRuntimeContext(Biz biz) {
        checkBizState(biz);
        BizRuntimeContext bizRuntimeContext = BizRuntimeContextRegistry.getBizRuntimeContext(biz);
        if (bizRuntimeContext == null) {
            throw new BizRuntimeException(E100002, "biz runtime context is null");
        }
        if (bizRuntimeContext.getRootApplicationContext() == null) {
            throw new BizRuntimeException(E100002, "biz spring context is null");
        }
        return bizRuntimeContext;
    }

    private static void checkBizState(Biz biz) {
        if (biz == null) {
            throw new BizRuntimeException(E100003, "biz does not exist");
        }
        if (biz.getBizState() != BizState.ACTIVATED && biz.getBizState() != BizState.DEACTIVATED) {
            throw new BizRuntimeException(E100004, "biz state is not valid");
        }
    }
}
