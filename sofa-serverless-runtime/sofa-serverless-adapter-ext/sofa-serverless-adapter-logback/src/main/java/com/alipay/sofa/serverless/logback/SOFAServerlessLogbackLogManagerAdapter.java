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
package com.alipay.sofa.serverless.logback;

import ch.qos.logback.classic.LoggerContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 按ClassLoader生成 logback context.
 *
 * @author : chenlei3641
 */
public class SOFAServerlessLogbackLogManagerAdapter {
    private static final Map<ClassLoader, LoggerContext> CLASS_LOADER_LOGGER_CONTEXT = new HashMap<>();

    public static LoggerContext getContext(ClassLoader cls) {
        LoggerContext loggerContext = CLASS_LOADER_LOGGER_CONTEXT.get(cls);
        if (null == loggerContext) {
            synchronized (SOFAServerlessLogbackLogManagerAdapter.class) {
                loggerContext = CLASS_LOADER_LOGGER_CONTEXT.get(cls);
                if (null == loggerContext) {
                    loggerContext = new LoggerContext();
                    loggerContext.setName(cls.toString());
                    CLASS_LOADER_LOGGER_CONTEXT.put(cls, loggerContext);
                }
            }
        }
        return loggerContext;
    }

    public static void clearContext(ClassLoader cls) {
        CLASS_LOADER_LOGGER_CONTEXT.remove(cls);
    }

    public static List<String> getContextNames(){
        return CLASS_LOADER_LOGGER_CONTEXT.values().stream().map(LoggerContext::getName).collect(Collectors.toList());
    }
}
