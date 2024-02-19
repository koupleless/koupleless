---
title: 模块信息查看
date: 2024-01-25T10:28:32+08:00
description: Koupleless 模块信息查看
weight: 800
---

#### 查看某个基座上所有安装的模块名称和状态
```
kubectl get module -n <namespace> -l koupleless.alipay.com/base-instance-ip=<pod-ip> -o custom-columns=NAME:.metadata.name,STATUS:.status.status
```
或
```
kubectl get module -n <namespace> -l koupleless.alipay.com/base-instance-name=<pod-name> -o custom-columns=NAME:.metadata.name,STATUS:.status.status
```
#### 查看某个基座上所有安装的模块详细信息
```
kubectl describe module -n <namespace> -l koupleless.alipay.com/base-instance-ip=<pod-ip>
```
或
```
kubectl describe module -n <namespace> -l koupleless.alipay.com/base-instance-name=<pod-name>
```

替换```<pod-ip>```为需要查看的基座ip，```<pod-name>```为需要查看的基座名称，```<namespace>```为需要查看资源的namespace