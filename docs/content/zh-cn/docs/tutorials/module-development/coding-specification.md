---
title: 编码规范
weight: 100
---

<a name="ARno2"></a>
## 基础规范
1. SOFAServerless 模块中官方验证并兼容的中间件客户端列表[详见此处](/docs/tutorials/module-operation/runtime-compatibility-list)。基座中可以使用任意中间件客户端。
2. 如果使用了模块热卸载能力，模块自定义的 Timer、ThreadPool 等需要在模块**卸载时手动清理**。您可以监听 Spring 的 **ContextClosedEvent** 事件，在事件处理函数中清理必要资源，也可以在 Spring XML 定义 Timer、ThreadPool 的地方指定它们的 **destroy-method，**在模块卸载时，Spring 会自动执行** destroy-method**。
3. 基座启动时会部署所有模块，所以基座编码时，一定要向所有模块兼容，否则基座会发布失败。如果遇到无法绕过的不兼容变更（一般是在模块拆分过程中会有比较多的基座与模块不兼容变更），请参见[基座与模块不兼容发布](/docs/tutorials/module-operation/incompatible-base-and-module-upgrade)。

<a name="MXoEX"></a>
## 知识点
[模块瘦身](../module-slimming)  (重要)<br />
[模块与模块、模块与基座通信](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-ark-jvm/)  (重要)<br />
[模块测试](../module-debug)  (重要)<br />
[模块复用基座拦截器](../reuse-base-interceptor)<br />
[模块复用基座数据源](../reuse-base-datasource)<br />
[基座与模块间类委托加载原理介绍](/docs/introduction/architecture/class-delegation-principle)

<br/>
<br/>
