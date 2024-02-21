---
title: CRD Model Design
date: 2024-01-25T10:28:32+08:00
description: CRD Model Design for Koupleless ModuleController
weight: 200
---

## Comparison of CRD Models
| Native K8S CRD | ModuleController CRD | Relationship and Difference |
| --- | --- | --- |
| Pod | Module | Pod: The smallest deployable computing unit created and managed in K8S. Module: The smallest deployable computing unit created and managed in Serverless. |
| PodSpec | ModuleSpec | PodSpec: Describes the specifications of a Pod, including containers, scheduling, volumes, etc. ModuleSpec: Describes the specifications of a Module, including module, service, and scheduling (affinity). |
| PodTemplate | ModuleTemplate | PodTemplate: Defines the template for generating Pod replicas, including PodSpec. ModuleTemplate: Defines the template for generating Module replicas, including ModuleGroupSpec. |
| Deployment | ModuleDeployment | Deployment: Defines the desired state and replica count for Pods. ModuleDeployment: Defines the desired state and replica count for Modules. |
| ReplicaSet | ModuleReplicaSet | ReplicaSet: Manages the running replicas of Pods. ModuleReplicaSet: Manages the running replicas of Modules. |


## ModuleDeployment CRD Model

![image](https://github.com/sofastack/sofa-serverless/assets/13743483/863d8ede-4904-423e-9473-77466af33c46)

## Module CRD Model

![image](https://github.com/sofastack/sofa-serverless/assets/13743483/f4e109eb-4b10-4835-a502-7d723b1ca73c)

## ModuleTemplate CRD Model

![image](https://github.com/sofastack/sofa-serverless/assets/13743483/db4fd36b-d698-4946-8d62-6e6651d3f18a)

## ModuleReplicaSet CRD Model

![image](https://github.com/sofastack/sofa-serverless/assets/13743483/13fbf29e-3977-4138-b3dd-849ce871fb3b)


<br/>
