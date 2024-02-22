---
title: Quick Start
date: 2024-01-25T10:28:32+08:00
description: Koupleless Quick Start
weight: 200
---

# Experiment 1: One-Click Multi-Application Consolidated Deployment

Consolidated deployment refers to selecting one application as the base and then consolidating multiple other applications onto this base to achieve the ultimate resource cost reduction for long-tail applications. Typical business scenarios include low-cost delivery of applications and one-click reconsolidation of over-split microservices.

1. Select an application as the base (referred to as **foundation** in Koupleless terminology) and [upgrade a regular application to the foundation](/docs/tutorials/base-create/springboot-and-sofaboot/).
2. Select an application as the upper-layer application (referred to as **module** in Koupleless terminology) and [convert it to a module application and complete the consolidated deployment](/docs/tutorials/module-create/springboot-and-sofaboot/).
<br/>
You can also directly use the [official demo and documentation](https://github.com/koupleless/koupleless/tree/master/samples/springboot-samples/service) 在本地完成实验。 

Tip: Whether it is the **base** or the **module**, after integrating Koupleless, the same code branch can both start independently as before and achieve consolidated deployment.

<br/>
<br/>


# Experiment 2: One-Click Experience of Application Instant Hot Deployment

## Step 1: Local Software Installation
Download and install **go** (recommended version 1.20 or above), **docker**, **minikube**, **kubectl**.
- Note: Before Step 2, please start docker and minikube
```shell
# For macOS, run the following commands:
# open --background -a Docker
open --background -a Docker

# minikube start
minikube start
```

## Step 2: One-Click Start Koupleless
Use **git** to clone the Koupleless project from GitHub: [https://github.com/koupleless/koupleless](https://github.com/koupleless/koupleless) <br />Execute the **make dev** command under the **module-controller** directory to deploy the environment with one click. The minikube service command will be automatically executed to pop up a webpage. Since you have not yet released the module, the webpage will not display any content.

## Step 3: Module Deployment in Seconds
Execute the following command:
```bash
kubectl apply -f config/samples/module-deployment_v1alpha1_moduledeployment_provider.yaml
```
You can deploy the module application online in seconds. Please wait for the local Module CR resource Status field value to change to **Available** (about 1 second, indicating that the module has been deployed). Then, refresh the webpage automatically opened in Step 2, and you will see a simple book selling page. The logic of selling books is implemented in the module: <br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/671/1694161452232-15aec134-3b2a-491f-9295-0c5f8f7341af.png#clientId=ue383ca9b-aa63-4&from=paste&height=443&id=ub3eb7eb8&originHeight=1318&originWidth=1626&originalType=binary&ratio=2&rotation=0&showTitle=false&size=168110&status=done&style=none&taskId=u07f60163-67e4-42fa-bc41-76e43a09c1f&title=&width=546)

## Step 4: Clean Up Local Environment
You can use **make undev** to clean all local resources and clean up the local environment.

<br/>
<br/>

# Welcome to Learn Koupleless Video Tutorials

[Click here](/docs/video-training/)to view the Koupleless platform and development framework video training courses.
