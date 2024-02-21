---
title: ModuleController Architecture Design
date: 2024-01-25T10:28:32+08:00
description: Koupleless ModuleController Architecture Design
weight: 100
---

## Introduction
ModuleController is a K8S controller that follows the K8S architecture. It defines and implements core models and reconciling capabilities such as ModuleDeployment, ModuleReplicaSet, and Module. This enables the second-level operation and maintenance scheduling of Serverless modules, as well as coordinated operation and maintenance capabilities with the infrastructure.

## Basic Architecture
ModuleController currently consists of three components: ModuleDeployment Operator, ModuleReplicaSet Operator, and Module Operator. Similar to the native Deployment in K8S, **creating a ModuleDeployment will reconcile a ModuleReplicaSet, which in turn reconciles a Module. Finally, the Module Operator will invoke the Arklet SDK inside the Pod to install or uninstall the module**. Additionally, ModuleController will automatically generate a K8S Service for ModuleDeployment. Enterprises can listen for changes in the IP of this Service to integrate with their own traffic control system, enabling granular traffic cutting and hanging for modules.<br />
[![](./architecture.png#from=url&height=536&id=ZnBYG&originHeight=502&originWidth=645&originalType=binary&ratio=2&rotation=0&showTitle=false&status=done&style=none&title=&width=689)](architecture.png)

## Feature List and RoadMap

- **08.15: Version 0.2** Release (including asymmetric module deployment, uninstallation, scaling, replica maintenance, infrastructure operation and maintenance linkage).
- **08.25: Version 0.3** Release (including rollback chain, parameter validation, unit test coverage of 80/60, CI automation, developer guide).
- **09.31: Version 0.5** Release (1:1 scaling before scaling, module rollback, two scheduling strategies, state feedback, 1+ end-to-end integration tests).
- **10.30: Version 0.6** Release (support for enterprise layer 4/7 traffic control linkage via K8S Service, a total of 10+ end-to-end integration tests).
- **11.30: Version 1.0** Release (support for peer-to-peer deployment and operation, various fixes and polishing, a total of 20+ end-to-end integration tests).
- **12.30: Version 1.1** Release (support for automatic elastic scaling of modules and infrastructure, enhancement of peer-to-peer and asymmetric deployment and operation capabilities).

<br/>
