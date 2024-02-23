---
title: Koupleless 多应用治理补丁治理
date: 2024-01-25T10:28:32+08:00
description: Koupleless 多应用治理补丁治理
weight: 1
---

# Koupleless 为什么需要多应用治理补丁？
Koupleless 是一种多应用的架构，而传统的中间件可能只考虑了一个应用的场景，故在一些行为上无法兼容多应用共存的行为，会发生共享变量污染、classLoader 加载异常、class 判断不符合预期等问题。
由此，在使用 Koupleless 中间件时，我们需要对一些潜在的问题做补丁，覆盖掉原有中间件的实现，使开源的中间件也能兼容多应用的模式。

# Koupleless 多应用治理补丁方案调研
在多应用兼容性治理中，我们不仅仅只考虑生产部署，还要考虑用户本地开发的兼容性(IDEA 点击 Debug)，单测编写的兼容性(如 @SpringbootTest)等等。

<br/>下面是不同方案的对比表格。
## 方案对比

| 方案名                                                            | 接入成本 | 可维护性 | 部署兼容性 | IDE 兼容性 | 单测兼容性 |
|----------------------------------------------------------------| ------- | ------- | -------- | --------- | --------- |
| A：将补丁包的依赖放在 maven dependency 的首部，以此保证补丁类能优先被 classLoader 加载。   | 低。<br>用户只需要控制 maven 家在的顺序。 | 低<br>用户需要严格保证相关依赖在最前面，且启动的时候不手动传入 classpath。 | 兼容✅ | 兼容✅ | 兼容✅ |
| B：通过 maven 插件修改 springboot 构建产物的索引文件的顺序。                       | 低。<br>只需要新增一个 package 周期的 maven 插件即可，用户感知低。 | 中<br>用户需要保证启动的时候不手动传入 classpath。 | 兼容✅ | 不兼容❌<br>jetbrains 无法兼容，jetbrains 会自己构建 cli 命令行把 classpath 按照 maven 依赖的顺序传进去，这会导致 adapter 的顺序加载不一定是最优先的。 | 不兼容❌<br>单测不走 repackage 周期，不依赖 classpath.idx 文件。 |
| C：新增自定义的 springboot 的 jarlaunch 启动器，通过启动器控制 classLoader 加载的行为。 | 高。<br>需要用户修改自己的基座启动逻辑，使用 Koupleless 自定义的 jarlaunch。 | 高<br>自定义的 jarlaunch 可以通过钩子控制代码的加载顺序。 | 兼容✅ | 兼容✅<br>但需要配置 IDE 使用自定义的 jarlaunch。 | 不兼容❌<br>因为单测不会走 jarlaunch 逻辑。 |
| D：增强基座的 classloader, 保证优先搜索和加载补丁类。                             | 高。<br>用户需要初始化增强的代码，且该模式对 sofa-ark 识别 master biz 的逻辑也有侵入，需要改造支持。 | 高<br>基座的 classloader 可以编程化地控制依赖加载的顺序。 | 兼容✅ | 兼容✅ | 兼容✅ |
| E：通过 maven 插件配置配置拷贝补丁类代码到当前项目中, 当前项目的文件会被优先加载。                 | 高。<br>maven 目前的拷贝插件无法用通配符，所以接入一个 adapter 就得多一个配置。 | 高<br>用户只要配置了，就可以保证依赖有限被加载（因为本地项目的类最优先被加载）。 | 兼容✅ | 兼容✅ | 不兼容❌<br>因为单测不会走到 package 周期，而 maven 的拷贝插件是在 package 周期生效的。 |

## 结论
综合地来看，没有办法完全做到用户 0 感知接入，每个方法都需要微小程度的业务改造。
在诸多方案中，A 和 D 能做到完全兼容，不过 A 方案不需要业务改代码，也不会侵入运行时逻辑，仅需要用户在 maven dependency 的第一行中加入如下依赖:
```xml
<dependency>
  <groupId>com.alipay.sofa.koupleless</groupId>
  <artifactId>koupleless-base-starter</artifactId>
  <version>${koupleless.runtime.version}</version>
  <type>pom</type>
</dependency>
```
故我们将采取方案 A。
<br/>如果你有更多的想法，或输入，欢迎开源社区讨论！
