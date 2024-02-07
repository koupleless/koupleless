---
title: 编码规范
date: 2024-01-25T10:28:32+08:00
description: Koupleless 编码规范
weight: 100
---

## 基础规范
1. Koupleless 模块中官方验证并兼容的中间件客户端列表[详见此处](/docs/tutorials/module-development/runtime-compatibility-list)。基座中可以使用任意中间件客户端。
   <br/><br/>
2. 如果使用了模块热卸载能力，您可以使用如下 API 装饰模块代码中声明的 ExecutorService（典型如各种线程池）、Timer、Thread 对象，在模块卸载时，
   Koupleless Arklet 客户端会尝试自动清理被装饰器装饰过的 ExecutorService、Timer、Thread：
   <br/>
    - 在模块代码中，装饰需要自动清理的 ExecutorService，底层会调用 ExecutorService 对象的 shutdownNow 和 awaitTermination 接口，会尽可能优雅释放线程（不保证 100% 释放，比如线程一直在等待）：
      ShutdownExecutorServicesOnUninstallEventHandler.manageExecutorService(myExecutorService);
      其中，myExecutorService 需要是 ExecutorService 的子类型。
      您也可以在模块 SOFABoot properties 文件中配置 com.alipay.koupleless.executor.cleanup.timeout.seconds 指定线程池 awaitTermination 的优雅等待时间。
      <br/><br/>
    - 在模块代码中，装饰需要自动清理的 Timer，底层会调用 Timer 对象的 cancel：
      CancelTimersOnUninstallEventHandler.manageTimer(myTimer);
      <br/><br/>
    - 在模块代码中，装饰需要自动清理的 Thread，底层会强行调用 Thread 对象的 stop：
      ForceStopThreadsOnUninstallEventHandler.manageThread(myThread);
      注意：JDK 并不推荐强行 stop 线程，会导致线程非预期的强行释放锁，可能引发非预期问题。除非您确定线程被暴力关闭不会引发相关问题，否则慎用。
      <br/><br/>
3. 如果使用了模块热卸载能力，并且还有其他资源、对象需要清理，您可以监听 Spring 的 **ContextClosedEvent** 事件，在事件处理函数中清理必要的资源和对象，
   也可以在 Spring XML 定义 Bean 的地方指定它们的 **destroy-method，**在模块卸载时，Spring 会自动执行** destroy-method**。
   <br/><br/>
4. 基座启动时会部署所有模块，所以基座编码时，一定要向所有模块兼容，否则基座会发布失败。如果遇到无法绕过的不兼容变更（一般是在模块拆分过程中会有比较多的基座与模块不兼容变更），
   请参见[基座与模块不兼容发布](/docs/tutorials/module-operation/incompatible-base-and-module-upgrade)。
   <br/>

## 知识点
[模块瘦身](../module-slimming)  (重要)<br />
[模块与模块、模块与基座通信](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-ark-jvm/)  (重要)<br />
[模块测试](../module-debug)  (重要)<br />
[模块复用基座拦截器](../reuse-base-interceptor)<br />
[模块复用基座数据源](../reuse-base-datasource)<br />
[基座与模块间类委托加载原理介绍](/docs/introduction/architecture/class-delegation-principle)

<br/>
<br/>
