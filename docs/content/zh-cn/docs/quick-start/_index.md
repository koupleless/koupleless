---
title: 快速开始
weight: 200
---

## 步骤 1：本地软件安装
下载安装 **go**（建议 1.20 或以上）、**docker**、**minikube**、**kubectl**。

## 步骤 2：一键启动 SOFAServerless
使用 **git** 拉取 GitHub sofa-severless 项目：[https://github.com/sofastack/sofa-serverless](https://github.com/sofastack/sofa-serverless) <br />在 **module-controller** 目录下执行 **make dev** 命令一键部署环境，会自动执行 minikube service 命令弹出网页，由于此时您还没有发布模块，所以网页不会有任何内容显示。

## 步骤 3：秒级发布模块
执行以下命令：
```bash
kubectl apply -f config/samples/module-deployment_v1alpha1_moduledeployment.yaml
```
即可秒级发布上线模块应用。请等待本地 Module CR 资源 Status 字段值变更为 Available**（约 1 秒，表示模块发布完毕）**，再刷新步骤 2 自动打开的网页，即可看到一个简单的卖书页面，这个卖书逻辑就是在模块里实现的：<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/671/1694161452232-15aec134-3b2a-491f-9295-0c5f8f7341af.png#clientId=ue383ca9b-aa63-4&from=paste&height=443&id=ub3eb7eb8&originHeight=1318&originWidth=1626&originalType=binary&ratio=2&rotation=0&showTitle=false&size=168110&status=done&style=none&taskId=u07f60163-67e4-42fa-bc41-76e43a09c1f&title=&width=546)

## 步骤 4：清理本地环境
您可以使用 **make undev** 删除所有本地资源，清理本地环境。

<br/>
<br/>
