---
title: 快速开始
description: What does your user need to know to try your project?
categories: [Examples, Placeholders]
tags: [test, docs]
weight: 2
---

{{% pageinfo %}}
This is a placeholder page that shows you how to use this template site.
{{% /pageinfo %}}

## 步骤 1：本地软件安装
下载安装 go（建议 1.20 及以上）、docker、minikube、kubectl，并确保连接到网络。

## 步骤 2：一键启动 SOFAServerless
使用 git 拉取 GitHub sofa-severless 项目：https://github.com/sofastack/sofa-serverless
在 module-controller 目录下执行 make dev 命令一键部署环境，会自动执行 minikube service 命令弹出网页，由于此时您还没有发布模块，所以网页不会有任何内容显示。

## 步骤 3：快速发布模块
执行以下命令：
kubectl apply -f config/samples/module-deployment_v1alpha1_moduledeployment.yaml
即可秒级发布上线模块应用。请等待本地 Module CR 资源 Status 字段值更改为 Available（约 5 秒 TODO），再刷新步骤 2 自动打开的网页，即可看到一个简单的卖书页面：

{{% imgproc default Fill "400x450" %}}
默认的书籍和排序
{{% /imgproc %}}


1. 模块扩容，将上方路径内yaml文件的replicas字段的值增大后kubectl apply -f，模块缩容，减小replicas字段的值
2. 等待Module资源Status更改为Available，刷新自动打开的网页，效果如下图所示
{{% imgproc reordered Fill "400x450" %}} 
更新后的书籍和排序
{{% /imgproc %}}
3. make undev删除部署资源，清理本地环境


