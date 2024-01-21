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
package com.alipay.sofa.koupleless.spring.loader;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.boot.loader.LaunchedURLClassLoader;
import org.springframework.boot.loader.archive.Archive;

/**
 * A cached LaunchedURLClassLoader to accelerate load classes and resources
 * @author zjulbj
 * @daye 2023/12/26
 * @version CachedLaunchedURLClassLoader.java, v 0.1 2023年12月26日 14:45 syd
 */
public class CachedLaunchedURLClassLoader extends LaunchedURLClassLoader {
    private static final int                   ENTRY_CACHE_SIZE  = Integer.getInteger(
                                                                     "serverless.class.cache.size",
                                                                     6000);
    private static final Object                NOT_FOUND         = new Object();
    protected final Map<String, Object>        classCache        = Collections
                                                                     .synchronizedMap(new LinkedHashMap<String, Object>(
                                                                         ENTRY_CACHE_SIZE, 0.75f,
                                                                         true) {
                                                                         @Override
                                                                         protected boolean removeEldestEntry(Map.Entry<String, Object> eldest) {
                                                                             return size() >= ENTRY_CACHE_SIZE;
                                                                         }
                                                                     });
    protected final Map<String, Optional<URL>> resourceUrlCache  = Collections
                                                                     .synchronizedMap(new LinkedHashMap<String, Optional<URL>>(
                                                                         ENTRY_CACHE_SIZE, 0.75f,
                                                                         true) {
                                                                         @Override
                                                                         protected boolean removeEldestEntry(Map.Entry<String, Optional<URL>> eldest) {
                                                                             return size() >= ENTRY_CACHE_SIZE;
                                                                         }
                                                                     });
    protected final Map<String, Optional>      resourcesUrlCache = Collections
                                                                     .synchronizedMap(new LinkedHashMap<String, Optional>(
                                                                         ENTRY_CACHE_SIZE, 0.75f,
                                                                         true) {
                                                                         @Override
                                                                         protected boolean removeEldestEntry(Map.Entry<String, Optional> eldest) {
                                                                             return size() >= ENTRY_CACHE_SIZE;
                                                                         }
                                                                     });

    static {
        ClassLoader.registerAsParallelCapable();
    }

    public CachedLaunchedURLClassLoader(boolean exploded, Archive rootArchive, URL[] urls,
                                        ClassLoader parent) {
        super(exploded, rootArchive, urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return loadClassWithCache(name, resolve);
    }

    @Override
    public URL findResource(String name) {
        Optional<URL> urlOptional = resourceUrlCache.get(name);
        if (urlOptional != null) {
            return urlOptional.orElse(null);
        }
        URL url = super.findResource(name);
        resourceUrlCache.put(name, url != null ? Optional.of(url) : Optional.empty());
        return url;
    }

    @Override
    public Enumeration<URL> findResources(String name) throws IOException {
        Optional<Enumeration<URL>> urlOptional = resourcesUrlCache.get(name);
        if (urlOptional != null) {
            return urlOptional.orElse(null);
        }
        Enumeration<URL> enumeration = super.findResources(name);
        if (enumeration == null || !enumeration.hasMoreElements()) {
            resourcesUrlCache.put(name, Optional.empty());
        }
        return enumeration;
    }

    /**
     * NOTE: Only cache ClassNotFoundException when class not found.
     * If class found, do not cache, and just use parent class loader cache.
     *
     * @param name
     * @param resolve
     * @return
     * @throws ClassNotFoundException
     */
    protected Class<?> loadClassWithCache(String name, boolean resolve)
                                                                       throws ClassNotFoundException {
        Object resultInCache = classCache.get(name);
        if (resultInCache == NOT_FOUND) {
            throw new ClassNotFoundException(name);
        }
        try {
            Class<?> clazz = super.findLoadedClass(name);
            if (clazz == null) {
                clazz = super.loadClass(name, resolve);
            }
            return clazz;
        } catch (ClassNotFoundException exception) {
            classCache.put(name, NOT_FOUND);
            throw exception;
        }
    }

    @Override
    public void clearCache() {
        super.clearCache();
        classCache.clear();
        resourceUrlCache.clear();
        resourcesUrlCache.clear();
    }
}
