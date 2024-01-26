---
title: 阿里国际数字商业集团中台业务三倍提效
date: 2024-01-25T10:28:32+08:00
description: Koupelelss 阿里国际数字商业集团中台业务三倍提效
weight: 1100
type: docs
---
> 作者: 朱林(封渊)、张建明(明门)

# 项目背景
过去几年，AIDC（阿里巴巴海外数字商业）业务板块在全球多个国家和地区拓展。国际电商业务模式上分为"跨境"和"本对本"两种，分别基于AE(跨境)、Lazada、Daraz、Mirivia、Trendyol 等多个电商平台承载。以下将不同的电商平台统称为“站点”。

<img alt="阿里巴巴国际数字商业背景" src="https://github.com/sofastack/sofa-serverless/assets/11410549/e7bdded0-a6d6-48ae-a373-49429e1bc8ee" width="800"></img>

对于整个电商业务而言，各个站点核心的买卖家基础链路存在一定的差异性，但更多还是共性。通过抽象出一个通用的平台，在各个站点实现低成本复用有助于更高效的支持上层业务。所以过去几年基础链路一直在尝试通过平台化的建设思路，通过 `1个全球化业务平台 + N个业务站点` 的模式支持业务发展；其中技术的迭代经历了五个阶段的发展，从最初的中心化中台集成业务的架构模式，逐步转变为去中心化被业务集成的架构，已经基本可以满足全球化各个站点业务和平台各自闭环迭代。

<img alt="全球化业务平台。业务站点。" src="https://github.com/sofastack/sofa-serverless/assets/11410549/8c10464c-6c5a-4fce-a59f-192b809b15bf" width="900"></img>

各个站点逻辑上是基于国际化中台(平台)做个性化定制，在交付/运维态各个站点被拆分成独立的应用，分别承载各自业务流量，平台能力通过二方包方式被站点应用集成，同时平台具备能力扩展机制，业务站点的研发能在站点应用中覆写平台逻辑，这最大化的保证了站点业务的研发/运维自主性，同时一定程度上保证了平台能力的复用性。
但由于当前各个电商站点处于不同的发展时期，并且本对本跟跨境在业务模式上也存在差异性，以及业务策略上的不断变化，业务的快速迭代跟平台能力的后置沉淀逐步形成了矛盾，主要表现在如下几个方面：

- **平台重复建设**：由于平台采用开放、被集成的策略且缺乏一定的约束，需求的迭代即便是需要改动平台逻辑，站点也基本自闭环，在平台能力沉淀、稳定性、性能、开放性等各个站点都存在重复建设，支持不同站点的平台版本差异性逐步扩大；
- **站点维护成本高**：各自闭环的站点应用，由于同时维护了定制的平台能力，承担了部分"平台团队的职责"，逐渐的增加了站点研发团队的负担，导致人力成本升高；
- **研发迭代效率低**：核心应用构建部署效率低下，以交易站点应用为例，系统启动时长稳定在 300s+，编译时长 150s+，镜像构建时长 30s+，容器重新初始化等调度层面的耗时 2min 左右，研发环境一天部署次数100+，如果能降低构建部署时长，将有效的降低研发等待时长；

所以，下一代的架构迭代将需要重点解决平台去中心化被业务集成的架构模式下如何实现能力的迭代自主性以及版本统一，另外也需要考虑如何进一步降低站点的研发、运维成本，提升构建、部署效率，让业务研发真正只关注在自身的业务逻辑定制。
Serverless 的技术理念中，强调关注点分离，让业务研发专注在业务逻辑的研发，而不太需要关注底层平台。这样一个理念，结合上述我们面临的问题或许是一个比较好的解法，让平台从二方包升级成为一个平台基座应用，统一收敛平台的迭代，包括应用运行时的升级；让业务站点应用轻量化，只关注定制逻辑的开发，提升部署效率，降低维护成本，整体逻辑架构图如下：

<img alt="阿里巴巴国际研发痛点" src="https://github.com/sofastack/sofa-serverless/assets/11410549/8b6d120f-42b3-495d-9879-6be1a8000ebc" width="1200"></img>


