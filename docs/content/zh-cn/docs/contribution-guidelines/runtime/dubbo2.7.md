---
title: dubbo2.7 的多模块化适配
date: 2024-1-19T19:55:35+08:00
weight: 1
---

## 为什么需要做适配
原生 dubbo2.7 在多模块场景下，无法支持模块发布自己的dubbo服务，调用时存在序列化、类加载异常等一系列问题。

## 多模块适配方案

dubbo2.7多模块适配SDK
```xml
<dependency>
   <groupId>com.alipay.sofa.serverless</groupId>
   <artifactId>sofa-serverless-adapter-dubbo2.7</artifactId>
   <version>0.5.7-SNAPSHOT</version>
</dependency>
```

主要从类加载、服务发布、服务卸载、服务隔离、模块维度服务管理、配置管理、序列化等方面进行适配。

### 1. AnnotatedBeanDefinitionRegistryUtils使用基座classloader无法加载模块类
com.alibaba.spring.util.AnnotatedBeanDefinitionRegistryUtils#isPresentBean

```java
public static boolean isPresentBean(BeanDefinitionRegistry registry, Class<?> annotatedClass) {
    ...

    //        ClassLoader classLoader = annotatedClass.getClassLoader(); // 原生逻辑
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();   // 改为使用tccl加载类

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

### 2. 模块维度的服务、配置资源管理
1. com.alipay.sofa.serverless.support.dubbo.ServerlessServiceRepository 替代原生 org.apache.dubbo.rpc.model.ServiceRepository
   
原生service采用interfaceName作为缓存，在基座、模块发布同样interface，不同group服务时，无法区分，替代原生service缓存模型，采用Interface Class类型作为key，同时采用包含有group的path作为key，支持基座、模块发布同interface不同group的场景
```java
private static ConcurrentMap<Class<?>, ServiceDescriptor> globalClassServices = new ConcurrentHashMap<>();

private static ConcurrentMap<String, ServiceDescriptor>   globalPathServices  = new ConcurrentHashMap<>();
```
  
2. com.alipay.sofa.serverless.support.dubbo.ServerlessConfigManager 替代原生 org.apache.dubbo.config.context.ConfigManager 
    
    为原生config添加classloader维度的key，不同模块根据classloader隔离不同的配置
    
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
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();   // 根据当前线程classloader隔离不同配置缓存
    globalConfigsCache.computeIfAbsent(contextClassLoader, k -> new HashMap<>());
    return globalConfigsCache.get(contextClassLoader);
}
```

ServerlessServiceRepository 和 ServerlessConfigManager 都依赖 dubbo ExtensionLoader 的扩展机制，从而替代原生逻辑，具体原理可参考 org.apache.dubbo.common.extension.ExtensionLoader.createExtension

### 3. 模块维度服务发布、服务卸载
override DubboBootstrapApplicationListener 禁止原生dubbo模块启动、卸载时发布、卸载服务

- com.alipay.sofa.serverless.support.dubbo.BizDubboBootstrapListener

原生dubbo2.7只在基座启动完成后发布dubbo服务，在多模块时，无法支持模块的服务发布，Ark采用监听器监听模块启动事件，并手动调用dubbo进行模块维度的服务发布

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

原生dubbo2.7在模块卸载时会调用DubboShutdownHook，将JVM中所有dubbo service unexport，导致模块卸载后基座、其余模块服务均被卸载，Ark采用监听器监听模块spring上下文关闭事件，手动卸载当前模块的dubbo服务，保留基座、其余模块的dubbo服务

```java
private void onContextClosedEvent(ContextClosedEvent event) {
        // DubboBootstrap.unexportServices 会 unexport 所有服务，只需要 unexport 当前 biz 的服务即可
        Map<String, ServiceConfigBase<?>> exportedServices = ReflectionUtils.getField(dubboBootstrap, DubboBootstrap.class, "exportedServices");

        Set<String> bizUnexportServices = new HashSet<>();
        for (Map.Entry<String, ServiceConfigBase<?>> entry : exportedServices.entrySet()) {
            String serviceKey = entry.getKey();
            ServiceConfigBase<?> sc = entry.getValue();
            if (sc.getRef().getClass().getClassLoader() == Thread.currentThread().getContextClassLoader()) {   // 根据ref服务实现的类加载器区分模块服务
                bizUnexportServices.add(serviceKey);
                configManager.removeConfig(sc);   // 从configManager配置管理中移除服务配置
                sc.unexport();   // 进行服务unexport
                serviceRepository.unregisterService(sc.getUniqueServiceName());   // 从serviceRepository服务管理中移除配置
            }
        }
        for (String service : bizUnexportServices) {
            exportedServices.remove(service);    // 从DubboBootstrap中移除该service
        }
    }
```

