---
title: "Springboot_sofaboot"
date: 2023-09-14T10:31:44+08:00
weight: 1
---

## 前提条件
1. 您的应用是 SpringBoot 应用或者是 SOFABoot 应用。SpringBoot2 和 SpringBoot3、SOFABoot3 和 SOFABoot4 均支持。其中需要 SpringBoot >= 2.0 版本、SOFABoot >= 3.9 版本。
2. 您的应用当前使用 K8S 做发布运维。2024 年我们会支持非容器化部署的应用，比如应用部署在虚拟机上仍然能使用 SOFAServerless 模块研发运维体系。

## 接入步骤
### 步骤 1：修改应用主 pom.xml

升级 SpringBoot 或者 SOFABoot 到最低版本后，修改应用主 pom.xml 和 bootstrap/pom.xml，并引入 Serverless 运行时依赖：

```xml
<dependencyManagement>
  <!-- If SpringBoot Application -->
  <dependencies>
    <groupId>com.alipay.sofa.serverless</groupId>
		<artifactId>sofa-serverless-runtime-starter</artifactId>
		<version>0.1-SNAPSHOT</version>
  </dependencies>
  <!-- Else If SOFABoot Application -->
  <dependencies>
    <dependency>
    	<groupId>com.alipay.sofa</groupId>
    	<artifactId>runtime-sofa-boot-plugin</artifactId>
		</dependency>
  </dependencies>
</dependencyManagement>
```
注意：确保主 pom 和 bootstrap/pom.xml 不要直接或间接引入 slf4j-log4j12、jcl-over-slf4j 依赖，可以在 pom 中 exclude 掉这些依赖，不然会导致模块日志打印有问题。

### 步骤 2：配置 application.properties（可选）

有些应用升级到 SpringBoot 或 SOFABoot 高版本后，Spring 会报错 Bean 循环依赖问题，此时需要修改 application.properties：

```properties
# 如果依赖了高版本的 Spring，默认开启了循环依赖校验，遇到 Spring Bean 循环引用报错，可以关掉：
spring.main.allow-circular-references=true
```

### 步骤 3：Metaspace 大小修改

如果开启了模块热部署，业务在模块卸载的事件处理函数中又没有做比较彻底的资源清理，那么每次热部署模块可能会导致 Metaspace 增长，最终会频繁触发 FullGC 导致模块部署变得很慢。为了加快部署、增加热部署次数、提高稳定性，强烈建议对 JVM 参数进行修改，调大 PermGen 或者 Metaspace 大小。示例：

```properties
-XX:MetaspaceSize=512m -XX:MaxMetaspaceSize=512m
```