# 概念阐述

Serverless 普遍的理解是“无服务器架构”。它是云原生的主要技术之一，无服务器指的是用户不必关心应用运行和运维的管理，可以让用户去开发和使用应用程序，而不用去管理基础设施，云服务商提供、配置、管理用于运算的底层基础设施，将应用程序从基础结构中抽离出来，让开发人员可以更加专注于业务逻辑，从而提高交付能力，降低工作成本。传统 Serverless 在实现上其实就是 **FaaS** **+** **BaaS**。FaaS 承载代码片段（即函数），可随时随地创建、使用、销毁，无法自带状态。和 BaaS（后端即服务）搭配使用。两者合在一起，才最终实现了完整行为的 Serverless 服务。

<img alt="Serverless 概念" src="https://github.com/sofastack/sofa-serverless/assets/11410549/fe45cf14-0f19-42ed-8a06-6d461c2209f4" width="400"></img>

在传统 Serverless 技术体系下，Java 应用架构更多的是解决了 `IaaS 层 + 容器化` 的问题，Serverless 本身无法将涵盖范围下探到 JVM 内部。因此，对于一个复杂的 Java 巨石应用，可以借助 Serverless 的理念将 Java 技术栈下的业务代码和基础设施(中间件)依赖做更进一步的剥离与拆分。本次实践中的 Serverless 改造可以抽象为如下过程和目标：

<img alt="Java Serverless" src="https://github.com/sofastack/sofa-serverless/assets/11410549/1edb95e4-627c-4244-bdf2-844b2bd265a3" width="650"></img>

将一个单体应用横向拆分成上下两层：

- **基座**：将一些业务应用迭代中**不经常变更**的基础组件以及 Lib 包下沉到一个新的应用中去，这个新应用我们称之为 `基座应用`，有以下特点:
   - 基座是可独立发布与运维的
   - 基座应用开发者可以统一升级中间件和组件版本，在保证兼容性的前提下，上层 App 无需感知整个升级过程
   - 基座具备不同站点间的可复用性，一个交易的基座可以被 AE、Lazada、Daraz等不同的站点 App 共用
- **Serverless App**：为了最大程度减少业务的改造成本，业务团队维护的 App 依旧保持其自身的发布运维职责的独立性不变，Serverless 化后业务开发人员只需要关心业务代码即可。JVM 对外服务的载体依旧是业务应用。


# 技术实施

<img alt="阿里巴巴国际 Serverless 演进" src="https://github.com/sofastack/sofa-serverless/assets/11410549/160e41e5-3227-409d-b81a-05800f24a306" width="550"></img>

Serverless 架构演进的实施过程分为两个部分：

1. 重新设计 Serverless 架构模型下的应用架构分层与职责划分，让业务减负，让 SRE 提效。
2. 在研发、发布&运维等领域采用新的研发框架、交付模型与发布运维产品来支持业务快速迭代。
   

## 应用架构
以 Daraz 基础链路为例，应用架构的分层结构、交互关系、团队职责如下：

<img alt="阿里巴巴国际 Serverless 应用架构" src="https://github.com/sofastack/sofa-serverless/assets/11410549/7af3a82f-31b8-4ed4-ae18-e7d2606a261a" width="650"></img>

我们将一个 Serverless 应用完整交付所需的支撑架构进行逻辑分层与开发职责划分，并且清晰定义出 App 与基座之间交互的协议标准。

## 研发域

<img alt="阿里巴巴国际 Serverless 研发运维平台" src="https://github.com/sofastack/sofa-serverless/assets/11410549/e7957e02-0f14-4f79-9b00-d748ce806722" width="700"></img>

- 构建了 Serverless 运行时框架，驱动"基座-Serverless App"的运行与交互
- 与 Aone 研发平台团队协作，建设一整套 Serverless 模式下基座与 App 的发布&运维产品体系

### voyager-serverless framework

<img alt="voyager-serverless" src="https://github.com/sofastack/sofa-serverless/assets/11410549/92bb0727-abad-49a0-a175-9cba64ffdbbb" width="350"></img>

