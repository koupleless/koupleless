---
title: logback 的多模块化适配
date: 2024-1-18T15:32:35+08:00
weight: 1
---

## 为什么需要做适配
原生 logback 只有默认日志上下文，各个模块间日志配置无法隔离，无法支持独立的模块日志配置，最终导致在合并部署多模块场景下，模块只能使用基座的日志配置，对模块日志打印带来不便。

## 多模块适配方案
Logback 支持原生扩展 ch.qos.logback.classic.selector.ContextSelector，该接口支持自定义上下文选择器，Ark 默认实现了 ContextSelector 对多个模块的 LoggerContext 进行隔离 （参考 com.alipay.sofa.ark.common.adapter.ArkLogbackContextSelector），不同模块使用各自独立的 LoggerContext，确保日志配置隔离

启动期，经由 spring 日志系统 LogbackLoggingSystem 对模块日志配置以及日志上下文进行初始化

指定上下文选择器为 com.alipay.sofa.ark.common.adapter.ArkLogbackContextSelector，添加JVM启动参数
> -Dlogback.ContextSelector=com.alipay.sofa.ark.common.adapter.ArkLogbackContextSelector

当使用 slf4j 作为日志门面，logback 作为日志实现框架时，在基座启动时，首次进行 slf4j 静态绑定时，将初始化具体的 ContextSelector，当没有自定义上下文选择器时，将使用 DefaultContextSelector, 当我们指定上下文选择器时，将会初始化 ArkLogbackContextSelector 作为上下文选择器

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

在 ArkLogbackContextSelector 中，我们使用 ClassLoader 区分不同模块，将模块 LoggerContext 根据 ClassLoader 缓存

根据 classloader 获取不同的 LoggerContext，在 Spring 环境启动时，根据 spring 日志系统初始化日志上下文，通过 org.springframework.boot.logging.logback.LogbackLoggingSystem.getLoggerContext 获取日志上下文，此时将会使用 Ark 实现的自定义上下文选择器 com.alipay.sofa.ark.common.adapter.ArkLogbackContextSelector.getLoggerContext() 返回不同模块各自的 LoggerContext

```java
public LoggerContext getLoggerContext() {
  ClassLoader classLoader = this.findClassLoader();
  if (classLoader == null) {
      return defaultLoggerContext;
  }
  return getContext(classLoader);
}
```

获取 classloader 时，首先获取线程上下文 classloader，当发现是模块的classloader时，直接返回，若tccl不是模块classloader，则从ClassContext中获取调用Class堆栈，遍历堆栈，当发现模块classloader时直接返回，这样做的目的是为了兼容tccl没有保证为模块classloader时的场景，
比如在模块代码中使用logger打印日志时，当前类由模块classloader自己加载，通过ClassContext遍历可以最终获得当前类，获取到模块classloader，以便确保使用模块对应的 LoggerContext

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

获取到合适 classloader 后，为不同 classloader选择不同的 LoggerContext 实例，所有模块上下文缓存在 com.alipay.sofa.ark.common.adapter.ArkLogbackContextSelector.CLASS_LOADER_LOGGER_CONTEXT 中，以 classloader 为 key

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

## 多模块 logback 使用样例
[多模块 logback 使用样例](https://github.com/sofastack/sofa-serverless/tree/master/samples/springboot-samples/logging/logback/README.md)

[详细查看ArkLogbackContextSelector源码](https://github.com/sofastack/sofa-ark/blob/master/sofa-ark-parent/core/common/src/main/java/com/alipay/sofa/ark/common/adapter/ArkLogbackContextSelector.java)

