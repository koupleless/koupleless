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
package com.alipay.sofa.serverless.log4j2;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.core.util.NameUtil;
import org.apache.logging.log4j.message.Message;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.logging.LogFile;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggerConfiguration;
import org.springframework.boot.logging.LoggingInitializationContext;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.logging.LoggingSystemFactory;
import org.springframework.boot.logging.log4j2.Log4J2LoggingSystem;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.SystemPropertyUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;

/**
 * SpringBoot 默认使用 log4j2.6.2 的 API，与 log4j2.8.2 不兼容
 *
 * @author ruoshan
 * @version $Id: AlipayLog4J2LoggingSystem.java, v 0.1 2018年08月24日 12:09 PM ruoshan Exp $
 */
public class SOFAServerlessLog4j2LoggingSystem extends Log4J2LoggingSystem {

    private static final String           FILE_PROTOCOL             = "file";

    private static final String           DEFAULT_LOG4j_CONFIG_PATH = "classpath:org/springframework/boot/logging/log4j2/";

    private static final LogLevels<Level> LEVELS                    = new LogLevels<>();

    static {
        LEVELS.map(LogLevel.TRACE, Level.TRACE);
        LEVELS.map(LogLevel.DEBUG, Level.DEBUG);
        LEVELS.map(LogLevel.INFO, Level.INFO);
        LEVELS.map(LogLevel.WARN, Level.WARN);
        LEVELS.map(LogLevel.ERROR, Level.ERROR);
        LEVELS.map(LogLevel.FATAL, Level.FATAL);
        LEVELS.map(LogLevel.OFF, Level.OFF);
    }

    private static final Filter           FILTER                    = new AbstractFilter() {

                                                                        @Override
                                                                        public Result filter(LogEvent event) {
                                                                            return Result.DENY;
                                                                        }

                                                                        @Override
                                                                        public Result filter(Logger logger,
                                                                                             Level level,
                                                                                             Marker marker,
                                                                                             Message msg,
                                                                                             Throwable t) {
                                                                            return Result.DENY;
                                                                        }

                                                                        @Override
                                                                        public Result filter(Logger logger,
                                                                                             Level level,
                                                                                             Marker marker,
                                                                                             Object msg,
                                                                                             Throwable t) {
                                                                            return Result.DENY;
                                                                        }

                                                                        @Override
                                                                        public Result filter(Logger logger,
                                                                                             Level level,
                                                                                             Marker marker,
                                                                                             String msg,
                                                                                             Object... params) {
                                                                            return Result.DENY;
                                                                        }

                                                                    };

