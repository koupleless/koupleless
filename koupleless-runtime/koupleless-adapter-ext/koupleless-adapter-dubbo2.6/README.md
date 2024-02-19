<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

# dubbo 2.6.x support

## Problem Description


After we have integrated Dubbo into the base platform, we simultaneously removed the Dubbo components from the biz module. However, we noticed that Dubbo's ExtensionLoader is a static class, which gets loaded once and then remains stable. This prevents it from loading different biz modules according to different classloaders.

![image.png](https://cdn.nlark.com/yuque/0/2023/png/145710/1698218715966-4e510ce8-4031-4b0e-b5c6-293c4dcfe140.png#averageHue=%230f0f0f&clientId=u8bee2931-a7cd-4&from=paste&height=684&id=uc16f2576&originHeight=684&originWidth=1495&originalType=binary&ratio=1&rotation=0&showTitle=false&size=218206&status=done&style=none&taskId=u0b300a31-daab-4212-8ea9-85f4e5d34f0&title=&width=1495)

This results in an error when starting a new biz.

## Problem solution
Consider expanding the current version of the ExtensionLoader to support different classloaders. This way, the ExtensionClassLoader can use different BizClassLoaders to reload the corresponding resources.

![image.png](https://cdn.nlark.com/yuque/0/2023/png/145710/1698218819626-c5a93c93-3b11-4e7f-a311-87655e085757.png#averageHue=%23646438&clientId=u8bee2931-a7cd-4&from=paste&height=506&id=uf0cd09e8&originHeight=506&originWidth=1422&originalType=binary&ratio=1&rotation=0&showTitle=false&size=171265&status=done&style=none&taskId=u935dc20e-8e5a-4de6-9c8e-dd6aec82457&title=&width=1422)
![image.png](https://cdn.nlark.com/yuque/0/2023/png/145710/1698218841564-f45a7168-ad17-4f19-b765-7b1b67cf93ee.png#averageHue=%2373633d&clientId=u8bee2931-a7cd-4&from=paste&height=551&id=u9b971db3&originHeight=551&originWidth=1651&originalType=binary&ratio=1&rotation=0&showTitle=false&size=226591&status=done&style=none&taskId=u3dfd5b70-fe18-4dfd-bed8-0f1115c2937&title=&width=1651)

At this time, because the corresponding Thread's ContextClassLoader is set when the biz is loaded, this can in turn trigger the corresponding SPI loading.

```
package com.alibaba.dubbo.common.extension;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.support.ActivateComparator;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.Holder;
import com.alibaba.dubbo.common.utils.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * Load dubbo extensions
 * <ul>
 * <li>auto inject dependency extension </li>
 * <li>auto wrap extension in wrapper </li>
 * <li>default extension is an adaptive instance</li>
 * </ul>
 *
 * @see <a href="http://java.sun.com/j2se/1.5.0/docs/guide/jar/jar.html#Service%20Provider">Service Provider in Java 5</a>
 * @see com.alibaba.dubbo.common.extension.SPI
 * @see com.alibaba.dubbo.common.extension.Adaptive
 * @see com.alibaba.dubbo.common.extension.Activate
 */
public class ExtensionLoader<T> {

    private static final Logger logger = LoggerFactory.getLogger(ExtensionLoader.class);

    private static final String SERVICES_DIRECTORY = "META-INF/services/";

    private static final String DUBBO_DIRECTORY = "META-INF/dubbo/";

    private static final String DUBBO_INTERNAL_DIRECTORY = DUBBO_DIRECTORY + "internal/";

    private static final Pattern NAME_SEPARATOR = Pattern.compile("\\s*[,]+\\s*");

    private static final ConcurrentMap<Class<?>, ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    //add by qixiaobo start
    private static final ConcurrentMap<ClassLoader, ConcurrentMap<Class<?>, ExtensionLoader<?>>> EXTENSION_LOADERS_SUPPORT_CLASSLOADER = new ConcurrentHashMap<>();
    static{
        EXTENSION_LOADERS_SUPPORT_CLASSLOADER.put(findClassLoader(),EXTENSION_LOADERS);
    }
    //add by qixiaobo end
    private static final ConcurrentMap<Class<?>, Object> EXTENSION_INSTANCES = new ConcurrentHashMap<Class<?>, Object>();

    // ==============================

    private final Class<?> type;

    private final ExtensionFactory objectFactory;

    private final ConcurrentMap<Class<?>, String> cachedNames = new ConcurrentHashMap<Class<?>, String>();

    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<Map<String, Class<?>>>();

    private final Map<String, Activate> cachedActivates = new ConcurrentHashMap<String, Activate>();
    private final ConcurrentMap<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<String, Holder<Object>>();
    private final Holder<Object> cachedAdaptiveInstance = new Holder<Object>();
    private volatile Class<?> cachedAdaptiveClass = null;
    private String cachedDefaultName;
    private volatile Throwable createAdaptiveInstanceError;

    private Set<Class<?>> cachedWrapperClasses;

    private Map<String, IllegalStateException> exceptions = new ConcurrentHashMap<String, IllegalStateException>();

    private ExtensionLoader(Class<?> type) {
        this.type = type;
        objectFactory = (type == ExtensionFactory.class ? null : ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension());
    }

    private static <T> boolean withExtensionAnnotation(Class<T> type) {
        return type.isAnnotationPresent(SPI.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type) {
        if (type == null)
            throw new IllegalArgumentException("Extension type == null");
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
        }
        if (!withExtensionAnnotation(type)) {
            throw new IllegalArgumentException("Extension type(" + type +
                    ") is not extension, because WITHOUT @" + SPI.class.getSimpleName() + " Annotation!");
        }
        ClassLoader classLoader = findClassLoader();
        ConcurrentMap<Class<?>, ExtensionLoader<?>> classExtensionLoaderConcurrentMap = EXTENSION_LOADERS_SUPPORT_CLASSLOADER.get(classLoader);
        if (classExtensionLoaderConcurrentMap == null) {
            EXTENSION_LOADERS_SUPPORT_CLASSLOADER.putIfAbsent(classLoader, new ConcurrentHashMap<>());
            classExtensionLoaderConcurrentMap = EXTENSION_LOADERS_SUPPORT_CLASSLOADER.get(classLoader);
        }
//
        ExtensionLoader<T> loader = (ExtensionLoader<T>) classExtensionLoaderConcurrentMap.get(type);
        if (loader == null) {
            EXTENSION_LOADERS.putIfAbsent(type, new ExtensionLoader<T>(type));
            loader = (ExtensionLoader<T>) EXTENSION_LOADERS.get(type);
        }
        return loader;
    }

    private static ClassLoader findClassLoader() {
        //add by qixiaobo start support classloader
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if(classLoader!=null) return classLoader;
        //add by qixiaobo end
        return ExtensionLoader.class.getClassLoader();
    }

    public String getExtensionName(T extensionInstance) {
        return getExtensionName(extensionInstance.getClass());
    }

    public String getExtensionName(Class<?> extensionClass) {
        return cachedNames.get(extensionClass);
    }

    /**
     * This is equivalent to {@code getActivateExtension(url, key, null)}
     *
     * @param url url
     * @param key url parameter key which used to get extension point names
     * @return extension list which are activated.
     * @see #getActivateExtension(com.alibaba.dubbo.common.URL, String, String)
     */
    public List<T> getActivateExtension(URL url, String key) {
        return getActivateExtension(url, key, null);
    }

    /**
     * This is equivalent to {@code getActivateExtension(url, values, null)}
     *
     * @param url    url
     * @param values extension point names
     * @return extension list which are activated
     * @see #getActivateExtension(com.alibaba.dubbo.common.URL, String[], String)
     */
    public List<T> getActivateExtension(URL url, String[] values) {
        return getActivateExtension(url, values, null);
    }

    /**
     * This is equivalent to {@code getActivateExtension(url, url.getParameter(key).split(","), null)}
     *
     * @param url   url
     * @param key   url parameter key which used to get extension point names
     * @param group group
     * @return extension list which are activated.
     * @see #getActivateExtension(com.alibaba.dubbo.common.URL, String[], String)
     */
    public List<T> getActivateExtension(URL url, String key, String group) {
        String value = url.getParameter(key);
        return getActivateExtension(url, value == null || value.length() == 0 ? null : Constants.COMMA_SPLIT_PATTERN.split(value), group);
    }

    /**
     * Get activate extensions.
     *
     * @param url    url
     * @param values extension point names
     * @param group  group
     * @return extension list which are activated
     * @see com.alibaba.dubbo.common.extension.Activate
     */
    public List<T> getActivateExtension(URL url, String[] values, String group) {
        List<T> exts = new ArrayList<T>();
        List<String> names = values == null ? new ArrayList<String>(0) : Arrays.asList(values);
        if (!names.contains(Constants.REMOVE_VALUE_PREFIX + Constants.DEFAULT_KEY)) {
            getExtensionClasses();
            for (Map.Entry<String, Activate> entry : cachedActivates.entrySet()) {
                String name = entry.getKey();
                Activate activate = entry.getValue();
                if (isMatchGroup(group, activate.group())) {
                    T ext = getExtension(name);
                    if (!names.contains(name)
                            && !names.contains(Constants.REMOVE_VALUE_PREFIX + name)
                            && isActive(activate, url)) {
                        exts.add(ext);
                    }
                }
            }
            Collections.sort(exts, ActivateComparator.COMPARATOR);
        }
        List<T> usrs = new ArrayList<T>();
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            if (!name.startsWith(Constants.REMOVE_VALUE_PREFIX)
                    && !names.contains(Constants.REMOVE_VALUE_PREFIX + name)) {
                if (Constants.DEFAULT_KEY.equals(name)) {
                    if (!usrs.isEmpty()) {
                        exts.addAll(0, usrs);
                        usrs.clear();
                    }
                } else {
                    T ext = getExtension(name);
                    usrs.add(ext);
                }
            }
        }
        if (!usrs.isEmpty()) {
            exts.addAll(usrs);
        }
        return exts;
    }

    private boolean isMatchGroup(String group, String[] groups) {
        if (group == null || group.length() == 0) {
            return true;
        }
        if (groups != null && groups.length > 0) {
            for (String g : groups) {
                if (group.equals(g)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isActive(Activate activate, URL url) {
        String[] keys = activate.value();
        if (keys.length == 0) {
            return true;
        }
        for (String key : keys) {
            for (Map.Entry<String, String> entry : url.getParameters().entrySet()) {
                String k = entry.getKey();
                String v = entry.getValue();
                if ((k.equals(key) || k.endsWith("." + key))
                        && ConfigUtils.isNotEmpty(v)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get extension's instance. Return <code>null</code> if extension is not found or is not initialized. Pls. note
     * that this method will not trigger extension load.
     * <p>
     * In order to trigger extension load, call {@link #getExtension(String)} instead.
     *
     * @see #getExtension(String)
     */
    @SuppressWarnings("unchecked")
    public T getLoadedExtension(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Extension name == null");
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<Object>());
            holder = cachedInstances.get(name);
        }
        return (T) holder.get();
    }

    /**
     * Return the list of extensions which are already loaded.
     * <p>
     * Usually {@link #getSupportedExtensions()} should be called in order to get all extensions.
     *
     * @see #getSupportedExtensions()
     */
    public Set<String> getLoadedExtensions() {
        return Collections.unmodifiableSet(new TreeSet<String>(cachedInstances.keySet()));
    }

    /**
     * Find the extension with the given name. If the specified name is not found, then {@link IllegalStateException}
     * will be thrown.
     */
    @SuppressWarnings("unchecked")
    public T getExtension(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Extension name == null");
        if ("true".equals(name)) {
            return getDefaultExtension();
        }
        Holder<Object> holder = cachedInstances.get(name);
        if (holder == null) {
            cachedInstances.putIfAbsent(name, new Holder<Object>());
            holder = cachedInstances.get(name);
        }
        Object instance = holder.get();
        if (instance == null) {
            synchronized (holder) {
                instance = holder.get();
                if (instance == null) {
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return (T) instance;
    }

    /**
     * Return default extension, return <code>null</code> if it's not configured.
     */
    public T getDefaultExtension() {
        getExtensionClasses();
        if (null == cachedDefaultName || cachedDefaultName.length() == 0
                || "true".equals(cachedDefaultName)) {
            return null;
        }
        return getExtension(cachedDefaultName);
    }

    public boolean hasExtension(String name) {
        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("Extension name == null");
        try {
            this.getExtensionClass(name);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public Set<String> getSupportedExtensions() {
        Map<String, Class<?>> clazzes = getExtensionClasses();
        return Collections.unmodifiableSet(new TreeSet<String>(clazzes.keySet()));
    }

    /**
     * Return default extension name, return <code>null</code> if not configured.
     */
    public String getDefaultExtensionName() {
        getExtensionClasses();
        return cachedDefaultName;
    }

    /**
     * Register new extension via API
     *
     * @param name  extension name
     * @param clazz extension class
     * @throws IllegalStateException when extension with the same name has already been registered.
     */
    public void addExtension(String name, Class<?> clazz) {
        getExtensionClasses(); // load classes

        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Input type " +
                    clazz + "not implement Extension " + type);
        }
        if (clazz.isInterface()) {
            throw new IllegalStateException("Input type " +
                    clazz + "can not be interface!");
        }

        if (!clazz.isAnnotationPresent(Adaptive.class)) {
            if (StringUtils.isBlank(name)) {
                throw new IllegalStateException("Extension name is blank (Extension " + type + ")!");
            }
            if (cachedClasses.get().containsKey(name)) {
                throw new IllegalStateException("Extension name " +
                        name + " already existed(Extension " + type + ")!");
            }

            cachedNames.put(clazz, name);
            cachedClasses.get().put(name, clazz);
        } else {
            if (cachedAdaptiveClass != null) {
                throw new IllegalStateException("Adaptive Extension already existed(Extension " + type + ")!");
            }

            cachedAdaptiveClass = clazz;
        }
    }

    /**
     * Replace the existing extension via API
     *
     * @param name  extension name
     * @param clazz extension class
     * @throws IllegalStateException when extension to be placed doesn't exist
     * @deprecated not recommended any longer, and use only when test
     */
    @Deprecated
    public void replaceExtension(String name, Class<?> clazz) {
        getExtensionClasses(); // load classes

        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Input type " +
                    clazz + "not implement Extension " + type);
        }
        if (clazz.isInterface()) {
            throw new IllegalStateException("Input type " +
                    clazz + "can not be interface!");
        }

        if (!clazz.isAnnotationPresent(Adaptive.class)) {
            if (StringUtils.isBlank(name)) {
                throw new IllegalStateException("Extension name is blank (Extension " + type + ")!");
            }
            if (!cachedClasses.get().containsKey(name)) {
                throw new IllegalStateException("Extension name " +
                        name + " not existed(Extension " + type + ")!");
            }

            cachedNames.put(clazz, name);
            cachedClasses.get().put(name, clazz);
            cachedInstances.remove(name);
        } else {
            if (cachedAdaptiveClass == null) {
                throw new IllegalStateException("Adaptive Extension not existed(Extension " + type + ")!");
            }

            cachedAdaptiveClass = clazz;
            cachedAdaptiveInstance.set(null);
        }
    }

    @SuppressWarnings("unchecked")
    public T getAdaptiveExtension() {
        Object instance = cachedAdaptiveInstance.get();
        if (instance == null) {
            if (createAdaptiveInstanceError == null) {
                synchronized (cachedAdaptiveInstance) {
                    instance = cachedAdaptiveInstance.get();
                    if (instance == null) {
                        try {
                            instance = createAdaptiveExtension();
                            cachedAdaptiveInstance.set(instance);
                        } catch (Throwable t) {
                            createAdaptiveInstanceError = t;
                            throw new IllegalStateException("fail to create adaptive instance: " + t.toString(), t);
                        }
                    }
                }
            } else {
                throw new IllegalStateException("fail to create adaptive instance: " + createAdaptiveInstanceError.toString(), createAdaptiveInstanceError);
            }
        }

        return (T) instance;
    }

    private IllegalStateException findException(String name) {
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (entry.getKey().toLowerCase().contains(name.toLowerCase())) {
                return entry.getValue();
            }
        }
        StringBuilder buf = new StringBuilder("No such extension " + type.getName() + " by name " + name);


        int i = 1;
        for (Map.Entry<String, IllegalStateException> entry : exceptions.entrySet()) {
            if (i == 1) {
                buf.append(", possible causes: ");
            }

            buf.append("\r\n(");
            buf.append(i++);
            buf.append(") ");
            buf.append(entry.getKey());
            buf.append(":\r\n");
            buf.append(StringUtils.toString(entry.getValue()));
        }
        return new IllegalStateException(buf.toString());
    }

    @SuppressWarnings("unchecked")
    private T createExtension(String name) {
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null) {
            throw findException(name);
        }
        try {
            T instance = (T) EXTENSION_INSTANCES.get(clazz);
            if (instance == null) {
                EXTENSION_INSTANCES.putIfAbsent(clazz, clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            }
            injectExtension(instance);
            Set<Class<?>> wrapperClasses = cachedWrapperClasses;
            if (wrapperClasses != null && !wrapperClasses.isEmpty()) {
                for (Class<?> wrapperClass : wrapperClasses) {
                    instance = injectExtension((T) wrapperClass.getConstructor(type).newInstance(instance));
                }
            }
            return instance;
        } catch (Throwable t) {
            throw new IllegalStateException("Extension instance(name: " + name + ", class: " +
                    type + ")  could not be instantiated: " + t.getMessage(), t);
        }
    }

    private T injectExtension(T instance) {
        try {
            if (objectFactory != null) {
                for (Method method : instance.getClass().getMethods()) {
                    if (method.getName().startsWith("set")
                            && method.getParameterTypes().length == 1
                            && Modifier.isPublic(method.getModifiers())) {
                        /**
                         * Check {@link DisableInject} to see if we need auto injection for this property
                         */
                        if (method.getAnnotation(DisableInject.class) != null) {
                            continue;
                        }
                        Class<?> pt = method.getParameterTypes()[0];
                        try {
                            String property = method.getName().length() > 3 ? method.getName().substring(3, 4).toLowerCase() + method.getName().substring(4) : "";
                            Object object = objectFactory.getExtension(pt, property);
                            if (object != null) {
                                method.invoke(instance, object);
                            }
                        } catch (Exception e) {
                            logger.error("fail to inject via method " + method.getName()
                                    + " of interface " + type.getName() + ": " + e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return instance;
    }

    private Class<?> getExtensionClass(String name) {
        if (type == null)
            throw new IllegalArgumentException("Extension type == null");
        if (name == null)
            throw new IllegalArgumentException("Extension name == null");
        Class<?> clazz = getExtensionClasses().get(name);
        if (clazz == null)
            throw new IllegalStateException("No such extension \"" + name + "\" for " + type.getName() + "!");
        return clazz;
    }

    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClasses.get();
        if (classes == null) {
            synchronized (cachedClasses) {
                classes = cachedClasses.get();
                if (classes == null) {
                    classes = loadExtensionClasses();
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    // synchronized in getExtensionClasses
    private Map<String, Class<?>> loadExtensionClasses() {
        final SPI defaultAnnotation = type.getAnnotation(SPI.class);
        if (defaultAnnotation != null) {
            String value = defaultAnnotation.value();
            if ((value = value.trim()).length() > 0) {
                String[] names = NAME_SEPARATOR.split(value);
                if (names.length > 1) {
                    throw new IllegalStateException("more than 1 default extension name on extension " + type.getName()
                            + ": " + Arrays.toString(names));
                }
                if (names.length == 1) cachedDefaultName = names[0];
            }
        }

        Map<String, Class<?>> extensionClasses = new HashMap<String, Class<?>>();
        loadDirectory(extensionClasses, DUBBO_INTERNAL_DIRECTORY);
        loadDirectory(extensionClasses, DUBBO_DIRECTORY);
        loadDirectory(extensionClasses, SERVICES_DIRECTORY);
        return extensionClasses;
    }

    private void loadDirectory(Map<String, Class<?>> extensionClasses, String dir) {
        String fileName = dir + type.getName();
        try {
            Enumeration<java.net.URL> urls;
            ClassLoader classLoader = findClassLoader();
            if (classLoader != null) {
                urls = classLoader.getResources(fileName);
            } else {
                urls = ClassLoader.getSystemResources(fileName);
            }
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    java.net.URL resourceURL = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceURL);
                }
            }
        } catch (Throwable t) {
            logger.error("Exception when load extension class(interface: " +
                    type + ", description file: " + fileName + ").", t);
        }
    }

    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, java.net.URL resourceURL) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(resourceURL.openStream(), "utf-8"));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    final int ci = line.indexOf('#');
                    if (ci >= 0) line = line.substring(0, ci);
                    line = line.trim();
                    if (line.length() > 0) {
                        try {
                            String name = null;
                            int i = line.indexOf('=');
                            if (i > 0) {
                                name = line.substring(0, i).trim();
                                line = line.substring(i + 1).trim();
                            }
                            if (line.length() > 0) {
                                loadClass(extensionClasses, resourceURL, Class.forName(line, true, classLoader), name);
                            }
                        } catch (Throwable t) {
                            IllegalStateException e = new IllegalStateException("Failed to load extension class(interface: " + type + ", class line: " + line + ") in " + resourceURL + ", cause: " + t.getMessage(), t);
                            exceptions.put(line, e);
                        }
                    }
                }
            } finally {
                reader.close();
            }
        } catch (Throwable t) {
            logger.error("Exception when load extension class(interface: " +
                    type + ", class file: " + resourceURL + ") in " + resourceURL, t);
        }
    }

    private void loadClass(Map<String, Class<?>> extensionClasses, java.net.URL resourceURL, Class<?> clazz, String name) throws NoSuchMethodException {
        if (!type.isAssignableFrom(clazz)) {
            throw new IllegalStateException("Error when load extension class(interface: " +
                    type + ", class line: " + clazz.getName() + "), class "
                    + clazz.getName() + "is not subtype of interface.");
        }
        if (clazz.isAnnotationPresent(Adaptive.class)) {
            if (cachedAdaptiveClass == null) {
                cachedAdaptiveClass = clazz;
            } else if (!cachedAdaptiveClass.equals(clazz)) {
                throw new IllegalStateException("More than 1 adaptive class found: "
                        + cachedAdaptiveClass.getClass().getName()
                        + ", " + clazz.getClass().getName());
            }
        } else if (isWrapperClass(clazz)) {
            Set<Class<?>> wrappers = cachedWrapperClasses;
            if (wrappers == null) {
                cachedWrapperClasses = new ConcurrentHashSet<Class<?>>();
                wrappers = cachedWrapperClasses;
            }
            wrappers.add(clazz);
        } else {
            clazz.getConstructor();
            if (name == null || name.length() == 0) {
                name = findAnnotationName(clazz);
                if (name.length() == 0) {
                    throw new IllegalStateException("No such extension name for the class " + clazz.getName() + " in the config " + resourceURL);
                }
            }
            String[] names = NAME_SEPARATOR.split(name);
            if (names != null && names.length > 0) {
                Activate activate = clazz.getAnnotation(Activate.class);
                if (activate != null) {
                    cachedActivates.put(names[0], activate);
                }
                for (String n : names) {
                    if (!cachedNames.containsKey(clazz)) {
                        cachedNames.put(clazz, n);
                    }
                    Class<?> c = extensionClasses.get(n);
                    if (c == null) {
                        extensionClasses.put(n, clazz);
                    } else if (c != clazz) {
                        throw new IllegalStateException("Duplicate extension " + type.getName() + " name " + n + " on " + c.getName() + " and " + clazz.getName());
                    }
                }
            }
        }
    }

    private boolean isWrapperClass(Class<?> clazz) {
        try {
            clazz.getConstructor(type);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    @SuppressWarnings("deprecation")
    private String findAnnotationName(Class<?> clazz) {
        com.alibaba.dubbo.common.Extension extension = clazz.getAnnotation(com.alibaba.dubbo.common.Extension.class);
        if (extension == null) {
            String name = clazz.getSimpleName();
            if (name.endsWith(type.getSimpleName())) {
                name = name.substring(0, name.length() - type.getSimpleName().length());
            }
            return name.toLowerCase();
        }
        return extension.value();
    }

    @SuppressWarnings("unchecked")
    private T createAdaptiveExtension() {
        try {
            return injectExtension((T) getAdaptiveExtensionClass().newInstance());
        } catch (Exception e) {
            throw new IllegalStateException("Can not create adaptive extension " + type + ", cause: " + e.getMessage(), e);
        }
    }

    private Class<?> getAdaptiveExtensionClass() {
        getExtensionClasses();
        if (cachedAdaptiveClass != null) {
            return cachedAdaptiveClass;
        }
        return cachedAdaptiveClass = createAdaptiveExtensionClass();
    }

    private Class<?> createAdaptiveExtensionClass() {
        String code = createAdaptiveExtensionClassCode();
        ClassLoader classLoader = findClassLoader();
        com.alibaba.dubbo.common.compiler.Compiler compiler = ExtensionLoader.getExtensionLoader(com.alibaba.dubbo.common.compiler.Compiler.class).getAdaptiveExtension();
        return compiler.compile(code, classLoader);
    }

    private String createAdaptiveExtensionClassCode() {
        StringBuilder codeBuilder = new StringBuilder();
        Method[] methods = type.getMethods();
        boolean hasAdaptiveAnnotation = false;
        for (Method m : methods) {
            if (m.isAnnotationPresent(Adaptive.class)) {
                hasAdaptiveAnnotation = true;
                break;
            }
        }
        // no need to generate adaptive class since there's no adaptive method found.
        if (!hasAdaptiveAnnotation)
            throw new IllegalStateException("No adaptive method on extension " + type.getName() + ", refuse to create the adaptive class!");

        codeBuilder.append("package ").append(type.getPackage().getName()).append(";");
        codeBuilder.append("\nimport ").append(ExtensionLoader.class.getName()).append(";");
        codeBuilder.append("\npublic class ").append(type.getSimpleName()).append("$Adaptive").append(" implements ").append(type.getCanonicalName()).append(" {");

        for (Method method : methods) {
            Class<?> rt = method.getReturnType();
            Class<?>[] pts = method.getParameterTypes();
            Class<?>[] ets = method.getExceptionTypes();

            Adaptive adaptiveAnnotation = method.getAnnotation(Adaptive.class);
            StringBuilder code = new StringBuilder(512);
            if (adaptiveAnnotation == null) {
                code.append("throw new UnsupportedOperationException(\"method ")
                        .append(method.toString()).append(" of interface ")
                        .append(type.getName()).append(" is not adaptive method!\");");
            } else {
                int urlTypeIndex = -1;
                for (int i = 0; i < pts.length; ++i) {
                    if (pts[i].equals(URL.class)) {
                        urlTypeIndex = i;
                        break;
                    }
                }
                // found parameter in URL type
                if (urlTypeIndex != -1) {
                    // Null Point check
                    String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"url == null\");",
                            urlTypeIndex);
                    code.append(s);

                    s = String.format("\n%s url = arg%d;", URL.class.getName(), urlTypeIndex);
                    code.append(s);
                }
                // did not find parameter in URL type
                else {
                    String attribMethod = null;

                    // find URL getter method
                    LBL_PTS:
                    for (int i = 0; i < pts.length; ++i) {
                        Method[] ms = pts[i].getMethods();
                        for (Method m : ms) {
                            String name = m.getName();
                            if ((name.startsWith("get") || name.length() > 3)
                                    && Modifier.isPublic(m.getModifiers())
                                    && !Modifier.isStatic(m.getModifiers())
                                    && m.getParameterTypes().length == 0
                                    && m.getReturnType() == URL.class) {
                                urlTypeIndex = i;
                                attribMethod = name;
                                break LBL_PTS;
                            }
                        }
                    }
                    if (attribMethod == null) {
                        throw new IllegalStateException("fail to create adaptive class for interface " + type.getName()
                                + ": not found url parameter or url attribute in parameters of method " + method.getName());
                    }

                    // Null point check
                    String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"%s argument == null\");",
                            urlTypeIndex, pts[urlTypeIndex].getName());
                    code.append(s);
                    s = String.format("\nif (arg%d.%s() == null) throw new IllegalArgumentException(\"%s argument %s() == null\");",
                            urlTypeIndex, attribMethod, pts[urlTypeIndex].getName(), attribMethod);
                    code.append(s);

                    s = String.format("%s url = arg%d.%s();", URL.class.getName(), urlTypeIndex, attribMethod);
                    code.append(s);
                }

                String[] value = adaptiveAnnotation.value();
                // value is not set, use the value generated from class name as the key
                if (value.length == 0) {
                    char[] charArray = type.getSimpleName().toCharArray();
                    StringBuilder sb = new StringBuilder(128);
                    for (int i = 0; i < charArray.length; i++) {
                        if (Character.isUpperCase(charArray[i])) {
                            if (i != 0) {
                                sb.append(".");
                            }
                            sb.append(Character.toLowerCase(charArray[i]));
                        } else {
                            sb.append(charArray[i]);
                        }
                    }
                    value = new String[]{sb.toString()};
                }

                boolean hasInvocation = false;
                for (int i = 0; i < pts.length; ++i) {
                    if (pts[i].getName().equals("com.alibaba.dubbo.rpc.Invocation")) {
                        // Null Point check
                        String s = String.format("\nif (arg%d == null) throw new IllegalArgumentException(\"invocation == null\");", i);
                        code.append(s);
                        s = String.format("\nString methodName = arg%d.getMethodName();", i);
                        code.append(s);
                        hasInvocation = true;
                        break;
                    }
                }

                String defaultExtName = cachedDefaultName;
                String getNameCode = null;
                for (int i = value.length - 1; i >= 0; --i) {
                    if (i == value.length - 1) {
                        if (null != defaultExtName) {
                            if (!"protocol".equals(value[i]))
                                if (hasInvocation)
                                    getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                                else
                                    getNameCode = String.format("url.getParameter(\"%s\", \"%s\")", value[i], defaultExtName);
                            else
                                getNameCode = String.format("( url.getProtocol() == null ? \"%s\" : url.getProtocol() )", defaultExtName);
                        } else {
                            if (!"protocol".equals(value[i]))
                                if (hasInvocation)
                                    getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                                else
                                    getNameCode = String.format("url.getParameter(\"%s\")", value[i]);
                            else
                                getNameCode = "url.getProtocol()";
                        }
                    } else {
                        if (!"protocol".equals(value[i]))
                            if (hasInvocation)
                                getNameCode = String.format("url.getMethodParameter(methodName, \"%s\", \"%s\")", value[i], defaultExtName);
                            else
                                getNameCode = String.format("url.getParameter(\"%s\", %s)", value[i], getNameCode);
                        else
                            getNameCode = String.format("url.getProtocol() == null ? (%s) : url.getProtocol()", getNameCode);
                    }
                }
                code.append("\nString extName = ").append(getNameCode).append(";");
                // check extName == null?
                String s = String.format("\nif(extName == null) " +
                                "throw new IllegalStateException(\"Fail to get extension(%s) name from url(\" + url.toString() + \") use keys(%s)\");",
                        type.getName(), Arrays.toString(value));
                code.append(s);

                s = String.format("\n%s extension = (%<s)%s.getExtensionLoader(%s.class).getExtension(extName);",
                        type.getName(), ExtensionLoader.class.getSimpleName(), type.getName());
                code.append(s);

                // return statement
                if (!rt.equals(void.class)) {
                    code.append("\nreturn ");
                }

                s = String.format("extension.%s(", method.getName());
                code.append(s);
                for (int i = 0; i < pts.length; i++) {
                    if (i != 0)
                        code.append(", ");
                    code.append("arg").append(i);
                }
                code.append(");");
            }

            codeBuilder.append("\npublic ").append(rt.getCanonicalName()).append(" ").append(method.getName()).append("(");
            for (int i = 0; i < pts.length; i++) {
                if (i > 0) {
                    codeBuilder.append(", ");
                }
                codeBuilder.append(pts[i].getCanonicalName());
                codeBuilder.append(" ");
                codeBuilder.append("arg").append(i);
            }
            codeBuilder.append(")");
            if (ets.length > 0) {
                codeBuilder.append(" throws ");
                for (int i = 0; i < ets.length; i++) {
                    if (i > 0) {
                        codeBuilder.append(", ");
                    }
                    codeBuilder.append(ets[i].getCanonicalName());
                }
            }
            codeBuilder.append(" {");
            codeBuilder.append(code.toString());
            codeBuilder.append("\n}");
        }
        codeBuilder.append("\n}");
        if (logger.isDebugEnabled()) {
            logger.debug(codeBuilder.toString());
        }
        return codeBuilder.toString();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[" + type.getName() + "]";
    }

}

```
![image.png](https://cdn.nlark.com/yuque/0/2023/png/145710/1698218917861-37b9d42d-b9f8-47df-9046-7edb34c62bc8.png#averageHue=%233b3f42&clientId=u8bee2931-a7cd-4&from=paste&height=425&id=ud6a171c1&originHeight=425&originWidth=508&originalType=binary&ratio=1&rotation=0&showTitle=false&size=42315&status=done&style=none&taskId=u3a538066-041e-4225-b8f3-c4f9002f3b3&title=&width=508)

dubbo version 2.6.4
After loading multiple biz modules in Dubbo, some ClassNotFoundExceptions occurred due to classloader issues during startup.

```
2023-10-24 13:38:34,627 [WARN] [NettyServerWorker-9-9] c.a.d.r.p.d.DecodeableRpcInvocation:? []  [DUBBO] Decode argument failed: com.f6car.merchant.so.org.TgOrgGroupMemberSo, dubbo version: 2.6.12, current host: 172.27.121.46
java.lang.ClassNotFoundException: com.f6car.merchant.so.org.TgOrgGroupMemberSo
	at java.net.URLClassLoader.findClass(URLClassLoader.java:387)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:418)
	at sun.misc.Launcher$AppClassLoader.loadClass(Launcher.java:352)
	at java.lang.ClassLoader.loadClass(ClassLoader.java:351)
	at java.lang.Class.forName0(Native Method)
	at java.lang.Class.forName(Class.java:348)
	at java.io.ObjectInputStream.resolveClass(ObjectInputStream.java:758)
	at com.alibaba.dubbo.common.utils.SerialDetector.resolveClass(SerialDetector.java:67)
	at java.io.ObjectInputStream.readNonProxyDesc(ObjectInputStream.java:1986)
	at java.io.ObjectInputStream.readClassDesc(ObjectInputStream.java:1850)
	at java.io.ObjectInputStream.readOrdinaryObject(ObjectInputStream.java:2160)
	at java.io.ObjectInputStream.readObject0(ObjectInputStream.java:1667)
	at java.io.ObjectInputStream.readObject(ObjectInputStream.java:503)
	at java.io.ObjectInputStream.readObject(ObjectInputStream.java:461)
	at com.alibaba.dubbo.common.serialize.java.JavaObjectInput.readObject(JavaObjectInput.java:70)
	at com.alibaba.dubbo.common.serialize.java.JavaObjectInput.readObject(JavaObjectInput.java:77)
	at com.alibaba.dubbo.rpc.protocol.dubbo.DecodeableRpcInvocation.decode(DecodeableRpcInvocation.java:122)
	at com.alibaba.dubbo.rpc.protocol.dubbo.DecodeableRpcInvocation.decode(DecodeableRpcInvocation.java:72)
	at com.alibaba.dubbo.rpc.protocol.dubbo.DubboCodec.decodeBody(DubboCodec.java:138)
	at com.alibaba.dubbo.remoting.exchange.codec.ExchangeCodec.decode(ExchangeCodec.java:126)
	at com.alibaba.dubbo.remoting.exchange.codec.ExchangeCodec.decode(ExchangeCodec.java:86)
	at com.alibaba.dubbo.rpc.protocol.dubbo.DubboCountCodec.decode(DubboCountCodec.java:46)
	at com.alibaba.dubbo.remoting.transport.netty4.NettyCodecAdapter$InternalDecoder.decode(NettyCodecAdapter.java:95)
	at io.netty.handler.codec.ByteToMessageDecoder.decodeRemovalReentryProtection(ByteToMessageDecoder.java:529)
	at io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:468)
```

![image.png](https://cdn.nlark.com/yuque/0/2023/png/145710/1698398961430-bb632e4e-d008-4cc4-a4dc-95a634a8ab9b.png#averageHue=%23e6e6e6&clientId=u9afc399f-ea80-4&from=paste&height=417&id=u6286cb3e&originHeight=417&originWidth=866&originalType=binary&ratio=1&rotation=0&showTitle=false&size=118101&status=done&style=none&taskId=u07169dd2-776b-43fb-b490-8098407824c&title=&width=866)

It is clear that this is the base's classLoader. So why does Dubbo directly use the current classLoader for serialization and deserialization after exposing the port? This is fine for normal, non-biz isolated scenarios. For cases where there is biz isolation, unless an independent bizClassLoader is used during deserialization, the issue is how do I know which biz should be called in the current scenario???

```java
@Override
public Object decode(Channel channel, InputStream input) throws IOException {
    ObjectInput in = CodecSupport.getSerialization(channel.getUrl(), serializationType)
    .deserialize(channel.getUrl(), input);
    this.put(SERIALIZATION_ID_KEY, serializationType);

    String dubboVersion = in.readUTF();
    request.setVersion(dubboVersion);
    setAttachment(Constants.DUBBO_VERSION_KEY, dubboVersion);

    String path = in.readUTF();
    setAttachment(Constants.PATH_KEY, path);
    String version = in.readUTF();
    setAttachment(Constants.VERSION_KEY, version);

    setMethodName(in.readUTF());
    try {
        if (Boolean.parseBoolean(System.getProperty(SERIALIZATION_SECURITY_CHECK_KEY, "false"))) {
            CodecSupport.checkSerialization(path, version, serializationType);
        }

        Object[] args;
        Class<?>[] pts;
        String desc = in.readUTF();
        if (desc.length() == 0) {
            pts = DubboCodec.EMPTY_CLASS_ARRAY;
            args = DubboCodec.EMPTY_OBJECT_ARRAY;
        } else {
            pts = ReflectUtils.desc2classArray(desc);
            args = new Object[pts.length];
            for (int i = 0; i < args.length; i++) {
                try {
                    args[i] = in.readObject(pts[i]);
                } catch (Exception e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Decode argument failed: " + e.getMessage(), e);
                    }
                }
            }
        }
        setParameterTypes(pts);

        Map<String, String> map = (Map<String, String>) in.readObject(Map.class);
        if (map != null && map.size() > 0) {
            Map<String, String> attachment = getAttachments();
            if (attachment == null) {
                attachment = new HashMap<String, String>();
            }
            attachment.putAll(map);
            setAttachments(attachment);
        }
        //decode argument ,may be callback
        for (int i = 0; i < args.length; i++) {
            args[i] = decodeInvocationArgument(channel, this, pts, i, args[i]);
        }

        setArguments(args);

    } catch (ClassNotFoundException e) {
        throw new IOException(StringUtils.toString("Read invocation data failed.", e));
    } finally {
        if (in instanceof Cleanable) {
            ((Cleanable) in).cleanup();
        }
    }
    return this;
}
```

![image.png](https://cdn.nlark.com/yuque/0/2023/png/145710/1698399359953-e9745f87-c4cb-4e46-aa79-8ba355aec69f.png#averageHue=%232f2f2e&clientId=u9afc399f-ea80-4&from=paste&height=324&id=uc443f7ac&originHeight=324&originWidth=1226&originalType=binary&ratio=1&rotation=0&showTitle=false&size=64184&status=done&style=none&taskId=u958d86b2-04e1-4409-a75c-9b2158d31e6&title=&width=1226)

This means, it is necessary to automatically resolve the corresponding Biz based on the relevant Path and then perform parsing using the classLoader of different biz **thus, it is best to record the relationship with the related bizClassLoader when registering RPCs, which can later be used in reverse**. To avoid this problem, Dubbo is currently placed inside the biz. Different Dubbo biz may encounter port conflicts, so setting the Dubbo port to -1 will resolve this issue.

![image.png](https://cdn.nlark.com/yuque/0/2023/png/145710/1698399510036-576be94b-6931-4abb-bfcc-f366931b458e.png#averageHue=%23302f2f&clientId=ua7a818cb-964d-4&from=paste&height=373&id=u6ec02f46&originHeight=373&originWidth=1304&originalType=binary&ratio=1&rotation=0&showTitle=false&size=98371&status=done&style=none&taskId=uce2b5b3e-742a-4538-8d96-fbb20342baa&title=&width=1304)

After supporting multiple biz, due to the Spring context being bound to the injection supported by Dubbo's SPI, it is essential to isolate the containers of different biz to prevent bean leakage.

```java
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
package com.alibaba.dubbo.config.spring.extension;

import com.alibaba.dubbo.common.extension.ExtensionFactory;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SpringExtensionFactory
 */
public class SpringExtensionFactory implements ExtensionFactory {
    private static final Logger logger = LoggerFactory.getLogger(SpringExtensionFactory.class);

    private static final Map<ClassLoader, Set<ApplicationContext>> contextsWithClassLoader = new ConcurrentHashMap<>();

    public static void addApplicationContext(ApplicationContext context) {
        getContexts().add(context);
    }

    public static void removeApplicationContext(ApplicationContext context) {
        getContexts().remove(context);
    }

    // currently for test purpose
    public static void clearContexts() {

        getContexts().clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getExtension(Class<T> type, String name) {
        for (ApplicationContext context : getContexts()) {
            if (context.containsBean(name)) {
                Object bean = context.getBean(name);
                if (type.isInstance(bean)) {
                    return (T) bean;
                }
            }
        }

        logger.warn("No spring extension(bean) named:" + name + ", try to find an extension(bean) of type " + type.getName());

        for (ApplicationContext context : getContexts()) {
            try {
                return context.getBean(type);
            } catch (NoUniqueBeanDefinitionException multiBeanExe) {
                throw multiBeanExe;
            } catch (NoSuchBeanDefinitionException noBeanExe) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Error when get spring extension(bean) for type:" + type.getName(), noBeanExe);
                }
            }
        }

        logger.warn("No spring extension(bean) named:" + name + ", type:" + type.getName() + " found, stop get bean.");

        return null;
    }

    private static ClassLoader findClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader != null) return classLoader;
        return SpringExtensionFactory.class.getClassLoader();
    }
    private static Set<ApplicationContext> getContexts(){
        ClassLoader classLoader = findClassLoader();
        Set<ApplicationContext> contexts = null;
        if ((contexts = contextsWithClassLoader.get(classLoader)) == null) {
            contextsWithClassLoader.put(classLoader, new ConcurrentHashSet<>());
            contexts = contextsWithClassLoader.get(classLoader);
        };
        return contexts;
    }

}

```


dubbo放入base之后dubbo 由于一部分代码是static执行的 导致部分场景下不同biz的初始化的生命周期不一致
导致我们部分场景下出现异常
举例如下 我们的ReferenceConfig在初次加载时会触发

![image.png](https://cdn.nlark.com/yuque/0/2023/png/145710/1699266065661-2f31de12-b218-44da-97d9-d6477eaf1f87.png#averageHue=%231f2022&clientId=u898e26e6-1421-4&from=paste&height=498&id=u09ba332b&originHeight=498&originWidth=1359&originalType=binary&ratio=1&rotation=0&showTitle=false&size=115077&status=done&style=none&taskId=u7438f359-df95-43d0-aeb1-4b6064d16d6&title=&width=1359)

这样在不同的biz时由于已经加载完毕后因此无法自动触发导致缺少部分初始化场景 需要补齐

![image.png](https://cdn.nlark.com/yuque/0/2023/png/145710/1699266024533-6613a574-3a17-48c1-8009-065ae4520587.png#averageHue=%23232427&clientId=u898e26e6-1421-4&from=paste&height=575&id=udffabb6f&originHeight=575&originWidth=1476&originalType=binary&ratio=1&rotation=0&showTitle=false&size=182369&status=done&style=none&taskId=udd60c607-7e0a-4119-a116-ef52159ab3d&title=&width=1476)


目前在同一个端口下 dubbo接收到请求无法判断到当前模块
已知我们使用的是原生的java序列化方式
这样自然需要想办法来找到对应的模块 从而取到对应的classLoader
经过sofa社区的尚之同学的建议 给到了如下方案


```java
package com.alibaba.dubbo.demo.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.serialize.ObjectInput;
import com.alibaba.dubbo.common.serialize.ObjectOutput;
import com.alibaba.dubbo.common.serialize.java.JavaObjectInput;
import com.alibaba.dubbo.common.serialize.java.JavaObjectOutput;
import com.alibaba.dubbo.common.serialize.java.JavaSerialization;
import com.alibaba.dubbo.config.model.ApplicationModel;
import com.alibaba.dubbo.config.spring.ServiceBean;

import org.springframework.context.ApplicationContext;

/**
 *
 * @author syd
 * @version ClassLoaderJavaSerialization.java, v 0.1 2023年10月28日 19:18 syd
 */
public class ClassLoaderJavaSerialization extends JavaSerialization {

    @Override
    public byte getContentTypeId() {
        return 3;
    }

    @Override
    public String getContentType() {
        return "x-application/java";
    }

    @Override
    public ObjectOutput serialize(URL url, OutputStream out) throws IOException {
        return new JavaObjectOutput(out);
    }

    @Override
    public ObjectInput deserialize(URL url, InputStream is) throws IOException {
        ClassLoader classLoader = getClassLoaderByUrl(url);
        return new JavaObjectInput(new ClassLoaderObjectInputStream(classLoader, is));
    }

    private ClassLoader getClassLoaderByUrl(URL url) {
        ServiceBean serviceBean = (ServiceBean) ApplicationModel.getProviderModel(url.getServiceKey()).getMetadata();
        try {
            Field field = ServiceBean.class.getField("applicationContext");
            ApplicationContext applicationContext = (ApplicationContext) field.get(serviceBean);
            return applicationContext.getClassLoader();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class ClassLoaderObjectInputStream extends ObjectInputStream {
        /** The class loader to use. */
        private final ClassLoader classLoader;

        /**
         * Constructs a new ClassLoaderObjectInputStream.
         *
         * @param classLoader  the ClassLoader from which classes should be loaded
         * @param inputStream  the InputStream to work on
         * @throws IOException in case of an I/O error
         * @throws StreamCorruptedException if the stream is corrupted
         */
        public ClassLoaderObjectInputStream(
            ClassLoader classLoader, InputStream inputStream)
        throws IOException, StreamCorruptedException {
            super(inputStream);
            this.classLoader = classLoader;
        }

        /**
         * Resolve a class specified by the descriptor using the
         * specified ClassLoader or the super ClassLoader.
         *
         * @param objectStreamClass  descriptor of the class
         * @return the Class object described by the ObjectStreamClass
         * @throws IOException in case of an I/O error
         * @throws ClassNotFoundException if the Class cannot be found
         */
        @Override
        protected Class<?> resolveClass(ObjectStreamClass objectStreamClass)
                throws IOException, ClassNotFoundException {

            Class<?> clazz = Class.forName(objectStreamClass.getName(), false, classLoader);

            if (clazz != null) {
                // the classloader knows of the class
                return clazz;
            } else {
                // classloader knows not of class, let the super classloader do it
                return super.resolveClass(objectStreamClass);
            }
        }

        /**
         * Create a proxy class that implements the specified interfaces using
         * the specified ClassLoader or the super ClassLoader.
         *
         * @param interfaces the interfaces to implement
         * @return a proxy class implementing the interfaces
         * @throws IOException in case of an I/O error
         * @throws ClassNotFoundException if the Class cannot be found
         * @see java.io.ObjectInputStream#resolveProxyClass(java.lang.String[])
         * @since Commons IO 2.1
         */
        @Override
        protected Class<?> resolveProxyClass(String[] interfaces) throws IOException,
                ClassNotFoundException {
            Class<?>[] interfaceClasses = new Class[interfaces.length];
            for (int i = 0; i < interfaces.length; i++) {
                interfaceClasses[i] = Class.forName(interfaces[i], false, classLoader);
            }
            try {
                return Proxy.getProxyClass(classLoader, interfaceClasses);
            } catch (IllegalArgumentException e) {
                return super.resolveProxyClass(interfaces);
            }
        }
    }
}
```

We found that in order to make this solution works well, we must modify Java serialization. Meanwhile, other Dubbos that do not support multiple modules still retain the name of Java instead of creating a new SPI extension. After research and validation, we have discovered that

![image.png](https://cdn.nlark.com/yuque/0/2023/png/145710/1699262773133-6db151d9-abc0-4c61-b4fb-7d1b48a70722.png#averageHue=%23636a61&clientId=u9cade2e4-accb-4&from=paste&height=674&id=u0859502a&originHeight=674&originWidth=1120&originalType=binary&ratio=1&rotation=0&showTitle=false&size=320040&status=done&style=none&taskId=u7354dcef-1b23-4559-83f6-ce08a2c6330&title=&width=1120)

The information carried in the original URL may not match the specific invocation. The reason is that Dubbo has two types of connections. The first type is a shared connection where there is one multi-service shared connection between the consumer and each provider instance. The second type is an exclusive connection where there is an independent connection for each service exposed by each provider instance to the consumer.
Therefore, what we need is to get the corresponding path in the invocation.

Therefore, we need to overwrite DecodeableRpcInvocation.

![image.png](https://cdn.nlark.com/yuque/0/2023/png/145710/1699263362261-24276699-018e-4fe8-8dae-473064b1ef8c.png#averageHue=%2326272a&clientId=u9cade2e4-accb-4&from=paste&height=597&id=ueea820e0&originHeight=597&originWidth=1315&originalType=binary&ratio=1&rotation=0&showTitle=false&size=191220&status=done&style=none&taskId=u9e8da21b-5ced-4187-9dde-5e2ab6113eb&title=&width=1315)

Here is an additional point that needs attention.

```java
package com.alibaba.dubbo.common.serialize.java;

import com.alibaba.dubbo.common.serialize.nativejava.NativeJavaObjectInput;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;

public class ClassLoaderJavaObjectInput extends NativeJavaObjectInput {
    public final static int MAX_BYTE_ARRAY_LENGTH = 8 * 1024 * 1024;

    public ClassLoaderJavaObjectInput(InputStream is) throws IOException {
        super((ObjectInputStream)(is instanceof ObjectInputStream ? is : new ObjectInputStream(is)));
    }

    @Override
    public byte[] readBytes() throws IOException {
        int len = getObjectInputStream().readInt();
        if (len < 0)
            return null;
        if (len == 0)
            return new byte[0];
        if (len > MAX_BYTE_ARRAY_LENGTH)
            throw new IOException("Byte array length too large. " + len);

        byte[] b = new byte[len];
        getObjectInputStream().readFully(b);
        return b;
    }

    @Override
    public String readUTF() throws IOException {
        int len = getObjectInputStream().readInt();
        if (len < 0)
            return null;

        return getObjectInputStream().readUTF();
    }

    @Override
    public Object readObject() throws IOException, ClassNotFoundException {
        byte b = getObjectInputStream().readByte();
        if (b == 0)
            return null;

        return getObjectInputStream().readObject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T readObject(Class<T> cls) throws IOException,
            ClassNotFoundException {
        return (T) readObject();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T readObject(Class<T> cls, Type type) throws IOException, ClassNotFoundException {
        return (T) readObject();
    }

    public InputStream getInputStream(){
        return getObjectInputStream();
    }

}

```

![image.png](https://cdn.nlark.com/yuque/0/2023/png/145710/1699263409159-4bc365de-877f-429c-8a81-123d23328a36.png#averageHue=%23202124&clientId=u9cade2e4-accb-4&from=paste&height=161&id=u2ddc141f&originHeight=161&originWidth=1051&originalType=binary&ratio=1&rotation=0&showTitle=false&size=30124&status=done&style=none&taskId=ud5663a6e-def8-4ac0-9d9d-0e3bc8d2f63&title=&width=1051)![image.png](https://cdn.nlark.com/yuque/0/2023/png/145710/1699263420045-b82eded9-7d1a-4757-a6f3-ffb48ed282e9.png#averageHue=%231f2124&clientId=u9cade2e4-accb-4&from=paste&height=296&id=ud1663c18&originHeight=296&originWidth=817&originalType=binary&ratio=1&rotation=0&showTitle=false&size=53230&status=done&style=none&taskId=u6b26ebe9-3404-491f-b7b8-8d2b93e8845&title=&width=817)

Note that the base class supports both inputStream and ObjectInputStream. If you don't forcefully convert, it will go to the wrong constructor, which will lead to the destruction of the stream.
