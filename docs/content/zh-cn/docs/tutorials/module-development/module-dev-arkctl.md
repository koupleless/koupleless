---
title: 模块本地开发
weight: 400
---

## Arkctl 工具安装

Arkctl 模块安装主要提供自动打包和部署能力，自动打包调用 mvn 命令构建模块 jar包，自动部署调用 [arklet](/docs/contribution-guidelines/arklet/architecture/) 提供的 api 接口进行部署。如果不想使用命令行工具，也可以直接使用 arklet 提供的 api 接口发起部署操作。

方法一：

1. 本地安装 go 环境，go 依赖版本在 1.21 以上。
2. 执行 go install `todo 独立的 arkctl go 仓库` 命令，安装 arkctl 工具。

方法二：

1. 在 [二进制列表](https://github.com/sofastack/sofa-serverless/releases/tag/arkctl-release-0.1.0) 中下载对应的二进制并加入到本地
   path 中。

### 本地快速部署

你可以使用 arkctl 工具快速地进行模块的构建和部署，提高本地调试和研发效率。

#### 场景 1：模块 jar 包构建 + 部署到本地运行的基座中。

准备：

1. 在本地启动一个基座。
2. 打开一个模块项目仓库。

执行命令：

```shell
# 需要在仓库的根目录下执行。
# 比如，如果是 maven 项目，需要在根 pom.xml 所在的目录下执行。
arkctl deploy
```

命令执行完成后即部署成功，用户可以进行相关的模块功能调试验证。

#### 场景 2：部署一个本地构建好的 jar 包到本地运行的基座中。

准备：

1. 在本地启动一个基座。
2. 准备一个构建好的 jar 包。

执行命令：

```shell
arkctl deploy /path/to/your/pre/built/bundle-biz.jar
```

命令执行完成后即部署成功，用户可以进行相关的模块功能调试验证。

#### 场景 3: 模块 jar 包构建 + 部署到远程运行的 k8s 基座中。

准备:

1. 在远程已经运行起来的基座 pod。
2. 打开一个模块项目仓库。
3. 本地需要有具备 exec 权限的 k8s 证书以及 kubectl 命令行工具。

执行命令：

```shell
# 需要在仓库的根目录下执行。
# 比如，如果是 maven 项目，需要在根 pom.xml 所在的目录下执行。
arkctl deploy --pod {namespace}/{podName}
```

命令执行完成后即部署成功，用户可以进行相关的模块功能调试验证。

#### 场景 4: 在多模块的 Maven 项目中，在 Root 构建并部署子模块的 jar 包。

准备：

1. 在本地启动一个基座。
2. 打开一个多模块 Maven 项目仓库。

执行命令：

```shell
# 需要在仓库的根目录下执行。
# 比如，如果是 maven 项目，需要在根 pom.xml 所在的目录下执行。
arkctl deploy --sub ./path/to/your/sub/module
```

命令执行完成后即部署成功，用户可以进行相关的模块功能调试验证。

#### 场景 5: 查询当前基座中已经部署的模块。

准备：

1. 在本地启动一个基座。

执行命令：

```shell
arkctl status
```

#### 场景 6: 查询远程 k8s 环境基座中已经部署的模块。

准备：

1. 在远程 k8s 环境启动一个基座。
2. 确保本地有 kube 证书以及有关权限。

执行命令：

```shell
arkctl status --pod {namespace}/{name}
```
