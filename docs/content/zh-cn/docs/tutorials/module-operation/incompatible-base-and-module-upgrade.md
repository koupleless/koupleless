---
title: 基座和模块不兼容发布
date: 2024-01-25T10:28:32+08:00
description: Koupleless 基座和模块不兼容发布
weight: 300
---

## 步骤  1
修改基座代码和模块代码，然后将基座构建为新版本的镜像，将模块构建为新版本的代码包（Java 就是 Jar 包）。

## 步骤  2
修改模块对应的 ModuleDeployment.spec.template.spec.module.url 为新的模块代码包地址。

## 步骤  3
使用 K8S Deployment 发布基座到新版本镜像（会触发基座容器的替换或重启），基座容器启动时会拉取 ModuleDeployment 上最新的模块代码包地址，从而实现了基座与模块的不兼容变更（即同时发布）。<br/>

<br/>
<br/>
