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
package com.alipay.sofa.serverless.common.util;

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
 */
public class MultiBizProperties extends Properties {

    private static final String BIZ_CLASS_LOADER = "com.alipay.sofa.ark.container.service.classloader.BizClassLoader";

    private final Properties baseProperties;
    private ClassLoader baseClassLoader;
    private Map<ClassLoader, Properties> bizPropertiesMap;

    public MultiBizProperties(Properties baseProperties) {
        this.bizPropertiesMap = new HashMap<>();
        this.baseProperties = baseProperties;
    }

    public MultiBizProperties() {
        this(new Properties());
    }


    public synchronized Object setProperty(String key, String value) {
        return getBizProperties().setProperty(key, value);
    }

    @Override
    public String getProperty(String key) {
        return getBizProperties().getProperty(key);
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        return getBizProperties().getProperty(key, defaultValue);
    }

    @Override
    public synchronized void load(Reader reader) throws IOException {
        getBizProperties().load(reader);
    }

    @Override
    public synchronized void load(InputStream inStream) throws IOException {
        getBizProperties().load(inStream);
    }

    @Override
    public void list(PrintStream out) {
        getBizProperties().list(out);
    }

    @Override
    public void list(PrintWriter out) {
        getBizProperties().list(out);
    }

    @Override
    public void save(OutputStream out, String comments) {
        getBizProperties().save(out, comments);
    }

    @Override
    public void store(Writer writer, String comments) throws IOException {
        getBizProperties().store(writer, comments);
    }

    @Override
    public void store(OutputStream out, String comments) throws IOException {
        getBizProperties().store(out, comments);
    }

    @Override
    public synchronized void loadFromXML(InputStream in) throws IOException {
        getBizProperties().loadFromXML(in);
    }

    @Override
    public void storeToXML(OutputStream os, String comment) throws IOException {
        getBizProperties().storeToXML(os, comment);
    }

    @Override
    public void storeToXML(OutputStream os, String comment, String encoding) throws IOException {
        getBizProperties().storeToXML(os, comment, encoding);
    }

    @Override
    public Enumeration<?> propertyNames() {
        return getBizProperties().propertyNames();
    }

    @Override
    public Set<String> stringPropertyNames() {
        return getBizProperties().stringPropertyNames();
    }

    @Override
    public synchronized boolean remove(Object key, Object value) {
        return getBizProperties().remove(key, value);
    }

    @Override
    public synchronized Object get(Object key) {
        return getBizProperties().get(key);
    }

    @Override
    public synchronized Object remove(Object key) {
        return getBizProperties().remove(key);
    }

    @Override
    public synchronized Object put(Object key, Object value) {
        return getBizProperties().put(key, value);
    }

    @Override
    public synchronized boolean equals(Object o) {
        return getBizProperties().equals(o);
    }

    @Override
    public synchronized String toString() {
        return getBizProperties().toString();
    }

    @Override
    public Collection<Object> values() {
        return getBizProperties().values();
    }

    @Override
    public synchronized int hashCode() {
        return getBizProperties().hashCode();
    }

    @Override
    public synchronized void clear() {
        getBizProperties().clear();
    }

    @Override
    public synchronized Object clone() {
        MultiBizProperties mbp = new MultiBizProperties(baseProperties);
        mbp.bizPropertiesMap = new HashMap<>();
        bizPropertiesMap.forEach((k, p) -> mbp.put(k, p.clone()));
        mbp.bizPropertiesMap.putAll(bizPropertiesMap);
        mbp.baseClassLoader = baseClassLoader;
        return mbp;
    }

    @Override
    public synchronized boolean replace(Object key, Object oldValue, Object newValue) {
        return getBizProperties().replace(key, oldValue, newValue);
    }

    @Override
    public synchronized boolean isEmpty() {
        return getBizProperties().isEmpty();
    }

    @Override
    public synchronized Object replace(Object key, Object value) {
        return getBizProperties().replace(key, value);
    }

    @Override
    public synchronized boolean containsKey(Object key) {
        return getBizProperties().containsKey(key);
    }

    @Override
    public synchronized boolean contains(Object value) {
        return getBizProperties().contains(value);
    }

    @Override
    public synchronized void replaceAll(BiFunction<? super Object, ? super Object, ?> function) {
        getBizProperties().replaceAll(function);
    }

    @Override
    public synchronized int size() {
        return getBizProperties().size();
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        return getBizProperties().entrySet();
    }

    @Override
    public synchronized void putAll(Map<?, ?> t) {
        getBizProperties().putAll(t);
    }

    @Override
    public synchronized Object computeIfAbsent(Object key,
                                               Function<? super Object, ?> mappingFunction) {
        return getBizProperties().computeIfAbsent(key, mappingFunction);
    }

    @Override
    public synchronized Enumeration<Object> elements() {
        return getBizProperties().elements();
    }

    @Override
    public synchronized void forEach(BiConsumer<? super Object, ? super Object> action) {
        getBizProperties().forEach(action);
    }

    @Override
    public synchronized Object putIfAbsent(Object key, Object value) {
        return getBizProperties().putIfAbsent(key, value);
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        return getBizProperties().keys();
    }

    @Override
    public Set<Object> keySet() {
        return getBizProperties().keySet();
    }

    @Override
    public boolean containsValue(Object value) {
        return getBizProperties().containsValue(value);
    }

    @Override
    public synchronized Object getOrDefault(Object key, Object defaultValue) {
        return getBizProperties().getOrDefault(key, defaultValue);
    }

    @Override
    public synchronized Object computeIfPresent(Object key,
                                                BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return getBizProperties().computeIfPresent(key, remappingFunction);
    }

    @Override
    public synchronized Object compute(Object key,
                                       BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return getBizProperties().compute(key, remappingFunction);
    }

    @Override
    public synchronized Object merge(Object key, Object value,
                                     BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return getBizProperties().merge(key, value, remappingFunction);
    }

    private synchronized Properties getBizProperties() {
        ClassLoader invokeClassLoader = Thread.currentThread().getContextClassLoader();
        if (bizPropertiesMap.containsKey(invokeClassLoader)) {
            return bizPropertiesMap.get(invokeClassLoader);
        }
        for (ClassLoader classLoader = invokeClassLoader; classLoader != null; classLoader = classLoader.getParent()) {
            String name = classLoader.getClass().getName();
            if (Objects.equals(name, BIZ_CLASS_LOADER)) {
                Properties props = bizPropertiesMap.computeIfAbsent(classLoader, k -> new Properties(baseProperties));
                bizPropertiesMap.put(invokeClassLoader, props);
                return props;
            }
        }
        bizPropertiesMap.put(invokeClassLoader, baseProperties);
        return baseProperties;
    }

    /**
     * replace the system properties to multi biz properties<br/>
     * if you want to use, you need invoke the method in base application
     */
    public static void initSystem() {
        Properties properties = System.getProperties();
        MultiBizProperties multiBizProperties = new MultiBizProperties(properties);
        System.setProperties(multiBizProperties);
    }
}
