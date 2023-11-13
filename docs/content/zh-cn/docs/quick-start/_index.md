---
title: 快速开始
weight: 200
---

# 实验 1：一键实现多应用合并部署

合并部署是指：选定一个应用作为底座，然后将多个其它应用合并部署到这个底座之上，从而实现长尾应用的极致资源降本。典型业务场景为应用的低成本交付 以及 微服务过度拆分一键重新合并。

1. 选定一个应用作为底座（SOFAServerless 术语叫**基座**），将普通应用[一键升级为基座](/docs/tutorials/base-create/springboot-and-sofaboot/)。
2. 选定一个应用作为上层应用（SOFAServerless 术语叫**模块**），将其[一键转为模块应用并完成合并部署](/docs/tutorials/module-create/springboot-and-sofaboot/)。
<br/>
您也可以直接使用 [官方 Demo 和文档](https://github.com/sofastack/sofa-serverless/tree/master/samples/springboot-samples/service) 在本地完成实验。 

小贴士：无论**基座**还是**模块**，接入 SOFAServerless 后，同一套代码分支既能像原来一样独立启动，又能做到合并部署。

<br/>
<br/>


# 实验 2：一键体验应用秒级热部署

## 步骤 1：本地软件安装
下载安装 **go**（建议 1.20 或以上）、**docker**、**minikube**、**kubectl**。

## 步骤 2：一键启动 SOFAServerless
使用 **git** 拉取 GitHub sofa-severless 项目：[https://github.com/sofastack/sofa-serverless](https://github.com/sofastack/sofa-serverless) <br />在 **module-controller** 目录下执行 **make dev** 命令一键部署环境，会自动执行 minikube service 命令弹出网页，由于此时您还没有发布模块，所以网页不会有任何内容显示。

## 步骤 3：秒级发布模块
执行以下命令：
```bash
kubectl apply -f config/samples/module-deployment_v1alpha1_moduledeployment_provider.yaml
```
即可秒级发布上线模块应用。请等待本地 Module CR 资源 Status 字段值变更为 Available**（约 1 秒，表示模块发布完毕）**，再刷新步骤 2 自动打开的网页，即可看到一个简单的卖书页面，这个卖书逻辑就是在模块里实现的：<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/671/1694161452232-15aec134-3b2a-491f-9295-0c5f8f7341af.png#clientId=ue383ca9b-aa63-4&from=paste&height=443&id=ub3eb7eb8&originHeight=1318&originWidth=1626&originalType=binary&ratio=2&rotation=0&showTitle=false&size=168110&status=done&style=none&taskId=u07f60163-67e4-42fa-bc41-76e43a09c1f&title=&width=546)

## 步骤 4：清理本地环境
您可以使用 **make undev** 删除所有本地资源，清理本地环境。

<br/>
<br/>

# 欢迎大家学习 SOFAServerless 视频教程

[点击此处](/docs/tutorials/video-training)查看 SOFAServerless 平台与研发框架视频培训教程。
