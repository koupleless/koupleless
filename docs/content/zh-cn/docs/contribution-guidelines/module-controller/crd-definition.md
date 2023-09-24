---
title: CRD 模型设计
date: 2023-09-21T10:28:35+08:00
weight: 200
---

<a name="d83f790f"></a>
## CRD 模型对比
| K8S 原生 CRD | ModuleController CRD | 关系和区别 |
| --- | --- | --- |
| Pod | Module | Pod：K8S 中创建和管理的、最小的可部署的计算单元。     Module：Serverless 创建和管理的、最小的可部署的计算单元。 |
| PodSpec | ModuleSpec | PodSpec：对 Pod 的描述。包含容器、调度、卷等。     ModuleSpec：对 Module 的描述，包含模块、服务、调度（亲和性）。 |
| PodTemplate | ModuleTemplate | PodTemplate：定义 Pod 的生成副本，包含 PodSpec。     ModuleTemplate：定义 Module 的生成副本，包含 ModuleGroupSpec。 |
| Deployment | ModuleDeployment | Deployment：定义 Pod 的期望状态和副本数量。     ModuleDeployment：定义 Module 的期望状态和副本数量。 |
| ReplicaSet | ModuleReplicaSet | ReplicaSet：管理 Pod 的运行副本。     <br />ModuleReplicaSet：管理 Module 的运行副本。 |


<a name="14d02f3f"></a>
## ModuleDeployment CRD 模型

![](https://github.com/sofastack/sofa-serverless/assets/13743483/4f84d04d-7252-496d-9eb4-5540c39a5860#id=li81F&originHeight=2870&originWidth=1972&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=)

<a name="8392d397"></a>
## Module CRD 模型

![](https://github.com/sofastack/sofa-serverless/assets/13743483/e1ba399a-f2e6-4040-a886-da77800c3a5b#id=gvCx6&originHeight=1571&originWidth=1766&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=)

<a name="7a8e666e"></a>
## ModuleTemplate CRD 模型

![](https://github.com/sofastack/sofa-serverless/assets/13743483/db4fd36b-d698-4946-8d62-6e6651d3f18a#id=tdukT&originHeight=455&originWidth=1092&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=)

<a name="03b3bef7"></a>
## ModuleReplicaSet CRD 模型

![](https://github.com/sofastack/sofa-serverless/assets/13743483/29a1846e-3d46-4b58-bc1a-25c7f3663942#id=kN3c7&originHeight=2032&originWidth=1706&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=)


<br/>
