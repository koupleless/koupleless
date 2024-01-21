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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class BizRuntimeContextRegistry {
    private static ConcurrentHashMap<ClassLoader, BizRuntimeContext> contextMap = new ConcurrentHashMap<>();

    public static void registerBizRuntimeManager(BizRuntimeContext bizRuntimeContext) {
        contextMap.put(bizRuntimeContext.getAppClassLoader(), bizRuntimeContext);
    }

    public static void unRegisterBizRuntimeManager(BizRuntimeContext bizRuntimeContext) {
        contextMap.remove(bizRuntimeContext.getAppClassLoader());
    }

    public static Set<BizRuntimeContext> getRuntimeSet() {
        return Collections.unmodifiableSet(new CopyOnWriteArraySet<>(contextMap.values()));
    }

    public static ConcurrentHashMap<ClassLoader, BizRuntimeContext> getRuntimeMap() {
        return contextMap;
    }

    /**
     * 获取 biz 对应的 SofaRuntimeManager
     * @param biz
     * @return
     */
    public static BizRuntimeContext getBizRuntimeContext(Biz biz) {
        if (BizRuntimeContextRegistry.getRuntimeMap().containsKey(biz.getBizClassLoader())) {
            return BizRuntimeContextRegistry.getRuntimeMap().get(biz.getBizClassLoader());
        }

        throw new BizRuntimeException(ErrorCodes.SpringContextManager.E100002,
            "No BizRuntimeContext found for biz: " + biz.getBizName());
    }

    public static BizRuntimeContext getBizRuntimeContextByClassLoader(ClassLoader classLoader) {
        if (BizRuntimeContextRegistry.getRuntimeMap().containsKey(classLoader)) {
            return BizRuntimeContextRegistry.getRuntimeMap().get(classLoader);
        }

        throw new BizRuntimeException(ErrorCodes.SpringContextManager.E100002,
            "No BizRuntimeContext found for classLoader: " + classLoader);
    }
}
