package com.alipay.sofa.serverless.common.util;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MultiBizProperties extends Properties {
    private final ClassLoader baseClassLoader;

    private Map<ClassLoader, Properties> bizPropertiesMap;

    public MultiBizProperties(ClassLoader baseClassLoader) {
        this.baseClassLoader = baseClassLoader;
        bizPropertiesMap = new ConcurrentHashMap<>();
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
        return getCombineProperties().propertyNames();
    }

    @Override
    public Set<String> stringPropertyNames() {
        return getCombineProperties().stringPropertyNames();
    }

    @Override
    public synchronized boolean remove(Object key, Object value) {
        return getBizProperties().remove(key, value);
    }

    @Override
    public synchronized Object get(Object key) {
        return getCombineProperties().get(key);
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
        return getCombineProperties().equals(o);
    }

    @Override
    public synchronized String toString() {
        return getCombineProperties().toString();
    }

    @Override
    public Collection<Object> values() {
        return getCombineProperties().values();
    }

    @Override
    public synchronized int hashCode() {
        return getCombineProperties().hashCode();
    }

    @Override
    public synchronized void clear() {
        getBizProperties().clear();
    }

    @Override
    public synchronized Object clone() {
        MultiBizProperties p = new MultiBizProperties(baseClassLoader);
        p.bizPropertiesMap = new HashMap<>();
        p.bizPropertiesMap.putAll(bizPropertiesMap);
        return p;
    }

    @Override
    public synchronized boolean replace(Object key, Object oldValue, Object newValue) {
        return getBizProperties().replace(key, oldValue, newValue);
    }

    @Override
    public synchronized boolean isEmpty() {
        return getCombineProperties().isEmpty();
    }

    @Override
    public synchronized Object replace(Object key, Object value) {
        return getBizProperties().replace(key, value);
    }

    @Override
    public synchronized boolean containsKey(Object key) {
        return getCombineProperties().containsKey(key);
    }

    @Override
    public synchronized boolean contains(Object value) {
        return getCombineProperties().contains(value);
    }

    @Override
    public synchronized void replaceAll(BiFunction<? super Object, ? super Object, ?> function) {
        getBizProperties().replaceAll(function);
    }

    @Override
    public synchronized int size() {
        return getCombineProperties().size();
    }

    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        return getCombineProperties().entrySet();
    }

    @Override
    public synchronized void putAll(Map<?, ?> t) {
        getBizProperties().putAll(t);
    }

    @Override
    public synchronized Object computeIfAbsent(Object key, Function<? super Object, ?> mappingFunction) {
        return getBizProperties().computeIfAbsent(key, mappingFunction);
    }

    @Override
    public synchronized Enumeration<Object> elements() {
        return getCombineProperties().elements();
    }

    @Override
    public synchronized void forEach(BiConsumer<? super Object, ? super Object> action) {
        getCombineProperties().forEach(action);
    }

    @Override
    public synchronized Object putIfAbsent(Object key, Object value) {
        return getBizProperties().putIfAbsent(key, value);
    }

    @Override
    public synchronized Enumeration<Object> keys() {
        return getCombineProperties().keys();
    }

    @Override
    public Set<Object> keySet() {
        return getCombineProperties().keySet();
    }

    @Override
    public boolean containsValue(Object value) {
        return getCombineProperties().containsValue(value);
    }

    @Override
    public synchronized Object getOrDefault(Object key, Object defaultValue) {
        return getCombineProperties().getOrDefault(key, defaultValue);
    }

    @Override
    public synchronized Object computeIfPresent(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return getBizProperties().computeIfPresent(key, remappingFunction);
    }

    @Override
    public synchronized Object compute(Object key, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return getBizProperties().compute(key, remappingFunction);
    }

    @Override
    public synchronized Object merge(Object key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
        return getBizProperties().merge(key, value, remappingFunction);
    }

    protected ClassLoader getInvokeClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }

    private Properties getBizProperties() {
        ClassLoader classLoader = getInvokeClassLoader();
        return getProperties(classLoader);
    }

    private Properties getProperties(ClassLoader classLoader) {
        return bizPropertiesMap.computeIfAbsent(classLoader, k -> new Properties());
    }

    private Properties getCombineProperties() {
        Properties bizProperties = getBizProperties();
        Properties baseProperties = getProperties(baseClassLoader);
        Properties properties = new Properties(baseProperties);
        if (baseProperties == bizProperties) {
            return bizProperties;
        }
        properties.putAll(bizProperties);
        return properties;
    }
}
