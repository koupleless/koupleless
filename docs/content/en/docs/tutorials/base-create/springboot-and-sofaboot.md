---
title: SpringBoot or SOFABoot Upgrade to Foundation
date: 2024-01-25T10:28:32+08:00
description: Upgrade SpringBoot or SOFABoot to Koupleless Foundation
weight: 100
---

## Prerequisites
1. SpringBoot version >= 2.3.0 (for SpringBoot users)
2. SOFABoot version >= 3.9.0 or SOFABoot >= 4.0.0 (for SOFABoot users)

## Access Steps

### Code and Configuration Modifications

#### Modify application.properties
```properties
# Need to define the application name
spring.application.name = ${Replace with actual base app name}
```

#### Modify the main pom.xml
```xml
<properties>
    <sofa.ark.verion>2.2.7</sofa.ark.verion>
    <koupleless.runtime.version>1.0.0</koupleless.runtime.version>
</properties>
```

```xml
<!-- Place this as the first dependency in your build pom -->
<dependency>
    <groupId>com.alipay.koupleless</groupId>
    <artifactId>koupleless-base-starter</artifactId>
    <version>${koupleless.runtime.version}</version>
    <type>pom</type>
</dependency>

<!-- If using Spring Boot web, add this dependency. For more details, see https://www.sofastack.tech/projects/sofa-boot/sofa-ark-multi-web-component-deploy/ -->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>web-ark-plugin</artifactId>
</dependency>
```

### Startup Verification
If the foundation application can start normally, the validation is successful!

<br/>
<br/>
