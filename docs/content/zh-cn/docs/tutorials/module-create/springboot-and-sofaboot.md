---
title: SpringBoot 或 SOFABoot 一键升级为模块
weight: 100
---

本文讲解了 SpringBoot 或 SOFABoot 一键升级为模块的操作和验证步骤，仅需加一个 ark 打包插件即可实现普通应用一键升级为模块应用，并且能做到同一套代码分支，既能像原来 SpringBoot 一样独立启动，也能作为模块与其它应用合并部署在一起启动。

## 前提条件
1. SpringBoot 版本 >= 2.0.0（针对 SpringBoot 用户）
2. SOFABoot >= 3.9.0 或 SOFABoot >= 4.0.0（针对 SOFABoot 用户）

## 接入步骤

### 步骤 1：修改 application.properties

```properties
# 需要定义应用名
spring.application.name = ${替换为实际模块应用名}
```

### 步骤 2：添加模块打包插件

```xml
<plugins>
    <!--这里添加ark 打包插件-->
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
            <bizName>${替换为模块名}</bizName>
            <webContextPath>${模块自定义的 web context path}</webContextPath>
            <declaredMode>true</declaredMode>
        </configuration>
    </plugin>
    <plugin>
        <!--原来 spring-boot 打包插件 -->
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
            <outputDirectory>./target/boot</outputDirectory>
        </configuration>
    </plugin>
</plugins>
```

### 步骤 3：自动化瘦身模块

您可以使用 ark 打包插件的自动化瘦身能力，自动化瘦身模块应用里的 maven 依赖。这一步是必选的，否则构建出的模块 jar 包会非常大，而且启动会报错。
_扩展阅读_：如果模块不做依赖瘦身[独立引入 SpringBoot 框架会怎样？](/docs/faq/import-full-springboot-in-module)

### 步骤 4：构建成模块 jar 包

执行 `mvn clean package -DskipTest`, 可以在 target 目录下找到打包生成的 ark biz jar 包，也可以在 target/boot 目录下找到打包生成的普通的 springboot jar 包。

**小贴士**：[模块中支持的完整中间件清单](/docs/tutorials/module-development/runtime-compatibility-list/)。


## 实验：验证模块既能独立启动，也能被合并部署

增加模块打包插件（sofa-ark-maven-plugin）进行打包后，只会新增 ark-biz.jar 构建产物，与原生 spring-boot-maven-plugin 打包的可执行Jar 互相不冲突、不影响。
当服务器部署时，期望独立启动，就使用原生 spring-boot-maven-plugin 构建出的可执行 Jar 作为构建产物；期望作为 ark 模块部署到基座中时，就使用 sofa-ark-maven-plugin 构建出的 xxx-ark-biz.jar 作为构建产物

### 验证能独立启动

普通应用改造成模块之后，还是可以独立启动，可以验证一些基本的启动逻辑，只需要在启动配置里勾选自动添加 `provided`scope 到 classPath 即可，后启动方式与普通应用方式一致。通过自动瘦身改造的模块，也可以在 `target/boot` 目录下直接通过 springboot jar 包启动，[点击此处](https://github.com/sofastack/sofa-serverless/blob/module-slimming/samples/springboot-samples/slimming )查看详情。<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695032642009-a5248a99-d91b-4420-b830-600b35eaa402.png#clientId=u4eb3445f-d3dc-4&from=paste&height=606&id=ued085b28&originHeight=1212&originWidth=1676&originalType=binary&ratio=2&rotation=0&showTitle=false&size=169283&status=done&style=none&taskId=u78d21e68-c71c-42d1-ac4c-8b41381bfa4&title=&width=838)

### 验证能合并部署到基座上

1. 启动上一步（验证能独立启动步骤）的基座
2. 发起模块部署
```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "${模块名}",
    "bizVersion": "${模块版本}",
    "bizUrl": "file:///path/to/ark/biz/jar/target/xx-xxxx-ark-biz.jar"
}'
```
返回如下信息表示模块安装成功<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695021262517-34e6728e-b39e-4996-855b-d866e839fd0a.png#clientId=ueb52f3f0-186e-4&from=paste&height=226&id=u8ab265a1&originHeight=452&originWidth=1818&originalType=binary&ratio=2&rotation=0&showTitle=false&size=60390&status=done&style=none&taskId=uf3b43b8e-80dd-43db-b486-3ca38663e5e&title=&width=909)

3. 查看当前模块信息，除了基座 base 以外，还存在一个模块 dynamic-provider

![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695021372335-9fbce7ae-ab41-44e8-ab51-6a771bddfef3.png#clientId=ueb52f3f0-186e-4&from=paste&height=367&id=u301dd5fb&originHeight=734&originWidth=1186&originalType=binary&ratio=2&rotation=0&showTitle=false&size=97949&status=done&style=none&taskId=u8570e201-b10d-460a-946a-d9c94529834&title=&width=593)

4. 卸载模块
```json
curl --location --request POST 'localhost:1238/uninstallBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "dynamic-provider",
    "bizVersion": "0.0.1-SNAPSHOT"
}'
```
返回如下，表示卸载成功
```json
{
    "code": "SUCCESS",
    "data": {
        "code": "SUCCESS",
        "message": "Uninstall biz: dynamic-provider:0.0.1-SNAPSHOT success."
    }
}
```
