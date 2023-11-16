[![Coverage Status](https://codecov.io/gh/sofastack/sofa-serverless/branch/master/graph/badge.svg)](https://codecov.io/gh/sofastack/sofa-serverless/branch/master/graph/badge.svg)
![license](https://img.shields.io/badge/license-Apache--2.0-green.svg)
![Maven Central](https://img.shields.io/maven-central/v/com.alipay.sofa.serverless/sofa-serverless-runtime)

完整产品介绍欢迎查阅 [SOFAServerless 官网](https://sofaserverless.gitee.io/home/)

<br/>

欢迎来到 SOFAServerless 开源社区。SOFAServerless 是一种模块化的应用架构模式，能够帮助大中小企业低成本地实现极速研发、运维、微服务演进和人员协作，从而为企业实现降本增效。欢迎加入钉钉群关注我们或者与我们一起共建面向普通应用低成本接入的 Serverless 解决方案。钉钉群号：24970018417 。


![A4单页_画板 1-01](https://github.com/sofastack/sofa-serverless/assets/101314559/152a0669-2d90-405c-8d5c-fd8c8d901ca0)

![A4单页_画板 1-02](https://github.com/sofastack/sofa-serverless/assets/101314559/4e28eecd-0e73-4b7c-a1e4-61337b188f47)

# SOFAServerless
SOFAServerless 技术体系是蚂蚁集团随着业务发展、研发运维的复杂性和成本不断增加的情况下，为帮助应用又快又稳地迭代而研发。从细化研发运维粒度和屏蔽基础设施的角度出发，演进出的一套低成本接入的 Serverless 解决方案。

核心方式是通过类隔离和热部署技术，将应用从代码结构和开发者阵型拆分为两个层次：业务模块和基座，基座为业务模块提供计算环境并屏蔽基础设施，模块开发者不感知机器、容量、中间件等基础设施，专注于业务研发帮助业务快速向前发展。

## 背景
在应用架构领域，不可避免的问题是应用随着业务的复杂度不断增加，研发运维的过程中的问题会不断暴露出来。首先我们看一下普通应用研发和运维过程中的流程是什么样的：

![image](https://user-images.githubusercontent.com/101314559/172528801-065a18ee-3ce7-46b9-9b78-3f66f9955c97.png)

如图所示，从需求到设计、开发、线下测试，再到发布线上的研发运维不断反馈、循环迭代的过程。可以简化为开发同学提交代码到代码仓库，在线下做并行的验证测试，测试通过之后在线上发布，发布过程是串行的，只能够有一个发布窗口，这样的过程在应用体量业务还不太复杂的情况下问题，并不是很明显。

但当业务复杂度不断增加，普通应用迭代过程在会出现一些新的问题，如下图：

![image](https://user-images.githubusercontent.com/101314559/172525833-311229f1-c631-4170-a16d-1e6d7550b6bc.png)

1. 管理成本高：需求管理、代码管理、人员管理。
2. 时间成本高：线上验证与发布互相阻塞。单次启动慢。
3. 变更风险高：一次变更涉及所有代码。一次变更涉及所有机器。

另外，由于这些问题是因为多个业务与研发任务耦合在某些单点上导致的，研发运维的成本随着业务的复杂度呈现出指数增长的特点：

![image](https://user-images.githubusercontent.com/101314559/172529176-882bd36b-05a6-4450-aa53-24ef64a7e326.png)

## SOFAServerless 研发运维体系
对于这些问题，业界已经发展并演进出了多种应用架构，从单体架构 -> 垂直架构 -> SOA 架构 -> 分布式微服务架构 -> 服务网格架构等，我们分析这些演进过程为解决遇到的研发运维问题提出 SOFAServerless 研发运维体系。

通过借助 SOFAArk 框架将应用拆分成基座和模块，同时将应用里的接口按场景维度做分组，使得业务可以按一组接口的粒度进行极速发布运维以及资源按需隔离。

![image](https://user-images.githubusercontent.com/101314559/172529808-e09349c2-ff07-4431-8f5b-a1786cd0cfe5.png)

从这张图里可以看到 Serverless 应用拆分的形态，通过把一个普通的 Java 应用拆出多个模块，进一步对应用进行了拆分：基座和模块，对应的研发人员也划分为基座开发者和模块开发者。

基座负责沉淀通用的逻辑，为模块提供计算和环境，并为模块开发者屏蔽基础设施，让模块开发者不需要关心容量和资源等。各个模块则是独立的代码仓库，可以进行独立的研发运维，这样研发运维粒度就得到了精细化，并且由于基座为模块屏蔽了环境与基础设施，模块开发者可以专注于业务开发从而提高了业务创新效率。SOFAServerless 开源版首个版本计划在 10 月左右透出，届时欢迎大家一起来建设社区 Serverless 技术。

## SOFAServerless 优势
### 极速发布运维
相对于普通应用的镜像构建需要 3 分钟，发布需要镜像下载、启动、挂载流量大概 3 分钟，总共平均需要 6 分钟；模块构建只需要 10 秒，启动大概 1~10 秒 （模块大小可大可小，对于较小的模块，速度可以做到毫秒级别） 。
把一次发布耗时从原来的 6 分钟下降到 15 秒，一次迭代从原来 2 周下降到了 2 天，最快可以 5 分钟上线的。

### 快速弹性调度
![image](https://github.com/sofastack/sofa-serverless/assets/13743483/f61807e7-f4b5-4078-b9ba-978461f41701)

对于线上集群的部署形态，不同的机器上部署的模块不尽相同。例如对于模块 1，只安装在了第一第二台机器上，那么模块升级时只会涉及到这两台机器本身，变更的机器范围就比较小了。另外，模块 1 如果要扩容的话，可以从集群内筛选出较空闲的机器进行模块热部署即可，一般也就是 10s 级别，所以能做大快速的水平扩展能力。

### 低变更风险
对于一次模块的升级变更，只会涉及模块自身的代码本身不会设计整个应用代码。模块变更需要更新的机器也只是模块安装过的机器本身，不会涉及到整个集群，所以变更范围大大缩小，变更风险也相较普通应用能得到明显减少。

### 极致省资源
因为模块应用是合并部署在基座上，所以可以完全复用基座的 CPU、Mem 等物理资源，相比每个应用空跑 SpringBoot 都需要花费近百兆 JVM 内存，采用 SOFAServerless 多个模块应用只要复用一份 SpringBoot JVM 即可，极大节省了资源成本。

### 并行独立迭代
多个模块的开发和发布运维，可以并行进行，互相不影响，也有效解决了迭代冲突、环境抢占等企业应用的研发痛点。

<br/>

## SOFAServerless 开源组件

|  系统/组件   | 职责描述  |
|  ----  | ----  |
| <b>sofa-ark</b>  | 原来的 sofa-ark 2.0 Java 合并部署与热部署框架。 |
| <b>arkctl</b>  | 研发运维本地工具集，帮助开发者轻松拆分合并模块、开发测试、部署调试、瘦身模块等等。 |
| <b>arklet</b>  | 一个客户端框架，对接 SOFAArk 实现 Biz 模块的热部署和热卸载，并且暴露 HTTP API 接口可以让上游系统或者开发者直接使用。 |
| <b>nodejslet</b>  | 一个客户端框架，对接 NodeJS 原生 API 实现 NodeJS 模块的热替换，并且暴露 HTTP API 接口可以让上游系统或者开发者直接使用。 |
| <b>module-controller</b> | 模块运维、调度、弹性伸缩系统，清晰的定义 ModuleDeployment 相关模型和 API，并且既支持 K8S CR + ETCD 调和的实现方式又支持标准 HTTP/RPC + DB 的实现方式。底层对接编排模块热替换的客户端（arklet、nodejslet 等）。最终实现模块秒级发布运维能力，让开发者享受 Serverless 发布运维体验。 |

<br/>

## SOFAServerless 开源版大体里程碑

SOFAServerless 开源版非常希望并且欢迎与社区同学一起共建，凡是 23 年下半年参与社区共建的同学我们都会颁发奖品。2023 年下半年初步的里程碑计划如下：

- <b>2023.8：</b>整体 0.1 版本发布
- <b>2023.8：</b>完成 SOFABoot 完整的部署功能验证，产出 benchmark 基线
- <b>2023.9：</b>发布基础运维和调度系统 ModuleController 0.5 版
- <b>2023.9：</b>发布研发运维工具 Arkctl 与 Arklet 0.5 版
- <b>2023.10：</b>发布模块半自动拆分工具 0.5 版
- <b>2023.10：</b>编写基础使用教程，新增 2+ SOFABoot 技术栈公司使用
- <b>2023.11：</b>支持 SpringBoot 完整能力和 5+ 社区常用中间件
- <b>2023.11：</b>SOFAServerless (ModuleController、Arkctl、Arklet、SpringBoot 兼容) 1.0 版本正式发布
- <b>2023.12：</b>基础自动弹性伸缩能力发布

更具体的里程碑细节随开源项目推进及时更新。
