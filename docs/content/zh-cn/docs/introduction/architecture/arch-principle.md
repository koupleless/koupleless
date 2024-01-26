---
title: 架构原理
date: 2024-01-25T10:28:32+08:00
description: Koupleless 架构
weight: 100
---

## 模块化应用架构
为了解决这些问题，我们对应用同时做了横向和纵向的拆分。首先第一步纵向拆分：把应用拆分成**基座**和**业务**两层，这两层分别对应两层的组织分工。基座小组与传统应用一样，负责机器维护、通用逻辑沉淀、业务架构治理，并为业务提供运行资源和环境。通过关注点分离的方式为业务屏蔽业务以下所有基础设施，聚焦在业务自身上。第二部我们将业务进行横向切分出多个模块，多个模块之间独立并行迭代互不影响，同时模块由于不包含基座部分，构建产物非常轻量，启动逻辑也只包含业务本身，所以启动快，具备秒级的验证能力，让模块开发得到极致的提效。<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695131313965-18385213-eded-4a6b-b554-db5312fa2c9d.png#clientId=ua84a92a5-30aa-4&from=paste&height=431&id=udb6b29d5&originHeight=862&originWidth=3448&originalType=binary&ratio=2&rotation=0&showTitle=false&size=192627&status=done&style=none&taskId=u9a114a24-0887-48d9-87b2-57d3e15eb80&title=&width=1724)<br />拆分之前，每个开发者可能感知从框架到中间件到业务公共部分到业务自身所有代码和逻辑，拆分后，团队的协作分工也从发生改变，研发人员分工出两种角色，基座和模块开发者，模块开发者不关系资源与容量，享受秒级部署验证能力，聚焦在业务逻辑自身上。<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695131554610-ef5c4a2f-0080-45eb-8fed-55fdf5d827f9.png#clientId=ua84a92a5-30aa-4&from=paste&height=459&id=u7227f759&originHeight=918&originWidth=3714&originalType=binary&ratio=2&rotation=0&showTitle=false&size=309179&status=done&style=none&taskId=u12307968-2a79-4f77-9c78-e976399c60e&title=&width=1857)

这里要重点看下我们是如何做这些纵向和横向切分的，切分是为了隔离，隔离是为了能够独立迭代、剥离不必要的依赖，然而如果只是隔离是没有共享相当于只是换了个部署的位置而已，很难有好的效果。所以我们除了隔离还有共享能力，所以这里需要聚焦在隔离与共享上来理解模块化架构背后的原理。

### 模块的定义
在这之前先看下这里的模块是什么？模块是通过原来应用减去基座部分得到的，这里的减法是通过设置模块里依赖的 scope 为 provided 实现的，<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695132446404-0571be28-5cdf-452e-90f5-001a4209c750.png#clientId=u177778f7-e9cd-4&from=paste&height=142&id=ud796498d&originHeight=516&originWidth=1834&originalType=binary&ratio=2&rotation=0&showTitle=false&size=108247&status=done&style=none&taskId=u8201db6e-cf5e-4fbd-ab24-6a0223e1709&title=&width=506)<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695132481921-6fb1c3da-0de3-46ce-bf8e-cc645f63157c.png#clientId=u177778f7-e9cd-4&from=paste&height=187&id=u31cba15e&originHeight=524&originWidth=1026&originalType=binary&ratio=2&rotation=0&showTitle=false&size=205261&status=done&style=none&taskId=u2c981d7a-dfff-43c6-b6c6-5c6a5701d2b&title=&width=367)<br />一个模块可以由这三点定义：

1. SpringBoot 打包生成的 jar 包
2. 一个模块： 一个 SpringContext + 一个 ClassLoader
3. 热部署（升级的时候不需要启动进程）
   
### 模块的隔离与共享
模块通过 ClassLoader 隔离配置和代码，SpringContext 隔离 Bean 和服务，可以通过调用 Spring ApplicationContext 的start close 方法来动态启动和关闭服务。通过 SOFAArk 来共享模块和基座的配置和代码 Class，通过 SpringContext Manager 来共享多模块间的 Bean 和服务。<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695132610081-3efe470f-5c65-4d46-b4e4-1ecb15c8d789.png#clientId=u771aab18-101c-4&from=paste&height=313&id=u4c63a679&originHeight=972&originWidth=1334&originalType=binary&ratio=2&rotation=0&showTitle=false&size=160772&status=done&style=none&taskId=uafe9a1eb-025c-4e1e-9316-35b8bd32b96&title=&width=429)<br />并且在 JVM 内通过

