---
title: Upgrade to Module with One-Click for SpringBoot or SOFABoot
date: 2024-01-25T10:28:32+08:00
description: Upgrade to Koupleless Module with One-Click for SpringBoot or SOFABoot
weight: 100
---

This article explains the steps and verification process for upgrading SpringBoot or SOFABoot to a module with one click. With just one ark packaging plugin, ordinary applications can be upgraded to module applications. The same code branch can be used for both independent startup like SpringBoot and for deployment as modules alongside other applications.

## Prerequisites
1. SpringBoot version >= 2.3.0 (for SpringBoot users)
2. SOFABoot >= 3.9.0 or SOFABoot >= 4.0.0 (for SOFABoot users)

## Access Steps

### Step 1: Modify application.properties

```properties
# Need to define the application name
spring.application.name = ${Replace with actual module app name}
```

### Step 2: Add Dependencies and Packaging Plugins for the Module

**Note**： The order of defining the sofa-ark plugin must be before the springboot packaging plugin;
```xml
<!-- Dependencies required for the module, mainly for inter-module communication --> 
<dependencies>
    <dependency>
        <groupId>com.alipay.sofa.koupleless</groupId>
        <artifactId>koupleless-app-starter</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>

<plugins>
<!-- Add the ark packaging plugin here -->
    <plugin>
        <groupId>com.alipay.sofa</groupId>
        <artifactId>sofa-ark-maven-plugin</artifactId>
        <version>{sofa.ark.version}</version>
        <executions>
            <execution>
                <id>default-cli</id>
                <goals>
                    <goal>repackage</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <skipArkExecutable>true</skipArkExecutable>
            <outputDirectory>./target</outputDirectory>
            <bizName>${Replace with module name}</bizName>
            <webContextPath>${Module's custom web context path}</webContextPath>
            <declaredMode>true</declaredMode>
        </configuration>
    </plugin>
<!-- Build a regular SpringBoot fat jar, used for independent deployment, can be removed if not needed -->
    <plugin>
        <!-- Original spring-boot packaging plugin -->
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
    </plugin>
</plugins>
```

### Step 3: Automate Module Slimming

You can leverage the automated slimming capability provided by the ark packaging plugin to slim down the Maven dependencies in your module application. This step is mandatory; otherwise, the resulting module JAR file will be very large, and startup may fail.
_Extended Reading_: If the module does not optimize its dependencies[What will happen if SpringBoot framework is imported independently?](/docs/faq/import-full-springboot-in-module)

Step 4: Build the Module Jar Package

Execute **mvn clean package -DskipTest**, you can find the packaged ark biz jar in the target directory, or you can find the packaged regular springboot jar in the target/boot directory.

**Tip**：[Full Middleware Compatibility List Supported in the Module](/docs/tutorials/module-development/runtime-compatibility-list/)。


## Experiment: Verifying that the module can be started independently and deployed as a combined module

After adding the module packaging plugin (sofa-ark-maven-plugin) for packaging, only the ark-biz.jar build artifact will be added, which does not conflict with or affect the executable Jar built by the native spring-boot-maven-plugin.
When deploying on the server, if you want to start independently, use the executable Jar built by the native spring-boot-maven-plugin as the build artifact; if you want to deploy as an ark module to the base, use the ark-biz.jar built by the sofa-ark-maven-plugin as the build artifact.

### Verification of Deployment to the Base

1. Start the base from the previous step (verification of independent startup).
2. Initiate module deployment
```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "${Module Name}",
    "bizVersion": "${Module Version}",
    "bizUrl": "file:///path/to/ark/biz/jar/target/xx-xxxx-ark-biz.jar"
}'
```
If the following information is returned, it indicates that the module is installed successfully.<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695021262517-34e6728e-b39e-4996-855b-d866e839fd0a.png#clientId=ueb52f3f0-186e-4&from=paste&height=226&id=u8ab265a1&originHeight=452&originWidth=1818&originalType=binary&ratio=2&rotation=0&showTitle=false&size=60390&status=done&style=none&taskId=uf3b43b8e-80dd-43db-b486-3ca38663e5e&title=&width=909)

3. View Current Module Information: Besides the base "base," there is also a module named "dynamic-provider."

![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695021372335-9fbce7ae-ab41-44e8-ab51-6a771bddfef3.png#clientId=ueb52f3f0-186e-4&from=paste&height=367&id=u301dd5fb&originHeight=734&originWidth=1186&originalType=binary&ratio=2&rotation=0&showTitle=false&size=97949&status=done&style=none&taskId=u8570e201-b10d-460a-946a-d9c94529834&title=&width=593)

4. Uninstall the module
```shell
curl --location --request POST 'localhost:1238/uninstallBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "dynamic-provider",
    "bizVersion": "0.0.1-SNAPSHOT"
}'
```
If the following information is returned, it indicates that the uninstallation was successful.
```json
{
    "code": "SUCCESS",
    "data": {
        "code": "SUCCESS",
        "message": "Uninstall biz: dynamic-provider:0.0.1-SNAPSHOT success."
    }
}
```
### Verification of Independent Startup

After transforming a regular application into a module, it can still be started independently to verify some basic startup logic. Simply check the option to automatically add `provided` scope to the classpath in the startup configuration, and then use the same startup method as for regular applications. Modules transformed through automatic slimming can also be started directly using the SpringBoot jar package located in the `target/boot` directory. For more details, please refer to [this link](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/slimming)<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695032642009-a5248a99-d91b-4420-b830-600b35eaa402.png#clientId=u4eb3445f-d3dc-4&from=paste&height=606&id=ued085b28&originHeight=1212&originWidth=1676&originalType=binary&ratio=2&rotation=0&showTitle=false&size=169283&status=done&style=none&taskId=u78d21e68-c71c-42d1-ac4c-8b41381bfa4&title=&width=838)
