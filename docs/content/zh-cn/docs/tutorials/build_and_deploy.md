---
title: 开发验证与部署上线
date: 2024-01-25T10:28:32+08:00
description: Koupleless 基座、模块开发验证与部署上线
weight: 500
---

本文主要介绍动态合并部署模式，用于省资源与提高研发效率。如果你只是想节省资源，可以使用[静态合并部署](/docs/tutorials/module-development/static-merge-deployment/)。

![img.png](/img/build_and_deploy.png)

这里也提供了视频教程，[可点击此处查看](/docs/video-training/)。

## 基座接入

[可参考此处](/docs/tutorials/base-create/springboot-and-sofaboot)

## 模块接入

[可参考此处](/docs/tutorials/module-create/springboot-and-sofaboot)

## 模块开发验证

可以有两种开发验证方式：

1. 本地环境开发验证
2. K8S 集群环境开发验证

### 本地环境开发验证

#### 安装

1.
根据实际运行操作系统，[下载 arkctl ](https://github.com/koupleless/koupleless/releases/tag/arkctl-release-0.1.1)。
2. 将对应的二进制解压并放到合适的系统变量 PATH 下。
3. 在基座和模块已经改造完成后，启动好基座后，可以使用 arkctl 快速完成构建与部署，将模块部署到基座中。
   <br/>

#### 如何找到 PATH 的值？

Linux/Mac 下在终端执行

```shell
echo $PATH
# 选择一个目录，将 arkctl 放到该目录下
```

Windows 下

1. 右键我的电脑。
2. 点击属性。
3. 点击高级系统设置。
4. 点击环境变量。
5. 双击 Path 变量。
6. 在弹出的对话框中，可以看到当前的 Path 变量值。
7. 找到对应的目录，将 arkctl.exe 放到该目录下。

#### 使用

快速部署构建好的模块 jar 包。

```shell
arkctl deploy ${模块构建出的 jar 包路径}
```

构建当前目录下的 pom 项目，并将 target 目录下的 biz jar 包部署到基座中。

```shell
arkctl deploy 
```

查看基座目前部署的模块

```shell
arkctl status
```

有命令行交互的卸载模块

```shell
# 调用此命令后，会列出当前部署的模块，用户可以通过上下键选择要卸载的模块。
arkctl uninstall
```

制定模块名称卸载模块

```shell
arkctl uninstall ${模块名称:模块版本}
```

### K8S 集群环境开发验证, 以 minikube 集群为例

#### 基座发布

1. 基座构建成镜像，推送到镜像中心
2. 基座部署到 k8s 集群中，创建基座的 service，暴露端口,
   可[参考这里](https://github.com/koupleless/koupleless/blob/master/module-controller/config/samples/dynamic-stock-service.yaml)
3. 执行 minikube service base-web-single-host-service, 访问基座的服务

#### 模块发布

1. 部署模块到 k8s 集群中

```shell
arkctl deploy ${模块构建出的 jar 包路径} --pod ${namespace}/${podname}
```

## 模块部署上线

1. 使用 helm 方式部署 ModuleController 到 k8s 集群
2. 使用 ModuleController
   提供的模块部署能力，发布模块到集群机器上，具备可灰度、可追溯、流量无损能力，详细可[参见此处](/docs/tutorials/module-operation/module-online-and-offline/)

## 更多实验请查看 samples 用例

[点击此处](https://github.com/koupleless/koupleless/tree/master/samples)
