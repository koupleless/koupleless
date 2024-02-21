---
title: Adapting to Multi-Module with Dubbo 2.7
date: 2024-1-19T19:55:35+08:00
weight: 1
---

## Why Adaptation is Needed
The native Dubbo 2.7 cannot support module publishing its own Dubbo services in multi-module scenarios, leading to a series of issues such as serialization and class loading exceptions during invocation.

## Multi-Module Adaptation Solutions

Dubbo 2.7 Multi-Module Adaptation SDK
```xml
<dependency>
   <groupId>com.alipay.sofa.koupleless</groupId>
   <artifactId>koupleless-adapter-dubbo2.7</artifactId>
   <version>${koupleless.runtime.version}</version>
</dependency>
```

Mainly from aspects such as class loading, service publishing, service unloading, service isolation, module-level service management, configuration management, serialization, etc.

### 1. AnnotatedBeanDefinitionRegistryUtils Unable to Load Module Classes Using the Base Classloader
com.alibaba.spring.util.AnnotatedBeanDefinitionRegistryUtils#isPresentBean

```java
public static boolean isPresentBean(BeanDefinitionRegistry registry, Class<?> annotatedClass) {
    ...

    //        ClassLoader classLoader = annotatedClass.getClassLoader(); // Original logic
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();   // Changed to use tccl to load classes

    for (String beanName : beanNames) {
        BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
        if (beanDefinition instanceof AnnotatedBeanDefinition) {
            ...
            String className = annotationMetadata.getClassName();
            Class<?> targetClass = resolveClassName(className, classLoader);
            ...
        }
    }

    return present;
}
```

### 2. Module-Level Service and Configuration Resource Management
1. com.alipay.sofa.koupleless.support.dubbo.ServerlessServiceRepository Replaces the Native org.apache.dubbo.rpc.model.ServiceRepository

The native service uses the interfaceName as the cache key. When both the base and the module publish services with the same interface but different groups, it cannot distinguish between them. Replacing the native service caching model, using the Interface Class type as the key, and using the path containing the group as the key to support scenarios where the base and the module publish services with the same interface but different groups.
```java
private static ConcurrentMap<Class<?>, ServiceDescriptor> globalClassServices = new ConcurrentHashMap<>();

private static ConcurrentMap<String, ServiceDescriptor>   globalPathServices  = new ConcurrentHashMap<>();
```
  
2. com.alipay.sofa.koupleless.support.dubbo.ServerlessConfigManager Replaces the Native org.apache.dubbo.config.context.ConfigManager 

   Adds a classloader dimension key to the original config to isolate different configurations according to classloader in different modules.
    
```java
final Map<ClassLoader, Map<String, Map<String, AbstractConfig>>> globalConfigsCache = new HashMap<>();

public void addConfig(AbstractConfig config, boolean unique) {
    ...
    write(() -> {
        Map<String, AbstractConfig> configsMap = getCurrentConfigsCache().computeIfAbsent(getTagName(config.getClass()), type -> newMap());
        addIfAbsent(config, configsMap, unique);
    });
}
private Map<String, Map<String, AbstractConfig>> getCurrentConfigsCache() {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();   // Based on the current thread classloader to isolate different configuration caches
    globalConfigsCache.computeIfAbsent(contextClassLoader, k -> new HashMap<>());
    return globalConfigsCache.get(contextClassLoader);
}
```

ServerlessServiceRepository and ServerlessConfigManager both depend on the dubbo ExtensionLoader's extension mechanism to replace the original logic. For specific principles, please refer to org.apache.dubbo.common.extension.ExtensionLoader.createExtension.

### 3. Module-Level Service Install and Uninstall
override DubboBootstrapApplicationListener to prevent the original Dubbo module from starting or uninstalling when publishing or uninstalling services

- com.alipay.sofa.koupleless.support.dubbo.BizDubboBootstrapListener

The native Dubbo 2.7 only publishes Dubbo services after the base module is started. In the case of multi-modules, it cannot support module-level service publishing. Ark listens for module startup events using a listener and manually calls Dubbo to publish module-level services.

```java
private void onContextRefreshedEvent(ContextRefreshedEvent event) {
  try {
      ReflectionUtils.getMethod(DubboBootstrap.class, "exportServices")
          .invoke(dubboBootstrap);
      ReflectionUtils.getMethod(DubboBootstrap.class, "referServices").invoke(dubboBootstrap);
  } catch (Exception e) {
      
  }
}
```

The original Dubbo 2.7 unexports all services in the JVM when a module is uninstalled, leading to the unexporting of services from the base and other modules after the module is uninstalled. Ark listens for the spring context closing event of the module and manually unexports Dubbo services of the current module, retaining Dubbo services of the base and other modules.

