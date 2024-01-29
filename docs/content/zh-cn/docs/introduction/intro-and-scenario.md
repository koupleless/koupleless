---
title: 简介与适用场景
date: 2024-01-25T10:28:32+08:00
description: Koupleless 简介与适用场景
weight: 100
---

# 简介
Koupleless 是一种模块化的 Serverless 技术解决方案，它能让普通应用以比较低的代价演进为 Serverless 研发模式，让代码与资源解耦，轻松独立维护，与此同时支持秒级构建部署、合并部署、动态伸缩等能力为用户提供极致的研发运维体验，最终帮助企业实现降本增效。<br />随着各行各业的信息化数字化转型，企业面临越来越多的研发效率、协作效率、资源成本和服务治理痛点，接下来带领大家逐一体验这些痛点，以及它们在 Koupleless 中是如何被解决的。

# 适用场景

## 痛点 1：应用构建发布慢或者 SDK 升级繁琐
传统应用镜像化构建一般要 3 - 5 分钟，单机代码发布到启动完成也要 3 - 5 分钟，开发者每次验证代码修改或上线代码修改，都需要经历**数次 6 - 10 分钟**的构建发布等待，严重影响开发效率。此外，每次 SDK 升级（比如中间件框架、rpc、logging、json 等），都需要修改所有应用代码并重新构建发布，对开发者也造成了不必要的打扰。<br />通过使用 **Koupleless** **通用基座**与配套工具，您可以低成本的将应用切分为 “**基座**” 与 “**模块**”，其中基座沉淀了公司或者某个业务部门的公共 SDK，基座升级可以由专人负责，对业务开发者无感，业务开发者只需要编写模块。在我们目前支持的 Java 技术栈中，模块就是一个 SpringBoot 应用代码包（FatJar），只不过 SpringBoot 框架本身和其他的企业公共依赖在运行时会让基座提前加载预热，模块每次发布都会找一台预热 SpringBoot 的基座进行热部署，整个过程类似 AppEngine，能够帮用户实现应用 **10 秒级构建发布**和**公共 SDK 升级无感**。

<img width="800px" alt="应用构建发布速度" src="https://intranetproxy.alipay.com/skylark/lark/0/2023/png/671/1694592240984-8ea49823-ebd0-4bb7-909c-380f0439382b.png#clientId=u0d56718b-4144-4&from=paste&height=164&id=uab4fd245&originHeight=328&originWidth=2350&originalType=binary&ratio=2&rotation=0&showTitle=false&size=259703&status=done&style=none&taskId=u4aa5d723-f988-41e6-86fc-8c08d59e517&title=&width=1175" />

## 痛点 2：长尾应用资源成本高
在企业中，80% 的应用只服务了不到 20% 的流量，同时伴随着业务的变化，企业存在大量的**长尾应用**，这些长尾应用 CPU 使用率长期不到 10%，造成了极大的**资源浪费**。<br />通过使用 **Koupleless** **合并部署**与配套工具，您可以低成本的实现多个应用的合并部署，从而解决企业应用过度拆分和低流量业务带来的**资源浪费**，**节约成本**。<br />
<img width="700px" alt="应用机器成本" src="https://intranetproxy.alipay.com/skylark/lark/0/2023/png/671/1694593117757-d2932c29-c4c2-4ecc-9a41-59a750d53823.png#clientId=u0d56718b-4144-4&from=paste&height=132&id=u349c574f&originHeight=318&originWidth=1382&originalType=binary&ratio=2&rotation=0&showTitle=false&size=158864&status=done&style=none&taskId=u1389af9d-06db-468f-810a-09bc615b751&title=&width=574" /><br />
这里 “业务A 应用1” 在 Koupleless 术语中叫 “模块”。多个模块应用可以使用 SOFAArk 技术合并到同一个基座。基座可以是完全空的 SpringBoot应用（Java 技术栈），也可以下沉一些公共 SDK 到基座，模块应用每次发布会重启基座机器。在这种方式下，模块应用最大程度复用了基座的**内存**（Metaspace 和 Heap），构建产物**大小**也能从**数百 MB** 瘦身到**几十 MB** 甚至更激进，**CPU 使用率**也得到了有效提升。

