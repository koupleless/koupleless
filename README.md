[![Coverage Status](https://codecov.io/gh/koupleless/koupleless/branch/main/graph/badge.svg)](https://codecov.io/gh/koupleless/koupleless/branch/main/graph/badge.svg)
![license](https://img.shields.io/badge/license-Apache--2.0-green.svg)
![Maven Central](https://img.shields.io/maven-central/v/com.alipay.sofa.koupleless/koupleless-runtime)

你想让你的应用 10 秒启动，只占 20MB 内存吗？你是否遇到大应用多人协作互相阻塞、发布效率不高的问题？你是否遇到小应用太多，资源成本和长期维护成本太高的问题呢？如果你也是被这些问题困扰着的，那么 Koupleless 会是你想要的解决方案。Koupleless 从应用架构角度出发，采用模块化架构，以 **极低接入成本** 的方式，解决应用在研发、运维、运行等完整生命周期遇到的痛点问题：

1. 应用拆分过度，机器成本和长期维护成本高
2. 应用拆分不够，多人协作互相阻塞
3. 应用构建、启动与部署耗时久，应用迭代效率不高
4. SDK 版本碎片化严重，升级成本高周期长
5. 平台、中台搭建成本高，业务资产沉淀与架构约束困难
6. 微服务链路过长，调用性能不高
7. 微服务拆分、演进成本高

为什么 Koupleless 能解决呢？原因是 Koupleless 对传统应用同时进行了纵向和横向的拆分，纵向拆分出基座，横向拆分出多个模块，基座为模块屏蔽基础设施，模块只包含业务自身部分启动快且不感知基础设施专注于业务本身，模块开发者实际上具备了 Serverless 的体验。所以 Koupleless 是从细化研发运维粒度和屏蔽基础设施的两个方面，演进出的一套低成本接入的 Serverless 解决方案。
详细原理介绍[可以查看官网介绍](https://koupleless.gitee.io/docs/introduction/architecture/arch-principle/)。

![image](https://github.com/koupleless/koupleless/assets/3754074/004c0fa5-62f6-42d7-a77e-f7152ac89248)

最重要的是, Koupleless 能以 **极低成本** 帮助 **存量应用** 演进为模块化研发模式，解决上述问题，帮助企业降本增效提升竞争力。

## Koupleless 优势

Koupleless 是蚂蚁集团内部经过 5 年打磨成熟的研发框架和运维调度平台能力，相较于传统镜像化的应用模式研发、运维、运行阶段都有 10 倍左右的提升，总结起来 5 大特点：快、省、灵活部署、平滑演进、生产规模化验证。

<img width="788" alt="image" src="https://github.com/sofastack/sofa-serverless/assets/3754074/11d1d662-d33b-482b-946b-bf600aeb34da">

举个实际生产应用模块化研发部署与传统镜像化对比的性能数据

![image](https://github.com/koupleless/koupleless/assets/3754074/cf8877c6-80f1-4138-8314-0fd4deec6b40)

## 模块是什么？
这里的模块采用了极致的共享和隔离技术，隔离之后就可以做到热部署（不重启机器的方式更新线上代码）。
隔离 = 基于 [SOFAArk](https://github.com/sofastack/sofa-ark) 的 ClassLoader 类隔离, 基于 [SpringBoot](https://github.com/spring-projects/spring-boot) 的 SpringContext 对象隔离。
共享 = 基于 [SOFAArk](https://github.com/sofastack/sofa-ark) 的类委托加载，基于 SpringBootManager 的跨 SpringContext 的对象查找调用。

所以从物理上，可以认为模块 = 一个 ClassLoader + 一个 SpringContext。

## 基座是什么？
基座就是普通应用，与原有体系（比如标准 SpringBoot）没有任何区别。

## 快速开始
请查看[官网快速开始](https://koupleless.gitee.io/docs/quick-start/)

## Koupleless 组件

![image](https://github.com/sofastack/sofa-serverless/assets/101314559/995f1e17-f3be-4672-b1b8-c0c041590fb0)

## 如何参与社区
欢迎大家一起建设、搜索或者扫码加入开发者协作群。

|软件|群号|二维码|
|-|-|-|
| 钉钉群（推荐）| 24970018417 | <img width="256" alt="image" src="https://github.com/koupleless/koupleless/assets/3754074/7ba1db74-20c1-43a4-a2ab-d38c99a920cd"> |
| QQ 群 | 813757901 | <img width="256" alt="image" src="https://github.com/koupleless/koupleless/assets/3754074/4445e3b3-dbc9-4761-a578-90c578e21954"> |

## 长期规划与愿景
希望将这些能力做得更加极致、更加开放，适用更多的场景。帮助更多的企业解决应用研发问题，实现降本增效，最终成为全球绿色计算优秀的研发框架和解决方案，做到：

1. Speed as you need
2. Pay as you need
3. Deploy as you need
4. Evolution as you need

<img width="1069" alt="image" src="https://github.com/koupleless/koupleless/assets/3754074/17ebd41d-38c7-46e8-a4ba-b6b8bf8f76dd">
