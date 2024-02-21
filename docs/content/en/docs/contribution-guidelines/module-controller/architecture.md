---
title: ModuleController 架构设计
date: 2024-01-25T10:28:32+08:00
description: Koupleless ModuleController 架构设计
weight: 100
---

## 介绍
ModuleController 是一个 K8S 控制器，该控制器参考 K8S 架构，定义并且实现了 ModuleDeployment、ModuleReplicaSet、Module 等核心模型与调和能力，从而实现了 Serverless 模块的秒级运维调度，以及与基座的联动运维能力。

## 基本架构
ModuleController 目前包含 ModuleDeployment Opertor、ModuleReplicaSet Operator、Module Operator 三个组件。和 K8S 原生 Deployment 类似，**用户创建 ModuleDeployment 会调和出 ModuleReplicaSet，ModuleReplicaSet 会进一步调和出 Module，最终 Module Operator 会调用 Pod 里的 Arklet SDK 去安装或卸载模块**。此外 ModuleController 还会为 ModuleDeployment 自动生成 K8S Service，企业可以监听该 Service 的 IP 变化实现与自身流量控制系统的集成，从而实现模块粒度的切流和挂流。<br />
[![](../architecture.png#from=url&height=536&id=ZnBYG&originHeight=502&originWidth=645&originalType=binary&ratio=2&rotation=0&showTitle=false&status=done&style=none&title=&width=689)](architecture.png)

## 功能清单和 RoadMap

- **08.15：0.2 版本**上线（包括非对等模块发布、卸载、扩缩容、副本保持、基座运维联动）
- **08.25：0.3 版本**上线（包括回滚链路、各项参数校验、单测达到 80/60、CI 自动化、开发者指南）
- **09.31：0.5 版本**上线（1:1 先扩后缩、模块回滚、两种调度策略、状态回流、1+ 端到端集成测试）
- **10.30：0.6 版本**上线（支持以 K8S Service 方式联动企业四七层流量控制、总计 10+ 端到端集成测试）
- **11.30：1.0 版本**上线（支持对等发布运维、各项修复打磨、总计 20+ 端到端集成测试）
- **12.30：1.1 版本**上线（支持模块和基座自动弹性伸缩、对等与非对等发布运维能力完善）

<br/>