**voyager-serverless framework** 是一个基于 **[Koupleless](https://github.com/koupleless/koupleless/)** 技术自研的提供 `Serverless编程界面` 的研发框架，允许业务 App 以 `动态` 的方式被装载到一个正在运行的基座容器(ArkContainer)中。在 [Koupleless](https://github.com/koupleless/koupleless/) 模块隔离能力的基础上，我们针对阿里集团技术栈做了深度改造定制。

整套框架提供以下关键能力:

<img alt="阿里巴巴国际 Serverless 框架关键能力" src="https://github.com/sofastack/sofa-serverless/assets/11410549/b2bc97cb-2a0c-4d6d-a971-0dbc78dd2959" width="600"></img>

#### 隔离性与运行时原理

<img alt="Serverless 隔离性与运行时原理" src="https://github.com/sofastack/sofa-serverless/assets/11410549/3cc5dfda-b3e4-4edf-a553-34577425771a" width="1300"></img>

框架实现了基座与应用模块的 `ClassLoader隔离` 与 `SpringContext隔离`。启动流程上，一共分为 `两阶三步`，启动顺序自底向上：

- **一阶段基座启动**
   - **第一步**: 启动 Bootstrap，包含 Kondyle 以及 Pandora 容器，加载 `Kondyle 插件` 以及 `Pandora 中间件插件` 的 `类或对象`
   - **第二步**: `基座应用启动`，其内部顺序为
      - 启动 ArkContainer，初始化 Serverless 相关组件
      - 基座应用 SpringContext 初始化，对象初始化过程中加载 `基座自有类、基座Plugin类、依赖包类、中间件SDK类` 等
- **二阶段app启动**
   - **第三步**: `Serverless App启动`，由 ArkContainer 内置组件接受 `Fiber` 调度请求下载 App 制品并触发 App Launch
      - 创建 BizClassLoader，作为线程 ClassLoader 初始化 SpringContext，加载 `App自有类、基座Plugin类、依赖包类、中间件SDK类` 等

#### 通信机制

在 Serverless 形态下，基座与 App 之间可以通过 `进程内通信方式` 进行交互，目前提供两种通信模型: **SPI **和**基座Bean服务导出**
> SPI 本质上就是基于标准 Java SPI 的扩展的内部特殊实现，本文不额外赘述，这里重点介绍下 `基座Bean服务导出`。

一般情况下，基座的 SpringContext 与 App 的 SpringContext 是完全隔离的，且没有父子继承关系。因此 App 侧不能通过常规 `@Autowired` 的方式引用下层基座 SpringContext 中的 Bean。
除了 Class 的下沉，在一些场景下，基座可以将自己已经初始化好的 Bean 对象也下沉掉，声明并暴露给上层 App 使用。这样之后 App 启动的时候可以直接拿到基座 SpringContext 中的已经完成初始化的 Bean，用以加快 App 的启动速度。其过程如下：

<img alt="加快 Java 应用启动" src="https://github.com/sofastack/sofa-serverless/assets/11410549/99036b5c-1081-4869-8e6e-84127250f063" width="400"></img>

   1. 用户通过配置或者注解标注声明需要在基座中导出的 Bean 服务
   2. 基座启动结束后，隔离容器会自动将用户标注的 Bean 导出到一个缓冲区中，等待 App 的启动使用
   3. App 在基座上启动，App 的 SpringContext 初始化过程中，会在初始化阶段读取到缓冲区中需要导入的 Bean
   4. App 的 SpringContext 中的其他组件可以正常 `@Autowired` 这些导出的 Bean

#### 插件机制

Serverless 插件提供了一种能够让 App 运行时所需的类默认从基座中加载的机制，框架支持将平台基座需要暴露给上层 App 使用的SDK/二方包等包装成一个插件(Ark Plugin)，最终实现中台控制的包下沉到基座而不需要让上层业务改动：

<img alt="Serverless 插件机制" src="https://github.com/sofastack/sofa-serverless/assets/11410549/7bbbcbb9-46d4-46d0-b82d-1e9f21c754b0" width="500"></img>

### 中间件适配

在 Serverless 架构演进中，由于一个完整应用的启动过程被拆分成基座启动和 App 启动，因此在一二阶段相关中间件的初始化逻辑也发生了变化。我们对国际侧常用的中间件和产品组件进行了测试，并对部分中间件进行了适配改造。
总结起来，大部分问题都出现在中间件一些流程逻辑没设计这种多 ClassLoader 的场景，很多类/方法使用中不会将ClassLoader 对象作为参数传递进来，进而在初始化模型对象时出错，导致上下文交互异常。

### 开发配套

我们也提供了一整套完整且简单易用的配套工具，方便开发者快速接入 Serverless 体系：

<img alt="Serverless 研发配套" src="https://github.com/sofastack/sofa-serverless/assets/11410549/2d7c3c88-0924-4a9b-9d01-c6c718fcd4b0" width="600"></img>


## 发布 & 运维域

除了研发域，Serverless 架构在发布运维领域也带来很多新的变化。首先是研发运维分层拆分，实现了关注点分离，降低研发复杂度：

<img alt="Serverless 运维配套" src="https://github.com/sofastack/sofa-serverless/assets/11410549/1e06ba4e-ee6f-42cd-8beb-d848fe0c7b6d" width="1100"></img>

- **逻辑拆分**：将原本应用拆分，将业务代码和基础设施隔离，基础能力下沉，比如将改造前启动过程中耗时中间件、一些富二方库和需要管控的二方库等等下沉到基座中，实现了业务应用轻量化。
- **独立演进**：分层拆分后，基座和业务应用各自迭代，SRE 可以在基座上将基础设施的统一管控和升级，较少甚至杜绝了业务的升级成本。

<img alt="Serverless 运维配套" src="https://github.com/sofastack/sofa-serverless/assets/11410549/42d72ce3-e7c4-49c1-8154-ba1aac07bdfc" width="600"></img>

我们也和 Aone 一起合作，voyager-serverless 借助 `OSR(Open Serverless Runtime)` 标准协议接入 Aone Serverless 产品技术体系中去。借助新的发布模型和部署策略，在 App 构建速度和启动效率上得到很大提升。

<img alt="Serverless 运维配套" src="https://github.com/sofastack/sofa-serverless/assets/11410549/a2637716-0883-48fd-bad2-928aca1f5af3" width="700"></img>

### 构建效率提升

- **Maven 构建优化**: 由于很多依赖包都已经下沉到已经就绪的基座，因此对于 App 来说就可以减少需要构建的二方包数量以及 Class 文件数量，进而整体优化制品大小，提升构建效率
- **Docker 构建移除**: 由于 Serverless 模式下业务 App 部署的制品就是轻量化 Fat Jar，因此也无需进行 docker 构建

### 发布效率提升

在 Serverless 模式下，我们采用 `Surge+流式发布` 替换传统的分批发布来进一步提升 App 的发布效率。

| **名词** | **描述** |
| --- | --- |
| **分批发布** &nbsp;&nbsp;  | 分批发布策略是达到每个批次的新节点后，进行下一批次，如 100 个节点，10 个批次，第一批次卡点 10 个新节点，第二批次卡点 20 个新节点，依次类推 |
| **Surge** &nbsp;&nbsp; | Surge 发布策略可以在不影响业务可用性的前提下加速业务发布效率：<br/> 1) 发布时会新增加配置 Surge 比例的数量节点，比如业务有 10 台机器，Surge 配置百分比为 50，发布过程就会首先增加 5 台机器用于发布<br/> 2) 如果基座中配置了合理大小的 Buffer，那么可以直接从 Buffer 中获取这 5 台机器，直接发布新版本代码<br/> 3) 整体新版本节点数达到预期数量（本例中 10 台机器），直接下线旧节点，完成整次发布过程<br/>其中 Surge 结合流式发布一起使用，同时配合 Runtime 中合适数量的 Buffer，可以最大程度地加速业务发布效率 |