## 痛点 3：企业研发协作效率低
在企业中，一些应用需要**多人开发**协作。在传统研发模式下，每一个人的代码变更都需要发布整个应用，这就导致应用需要以**赶火车**式的方式进行研发迭代，大家需要统一一个时间窗口做迭代开发，统一的时间点做发布上线，因此存在大量的需求上线相互**等待**、环境机器**抢占**、迭代**冲突**等情况。<br />通过使用 **Koupleless**，您可以方便的将应用拆分为一个**基座**与多个功能**模块**，一个功能模块就是一组代码文件。不同的功能模块可以**同时进行**迭代开发和发布运维，模块间**互不感知互不影响**，这样就消除了传统应用迭代赶火车式的相互等待，每个模块拥有自己的独立迭代，**需求交付效率**因此得到了极大提升。如果您对模块额外启用了**热部署**方式（也可以每次发布模块重启整个基座），那么模块的单次构建+发布也会从普通应用的 **6 - 10 分钟减少到十秒级。**<br />
<img width="800px" alt="协作效率低" src="https://intranetproxy.alipay.com/skylark/lark/0/2023/png/671/1694594675815-3037ffe1-2048-4c86-bc50-456697b197d5.png#clientId=u0d56718b-4144-4&from=paste&height=552&id=u36ac4b83&originHeight=1066&originWidth=1154&originalType=binary&ratio=2&rotation=0&showTitle=false&size=428189&status=done&style=none&taskId=u7fc53ae9-ff48-4ae5-a821-44dbee64aaa&title=&width=598" />

## 痛点 4：难以沉淀业务资产提高中台效率
在一些中大型企业中，会沉淀各种**业务中台**应用。中台一般封装了业务的公共 API 实现，和 **SPI** 定义。其中 SPI 定义允许中台上的插件去实现各自的业务逻辑，流量进入中台应用后，会调用对应的 SPI 实现组件去完成相应的业务逻辑。中台应用内的组件，业务逻辑一般不复杂，如果拆出去部署为独立应用会带来高昂的**资源成本和运维成本**，而且构建发布**速度很慢**，严重加剧研发负担影响研发效率。<br />通过使用 Koupleless，您可以方便的将中台应用拆分一个**基座**和多个功能**模块**。基座可以沉淀比较厚的业务依赖、公共逻辑、API 实现、SPI 定义等（即所谓的业务资产），提供给上面的模块使用。模块使用基座的能力可以是对象之间或 Bean 之间的**直接调用**，代码几乎不用改造。而且多个模块间可以**同时进行**迭代开发和发布运维，**互不感知互不影响**，**协作交付效率**得到了极大提升。此外对于比较简单的模块还可以开启热部署，单次构建+发布也会从普通应用的 **6 - 10 分钟减少到 30 秒内。**<br />
<img width="800px" alt="提高中台效率" src="https://intranetproxy.alipay.com/skylark/lark/0/2023/png/671/1694601773808-b25f5beb-a4e4-4d93-ba55-6f61bf0377bc.png#clientId=u2162a7aa-3111-4&from=paste&height=386&id=uf98e4ae9&originHeight=1016&originWidth=1400&originalType=binary&ratio=2&rotation=0&showTitle=false&size=470581&status=done&style=none&taskId=u44970d8c-1234-447f-bbb6-ceea4a44cfc&title=&width=532" />

## 痛点 5：微服务演进成本高
企业里不同业务有不同的发展阶段，因此应用也拥有自己的生命周期。

**初创期**：一个初创的应用一般会先采用**单体架构**。<br />↓<br />**增长期**：随着业务增长，应用开发者也随之增加。此时您可能**不确定**业务的未来前景，也不希望过早把业务拆分成多个应用以避免不必要的**维护、治理和资源成本**，那么您可以用 **Koupleless** 低成本地将应用**拆分**为一个基座和多个功能**模块**，不同功能模块之间可以并行研发运维独立迭代，从而提高应用在此阶段的研发协作和**需求交付**效率。<br />↓<br />**成熟期**：随着业务进一步增长，您可以使用 Koupleless 低成本地将部分或全部功能模块**拆分成独立应用**去研发运维。<br />↓<br />**长尾期**：部分业务在经历增长期或者成熟期后，也可能慢慢步入到低活状态或者长尾状态，此时您可以用 Koupleless 低成本地将这些应用**一键改回模块**，**合并部署**到一起实现**降本增效**。

可以看到 **Koupleless** 支持企业应用低成本地在初创期、增长期、成熟期、长尾期之间平滑过渡甚至来回切换，从而轻松让应用架构与业务发展保持同步。<br />应用生命周期演进<br />
<img width="1200px" alt="微服务演进成本" src="https://intranetproxy.alipay.com/skylark/lark/0/2023/png/671/1694602307402-510d44ec-314c-44c4-96d8-bb978dd027ff.png#clientId=u2162a7aa-3111-4&from=paste&height=217&id=u8b5b547c&originHeight=434&originWidth=2458&originalType=binary&ratio=2&rotation=0&showTitle=false&size=266126&status=done&style=none&taskId=u09776530-0a41-4081-be17-c42db50e8b1&title=&width=1229" />

<br/>
<br/>
