---
title: 模块发布运维策略
date: 2024-01-25T10:28:32+08:00
description: Koupleless 模块发布运维策略
weight: 600
---


## 运维策略
为了实现生产环境的无损变更，模块发布运维提供了安全可靠的变更能力，用户可以在 ModuleDeployment CR spec 的 operationStrategy 中，配置发布运维的变更策略。operationStrategy 内具体字段解释如下：

| 字段名 | 字段解释                                                                          | 取值范围 | 取值解释 |
| --- |-------------------------------------------------------------------------------| --- | --- |
| batchCount | 分批发布运维批次数                                                                     | 1 - N | 分 1 - N 批次发布运维模块 |
| useBeta | 是否启用 beta 分组发布。启用 beta 分组发布会让第一批次只有一个 IP 做灰度，剩下的 IP 再划分成 (batchCount - 1) 批   | true 或 false | true 表示启用 beta 分组<br />false 表示不启用 beta 分组 |
| needConfirm | 是否启用分组确认。启用后每一批次模块发布运维后，都会暂停，修改 ModuleDeployment.spec.pause 字段为 **false** 后，则运维继续 | true 或 false | true 表示启用分组确认<br />false 表示不启用分组确认 |
| grayTime | 每一个发布运维批次完成后，sleep 多少时间才能继续执行下一个批次                                            | 0 - N | 批次间的灰度时长，单位秒，0 表示批次完成后立即执行下一批次，N 表示批次完成后 sleep N 秒再执行下一批次 |



## 调度策略
可以为基座 K8S Pod Deployment 配置 Label "koupleless.alipay.com/max-module-count"，指定每个 Pod 最多可以安装多少个模块。支持配置为 0 - N 的整数。模块支持打散调度和堆叠调度。<br />
**打散调度**：设置 ModuleDeployment.spec.schedulingStrategy.schedulingPolicy 为 **scatter**。打散调度表示在模块上线、扩容、替换时，优先把模块调度到模块数最少的机器上去安装。<br />
**堆叠调度**：设置 ModuleDeployment.spec.schedulingStrategy.schedulingPolicy 为 **stacking**。堆叠调度表示在模块上线、扩容、替换时，优先把模块调度到模块数最多且没达到基座 max-module-count 上限的机器上去安装。


## 保护机制
_(正在开发中，10.15 上线)_ 您可以配置 ModuleDeployment.spec.maxUnavailable 指定模块在发布运维过程中，最多有几个模块副本可以同时处在不可用状态。模块发布运维需要更新 K8S Service 并卸载模块，会导致该模块副本不可用。**配置为 50%** 表示模块发布运维的一个批次，必须保证**至少 50% 的模块副本可用**，否则 ModuleDeployment.status 会展示报错信息。


## 对等和非对等
您可以配置 ModuleDeployment.spec.replicas 指定模块采用对等还是非对等部署架构。<br />
**非对等架构**：设置 ModuleDeployment.spec.replicas 为 **0 - N **表示非对等架构。非对等架构下必须要为 ModuleDeployment、ModueRepicaSet 设置副本数，因此非对等架构下支持模块的扩容和缩容操作。<br />
**对等架构**：设置 ModuleDeployment.spec.replicas 为 **-1 **表示对等架构**。**对等架构下，K8S Pod Deployment 有多少副本数模块就自动安装到多少个 Pod，模块的副本数始终与 K8S Pod Deployment 副本数一致。因此对等架构下不支持模块的扩缩容操作。_对等架构正在建设中，预计 10.30 发布。_


<br/>
<br/>
