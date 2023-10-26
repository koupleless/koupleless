---
title: SpringBoot 或 SOFABoot 升级为基座
weight: 100
---

## 前提条件
1. SpringBoot 版本 >= 2.0.0
2. SOFABoot >= 3.9

## 接入步骤

### 代码与配置修改

#### 修改 application.properties
```properties
# 需要定义应用名
spring.application.name = ${替换为实际基座名}
```

#### 修改主 pom.xml
```xml
<properties>
    <sofa.ark.verion>2.2.4-SNAPSHOT</sofa.ark.verion>
    <sofa.serverless.runtime.version>0.5.1</sofa.serverless.runtime.version>
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

#### 修改启动参数
启动参数增加 `-Dsofa.ark.embed.enable=true`

### 启动验证

1. 基座正常启动
2. 发起 curl 命令，查看基座是否已经初始化好
```shell
curl --location --request POST 'localhost:1238/queryAllBiz'
```

3. 返回如下，表示有一个模块，该模块实际就是基座自身，基座改造成功
```json
{
    "code": "SUCCESS",
    "data": [
        {
            "bizName": "base",
            "bizState": "ACTIVATED",
            "bizVersion": "1.0.0",
            "mainClass": "embed main",
            "webContextPath": "/"
        }
    ]
}
```

<br/>
<br/>
