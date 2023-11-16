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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.springframework.boot.logging.LogFile;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.logback.LogbackLoggingSystem;
import org.springframework.util.StringUtils;
import org.springframework.util.SystemPropertyUtils;

/**
 * 支持将配置转换为 logback context.
 *
 * @author : chenlei3641
 */
public class SOFAServerlessLogbackLoggingSystem extends LogbackLoggingSystem {
    private static final String      CONFIGURATION_FILE_PROPERTY = "logback.configurationFile";

    private static final TurboFilter FILTER                      = new TurboFilter() {

                                                                     @Override
                                                                     public FilterReply decide(Marker marker,
                                                                                               ch.qos.logback.classic.Logger logger,
                                                                                               Level level,
                                                                                               String format,
                                                                                               Object[] params,
                                                                                               Throwable t) {
                                                                         return FilterReply.DENY;
                                                                     }

                                                                 };

    public SOFAServerlessLogbackLoggingSystem(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public void initialize(LoggingInitializationContext initializationContext,
                           String configLocation, LogFile logFile) {
        LoggerContext loggerContext = getLoggerContext();
        if (isAlreadyInitialized(loggerContext)) {
            return;
        }
        //--
        if (StringUtils.hasLength(configLocation)) {
            initializeWithSpecificConfig(initializationContext, configLocation, logFile);
            return;
        }
        initializeWithConventions(initializationContext, logFile);
        //--
        loggerContext.getTurboFilterList().remove(FILTER);
        markAsInitialized(loggerContext);
        if (StringUtils.hasText(System.getProperty(CONFIGURATION_FILE_PROPERTY))) {
            getLogger(SOFAServerlessLogbackLoggingSystem.class.getName()).warn(
                "Ignoring '" + CONFIGURATION_FILE_PROPERTY
                        + "' system property. Please use 'logging.config' instead.");
        }
    }

    private void initializeWithSpecificConfig(LoggingInitializationContext initializationContext,
                                              String configLocation, LogFile logFile) {
        configLocation = SystemPropertyUtils.resolvePlaceholders(configLocation);
        loadConfiguration(initializationContext, configLocation, logFile);
    }

    private void initializeWithConventions(LoggingInitializationContext initializationContext,
                                           LogFile logFile) {
        String config = getSelfInitializationConfig();
        if (config != null && logFile == null) {
            // self initialization has occurred, reinitialize in case of property changes
            reinitialize(initializationContext);
            return;
        }
        if (config == null) {
            config = getSpringInitializationConfig();
        }
        if (config != null) {
            loadConfiguration(initializationContext, config, logFile);
            return;
        }
        loadDefaults(initializationContext, logFile);
    }

    private LoggerContext getLoggerContext() {
        return SOFAServerlessLogbackLogManagerAdapter.getContext(Thread.currentThread()
            .getContextClassLoader());
    }

    private boolean isAlreadyInitialized(LoggerContext loggerContext) {
        return loggerContext.getObject(LoggingSystem.class.getName()) != null;
    }

    private void markAsInitialized(LoggerContext loggerContext) {
        loggerContext.putObject(LoggingSystem.class.getName(), new Object());
    }

    private ch.qos.logback.classic.Logger getLogger(String name) {
        LoggerContext factory = getLoggerContext();
        return factory.getLogger(getLoggerName(name));
    }

    private String getLoggerName(String name) {
        if (!StringUtils.hasLength(name) || Logger.ROOT_LOGGER_NAME.equals(name)) {
            return ROOT_LOGGER_NAME;
        }
        return name;
    }
}
