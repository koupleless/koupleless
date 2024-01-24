---
title: CRD 模型设计
date: 2024-01-25T10:28:32+08:00
description: Koupleless ModuleController CRD 模型设计
weight: 200
---

## CRD 模型对比
| K8S 原生 CRD | ModuleController CRD | 关系和区别 |
| --- | --- | --- |
| Pod | Module | Pod：K8S 中创建和管理的、最小的可部署的计算单元。     Module：Serverless 创建和管理的、最小的可部署的计算单元。 |
| PodSpec | ModuleSpec | PodSpec：对 Pod 的描述。包含容器、调度、卷等。     ModuleSpec：对 Module 的描述，包含模块、服务、调度（亲和性）。 |
| PodTemplate | ModuleTemplate | PodTemplate：定义 Pod 的生成副本，包含 PodSpec。     ModuleTemplate：定义 Module 的生成副本，包含 ModuleGroupSpec。 |
| Deployment | ModuleDeployment | Deployment：定义 Pod 的期望状态和副本数量。     ModuleDeployment：定义 Module 的期望状态和副本数量。 |
| ReplicaSet | ModuleReplicaSet | ReplicaSet：管理 Pod 的运行副本。     <br />ModuleReplicaSet：管理 Module 的运行副本。 |


## ModuleDeployment CRD 模型

![image](https://github.com/sofastack/sofa-serverless/assets/13743483/863d8ede-4904-423e-9473-77466af33c46)

## Module CRD 模型

![image](https://github.com/sofastack/sofa-serverless/assets/13743483/f4e109eb-4b10-4835-a502-7d723b1ca73c)

## ModuleTemplate CRD 模型

![image](https://github.com/sofastack/sofa-serverless/assets/13743483/db4fd36b-d698-4946-8d62-6e6651d3f18a)

## ModuleReplicaSet CRD 模型

![image](https://github.com/sofastack/sofa-serverless/assets/13743483/13fbf29e-3977-4138-b3dd-849ce871fb3b)


<br/>
