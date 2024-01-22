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
package com.alipay.sofa.koupleless.common.util;

import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Support multi-business Properties<br/>
 * Isolating configuration separation between different business modules<br/>
 * The default value of the configuration of the base application is used<br/>
 * <p>
 * If you want to use, you need to write the code in you base application
 * </p>
 * <code>
 * MultiBizProperties.initSystem();
 * </code>
 *
 * @author qq290584697
 */
public class MultiBizProperties extends Properties {

    private final String                  bizClassLoaderName;

    private static final String           BIZ_CLASS_LOADER = "com.alipay.sofa.ark.container.service.classloader.BizClassLoader";

    private Map<ClassLoader, Set<String>> modifiedKeysMap  = new HashMap<>();

    private final Properties              baseProperties;
    private Map<ClassLoader, Properties>  bizPropertiesMap;

    private MultiBizProperties(String bizClassLoaderName, Properties baseProperties) {
        this.bizPropertiesMap = new HashMap<>();
        this.baseProperties = baseProperties;
        this.bizClassLoaderName = bizClassLoaderName;
    }

    public MultiBizProperties(String bizClassLoaderName) {
        this(bizClassLoaderName, new Properties());
    }

    public synchronized Object setProperty(String key, String value) {
        addModifiedKey(key);
        return getWriteProperties().setProperty(key, value);
    }

    @Override
    public String getProperty(String key) {
        return getReadProperties().getProperty(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return getReadProperties().getProperty(key, defaultValue);
    }

    @Override
    public synchronized void load(Reader reader) throws IOException {
        Properties properties = new Properties();
        properties.load(reader);
        getWriteProperties().putAll(properties);
        addModifiedKeys(properties.stringPropertyNames());
    }

    @Override
    public synchronized void load(InputStream inStream) throws IOException {
        Properties properties = new Properties();
        properties.load(inStream);
        getWriteProperties().putAll(properties);
        addModifiedKeys(properties.stringPropertyNames());
    }

    @Override
    public void list(PrintStream out) {
        getWriteProperties().list(out);
    }

    @Override
    public void list(PrintWriter out) {
        getWriteProperties().list(out);
    }

    @Override
    public void save(OutputStream out, String comments) {
        Properties properties = getWriteProperties();
        properties.save(out, comments);
    }

    @Override
    public void store(Writer writer, String comments) throws IOException {
        Properties properties = getReadProperties();
        properties.store(writer, comments);
    }

    @Override
    public void store(OutputStream out, String comments) throws IOException {
        Properties properties = getReadProperties();
        properties.store(out, comments);
    }

    @Override
    public synchronized void loadFromXML(InputStream in) throws IOException {
        Properties properties = new Properties();
        properties.loadFromXML(in);
        getWriteProperties().putAll(properties);
        addModifiedKeys(properties.stringPropertyNames());
    }

    @Override
    public void storeToXML(OutputStream os, String comment) throws IOException {
        Properties properties = getReadProperties();
        properties.storeToXML(os, comment);
    }

    @Override
    public void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
        Properties properties = getReadProperties();
        properties.storeToXML(os, comment, encoding);
    }

    @Override
    public Enumeration<?> propertyNames() {
        return getReadProperties().propertyNames();
    }

    @Override
    public Set<String> stringPropertyNames() {
        return getReadProperties().stringPropertyNames();
    }

    @Override
    public synchronized boolean remove(Object key, Object value) {
        boolean success = getWriteProperties().remove(key, value);
        if (success) {
            addModifiedKey(key.toString());
        }
        return success;
    }

    @Override
    public synchronized Object get(Object key) {
        return getReadProperties().get(key);
    }

    @Override
    public synchronized Object remove(Object key) {
        if (key != null) {
            addModifiedKey(key.toString());
        }
        return getWriteProperties().remove(key);
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        String text = key == null ? null : key.toString();
        addModifiedKey(text);
        return getWriteProperties().put(key, value);
    }

    @Override
    public synchronized boolean equals(Object o) {
        return getReadProperties().equals(o);
    }

    @Override
    public synchronized String toString() {
        return getReadProperties().toString();
    }

    @Override
    public Collection<Object> values() {
        return getReadProperties().values();
    }

    @Override
    public synchronized int hashCode() {
        return getReadProperties().hashCode();
    }

    @Override
    public synchronized void clear() {
        Set<String> keys = baseProperties.stringPropertyNames();
        getWriteProperties().clear();
        addModifiedKeys(keys);
    }

    @Override
    public synchronized Object clone() {
        MultiBizProperties mbp = new MultiBizProperties(bizClassLoaderName, baseProperties);
        mbp.bizPropertiesMap = new HashMap<>();
        bizPropertiesMap.forEach((k, p) -> mbp.bizPropertiesMap.put(k, (Properties) p.clone()));
        mbp.bizPropertiesMap.putAll(bizPropertiesMap);
        mbp.modifiedKeysMap = new HashMap<>();
        modifiedKeysMap.forEach((k, s) -> mbp.modifiedKeysMap.put(k, new HashSet<>(s)));
        return mbp;
    }

    @Override
    public synchronized boolean replace(Object key, Object oldValue, Object newValue) {
        Object curValue = get(key);
        if (!Objects.equals(curValue, oldValue) || (curValue == null && !containsKey(key))) {
            return false;
        }
        put(key, newValue);
        return true;
    }

    @Override
    public synchronized boolean isEmpty() {
        return getReadProperties().isEmpty();
    }

    @Override
    public synchronized Object replace(Object key, Object value) {
        Object curValue;
        if (((curValue = get(key)) != null) || containsKey(key)) {
            curValue = put(key, value);
        }
        return curValue;
    }

    @Override
    public synchronized boolean containsKey(Object key) {
        return getReadProperties().containsKey(key);
    }

