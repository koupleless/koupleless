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
package com.alibaba.dubbo.common;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.config.model.ApplicationModel;
import com.alibaba.dubbo.config.model.ConsumerModel;
import com.alibaba.dubbo.config.model.ProviderModel;
import com.alibaba.dubbo.config.spring.ReferenceBean;
import com.alibaba.dubbo.config.spring.ServiceBean;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Field;

public class ClassLoaderUtil {
    static Field                serviceBeanApplicationContextField;
    static Field                referenceBeanApplicationContextField;
    private static final Logger log = LoggerFactory.getLogger(ClassLoaderUtil.class);

    static {
        try {
            serviceBeanApplicationContextField = ServiceBean.class
                .getDeclaredField("applicationContext");
            serviceBeanApplicationContextField.setAccessible(true);
            referenceBeanApplicationContextField = ReferenceBean.class
                .getDeclaredField("applicationContext");
            referenceBeanApplicationContextField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static ClassLoader getClassLoaderByPath(String path) {
        ProviderModel providerModel = null;
        for (ProviderModel iter : ApplicationModel.allProviderModels()) {
            if (iter.getServiceName().contains(path)) {
                providerModel = iter;
                break;
            }
        }
        if (providerModel == null) {
            log.warn("can not find classloader by path:" + path);
            return ClassLoader.getSystemClassLoader();
        }
        ServiceBean serviceBean = (ServiceBean) providerModel.getMetadata();
        try {
            ApplicationContext applicationContext = (ApplicationContext) serviceBeanApplicationContextField
                .get(serviceBean);
            ClassLoader classLoader = applicationContext.getClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            return classLoader;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static ClassLoader getClassLoaderByServiceNameAndApplication(String serviceName,
                                                                        String application) {
        ConsumerModel consumerModel = ApplicationModel.getConsumerModel(ApplicationModel
            .getConsumerModelKey(serviceName, application));
        if (consumerModel == null)
            return ClassLoader.getSystemClassLoader();
        ReferenceBean referenceBean = (ReferenceBean) consumerModel.getMetadata();
        try {
            ApplicationContext applicationContext = (ApplicationContext) referenceBeanApplicationContextField
                .get(referenceBean);
            ClassLoader classLoader = applicationContext.getClassLoader();
            Thread.currentThread().setContextClassLoader(classLoader);
            return classLoader;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