```java
private void onContextClosedEvent(ContextClosedEvent event) {
        // DubboBootstrap.unexportServices unexports all services, only need to unexport services of the current biz
        Map<String, ServiceConfigBase<?>> exportedServices = ReflectionUtils.getField(dubboBootstrap, DubboBootstrap.class, "exportedServices");

        Set<String> bizUnexportServices = new HashSet<>();
        for (Map.Entry<String, ServiceConfigBase<?>> entry : exportedServices.entrySet()) {
            String serviceKey = entry.getKey();
            ServiceConfigBase<?> sc = entry.getValue();
            if (sc.getRef().getClass().getClassLoader() == Thread.currentThread().getContextClassLoader()) {   // Distinguish module services based on the classloader of ref service implementation
                bizUnexportServices.add(serviceKey);
                configManager.removeConfig(sc);   // Remove service configuration from configManager
                sc.unexport();   // Unexport service
                serviceRepository.unregisterService(sc.getUniqueServiceName());   // Remove from serviceRepository
            }
        }
        for (String service : bizUnexportServices) {
            exportedServices.remove(service);    // Remove service from DubboBootstrap
        }
    }
```

### 4. Service Routing
- com.alipay.sofa.koupleless.support.dubbo.ConsumerRedefinePathFilter

When invoking Dubbo services, the service model (including interface, param, return types, etc.) is obtained from the ServiceRepository based on the path to perform service invocation, parameter, and return value serialization. The original Dubbo 2.7 uses interfaceName as the path to find the service model, which cannot support the scenario where the base module and other modules publish services with the same interface. Ark adds group information to the path on the consumer side through a custom filter to facilitate correct service routing on the provider side.

```java
public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
  if (invocation instanceof RpcInvocation) {
      RpcInvocation rpcInvocation = (RpcInvocation) invocation;
      // Original path is interfaceName, such as com.alipay.sofa.rpc.dubbo27.model.DemoService
      // Modified path is serviceUniqueName, such as masterBiz/com.alipay.sofa.rpc.dubbo27.model.DemoService
      rpcInvocation.setAttachment("interface", rpcInvocation.getTargetServiceUniqueName());   // Original path is interfaceName, such as
  }
  return invoker.invoke(invocation);
}
```

### 5. Serialization
- org.apache.dubbo.common.serialize.java.JavaSerialization
- org.apache.dubbo.common.serialize.java.ClassLoaderJavaObjectInput
- org.apache.dubbo.common.serialize.java.ClassLoaderObjectInputStream

When obtaining the serialization tool JavaSerialization, use ClassLoaderJavaObjectInput instead of the original JavaObjectInput and pass provider-side service classloader information.

```java
// org.apache.dubbo.common.serialize.java.JavaSerialization
public ObjectInput deserialize(URL url, InputStream is) throws IOException {
    return new ClassLoaderJavaObjectInput(new ClassLoaderObjectInputStream(null, is));   // Use ClassLoaderJavaObjectInput instead of the original JavaObjectInput, pass provider-side service classloader information
}

// org.apache.dubbo.common.serialize.java.ClassLoaderObjectInputStream
private ClassLoader classLoader;

public ClassLoaderObjectInputStream(final ClassLoader classLoader, final InputStream inputStream) {
  super(inputStream);
  this.classLoader = classLoader;
}
```

- org.apache.dubbo.rpc.protocol.dubbo.DecodeableRpcResult Client-side deserialization of return values

```java
// patch begin
if (in instanceof ClassLoaderJavaObjectInput) {
   InputStream is = ((ClassLoaderJavaObjectInput) in).getInputStream();
   if (is instanceof ClassLoaderObjectInputStream) {
      ClassLoader cl = serviceDescriptor.getServiceInterfaceClass().getClassLoader();  // Set provider-side service classloader information to ClassLoaderObjectInputStream
      ((ClassLoaderObjectInputStream) is).setClassLoader(cl);
   }
}
// patch end
```
- org.apache.dubbo.rpc.protocol.dubbo.DecodeableRpcResult Client-side deserialization of return values

```java
// patch begin
if (in instanceof ClassLoaderJavaObjectInput) {
   InputStream is = ((ClassLoaderJavaObjectInput) in).getInputStream();
   if (is instanceof ClassLoaderObjectInputStream) {
       ClassLoader cl = invocation.getInvoker().getInterface().getClassLoader(); // Set consumer-side service classloader information to ClassLoaderObjectInputStream
       ((ClassLoaderObjectInputStream) is).setClassLoader(cl);
   }
}
// patch end
```

## Example of Using Dubbo 2.7 in a Multi-Module Environment

[Example of Using Dubbo 2.7 in a Multi-Module Environment](https://github.com/koupleless/koupleless/tree/main/samples/dubbo-samples/rpc/dubbo27/README.md)

[dubbo2.7 Multi-Module Adaptation SDK Source Code](https://github.com/koupleless/koupleless/tree/main/koupleless-runtime/koupleless-adapter-ext/koupleless-adapter-dubbo2.7)

