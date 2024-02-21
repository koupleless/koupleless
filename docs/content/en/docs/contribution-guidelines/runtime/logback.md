---
title: Logback's adaptation for multi-module environments
date: 2024-1-18T15:32:35+08:00
weight: 1
---

## Why Adaptation is Needed
The native logback framework only provides a default logging context, making it impossible to isolate log configurations between different modules. Consequently, in scenarios involving deploying multiple modules together, modules can only utilize the logging configuration of the base application, causing inconvenience when logging from individual modules.

## Multi-Module Adaptation Solution
Logback supports native extension `ch.qos.logback.classic.selector.ContextSelector`, which allows for a custom context selector. Ark provides a default implementation of `ContextSelector` to isolate `LoggerContext` for multiple modules (refer to `com.alipay.sofa.ark.common.adapter.ArkLogbackContextSelector`). Each module uses its independent `LoggerContext`, ensuring log configuration isolation.

During startup, the log configuration and context initialization are handled by Spring's log system `LogbackLoggingSystem`.

Specify the context selector as `com.alipay.sofa.ark.common.adapter.ArkLogbackContextSelector` and add the JVM startup parameter:
> -Dlogback.ContextSelector=com.alipay.sofa.ark.common.adapter.ArkLogbackContextSelector

When using SLF4J as the logging facade with logback as the logging implementation framework, during the base application startup, when the SLF4J static binding is first performed, the specific `ContextSelector` is initialized. If no custom context selector is specified, the `DefaultContextSelector` will be used. However, when we specify a context selector, the `ArkLogbackContextSelector` will be initialized as the context selector.

ch.qos.logback.classic.util.ContextSelectorStaticBinder.init

```java
public void init(LoggerContext defaultLoggerContext, Object key) {
  ...

  String contextSelectorStr = OptionHelper.getSystemProperty(ClassicConstants.LOGBACK_CONTEXT_SELECTOR);
  if (contextSelectorStr == null) {
  contextSelector = new DefaultContextSelector(defaultLoggerContext);
  } else if (contextSelectorStr.equals("JNDI")) {
  // if jndi is specified, let's use the appropriate class
  contextSelector = new ContextJNDISelector(defaultLoggerContext);
  } else {
  contextSelector = dynamicalContextSelector(defaultLoggerContext, contextSelectorStr);
  }
}

static ContextSelector dynamicalContextSelector(LoggerContext defaultLoggerContext, String contextSelectorStr) {
  Class<?> contextSelectorClass = Loader.loadClass(contextSelectorStr);
  Constructor cons = contextSelectorClass.getConstructor(new Class[] { LoggerContext.class });
  return (ContextSelector) cons.newInstance(defaultLoggerContext);
}
```

In the ArkLogbackContextSelector, we utilize ClassLoader to differentiate between different modules and cache the LoggerContext of each module based on its ClassLoader

When obtaining the LoggerContext based on the ClassLoader, during the startup of the Spring environment, the logging context is initialized via the Spring logging system. This is achieved by calling org.springframework.boot.logging.logback.LogbackLoggingSystem.getLoggerContext, which returns the LoggerContext specific to each module using the custom context selector implemented by Ark, com.alipay.sofa.ark.common.adapter.ArkLogbackContextSelector.getLoggerContext().

```java
public LoggerContext getLoggerContext() {
  ClassLoader classLoader = this.findClassLoader();
  if (classLoader == null) {
      return defaultLoggerContext;
  }
  return getContext(classLoader);
}
```

When obtaining the classloader, first, the thread's context classloader is retrieved. If it is identified as the classloader of the module, it is returned directly. If the TCCL (thread context classloader) is not the classloader of the module, the call stack of the Class objects is traversed through the ClassContext. When encountering the classloader of the module in the call stack, it is returned directly. This approach is taken to accommodate scenarios where the TCCL is not guaranteed to be the classloader of the module.
For example, when logging is performed in module code, and the current class is loaded by the module's classloader itself, traversing the ClassContext allows us to eventually obtain the classloader of the module, ensuring the use of the module-specific LoggerContext.

```java
private ClassLoader findClassLoader() {
  ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
  if (classLoader != null && CONTAINER_CLASS_LOADER.equals(classLoader.getClass().getName())) {
      return null;
  }
  if (classLoader != null && BIZ_CLASS_LOADER.equals(classLoader.getClass().getName())) {
      return classLoader;
  }

  Class<?>[] context = new SecurityManager() {
      @Override
      public Class<?>[] getClassContext() {
          return super.getClassContext();
      }
  }.getClassContext();
  if (context == null || context.length == 0) {
      return null;
  }
  for (Class<?> cls : context) {
      if (cls.getClassLoader() != null
          && BIZ_CLASS_LOADER.equals(cls.getClassLoader().getClass().getName())) {
          return cls.getClassLoader();
      }
  }

  return null;
}
```

Once the appropriate ClassLoader is obtained, different LoggerContext instances are selected. All module contexts are cached in com.alipay.sofa.ark.common.adapter.ArkLogbackContextSelector.CLASS_LOADER_LOGGER_CONTEXT with the ClassLoader as the key.

```java
private LoggerContext getContext(ClassLoader cls) {
  LoggerContext loggerContext = CLASS_LOADER_LOGGER_CONTEXT.get(cls);
  if (null == loggerContext) {
      synchronized (ArkLogbackContextSelector.class) {
          loggerContext = CLASS_LOADER_LOGGER_CONTEXT.get(cls);
          if (null == loggerContext) {
              loggerContext = new LoggerContext();
              loggerContext.setName(Integer.toHexString(System.identityHashCode(cls)));
              CLASS_LOADER_LOGGER_CONTEXT.put(cls, loggerContext);
          }
      }
  }
  return loggerContext;
}
```

## Sample Usage of Multi-Module Logback
[Sample Usage of Multi-Module Logback](https://github.com/koupleless/koupleless/tree/master/samples/springboot-samples/logging/logback/README.md)

[View Source Code of ArkLogbackContextSelector](https://github.com/sofastack/sofa-ark/blob/master/sofa-ark-parent/core/common/src/main/java/com/alipay/sofa/ark/common/adapter/ArkLogbackContextSelector.java)

