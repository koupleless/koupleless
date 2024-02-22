---
title: FAQ List
date: 2024-01-25T10:28:32+08:00
description: Koupleless FAQ. List of common questions about Koupleless.
weight: 10
---

#### Question 1-1: Error Occurs During Module Installation Due to Spring Boot Dependency in Module Compile
```text
java.lang.IllegalArgumentException: Cannot instantiate interface org.springframework.context.ApplicationListener : com.alipay.koupleless.common.spring.KouplelessApplicationListener
```
##### Solution
The module needs to be slimmed down. Refer to this guide: [Module Slimming]](/docs/tutorials/module-development/module-slimming)

#### Question 1-2: Unable to Find `KouplelessApplicationListener` During Module Installation
Error message:
```text
com.alipay.sofa.ark.exception.ArkLoaderException: [ArkBiz Loader] module1:1.0-SNAPSHOT : can not load class: com.alipay.koupleless.common.spring.KouplelessApplicationListener
```
##### Solution
Please add the following dependency in the module:
```xml
<dependency>
    <groupId>com.alipay.koupleless</igroupId>
    <artifactId>koupleless-app-starter</artifactId>
    <version>0.5.6</version>
</dependency>
```
Or upgrade the Koupleless version to the latest one.

#### Question 1-3: Unable to install arkctl via go install
Encounter error when executing the following command:
```shell
go install koupleless.alipay.com/koupleless/v1/arkctl@latest
```
Error message:
```text
go: koupleless.alipay.com/koupleless/v1/arkctl@latest: module koupleless.alipay.com/koupleless/v1/arkctl: Get "https://proxy.golang.org/koupleless.alipay.com/koupleless/v1/arkctl/@v/list": dial tcp 142.251.42.241:443: i/o timeout
```
##### Solution
arkctl is present as a subdirectory of Koupleless, so it cannot be directly installed using go get, You can download the executable from here, Please refer to[ Install arkctl ](https://github.com/koupleless/koupleless/releases/tag/arkctl-release-0.1.0)

#### Question 1-4: Encounter `Master biz environment is null` error during module installation. 

##### Solution: Upgrade Koupleless to the latest version.
```xml
<dependency>
    <groupId>com.alipay.koupleless</igroupId>
    <artifactId>koupleless-app-starter</artifactId>
    <version>${最新版本号}</version>
</dependency>
```

#### Question 1-5: Module static merge deployment cannot find module package in the specified directory.
##### Solution: Upgrade Koupleless to the latest version.
```xml
<dependency>
    <groupId>com.alipay.koupleless</igroupId>
    <artifactId>koupleless-app-starter</artifactId>
    <version>${最新版本号}</version>
</dependency>
```
#### Question 1-6: The user project has a different and outdated version of Guice compared to the version in Koupleless.
Error Message: 
```text
Caused by: java.Lang.ClassNotFoundException: com.google.inject.multibindings.Multibinder
```
![guice_version_incompatibility.png](imgs/guice_version_incompatibility.png)

##### Solution: Upgrade the Guice version to a newer one, such as.
```xml
<dependency>
    <groupId>com.google.inject</groupId>
    <artifactId>guice</artifactId>
    <version>6.0.0</version>
</dependency>
```

#### Question 1-7: When sharing libraries between the module and the base, the module initializes the base's logic.
For example, if the base includes Druid but the module does not, according to the design, the module should not need to initialize the dataSource. However, if the module also initializes the dataSource, this behavior is unexpected and may result in errors.

##### Solution
1. Ensure that the module can be built independently, meaning it can execute `mvn clean package` in the module's directory without errors.
2. Upgrade Koupleless to the latest version, version 0.5.7.

#### Question 1-8: SOFABoot 基座或模块启动报 `The following classes could not be excluded because they are not auto-configuration classes: org.springframework.boot.actuate.autoconfigure.startup.StartupEndpointAutoConfiguration`
SOFABoot requires the concurrent inclusion of spring-boot-actuator-autoconfiguration，This is because in [sofa-boot through code definition](https://github.com/sofastack/sofa-boot/blob/82d0ca388b433ac18fb44704e2f2b280fda1b760/sofa-boot-project/sofa-boot/src/main/java/com/alipay/sofa/boot/env/SofaBootEnvironmentPostProcessor.java#L88)spring.exclude.autoconfiguration = `org.springframework.boot.actuate.autoconfigure.startup.StartupEndpointAutoConfiguration` is defined. If this class cannot be found during startup, an error occurs.

##### Solution
Include `spring-boot-actuator-autoconfiguration` in the base or module.
