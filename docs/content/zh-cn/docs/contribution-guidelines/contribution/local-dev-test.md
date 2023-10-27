---
title: 本地开发测试
date: 2023-09-21T10:28:35+08:00
weight: 100
---

## SOFAArk 和 Arklet
SOFAArk 是一个普通 Java SDK 项目，使用 Maven 作为依赖管理和构建工具，只需要本地安装 Maven 3.6 及以上版本即可正常开发代码和单元测试，无需其它的环境准备工作。<br />关于代码提交细节请参考：[完成第一次 PR 提交](../first-pr)。

## ModuleController
ModuleController 是一个标准的 K8S Golang Operator 组件，里面包含了 ModuleDeployment Operator、ModuleReplicaSet Operator、Module Operator，在本地可以使用 minikube 做开发测试，具体请参考[本地快速开始](/docs/quick-start)。<br />
编译构建请在 module-controller 目录下执行：
```bash
go mod download   # if compile module-controller first time
go build -a -o manager cmd/main.go  
```
单元测试执行请在 module-controller 目录下执行：
```bash
make test
```
您也可以使用 IDE 进行编译构建、开发调试和单元测试执行。<br />
module-controller 开发方式和标准 K8S Operator 开发方式完全一样，您可以参考 K8S Operator 开发[官方文档](https://kubernetes.io/zh-cn/docs/concepts/extend-kubernetes/operator/)。

## Arkctl
Arkctl 是一个普通 Golang 项目，他是一个命令行工具集，包含了用户在本地开发和运维模块过程中的常用工具，它和普通 Golang 程序开发完全一样，_当前初始版本还在开发中_。


<br/>
