---
title: SpringBoot 或 SOFABoot 升级为基座
date: 2024-01-25T10:28:32+08:00
description: SpringBoot 或 SOFABoot 升级为 Koupleless 基座
weight: 100
---

## 前提条件
1. SpringBoot 版本 >= 2.3.0（针对 SpringBoot 用户）
2. SOFABoot 版本 >= 3.9.0 或 SOFABoot >= 4.0.0（针对 SOFABoot 用户）

## 接入步骤

### 代码与配置修改

#### 修改 application.properties
```properties
# 需要定义应用名
spring.application.name = ${替换为实际基座应用名}
```

#### 修改主 pom.xml
```xml
<properties>
    <sofa.ark.verion>2.2.7</sofa.ark.verion>
    <koupleless.runtime.version>1.0.0</koupleless.runtime.version>
</properties>
```

```xml
<!-- 注意放在构建 pom 的第一个依赖位置 -->
<dependency>
    <groupId>com.alipay.koupleless</groupId>
    <artifactId>koupleless-base-starter</artifactId>
    <version>${koupleless.runtime.version}</version>
    <type>pom</type>
</dependency>

<!-- 如果使用了 springboot web，则加上这个依赖，详细查看https://www.sofastack.tech/projects/sofa-boot/sofa-ark-multi-web-component-deploy/ -->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>web-ark-plugin</artifactId>
</dependency>
```

### 启动验证
基座应用能正常启动即表示验证成功！

<br/>
<br/>
