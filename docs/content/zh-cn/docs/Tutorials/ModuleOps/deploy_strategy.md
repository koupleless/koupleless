---
title: 模块发布运维策略
date: 2017-01-04
weight: 6
---

## 变更策略

为了实现生产环境的无损变更，模块发布运维提供了安全可靠的变更能力，用户可以在 ModuleDeployment CR spec 的 strategy 中，配置发布运维的变更策略。strategy 内具体字段解释如下：


## 调度策略
您可以配置 ModuleDeployment.spec.schedulingStrategy.maxXxx，指定每个 Pod 最多可以安装多少个模块，支持配置 0 - N。

当前模块支持打散调度和集中调度。

打散调度：设置 ModuleDeployment.spec.schedulingStrategy 为 scatter。打散调度表示在模块上线、扩容、替换时，优先把模块调度到模块数最少的机器上去安装。
集中调度：设置 ModuleDeployment.spec.schedulingStrategy 为 stacking。堆叠调度表示在模块上线、扩容、替换时，优先把模块调度到模块数最多且没达到 maxXxx 上限的机器上去安装。

## 保护机制
您可以配置 ModuleDeployment.spec.MaxUnvaliable 指定模块在发布运维过程中，最多有几个模块副本可以同时处在不可用状态。模块发布运维需要更新 K8S Service 并卸载模块，会导致该模块副本不可用。配置为 50% 表示模块发布运维的一个批次，必须保证至少 50% 的模块副本可用，否则 ModuleDeployment.status 会展示报错信息。

## 对等和非对等

您可以配置 ModuleDeployment.spec.replicas 指定模块采用对等还是非对等部署架构。

### 非对等架构
设置 ModuleDeployment.spec.replicas 为 0 - N 表示非对等架构。非对等架构下必须要为 ModuleDeployment、ModueRepicaSet 设置副本数，因此非对等架构下支持模块的扩容和缩容操作。
### 对等架构
设置 ModuleDeployment.spec.replicas 为 -1 表示对等架构。对等架构下，K8S Pod Deployment 有多少副本数模块就自动安装到多少个 Pod，模块的副本数始终与 K8S Pod Deployment 副本数一致。因此对等架构下不支持模块的扩缩容操作。对等架构正在建设中，预计 10.30 发布。