    public SOFAServerlessLog4j2LoggingSystem(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    public void beforeInitialize() {
        LoggerContext loggerContext = getLoggerContext();
        if (isAlreadyInitialized(loggerContext)) {
            return;
        }
        configureJdkLoggingBridgeHandler();
        loggerContext.getConfiguration().addFilter(FILTER);
    }

    private void configureJdkLoggingBridgeHandler() {
        try {
            this.removeJdkLoggingBridgeHandler();
        } catch (Throwable var2) {
        }

    }

    @Override
    public void initialize(LoggingInitializationContext initializationContext,
                           String configLocation, LogFile logFile) {
        LoggerContext loggerContext = getLoggerContext();
        if (isAlreadyInitialized(loggerContext)) {
            return;
        }
        loggerContext.getConfiguration().removeFilter(FILTER);

        if (StringUtils.hasLength(configLocation)) {
            initializeWithSpecificConfig(initializationContext, configLocation, logFile);
            return;
        }
        initializeWithConventions(initializationContext, logFile);

        markAsInitialized(loggerContext);
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

    @Override
    protected void loadDefaults(LoggingInitializationContext initializationContext, LogFile logFile) {
        if (logFile != null) {
            loadConfiguration(DEFAULT_LOG4j_CONFIG_PATH + "log4j2-file.xml", logFile,
                getOverrides(initializationContext));
        } else {
            loadConfiguration(DEFAULT_LOG4j_CONFIG_PATH + "log4j2.xml", logFile,
                getOverrides(initializationContext));
        }
    }

    private List<String> getOverrides(LoggingInitializationContext initializationContext) {
        BindResult<List<String>> overrides = Binder.get(initializationContext.getEnvironment())
            .bind("logging.log4j2.config.override", Bindable.listOf(String.class));
        return overrides.orElse(Collections.emptyList());
    }

    protected void loadConfiguration(String location, LogFile logFile) {
        Assert.notNull(location, "Location must not be null");
        try {
            LoggerContext context = getLoggerContext();
            URL url = ResourceUtils.getURL(location);
            ConfigurationSource source = getConfigurationSource(url);

            context.start(ConfigurationFactory.getInstance().getConfiguration(context, source));
        } catch (Exception ex) {
            throw new IllegalStateException("Could not initialize Log4J2 logging from " + location,
                ex);
        }
    }

    protected void loadConfiguration(String location, LogFile logFile, List<String> overrides) {
        loadConfiguration(location, logFile);
    }

    private ConfigurationSource getConfigurationSource(URL url) throws IOException {
        InputStream stream = url.openStream();
        if (FILE_PROTOCOL.equals(url.getProtocol())) {
            return new ConfigurationSource(stream, ResourceUtils.getFile(url));
        }
        return new ConfigurationSource(stream, url);
    }

    @Override
    protected void reinitialize(LoggingInitializationContext initializationContext) {
        getLoggerContext().reconfigure();
    }

    @Override
    public void setLogLevel(String loggerName, LogLevel logLevel) {
        setLogLevel(loggerName, LEVELS.convertSystemToNative(logLevel));
    }

    private void setLogLevel(String loggerName, Level level) {
        LoggerConfig logger = getLogger(loggerName);
        if (level == null) {
            clearLogLevel(loggerName, logger);
        } else {
            setLogLevel(loggerName, logger, level);
        }
        getLoggerContext().updateLoggers();
    }

    private void clearLogLevel(String loggerName, LoggerConfig logger) {
        if (logger instanceof LevelSetLoggerConfig) {
            getLoggerContext().getConfiguration().removeLogger(loggerName);
        } else {
            logger.setLevel(null);
        }
    }

    private void setLogLevel(String loggerName, LoggerConfig logger, Level level) {
        if (logger == null) {
            getLoggerContext().getConfiguration().addLogger(loggerName,
                new LevelSetLoggerConfig(loggerName, level, true));
        } else {
            logger.setLevel(level);
        }
    }

    @Override
    public List<LoggerConfiguration> getLoggerConfigurations() {
        List<LoggerConfiguration> result = new ArrayList<>();
        getAllLoggers().forEach((name, loggerConfig) -> result.add(convertLoggerConfig(name, loggerConfig)));
        result.sort(CONFIGURATION_COMPARATOR);
        return result;
    }

    @Override
    public LoggerConfiguration getLoggerConfiguration(String loggerName) {
        LoggerConfig loggerConfig = getAllLoggers().get(loggerName);
        return (loggerConfig != null) ? convertLoggerConfig(loggerName, loggerConfig) : null;
    }

    private Map<String, LoggerConfig> getAllLoggers() {
        Map<String, LoggerConfig> loggers = new LinkedHashMap<>();
        for (Logger logger : getLoggerContext().getLoggers()) {
            addLogger(loggers, logger.getName());
        }
        getLoggerContext().getConfiguration().getLoggers().keySet().forEach((name) -> addLogger(loggers, name));
        return loggers;
    }

    private void addLogger(Map<String, LoggerConfig> loggers, String name) {
        Configuration configuration = getLoggerContext().getConfiguration();
        while (name != null) {
            loggers.computeIfAbsent(name, configuration::getLoggerConfig);
            name = getSubName(name);
        }
    }

    private String getSubName(String name) {
        if (!StringUtils.hasLength(name)) {
            return null;
        }
        int nested = name.lastIndexOf('$');
        return (nested != -1) ? name.substring(0, nested) : NameUtil.getSubName(name);
    }

    private LoggerConfiguration convertLoggerConfig(String name, LoggerConfig loggerConfig) {
        if (loggerConfig == null) {
            return null;
        }
        LogLevel level = LEVELS.convertNativeToSystem(loggerConfig.getLevel());
        if (!StringUtils.hasLength(name) || LogManager.ROOT_LOGGER_NAME.equals(name)) {
            name = ROOT_LOGGER_NAME;
        }
        boolean isLoggerConfigured = loggerConfig.getName().equals(name);
        LogLevel configuredLevel = (isLoggerConfigured) ? level : null;
        return new LoggerConfiguration(name, configuredLevel, level);
    }

    @Override
    public void cleanUp() {
        try {
            removeJdkLoggingBridgeHandler();
        } catch (Exception e) {
            // Ignore and continue
        }
        LoggerContext loggerContext = getLoggerContext();
        markAsUninitialized(loggerContext);
        loggerContext.getConfiguration().removeFilter(FILTER);
    }

    private void removeJdkLoggingBridgeHandler() {
        try {
            removeDefaultRootHandler();
        } catch (Throwable ex) {
            // Ignore and continue
        }
    }

    private void removeDefaultRootHandler() {
        try {
            java.util.logging.Logger rootLogger = java.util.logging.LogManager.getLogManager()
                .getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            if (handlers.length == 1 && handlers[0] instanceof ConsoleHandler) {
                rootLogger.removeHandler(handlers[0]);
            }
        } catch (Throwable ex) {
            // Ignore and continue
        }
    }

    private LoggerConfig getLogger(String name) {
        boolean isRootLogger = !StringUtils.hasLength(name) || ROOT_LOGGER_NAME.equals(name);
        return findLogger(isRootLogger ? LogManager.ROOT_LOGGER_NAME : name);
    }

    private LoggerConfig findLogger(String name) {
        Configuration configuration = getLoggerContext().getConfiguration();
        if (configuration instanceof AbstractConfiguration) {
            return ((AbstractConfiguration) configuration).getLogger(name);
        }
        return configuration.getLoggers().get(name);
    }

    private LoggerContext getLoggerContext() {
        return (LoggerContext) LogManager.getContext(
            Thread.currentThread().getContextClassLoader(), false);
    }

    private boolean isAlreadyInitialized(LoggerContext loggerContext) {
        return LoggingSystem.class.getName().equals(loggerContext.getExternalContext());
    }

    private void markAsInitialized(LoggerContext loggerContext) {
        loggerContext.setExternalContext(LoggingSystem.class.getName());
    }

    private void markAsUninitialized(LoggerContext loggerContext) {
        loggerContext.setExternalContext(null);
    }

    /**
     * {@link LoggingSystemFactory} that returns {@link Log4J2LoggingSystem} if possible.
     */
    @Order(Ordered.LOWEST_PRECEDENCE)
    public static class Factory implements LoggingSystemFactory {

        private static final boolean PRESENT = ClassUtils
                                                 .isPresent(
                                                     "org.apache.logging.log4j.core.impl.Log4jContextFactory",
                                                     Log4J2LoggingSystem.Factory.class
                                                         .getClassLoader());

        @Override
        public LoggingSystem getLoggingSystem(ClassLoader classLoader) {
            if (PRESENT) {
                return new Log4J2LoggingSystem(classLoader);
            }
            return null;
        }

    }

    /**
     * {@link LoggerConfig} used when the user has set a specific {@link Level}.
     */
    private static class LevelSetLoggerConfig extends LoggerConfig {

        LevelSetLoggerConfig(String name, Level level, boolean additive) {
            super(name, level, additive);
        }

    }
}