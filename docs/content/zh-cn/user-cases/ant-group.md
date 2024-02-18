---
title: 蚂蚁集团大规模 Serverless 降本增效实践
date: 2024-01-25T10:28:32+08:00
description: Koupleless 蚂蚁集团大规模 Serverless 降本增效实践
weight: 1000
type: docs
---
> 作者：刘煜、赵真灵、刘晶、代巍、孙仁恩等


# 蚂蚁集团业务痛点

蚂蚁集团过去 20 年经历了微服务架构飞速演进，与此同时应用数量和复杂度爆发式增长，带来了严重的企业成本和效率问题：

1. 大量长尾应用 CPU 使用率**不足 10%**，却由于多地域高可用消耗大量机器。
2. 应用一次构建+发布速度慢，平均 **10 分钟**，研发效率低下且无法快速**弹性**。
3. 多人开发的应用，功能必须攒一起 “**赶火车**”，迭代互相**阻塞**，协作和交付效率低下。
4. 业务 SDK 和部分框架升级对业务有较**高打扰**，无法做到基础设施对业务微感甚至无感。
5. 业务资产难以沉淀，大中台建设**成本高昂**。


# 蚂蚁集团 Koupleless 使用场景

## 合并部署降成本

在企业中 “**80%**” 的长尾应用仅服务 “**20%**” 的流量，蚂蚁集团也不例外。<br />在蚂蚁集团存在大量长尾应用，每个长尾应用至少需要 预发布、灰度、生产 3 个环境，每个环境最少需要部署 3 个机房，每个机房又必须保持 2 台机器高可用，因此大量长尾应用 CPU 使用率**不足 10%**。<br />通过使用 Koupleless，蚂蚁集团对长尾应用进行了服务器裁撤，借助类委托隔离、资源监控、日志监控等技术，在保证稳定性的前提下，实现了多应用的合并部署，极大降低了业务的运维成本和资源成本。<br /><img alt="合并部署。裁剪机器。" src="https://intranetproxy.alipay.com/skylark/lark/0/2023/png/671/1697010009124-285a0679-2462-434d-8d75-2aa5a7ede7be.png#clientId=u2fc31cce-a2b2-4&from=paste&height=182&id=ub16bde42&originHeight=364&originWidth=1438&originalType=binary&ratio=2&rotation=0&showTitle=false&size=163395&status=done&style=none&taskId=u4de74533-5e86-41e0-bb44-9bc8203b9c9&title=&width=719" width="700"><br />此外，采用这种模式，小应用可以不走应用申请上线流程也不需要申请机器，可以直接部署到业务通用基座之上，从而帮助小流量业务实现了快速创新。


## 模块化研发极致提效

在蚂蚁集团，很多部门存在开发者人数较多的应用，由于人数多，导致环境抢占、联调抢占、测试资源抢占情况严重，互相阻塞，一人 Delay 多人 Delay，导致需求交付效率低下。<br />通过使用 Koupleless，蚂蚁集团将协作人数较多的应用，一步步重构为基座代码和不同功能的模块代码。基座代码沉淀了各种 SDK 和业务的公共接口，由专人维护，而模块代码则内聚了某一个功能领域特有的业务逻辑，可以调用本地基座接口。模块采用热部署实现了**十秒级**构建、发布、伸缩，同时模块开发者**完全不用关心**服务器和基础设施，这样普通应用便以**很低的接入成本**实现了 **Serverless** 的研发体验。<br />以蚂蚁集团资金业务为例，资金通过将应用拆分为一个基座与多个模块，实现了发布运维、组织协作、集群流量隔离多个维度的极致提效。<br /><img alt="模块化研发提速。模块化研发提效。" src="https://intranetproxy.alipay.com/skylark/lark/0/2023/png/671/1697011295180-dfc3def2-968b-4456-95f5-447cfe6b8282.png#clientId=u2fc31cce-a2b2-4&from=paste&height=814&id=u32abf9c9&originHeight=1628&originWidth=2924&originalType=binary&ratio=2&rotation=0&showTitle=false&size=1774843&status=done&style=none&taskId=u08c84de5-b5eb-4c19-b724-9826d13e397&title=&width=1462" width="1200">

