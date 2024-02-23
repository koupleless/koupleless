---
title: Development Validation and Deployment
date: 2024-01-25T10:28:32+08:00
description: Koupleless Base and Module Development Validation and Deployment
weight: 500
---

This document mainly introduces the dynamic merge deployment mode, which is used to save resources and improve development efficiency. If you only want to save resources, you can use [static merge deployment](/docs/tutorials/module-development/static-merge-deployment/)。

![img.png](/img/build_and_deploy.png)

A video tutorial is also available [here](/docs/video-training/)。

## Base Integration
[Refer here for reference](/docs/tutorials/base-create/springboot-and-sofaboot)

## Module Integration
[Refer here for reference](/docs/tutorials/module-create/springboot-and-sofaboot)

## Module Development Verification
There can be two ways for development verification:
1. Local Environment Development Verification
2. K8S Cluster Environment Development Verification

### Local Environment Development Verification
#### Installation
1. Download [arkctl](https://github.com/koupleless/koupleless/releases/tag/arkctl-release-0.1.1) according to the actual operating system.
2. Unzip the corresponding binary and place it in the appropriate system variable PATH.
3. After the base and module are refactored, and the base is started, arkctl can be used to quickly build and deploy, deploying the module to the base.
<br/>

#### How to find the value of PATH?
In Linux/Mac, execute the following command in the terminal:
```shell
echo $PATH
# Choose a directory and place arkctl in that directory
```
In Windows:
1. Right-click on My Computer. 
2. Click on Properties. 
3. Click on Advanced System Settings. 
4. Click on Environment Variables. 
5. Double-click on the Path variable. 
6. In the popped dialog box, you can see the current Path variable value. 
7. Find the corresponding directory and place arkctl.exe in that directory.

#### Usage
Quickly deploy the built module jar package.
```shell
arkctl deploy ${path to the module jar package}
```
Build the pom project in the current directory and deploy the biz jar package in the target directory to the base.
```shell
arkctl deploy 
```
### K8S Cluster Environment Development Verification, using minikube cluster as an example
#### Base Deployment
1. Build the base into an image and push it to the image center.
2. Deploy the base to the k8s cluster, create a service for the base, expose the port, can [refer here](https://github.com/koupleless/koupleless/blob/master/module-controller/config/samples/dynamic-stock-service.yaml)
3. Execute minikube service base-web-single-host-service to access the base service.

#### Module Deployment
1. Deploy the module to the k8s cluster
```shell
arkctl deploy ${path to the module jar package} --pod ${namespace}/${podname}
```

## Module Deployment and Online
1. Use helm to deploy ModuleController to the k8s cluster.
2. Use the module deployment capability provided by ModuleController to publish the module to the cluster machines, with gray, traceable, and traffic-free capabilities. For details, please [refer here](/docs/tutorials/module-operation/module-online-and-offline/)


## For more experiments, please refer to the samples directory

[Refer here for reference](https://github.com/koupleless/koupleless/tree/master/samples)