### 4. 服务路由
- com.alipay.sofa.serverless.support.dubbo.ConsumerRedefinePathFilter

dubbo服务调用时通过path从ServiceRepository中获取正确的服务端服务模型（包括interface、param、return类型等）进行服务调用、参数、返回值的序列化，原生dubbo2.7采用interfaceName作为path查找service model，无法支持多模块下基座模块发布同interface的场景，Ark自定义consumer端filter添加group信息到path中，以便provider端进行正确的服务路由

```java
public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
  if (invocation instanceof RpcInvocation) {
      RpcInvocation rpcInvocation = (RpcInvocation) invocation;
      // 原生path为interfaceName，如com.alipay.sofa.rpc.dubbo27.model.DemoService
      // 修改后path为serviceUniqueName，如masterBiz/com.alipay.sofa.rpc.dubbo27.model.DemoService
      rpcInvocation.setAttachment("interface", rpcInvocation.getTargetServiceUniqueName());   // 原生path为interfaceName，如
  }
  return invoker.invoke(invocation);
}
```

### 5. 序列化
- org.apache.dubbo.common.serialize.java.JavaSerialization
- org.apache.dubbo.common.serialize.java.ClassLoaderJavaObjectInput
- org.apache.dubbo.common.serialize.java.ClassLoaderObjectInputStream

在获取序列化工具JavaSerialization时，使用ClassLoaderJavaObjectInput替代原生JavaObjectInput，传递provider端service classloader信息

```java
// org.apache.dubbo.common.serialize.java.JavaSerialization
public ObjectInput deserialize(URL url, InputStream is) throws IOException {
    return new ClassLoaderJavaObjectInput(new ClassLoaderObjectInputStream(null, is));   // 使用ClassLoaderJavaObjectInput替代原生JavaObjectInput，传递provider端service classloader信息
}

// org.apache.dubbo.common.serialize.java.ClassLoaderObjectInputStream
private ClassLoader classLoader;

public ClassLoaderObjectInputStream(final ClassLoader classLoader, final InputStream inputStream) {
  super(inputStream);
  this.classLoader = classLoader;
}
```

- org.apache.dubbo.rpc.protocol.dubbo.DecodeableRpcInvocation 服务端反序列化参数

```java
// patch begin
if (in instanceof ClassLoaderJavaObjectInput) {
   InputStream is = ((ClassLoaderJavaObjectInput) in).getInputStream();
   if (is instanceof ClassLoaderObjectInputStream) {
      ClassLoader cl = serviceDescriptor.getServiceInterfaceClass().getClassLoader();  // 设置provider端service classloader信息到ClassLoaderObjectInputStream中
      ((ClassLoaderObjectInputStream) is).setClassLoader(cl);
   }
}
// patch end
```
- org.apache.dubbo.rpc.protocol.dubbo.DecodeableRpcResult 客户端反序列化返回值

```java
// patch begin
if (in instanceof ClassLoaderJavaObjectInput) {
   InputStream is = ((ClassLoaderJavaObjectInput) in).getInputStream();
   if (is instanceof ClassLoaderObjectInputStream) {
       ClassLoader cl = invocation.getInvoker().getInterface().getClassLoader(); // 设置consumer端service classloader信息到ClassLoaderObjectInputStream中
       ((ClassLoaderObjectInputStream) is).setClassLoader(cl);
   }
}
// patch end
```

## 多模块 dubbo2.7 使用样例

[多模块 dubbo2.7 使用样例](https://github.com/sofastack/sofa-serverless/tree/master/samples/dubbo-samples/rpc/dubbo27/README.md)

[dubbo2.7多模块适配sdk源码](https://github.com/sofastack/sofa-serverless/tree/master/sofa-serverless-runtime/sofa-serverless-adapter-ext/sofa-serverless-adapter-dubbo2.7)

