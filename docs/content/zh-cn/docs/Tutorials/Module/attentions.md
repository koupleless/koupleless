---
title: 模块规范
date: 2017-01-04
weight: 2
---

## 基础规范

1. 如果使用了热卸载，模块自定义的 Timer、ThreadPool 等需要在模块卸载时手动清理，官方兼容的中间件无需关心，会被 SOFAServerelss 自动清理。您可以监听 Spring 的 ContextClosedEvent 事件，在事件处理函数中清理必要资源，也可以在 Spring XML 定义 Timer、ThreadPool 的地方指定它们的 destroy-method，在模块卸载时，Spring 会自动执行 destroy-method。
2. 基座启动时会部署所有模块，所以基座编码时，一定要向所有模块兼容，否则基座会发布失败。如果遇到无法绕过的不兼容变更（一般是在模块拆分过程中会有比较多的基座与模块不兼容变更），请参见基座与模块不兼容发布。

## 日志规范

1. 请使用 slf4j 提供的接口获取 Logger 并打印日志。
2. 日志配置文件统一为 resources/log4j2-spring.xml。
3. 如果使用异步日志，请使用 RollingFileAppender 搭配 AsyncLogger/AsyncRoot 的组合。
4. 确保未引入这些依赖 slf4j-log4j12、jcl-over-slf4j、logback-classic、logback-core。

如果有任何日志问题，请先看 stdout.log 和 stderr.log 里是否有日志初始化相关报错，再进一步排查。