---
title: log4j2 的多模块化适配
date: 2023-10-10T20:32:35+08:00
weight: 1
---

## 为什么需要做适配
原生 log4j2 在多模块下，模块没有独立打印的日志目录，统一打印到基座目录里，导致日志和对应的监控无法隔离。这里做适配的目的就是要让模块能有独立的日志目录。

## 普通应用 log4j2 的初始化
在 Spring 启动前，log4j2 会使用默认值初始化一次各种 logContext 和 Configuration，然后在 Spring 启动过程中，监听 Spring 事件进行初始化
`org.springframework.boot.context.logging.LoggingApplicationListener`，这里会调用到 Log4j2LoggingSystem.initialize 方法

![](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1696930949183-9519451c-be76-4d9b-bb6b-28a1b21e7fa7.png)

该方法会根据 loggerContext 来判断是否已经初始化过了

> 这里在多模块下会存在问题一
> 
> 这里的 getLoggerContext 是根据 org.apache.logging.log4j.LogManager 所在 classLoader 来获取 LoggerContext。根据某个类所在 ClassLoader 来提取 LoggerContext 在多模块化里会存在不稳定，因为模块一些类可以设置为委托给基座加载，所以模块里启动的时候，可能拿到的 LoggerContext 是基座的，导致这里 isAlreadyInitialized 直接返回，导致模块的 log4j2 日志无法进一步根据用户配置文件配置。

如果没初始化过，则会进入 super.initialize, 这里需要做两部分事情：

1. 获取到日志配置文件
2. 解析日志配置文件里的变量值
   这两部分在多模块里都可能存在问题，先看下普通应用过程是如何完成这两步的

### 获取日志配置文件
![](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1696931678652-81a19dc2-f618-48b0-add3-d098d3781966.png?x-oss-process=image%2Fresize%2Cw_1500%2Climit_0)

可以看到是通过 ResourceUtils.getURL 获取的 location 对应日志配置文件的 url，这里通过获取到当前线程上下文 ClassLoader 来获取 URL，这在多模块下没有问题（因为每个模块启动时线程上下文已经是 模块自身的 ClassLoader ）。

![](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1696931908899-f1fac1bb-f365-49f9-81a2-3e2d924c2b7d.png?x-oss-process=image%2Fresize%2Cw_1500%2Climit_0)

### 解析日志配置值

配置文件里有一些变量，例如这些变量

![](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1696932148670-d04bde21-e46b-476c-9cf5-53e43cc4dbe2.png)

这些变量的解析逻辑在 `org.apache.logging.log4j.core.lookup.AbstractLookup` 的具体实现里，包括


||变量写法|	代码逻辑地址|
|-|-|-|
| ${bundle:application:logging.file.path} |	org.apache.logging.log4j.core.lookup.ResourceBundleLookup |	根据 ResourceBundleLookup 所在 ClassLoader 提前到 application.properties, 读取里面的值 |
| ${ctx:logging.file.path} | org.apache.logging.log4j.core.lookup.ContextMapLookup | 根据 LoggerContext 上下文 ThreadContex 存储的值来提起，这里需要提前把 applicaiton.properties 的值设置到 ThreadContext 中 |

根据上面判断通过 bundle 的方式配置在多模块里不可行，因为 ResourceBundleLookup 可能只存在于基座中，导致始终只能拿到基座的 application.properties，导致模块的日志配置路径与基座相同，模块日志都打到基座中。所以需要改造成使用 ContextMapLookup。

## 预期多模块合并下的日志
基座与模块都能使用独立的日志配置、配置值，完全独立。但由于上述分析中，存在两处可能导致模块无法正常初始化的逻辑，故这里需要多 log4j2 进行适配。

### 多模块适配点
1. getLoggerContext() 能拿到模块自身的 LoggerContext
![](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1696938182575-51ce1066-21f0-47bb-8bdb-c3c7d0814ca3.png)
2. 需要调整成使用 ContextMapLookup，从而模块日志能获取到模块应用名，日志能打印到模块目录里

   a. 模块启动时将 application.properties 的值设置到 ThreadContext 中
   b. 日志配置时，只能使用 ctx:xxx:xxx 的配置方式

## 模块改造方式
[详细查看源码](https://github.com/sofastack/sofa-serverless/tree/master/sofa-serverless-runtime/sofa-serverless-adapter-ext/sofa-serverless-adapter-log4j2/src/main/java/com/alipay/sofa/serverless/log4j2)

