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
package com.alipay.sofa.koupleless.common;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.koupleless.common.exception.BizRuntimeException;
import com.alipay.sofa.koupleless.common.exception.ErrorCodes;
import com.alipay.sofa.koupleless.common.service.ServiceProxyCache;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BizRuntimeContext {

    private String                                           bizName;

    private ClassLoader                                      appClassLoader;

    private ApplicationContext                               rootApplicationContext;

    private Map<ClassLoader, Map<String, ServiceProxyCache>> serviceProxyCaches = new ConcurrentHashMap<>();

    public String getBizName() {
        return bizName;
    }

    public void setBizName(String bizName) {
        this.bizName = bizName;
    }

    public ClassLoader getAppClassLoader() {
        return appClassLoader;
    }

    public void setAppClassLoader(ClassLoader appClassLoader) {
        this.appClassLoader = appClassLoader;
    }

    public ApplicationContext getRootApplicationContext() {
        return rootApplicationContext;
    }

    public void setRootApplicationContext(ApplicationContext rootApplicationContext) {
        this.rootApplicationContext = rootApplicationContext;
    }

    public BizRuntimeContext(Biz biz) {
        this(biz, null);
    }

    public BizRuntimeContext(Biz biz, ApplicationContext applicationContext) {
        this.bizName = biz.getBizName();
        this.appClassLoader = biz.getBizClassLoader();
        this.rootApplicationContext = applicationContext;
    }

    public Map<ClassLoader, Map<String, ServiceProxyCache>> getServiceProxyCaches() {
        return serviceProxyCaches;
    }

    public void removeServiceProxyCaches(ClassLoader classLoader) {
        serviceProxyCaches.remove(classLoader);
    }

    /**
     * 方法名为 shutdown() 会导致卸载时候调用两次
     */
    public void shutdownContext() {
        try {
            AbstractApplicationContext applicationContext = (AbstractApplicationContext) rootApplicationContext;
            // only need shutdown when root context is active
            if (applicationContext.isActive()) {
                applicationContext.close();
            }
            appClassLoader = null;
        } catch (Throwable throwable) {
            throw new BizRuntimeException(ErrorCodes.SpringContextManager.E100001, throwable);
        }
    }

}
