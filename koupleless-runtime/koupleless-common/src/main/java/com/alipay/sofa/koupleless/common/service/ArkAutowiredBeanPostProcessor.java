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
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.koupleless.common.api.AutowiredFromBase;
import com.alipay.sofa.koupleless.common.api.AutowiredFromBiz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.alipay.sofa.koupleless.common.service.ServiceProxyFactory.determineMostSuitableBiz;

/**
 * @author: yuanyuan
 * @date: 2023/9/26 11:29 上午
 */
public class ArkAutowiredBeanPostProcessor implements BeanPostProcessor {

    private static final Logger LOGGER = LoggerFactory
                                           .getLogger(ArkAutowiredBeanPostProcessor.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClassType = bean.getClass();

        ReflectionUtils.doWithFields(beanClassType, field -> {
            LOGGER.info("Processing bean [{}] field [{}]", beanName, field);

            AutowiredFromBase autowiredFromBase = field.getAnnotation(AutowiredFromBase.class);
            AutowiredFromBiz autowiredFromBiz = field.getAnnotation(AutowiredFromBiz.class);

            String bizName;
            String bizVersion;
            String name;
            boolean required;
            if (autowiredFromBase != null) {
                Biz masterBiz = ArkClient.getMasterBiz();
                bizName = masterBiz.getBizName();
                bizVersion = masterBiz.getBizVersion();
                name = autowiredFromBase.name();
                required = autowiredFromBase.required();
            } else if (autowiredFromBiz != null) {
                bizName = autowiredFromBiz.bizName();
                bizVersion = autowiredFromBiz.bizVersion();
                name = autowiredFromBiz.name();
                required = autowiredFromBiz.required();
            } else {
                return;
            }

            ClassLoader clientClassLoader = Thread.currentThread().getContextClassLoader();

            Object serviceProxy = null;
            try {
                Class<?> fieldType = field.getType();
                if (StringUtils.hasText(name)) {
                    serviceProxy = ServiceProxyFactory.createServiceProxy(bizName, bizVersion, name, fieldType, clientClassLoader);
                }

                if (serviceProxy == null) {
                    if (!Collection.class.isAssignableFrom(fieldType) && !Map.class.isAssignableFrom(fieldType)) {
                        serviceProxy = ServiceProxyFactory.createServiceProxy(bizName, bizVersion, null, fieldType, clientClassLoader);
                    }
                }

                if (serviceProxy == null) {
                    Type genericType = field.getGenericType();
                    ParameterizedType parameterizedType = (ParameterizedType) genericType;
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    Class<?> serviceType = (Class<?>) actualTypeArguments[0];
                    if (Map.class.isAssignableFrom(fieldType)) {
                        serviceType = (Class<?>) actualTypeArguments[1];
                    }

                    Map<String, ?> serviceProxyMap = ServiceProxyFactory.batchCreateServiceProxy(bizName, bizVersion, serviceType, clientClassLoader);

                    if (Map.class.isAssignableFrom(fieldType)) {
                        serviceProxy = serviceProxyMap;
                    }

                    if (List.class.isAssignableFrom(fieldType)) {
                        List list = ArrayList.class.newInstance();
                        list.addAll(serviceProxyMap.values());
                        serviceProxy = list;
                    }

                    if (Set.class.isAssignableFrom(fieldType)) {
                        Set set = HashSet.class.newInstance();
                        set.addAll(serviceProxyMap.values());
                        serviceProxy = set;
                    }

                }
            } catch (Exception e) {
                if (required) {
                    throw new BeanCreationException(beanName, "Failed processing bean [" + beanName + "], injected object to bean [" + bean + "] field [" + field + "]", e);
                }
            }

            if (serviceProxy != null) {
                ReflectionUtils.makeAccessible(field);
                ReflectionUtils.setField(field, bean, serviceProxy);
                LOGGER.info("Finished processing bean [{}], success to inject service proxy to bean [{}] field [{}]", beanName, bean, field);
            }

        }, field -> !Modifier.isStatic(field.getModifiers())
                && (field.isAnnotationPresent(AutowiredFromBase.class) || field.isAnnotationPresent(AutowiredFromBiz.class)));

        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
                                                                              throws BeansException {
        return bean;
    }
}
