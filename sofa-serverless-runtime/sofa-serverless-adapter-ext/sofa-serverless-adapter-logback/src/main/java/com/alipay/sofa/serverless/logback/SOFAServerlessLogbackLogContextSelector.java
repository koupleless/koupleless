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
import ch.qos.logback.classic.selector.ContextSelector;

import java.util.List;

/**
 * 支持将配置转换为 logback context.
 *
 * @author : chenlei3641
 */
public class SOFAServerlessLogbackLogContextSelector implements ContextSelector {

    private LoggerContext defaultLoggerContext;

    public SOFAServerlessLogbackLogContextSelector(LoggerContext loggerContext) {
        this.defaultLoggerContext = loggerContext;
    }

    @Override
    public LoggerContext getLoggerContext() {
        return SOFAServerlessLogbackLogManagerAdapter.getContext(Thread.currentThread()
            .getContextClassLoader());
    }

    @Override
    public LoggerContext getLoggerContext(String s) {
        return SOFAServerlessLogbackLogManagerAdapter.getContext(Thread.currentThread()
            .getContextClassLoader());
    }

    @Override
    public LoggerContext getDefaultLoggerContext() {
        return defaultLoggerContext;
    }

    @Override
    public LoggerContext detachLoggerContext(String s) {
        return SOFAServerlessLogbackLogManagerAdapter.getContext(Thread.currentThread()
            .getContextClassLoader());
    }

    @Override
    public List<String> getContextNames() {
        return SOFAServerlessLogbackLogManagerAdapter.getContextNames();
    }
}