1. Ark Container 提供多 ClassLoader 运行环境
2. Arklet 来管理模块生命周期
3. Framework Adapter 将 SpringBoot 生命周期与模块生命周期关联起来
4. SOFAArk 默认委托加载机制，打通模块与基座类委托加载
5. SpringContext Manager 提供 Bean 与服务发现调用机制
6. 基座本质也是模块，拥有独立的 SpringContext 和 ClassLoader

![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695139080634-1669ea76-c486-47fc-ac4f-5900833896b9.png#clientId=u71a0730f-fb54-4&from=paste&height=275&id=u1cf30803&originHeight=722&originWidth=1428&originalType=binary&ratio=2&rotation=0&showTitle=false&size=198221&status=done&style=none&taskId=u88cd7c27-4850-4b02-9c6f-504b4456a94&title=&width=544)

但是在 Java 领域模块化技术已经发展了20年了，为什么这里的模块化技术能够在蚂蚁内部规模化落地，这里的核心原因是<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695139240123-37a5b5e7-38ee-4b33-b84b-4d58e8b9f371.png#clientId=u71a0730f-fb54-4&from=paste&height=596&id=u7b5e0183&originHeight=1192&originWidth=2954&originalType=binary&ratio=2&rotation=0&showTitle=false&size=587199&status=done&style=none&taskId=uc2ceea08-092e-4bfd-9566-d97ab3d3b74&title=&width=1477)<br />基于 SOFAArk 和 SpringContext Manager 的多模块能力，提供了低成本的使用方式。

#### 隔离方面
对于其他的模块化技术，从隔离角度来看，JPMS 和 Spring Modulith 的隔离是通过自定义的规则来做限制的，Spring Modulith 还需要在单元测试里执行 verify 来做校验，隔离能力比较弱且一定程度上是比较 tricky 的，对于存量应用使用来说也是有不小改造成本的，甚至说是存量应用无法改造。而 SOFAArk 和 OSGI 一样采用 ClassLoader 和 SpringContext 的方式进行配置与代码、bean与服务的隔离，对原生应用的启动模式完全保持一致。

#### 共享方面
SOFAArk 的隔离方式和 OSGI 是一致的，但是在共享方面 OSGI 和 JPMS、Spring Modulith 一样都需要在源模块和目标模块间定义导入导出列表或其他配置，这造成业务使用模块需要强感知和理解多模块的技术，使用成本是比较高的，而 SOFAArk 则定义了默认的类委托加载机制，和跨模块的 Bean 和服务发现机制，让业务不用改造的情况下能够使用多模块的能力。<br />这里额外提下，为什么基于 SOFAArk 的多模块化技术能提供这些默认的能力，而做到低成本的使用呢？这里主要的原因是因为我们对模块做了角色的区分，区分出了基座与模块，在这个核心原因基础上也对低成本使用这块比较重视，做了重要的设计考量和取舍。具体有哪些设计和取舍，可以查看技术实现文章。

### 模块间通信
模块间通信主要依托 SpringContext Manager 的 Bean 与服务发现调用机制提供基础能力，<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695171905613-2546f555-ff25-4a58-81aa-02d77bfb2b1d.png#clientId=ud7a2066a-ba29-4&from=paste&height=307&id=uc8826222&originHeight=724&originWidth=1048&originalType=binary&ratio=2&rotation=0&showTitle=false&size=202275&status=done&style=none&taskId=u537670c5-c728-487a-9710-80986ce8532&title=&width=444)

### 模块的可演进
回顾背景里提到的几大问题，可以看到通过模块化架构的隔离与共享能力，可以解决掉基础设施复杂、多人协作阻塞、资源与长期维护成本高的问题，但还有微服务拆分与业务敏捷度不一致的问题未解决。<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695175219841-965cd163-a4bd-4cd0-b828-c620b29c0ffc.png#clientId=uaaa65411-0843-4&from=paste&height=185&id=ua68375b7&originHeight=894&originWidth=2906&originalType=binary&ratio=2&rotation=0&showTitle=false&size=417377&status=done&style=none&taskId=ud94c9602-7cd1-4bcb-8654-39fe8938d37&title=&width=602)<br />在这里我们通过降低微服务拆分的成本来解决，那么怎么降低微服务拆分成本呢？这里主要是在单体架构和微服务架构之间增加模块化架构

1. 模块不占资源所以拆分没有资源成本
2. 模块不包含业务公共部分和框架、中间件部分，所以模块没有长期的 sdk 升级维护成本
3. 模块自身也是 SpringBoot，我们提供工具辅助单体应用低成本拆分成模块应用
4. 模块具备灵活部署能力，可以合并部署在一个 JVM 内，也可拆除独立部署，这样模块可以按需低成本演进成微服务或回退会单体应用模式

![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695175141130-d3b55e17-70c3-4e7c-aeef-2e071f89ada8.png#clientId=uaaa65411-0843-4&from=paste&height=316&id=u589ef06e&originHeight=632&originWidth=3642&originalType=binary&ratio=2&rotation=0&showTitle=false&size=139102&status=done&style=none&taskId=uf9f96d68-7456-4af5-951e-d9351092988&title=&width=1821)<br />图中的箭头是双向的，如果当前微服务拆分过多，也可以将多个微服务低成本改造成模块合并部署在一个 JVM 内。所以这里的本质是通过在单体架构和微服务架构之间增加一个可以双向过渡的模块化架构，降低改造成本的同时，也让开发者可以根据业务发展按需演进或回退。这样可以把微服务的这几个问题解决掉

### 模块化架构的优势
模块化架构的优势主要集中在这四点：快、省、灵活部署、可演进，<br />![image.png](https://github.com/sofastack/sofa-serverless/assets/3754074/11d1d662-d33b-482b-946b-bf600aeb34da)


与传统应用对比数据如下，可以看到在研发阶段、部署阶段、运行阶段都得到了10倍以上的提升效果。<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695180250909-f5eca1b3-c416-4bac-9732-549a9bed8b87.png#clientId=ueb39d37f-ca7b-4&from=paste&height=261&id=u8907b613&originHeight=522&originWidth=2838&originalType=binary&ratio=2&rotation=0&showTitle=false&size=219589&status=done&style=none&taskId=ua4b2bd1b-a75f-4945-abce-68826a43377&title=&width=1419)

## 平台架构
只有应用架构还不够，需要从研发阶段到运维阶段到运行阶段都提供完整的配套能力，才能让模块化应用架构的优势真正触达到研发人员。<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695182073971-12b14861-b6fa-470c-a140-737d40ff0b3e.png#clientId=u9014394b-3a6a-4&from=paste&height=192&id=ub53430b2&originHeight=384&originWidth=1720&originalType=binary&ratio=2&rotation=0&showTitle=false&size=79335&status=done&style=none&taskId=u1eb2a897-c2ca-437f-8d56-7067be175e2&title=&width=860)<br />在研发阶段，需要提供基座接入能力，模块创建能力，更重要的是模块的本地快速构建与联调能力；在运维阶段，提供快速的模块发布能力，在模块发布基础上提供 A/B 测试和秒级扩缩容能力；在运行阶段，提供模块的可靠性能力，模块可观测、流量精细化控制、调度和伸缩能力。

![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695182125970-f9529014-0386-4922-b8eb-5d0c82a7e5d8.png#clientId=u9014394b-3a6a-4&from=paste&height=370&id=uf365ffd8&originHeight=740&originWidth=2096&originalType=binary&ratio=2&rotation=0&showTitle=false&size=242246&status=done&style=none&taskId=uf07de18d-931e-4ffd-9540-d4be10de3e7&title=&width=1048)<br />组件视图

在整个平台里，需要四个组件：

1. 研发工具 Arkctl, 提供模块创建、快速联调测试等能力
2. 运行组件 SOFAArk, Arklet，提供模块运维、模块生命周期管理，多模块运行环境
3. 控制面组件 ModuleController
    1. ModuleDeployment 提供模块发布与运维能力
    2. ModuleScheduler 提供模块调度能力
    3. ModuleScaler 提供模块伸缩能力

<br/>
