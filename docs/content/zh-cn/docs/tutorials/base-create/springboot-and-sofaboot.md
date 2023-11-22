---
title: SpringBoot 或 SOFABoot 升级为基座
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
    <sofa.ark.verion>2.2.4</sofa.ark.verion>
    <sofa.serverless.runtime.version>0.5.3</sofa.serverless.runtime.version>
</properties>
```

```xml
<dependency>
    <groupId>com.alipay.sofa.serverless</groupId>
    <artifactId>sofa-serverless-base-starter</artifactId>
    <version>${sofa.serverless.runtime.version}</version>
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