- **瀑布流分批发布**：每一个批次的机器全部发布上线之后开始发布下一批次，批次内机器并行发布，批次之间串行。假设有100台机器，分10批次发布，每批发布的机器数为10台，发布总耗时为: 

<img alt="Serverless 流式发布" src="https://github.com/sofastack/sofa-serverless/assets/11410549/ccd21281-e158-4af9-a6b7-0a297005b3c7" width="850"></img>

- **Surge流式发布**：发布过程中，允许多分配一些机器来参与更新，核心原理为 `满足可用度的前提` 下，增大单轮次中参与更新的机器个数。例如有100台机器，在满足可用度≥90%的前提下，即任意时刻至少有90(100*90%)个机器在线，surge=5%的发布调度过程如下：

<img alt="Serverless 流式发布" src="https://github.com/sofastack/sofa-serverless/assets/11410549/a5d34726-e4b9-4ce9-956a-ca91ba35be95" width="850"></img>

<img alt="Serverless 流式发布" src="https://github.com/sofastack/sofa-serverless/assets/11410549/b7298771-81f3-4aaf-a4ff-30b0805a0875" width="900"></img>

借助这个新的发布模型，我们在开发变更最频繁的日常和预发环境全面开启 Surge 发布，用来加速业务 App 的发布。

