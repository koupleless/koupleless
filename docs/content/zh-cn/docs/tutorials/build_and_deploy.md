---
title: 开发验证与部署上线
weight: 500
---

本文主要介绍动态合并部署模式，用于省资源与提高研发效率。如果你只是想节省资源，可以使用[静态合并部署](/docs/tutorials/module-development/static-merge-deployment/)。

![img.png](/img/build_and_deploy.png)

这里也提供了视频教程，[可点击此处查看](/docs/video-training/)。

## 基座接入
[可参考此处](/docs/tutorials/base-create/springboot-and-sofaboot.md)

## 模块接入
[可参考此处](/docs/tutorials/module-create/springboot-and-sofaboot.md)

## 模块开发验证
可以有两种开发验证方式：
1. 本地环境开发验证
2. K8S 集群环境开发验证

### 本地环境开发验证
1. 根据实际运行操作系统，[下载 arkctl ](https://github.com/sofastack/sofa-serverless/releases/tag/arkctl-release-0.1.0) , 并放入 `/usr/local/bin` 目录中
2. 在基座和模块已经改造完成后，启动好基座后，可以使用 arkctl 快速完成构建与部署，将模块部署到基座中
```shell
arkctl deploy ${模块构建出的 jar 包路径}
```
### K8S 集群环境开发验证, 以 minikube 集群为例
#### 基座发布
1. 基座构建成镜像，推送到镜像中心
2. 基座部署到 k8s 集群中，创建基座的 service，暴露端口, 可[参考这里](https://github.com/sofastack/sofa-serverless/blob/master/module-controller/config/samples/dynamic-stock-service.yaml)
3. 执行 minikube service base-web-single-host-service, 访问基座的服务

#### 模块发布
1. 部署模块到 k8s 集群中
```shell
arkctl deploy ${模块构建出的 jar 包路径} --pod ${namespace}/${podname}
```

## 模块部署上线
1. 使用 helm 方式部署 ModuleController 到 k8s 集群
2. 使用 ModuleController 提供的模块部署能力，发布模块到集群机器上，具备可灰度、可追溯、流量无损能力，详细可[参见此处](/docs/tutorials/module-operation/module-online-and-offline/)


## 更多实验请查看 samples 用例

[点击此处](https://github.com/sofastack/sofa-serverless/tree/master/samples)
