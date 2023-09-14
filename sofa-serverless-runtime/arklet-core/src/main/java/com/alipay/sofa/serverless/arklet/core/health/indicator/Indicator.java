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

/**
 * @author: yuanyuan
 * @date: 2023/9/25 11:37 下午
 */
<<<<<<<< HEAD:sofa-serverless-runtime/sofa-serverless-common/src/main/java/com/alipay/sofa/serverless/common/service/ServiceProxyCache.java
public class ServiceProxyCache {
========
public abstract class Indicator {
>>>>>>>> bf47eef (optimize code):sofa-serverless-runtime/arklet-core/src/main/java/com/alipay/sofa/serverless/arklet/core/health/indicator/Indicator.java

    private Object               proxy;

<<<<<<<< HEAD:sofa-serverless-runtime/sofa-serverless-common/src/main/java/com/alipay/sofa/serverless/common/service/ServiceProxyCache.java
    private SpringServiceInvoker invoker;

    public ServiceProxyCache(Object proxy, SpringServiceInvoker invoker) {
        this.proxy = proxy;
        this.invoker = invoker;
    }

    public Object getProxy() {
        return proxy;
    }

    public void setProxy(Object proxy) {
        this.proxy = proxy;
    }

    public SpringServiceInvoker getInvoker() {
        return invoker;
    }

    public void setInvoker(SpringServiceInvoker invoker) {
        this.invoker = invoker;
========
    public Indicator(String indicatorId) {
        this.indicatorId = indicatorId;
    }

    /**
     * get health details
     * @return a map of health details
     */
    protected abstract Map<String, Object> getHealthDetails();

    /**
     * get indicator id
     * @return indicator id
     */
    public String getIndicatorId() {
        return indicatorId;
    }

    /**
     * get health model
     * @param builder input health builder
     * @return health model
     */
    public Health getHealthModel(HealthBuilder builder) {
        return builder.init().putHealthData(getIndicatorId(), getHealthDetails()).build();
>>>>>>>> bf47eef (optimize code):sofa-serverless-runtime/arklet-core/src/main/java/com/alipay/sofa/serverless/arklet/core/health/indicator/Indicator.java
    }
}
