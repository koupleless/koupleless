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
package com.alipay.sofa.koupleless.support.dubbo;

import org.apache.dubbo.common.context.FrameworkExt;
import org.apache.dubbo.common.extension.Wrapper;
import org.apache.dubbo.common.logger.Logger;
import org.apache.dubbo.common.logger.LoggerFactory;
import org.apache.dubbo.config.AbstractConfig;
import org.apache.dubbo.config.context.ConfigManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyMap;
import static org.apache.dubbo.common.constants.CommonConstants.DEFAULT_KEY;
import static org.apache.dubbo.common.utils.ReflectUtils.getProperty;
import static org.apache.dubbo.common.utils.StringUtils.isNotEmpty;
import static org.apache.dubbo.config.AbstractConfig.getTagName;

/**
 * @author: yuanyuan
 * @date: 2023/12/22 8:02 下午
 */
@Wrapper(matches = { "config" })
public class KouplelessConfigManager extends ConfigManager {

    private static final Logger                                      logger             = LoggerFactory
                                                                                            .getLogger(ConfigManager.class);

    final Map<ClassLoader, Map<String, Map<String, AbstractConfig>>> globalConfigsCache = new HashMap<>();

    private final ReadWriteLock                                      lock               = new ReentrantReadWriteLock();

    private ConfigManager                                            source;

    public KouplelessConfigManager(FrameworkExt frameworkExt) {
        if (frameworkExt instanceof ConfigManager) {
            this.source = (ConfigManager) frameworkExt;
        }
    }

    @Override
    public void addConfig(AbstractConfig config, boolean unique) {
        if (config == null) {
            return;
        }

        write(() -> {
            Map<String, AbstractConfig> configsMap = getCurrentConfigsCache().computeIfAbsent(getTagName(config.getClass()), type -> newMap());
            addIfAbsent(config, configsMap, unique);
        });
    }

    private Map<String, Map<String, AbstractConfig>> getCurrentConfigsCache() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        globalConfigsCache.computeIfAbsent(contextClassLoader, k -> new HashMap<>());
        return globalConfigsCache.get(contextClassLoader);
    }

    //    @Override
    //    public Map<String, AbstractConfig> getConfigsMap(String configType) {
    //        return read(() -> getCurrentConfigsCache().getOrDefault(configType, emptyMap()));
    //    }

    @Override
    public <C extends AbstractConfig> Map<String, C> getConfigsMap(String configType) {
        return (Map<String, C>) read(() -> getCurrentConfigsCache().getOrDefault(configType, emptyMap()));
    }

    @Override
    public <C extends AbstractConfig> Collection<C> getConfigs(String configType) {
        return (Collection<C>) read(() -> getConfigsMap(configType).values());
    }

    @Override
    public <C extends AbstractConfig> C getConfig(String configType, String id) {
        return read(() -> {
            Map<String, C> configsMap = (Map) getCurrentConfigsCache().getOrDefault(configType, emptyMap());
            return configsMap.get(id);
        });
    }

    @Override
    public <C extends AbstractConfig> C getConfig(String configType) throws IllegalStateException {
        return read(() -> {
            Map<String, C> configsMap = (Map) getCurrentConfigsCache().getOrDefault(configType, emptyMap());
            int size = configsMap.size();
            if (size < 1) {
//                throw new IllegalStateException("No such " + configType.getName() + " is found");
                return null;
            } else if (size > 1) {

                AtomicReference<C> defaultConfig = new AtomicReference<>();
                configsMap.forEach((str, config) -> {
                    if (Boolean.TRUE.equals(config.isDefault())) {
                        defaultConfig.compareAndSet(null, config);
                    }
                });

                if (defaultConfig.get() != null) {
                    return defaultConfig.get();
                }

                logger.warn("Expected single matching of " + configType + ", but found " + size + " instances, will randomly pick the first one.");
            }

            return configsMap.values().iterator().next();
        });
    }

    private void write(Runnable runnable) {
        write(() -> {
            runnable.run();
            return null;
        });
    }

    private <V> V write(Callable<V> callable) {
        V value = null;
        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            value = callable.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException(e.getCause());
        } finally {
            writeLock.unlock();
        }
        return value;
    }

    private <V> V read(Callable<V> callable) {
        Lock readLock = lock.readLock();
        V value = null;
        try {
            readLock.lock();
            value = callable.call();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            readLock.unlock();
        }
        return value;
    }

    private static Map newMap() {
        return new HashMap<>();
    }

    static <C extends AbstractConfig> void addIfAbsent(C config, Map<String, C> configsMap, boolean unique)
            throws IllegalStateException {

        if (config == null || configsMap == null) {
            return;
        }

        if (unique) { // check duplicate
            configsMap.values().forEach(c -> {
                checkDuplicate(c, config);
            });
        }

        String key = getId(config);

        C existedConfig = configsMap.get(key);

        if (existedConfig != null && !config.equals(existedConfig)) {
            if (logger.isWarnEnabled()) {
                String type = config.getClass().getSimpleName();
                logger.warn(String.format("Duplicate %s found, there already has one default %s or more than two %ss have the same id, " +
                        "you can try to give each %s a different id : %s", type, type, type, type, config));
            }
        } else {
            configsMap.put(key, config);
        }
    }

    private static void checkDuplicate(AbstractConfig oldOne, AbstractConfig newOne)
                                                                                    throws IllegalStateException {
        if (oldOne != null && !oldOne.equals(newOne)) {
            String configName = oldOne.getClass().getSimpleName();
            logger.warn("Duplicate Config found for " + configName
                        + ", you should use only one unique " + configName
                        + " for one application.");
        }
    }

    static <C extends AbstractConfig> String getId(C config) {
        String id = config.getId();
        return isNotEmpty(id) ? id : isDefaultConfig(config) ? config.getClass().getSimpleName()
                                                               + "#" + DEFAULT_KEY : null;
    }

    static <C extends AbstractConfig> boolean isDefaultConfig(C config) {
        Boolean isDefault = getProperty(config, "isDefault");
        return isDefault == null || TRUE.equals(isDefault);
    }
}
