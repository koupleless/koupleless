---
title: 模块编码
date: 2017-01-04
weight: 2
---

## 基础规范
1. 如果使用了热卸载，模块自定义的 Timer、ThreadPool 等需要在模块卸载时手动清理，官方兼容的中间件无需关心，会被 SOFAServerelss 自动清理。您可以监听 Spring 的 ContextClosedEvent 事件，在事件处理函数中清理必要资源，也可以在 Spring XML 定义 Timer、ThreadPool 的地方指定它们的 destroy-method，在模块卸载时，Spring 会自动执行 destroy-method。
2. 基座启动时会部署所有模块，所以基座编码时，一定要向所有模块兼容，否则基座会发布失败。如果遇到无法绕过的不兼容变更（一般是在模块拆分过程中会有比较多的基座与模块不兼容变更），请参见基座与模块不兼容发布。

## 基本原理
Serverless 底层借助 SOFAArk 框架，实现了模块之间、模块和基座之间的相互隔离，以下两个核心逻辑对编码非常重要，需要深刻理解：


1. 基座有独立的类加载器和 Spring 上下文，模块也有独立的类加载器和 Spring 上下文，相互之间 Spring 上下文都是隔离的。
2. 模块启动时会初始化各种对象，会优先使用模块的类加载器去加载构建产物 FatJar 中的 class、resource 和 Jar 包，找不到的类会委托基座的类加载器去查找。

## 模块瘦身
基于这套类委托的加载机制，让基座和模块共用的 class、resource 和 Jar 包通通下沉到基座中，可以让模块构建产物非常小，更重要的是还能让模块在运行中大量复用基座已有的 class、bean、service、IO 连接池、线程池等资源，从而模块消耗的内存非常少，启动也能非常快。
所谓模块瘦身，就是让基座已经有的 Jar 依赖务必在模块中剔除干净，在主 pom.xml 和 bootstrap/pom.xml 将共用的 Jar 包 scope 都声明为 provided，让其不参与打包构建。

## 日志规范
1. 请使用 slf4j 提供的接口获取 Logger 并打印日志。
2. 日志配置文件统一为 resources/log4j2-spring.xml。
3. 如果使用异步日志，请使用 RollingFileAppender 搭配 AsyncLogger/AsyncRoot 的组合。
4. 确保未引入这些依赖 slf4j-log4j12、jcl-over-slf4j、logback-classic、logback-core。

如果有任何日志问题，请先看 stdout.log 和 stderr.log 里是否有日志初始化相关报错，再进一步排查。

## 模块中支持的中间件
官方兼容的中间件清单可参见此处。正在整理中。

其它中间件兼容方式可参见此处。正在建设中。
