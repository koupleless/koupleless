---
title: 模块操作步骤
date: 2023-11-02T09:51:23+08:00
weight: 900
---

#### 扩缩容操作
1. 修改ModuleDeployment资源下spec.replicas，增大参数值为扩容，减少参数值为缩容。
2. ModuleDeployment资源下spec.replicas的值要跟基座实例个数保持一致。

#### 更新部署
1. 同时修改或修改其中ModuleDeployment资源下方字段将触发模块更新操作，会卸载上个版本的模块，安装新版本模块
    1. spec.template.spec.module.name
    2. spec.template.spec.module.version
2. 如果只更新下方字段，将触发强推基线操作，当替换基座时才会根据url地址安装模块
    1. spec.template.spec.module.url 