蚂蚁集团资金业务 Koupleless 架构演进和实践，详见：[https://mp.weixin.qq.com/s/uN0SyzkW_elYIwi03gis-Q](https://mp.weixin.qq.com/s/uN0SyzkW_elYIwi03gis-Q)


## 通用基座屏蔽基础设施

在蚂蚁集团，各种 SDK 的升级打扰、构建发布慢是痛点问题。借助 Koupleless 通用基座模式，蚂蚁集团帮助部分应用实现了基础设施微感升级，同时应用的构建与发布速度也从 **600 秒**减少到了 **90 秒**。<br /><br/><img alt="屏蔽基础设施" src="https://intranetproxy.alipay.com/skylark/lark/0/2023/png/671/1697016458930-17177051-a51f-4a88-956f-6cabfd4a7b97.png#clientId=u2fc31cce-a2b2-4&from=paste&height=265&id=u9661d43a&originHeight=530&originWidth=2370&originalType=binary&ratio=2&rotation=0&showTitle=false&size=450843&status=done&style=none&taskId=uf4bf486d-f806-4164-b786-9cd2e0ff7d3&title=&width=1185" width="800">

在 Koupleless 通用基座模式里，基座会提前启动好，这些预启动的基座包含了各种通用中间件、二方包和三方包的 SDK。借助 Koupleless 构建插件，业务应用会被构建成 FatJar 包，在发布业务应用新版本时，调度器会选择一台没有安装模块的空基座将模块应用 FatJar 热部署到基座，装有模块的老基座服务器会异步的替换成新服务器（空基座）。<br />基座由专职团队负责维护和升级，对模块应用开发者来说，便享受到了基础设施无感升级和极速构建发布体验。


## 低成本实现高效中台

在蚂蚁集团，有不少中台类业务，典型如各个业务线的玩法、营销、公益、搜索推荐、广告投放等。通过使用 Koupleless，这些中台业务逐渐演进成了基座 + 模块的交付方式，其中基座代码沉淀了通用逻辑，也定义了一些 SPI，而模块负责实现这些 SPI，流量会从基座代码进入，调用模块的 SPI 实现。<br />在中台场景下，模块一般都很轻，甚至只是一个代码片段，大部分模块都能在 **5 秒内**发布、扩容完成，而且模块开发者完全不关心基础设施，享受到了极致的 Serverless 研发体验。<br />以蚂蚁集团**搜索推荐**业务中台为例，搜索推荐业务将公共依赖、通用逻辑、流程引擎全部下沉到基座，并且定义了一些 SPI，搜索推荐算法由各个模块开发者实现，当前搜索推荐已经接入了 **1000+** 模块，平均代码发布上线不到 **1 天**，真正实现了代码的 “**朝写夕发**”。<br /><br/><img alt="代码 1 天上线" src="https://intranetproxy.alipay.com/skylark/lark/0/2023/png/671/1697024085963-a8b74e7b-37d5-469f-97da-7ef7b3e6889f.png#clientId=u2fc31cce-a2b2-4&from=paste&height=684&id=u44c95749&originHeight=1368&originWidth=1412&originalType=binary&ratio=2&rotation=0&showTitle=false&size=728809&status=done&style=none&taskId=u34dbef7c-95c4-4e42-9613-0a25f3362a3&title=&width=706" width="700">


# 总结与规划
Koupleless 在蚂蚁集团历经 5 年多的演进与打磨，目前已在所有业务线完成落地，支撑了全集团 **1/4** 的在线流量，帮助全集团实现了功能平均上线从 12 天**减少到 6 天**、长尾应用**服务器砍半**、**秒级发布运维**的极致降本增效结果。<br />未来，蚂蚁集团会更大规模推广 Koupleless 研发模式，并持续建设弹性能力，做到更极致的弹性体验和绿色低碳。同时，我们也会重点投入开源能力建设，希望和社区同学共同打造极致的模块化技术体系，为各行各业的软件创造技术价值，助力企业实现降本提效。
