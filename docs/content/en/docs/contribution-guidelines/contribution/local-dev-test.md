---
title: Local Development Testing
date: 2024-01-25T10:28:32+08:00
description: Koupleless Local Development Testing
weight: 100
---

## SOFAArk and Arklet
SOFAArk is a regular Java SDK project that uses Maven as its dependency management and build tool. You only need to install Maven 3.6 or higher locally to develop code and run unit tests normally, without any other environment preparation. <br /> For details on code submission, please refer to: [Completing the First PR Submission](../first-pr).

## ModuleController
ModuleController is a standard K8S Golang Operator component, which includes ModuleDeployment Operator, ModuleReplicaSet Operator, and Module Operator. You can use minikube for local development testing. For details, please refer to [Local Quick Start](/docs/quick-start). <br />
To compile and build, execute the following command in the module-controller directory:
```bash
go mod download   # if compile module-controller first time
go build -a -o manager cmd/main.go  
```
To run unit tests, execute the following command in the module-controller directory:
```bash
make test
```
You can also use an IDE for compiling, building, debugging, and running unit tests.<br />
The development approach for module-controller is exactly the same as the standard K8S Operator development approach. You can refer to the [official K8S Operator development documentation](https://kubernetes.io/zh-cn/docs/concepts/extend-kubernetes/operator/)。

## Arkctl
Arkctl is a regular Golang project, which is a command-line toolset that includes common tools for users to develop and maintain modules locally.
[You can refer here](/docs/tutorials/build_and_deploy)


<br/>