- 在进行 Serverless 改造前：
   - 为了保证发布过程中流量不受影响，一般情况下，一个预发环境会保留两台机器(replica = 2)，执行传统的分批发布(batch=2)，也就是每台机器轮流更新。
   - 这里我们假定应用启动耗时为5min，其中频繁变更的业务代码1min，平台以及中间件等组件加载耗时4min
   - 发布总耗时为 5min + 5min = **10min**

<img alt="Serverless 速度收益" src="https://github.com/sofastack/sofa-serverless/assets/11410549/2e7e7d83-5784-40dc-b60f-370819c390f6" width="1200"></img>

- 完成 Serverless 改造，采用 Surge 流式发布后
   - App 的预发环境只需要保留一台机器(replica = 1)，基座设置buffer = 1，即保留一台空基座用于准备给 App 调度使用
   - 发布策略上，App 环境 Surge 比例为100%
   - 由于只发布更新了 App 的 Biz 代码，发布总耗时为 **1min**，并且整个过程机器总成本保持不变

<img alt="Serverless 速度收益" src="https://github.com/sofastack/sofa-serverless/assets/11410549/2b9dda94-acf8-4846-8e21-821cda4a56cd" width="1150"></img>

同时，我们也在生产环境配置一定数量的基座 Buffer，支持站点 App 的快速弹性扩容。


# 总结与展望

目前已经完成 Daraz 业务站点的**交易、营销、履约、会员、逆向**应用的 Serverless 升级改造，在 `构建时长`、`单机应用启动时长`、`预发环境发布时长` 三个指标上均取得了较大优化效果。在个别领域，甚至真正做到了 App 的 **10秒** 级启动。

<img alt="阿里巴巴国际 Serverless 展望" src="https://github.com/sofastack/sofa-serverless/assets/11410549/298650c3-ef2f-48b3-8b85-3754bd62e5cb" width="1100"></img>


可以看到，本次 Serverless 架构的升级，无论从理论推演还是实践结果，都产生了较大的正向收益与效率提升，这给后续业务 App 的快速迭代带来了不少便利。同时，由于平台代码下沉为基座应用，也具备了跟业务站点正交的发布能力，基本上可以实现基础链路平台版本统一的目标；"关注点分离"也解放了业务开发者，让他们更多关注在自己的业务代码上。但是，还有一些如开发配套成熟度、问题定位与诊断、生产环境成本最优的基座配置等问题与挑战需要进一步解决。我们也会深度参与共建 **[Koupleless](https://github.com/koupleless/koupleless/)** 开源社区，发掘更多的落地场景与实践经验。

Serverless 从来不是某个单一的架构形态，它带来的更多是一种理念和生产方式。理解它、利用它，帮助我们拓宽新的思路与解题方法。