    @Override
    public synchronized boolean contains(Object value) {
        return getReadProperties().contains(value);
    }

    @Override
    public synchronized void replaceAll(BiFunction<? super Object, ? super Object, ?> function) {
        Map map = new HashMap();
        for (Map.Entry entry : entrySet()) {
            Object k = entry.getKey();
            Object v = entry.getValue();
            v = function.apply(k, v);
            map.put(k, v);
        }
        putAll(map);
    }

    @Override
    public synchronized int size() {
        return getReadProperties().size();
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        return getReadProperties().entrySet();
    }

    @Override
    public synchronized void putAll(Map map) {
        Set<String> keys = new HashSet<>();
        for (Object key : map.keySet()) {
            String text = key == null ? null : key.toString();
            keys.add(text);
        }
        addModifiedKeys(keys);
        getWriteProperties().putAll(map);
    }

    @Override
    public synchronized Object computeIfAbsent(Object key,
                                               Function<? super Object, ?> mappingFunction) {
        Object value = get(key);
        if (value == null) {
            Object newValue = mappingFunction.apply(key);
            if (newValue != null) {
                put(key, newValue);
                return newValue;
            }
        }
        return value;
    }

    @Override
    public synchronized Enumeration<Object> elements() {
        return getReadProperties().elements();
    }

    @Override
    public synchronized void forEach(BiConsumer<? super Object, ? super Object> action) {
        getReadProperties().forEach(action);
    }

    @Override
    public synchronized Object putIfAbsent(Object key, Object value) {
        Object v = get(key);
        if (v == null) {
            v = put(key, value);
        }
        return v;
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        return getReadProperties().keys();
    }

    @Override
    public Set<Object> keySet() {
        return getReadProperties().keySet();
    }

    @Override
    public boolean containsValue(Object value) {
        return getReadProperties().containsValue(value);
    }

    @Override
    public synchronized Object getOrDefault(Object key, Object defaultValue) {
        return getReadProperties().getOrDefault(key, defaultValue);
    }

    @Override
    public synchronized Object computeIfPresent(Object key,
                                                BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        Object oldValue = get(key);
        if (oldValue == null) {
            return null;
        }
        Object newValue = remappingFunction.apply(key, oldValue);
        if (newValue != null) {
            put(key, newValue);
            return newValue;
        }
        remove(key);
        return null;
    }

    @Override
    public synchronized Object compute(Object key,
                                       BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        Object oldValue = get(key);
        Object newValue = remappingFunction.apply(key, oldValue);
        if (newValue == null) {
            if (oldValue != null || containsKey(key)) {
                remove(key);
            }
            return null;
        }
        put(key, newValue);
        return newValue;
    }

    @Override
    public synchronized Object merge(Object key, Object value,
                                     BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        Object oldValue = get(key);
        Object newValue = (oldValue == null) ? value : remappingFunction.apply(oldValue, value);
        if (newValue == null) {
            remove(key);
        } else {
            put(key, newValue);
        }
        return newValue;
    }

    private synchronized Properties getReadProperties() {
        Properties bizProperties = getWriteProperties();
        if (bizProperties == baseProperties) {
            return baseProperties;
        }
        Properties properties = new Properties();
        properties.putAll(baseProperties);
        Set<String> modifiedKeys = getModifiedKeys();
        if (modifiedKeys != null) {
            modifiedKeys.forEach(properties::remove);
        }
        properties.putAll(bizProperties);
        return properties;
    }

    private synchronized Properties getWriteProperties() {
        ClassLoader invokeClassLoader = Thread.currentThread().getContextClassLoader();
        if (bizPropertiesMap.containsKey(invokeClassLoader)) {
            return bizPropertiesMap.get(invokeClassLoader);
        }
        for (ClassLoader classLoader = invokeClassLoader; classLoader != null; classLoader = classLoader.getParent()) {
            String name = classLoader.getClass().getName();
            if (Objects.equals(name, bizClassLoaderName)) {
                Properties props = bizPropertiesMap.computeIfAbsent(classLoader, k -> new Properties());
                bizPropertiesMap.put(invokeClassLoader, props);
                return props;
            }
        }
        bizPropertiesMap.put(invokeClassLoader, baseProperties);
        return baseProperties;
    }

    private synchronized Set<String> getModifiedKeys() {
        ClassLoader invokeClassLoader = Thread.currentThread().getContextClassLoader();
        if (modifiedKeysMap.containsKey(invokeClassLoader)) {
            return modifiedKeysMap.get(invokeClassLoader);
        }
        for (ClassLoader classLoader = invokeClassLoader; classLoader != null; classLoader = classLoader.getParent()) {
            String name = classLoader.getClass().getName();
            if (Objects.equals(name, bizClassLoaderName)) {
                Set<String> keys = modifiedKeysMap.computeIfAbsent(classLoader, k -> new HashSet<>());
                modifiedKeysMap.put(invokeClassLoader, keys);
                return keys;
            }
        }
        return null;
    }

    private void addModifiedKey(String key) {
        addModifiedKeys(Collections.singleton(key));
    }

    private void addModifiedKeys(Collection<String> keys) {
        Set<String> modifiedKeys = getModifiedKeys();
        if (modifiedKeys != null && keys != null) {
            modifiedKeys.addAll(keys);
        }
    }

    /**
     * replace the system properties to multi biz properties<br/>
     * if you want to use, you need invoke the method in base application
     */
    public static void initSystem(String bizClassLoaderName) {
        Properties properties = System.getProperties();
        MultiBizProperties multiBizProperties = new MultiBizProperties(bizClassLoaderName,
            properties);
        System.setProperties(multiBizProperties);
    }

    public static void initSystem() {
        initSystem(BIZ_CLASS_LOADER);
    }
}
