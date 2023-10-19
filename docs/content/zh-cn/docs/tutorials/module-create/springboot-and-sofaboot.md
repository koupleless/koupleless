---
title: SpringBoot 或 SOFABoot 升级为模块
weight: 100
---

<a name="mrj6h"></a>
## 前提条件
1. SpringBoot 版本 >= 2.0.0
2. SOFABoot >= 3.9

<a name="EmaQ2"></a>
## 接入步骤
<a name="A2kxP"></a>
### 修改 application.properties
```properties
# 需要定义应用名
spring.application.name = ${替换为实际模块名}
```
<a name="HOwyD"></a>
### 添加模块打包插件
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
</plugins>
```
<a name="PumLP"></a>
### 模块瘦身：模块里的依赖如果已经里也有，则将模块的该依赖 scope 设置成 provided，如
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter</artifactId>
    <scope>provided</scope>
</dependency>
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-web</artifactId>
<scope>provided</scope>
</dependency>
<dependency>
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-logging</artifactId>
<scope>provided</scope>
</dependency>
```
如果不设置成 provided，会出现报错[如果模块独立引入 SpringBoot 框架部分会怎样？](/docs/faq/import-full-springboot-in-module)
<a name="BBCza"></a>
### 构建成模块 jar 包
执行 `mvn clean package -DskipTest`, 可以在 target 目录下找到打包生成的 ark biz jar 包。

<a name="znPA9"></a>
## 校验内容
<a name="ufgZF"></a>
### 本地启动
普通应用改造成模块之后，还是可以独立启动，可以验证一些基本的启动逻辑，只需要在启动配置里勾选自动添加 `provided`scope 到 classPath 即可，后启动方式与普通应用方式一致。<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695032642009-a5248a99-d91b-4420-b830-600b35eaa402.png#clientId=u4eb3445f-d3dc-4&from=paste&height=606&id=ued085b28&originHeight=1212&originWidth=1676&originalType=binary&ratio=2&rotation=0&showTitle=false&size=169283&status=done&style=none&taskId=u78d21e68-c71c-42d1-ac4c-8b41381bfa4&title=&width=838)
<a name="tLuMm"></a>
### 部署到基座上

1. **启动上一个实验部署的基座**
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

5. 卸载模块
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

6. 查看卸载后模块列表
```json
curl --location --request POST 'localhost:1238/queryAllBiz'
```
返回信息，只有一个基座（mainClass = embed main）, 没有刚刚安装的模块，表示卸载已经成功
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

<a name="fm1IU"></a>
## 自动化改造
SOFAServerless 会在 Arkctl 提供自动化改造能力。10 月底会支持一键将 SpringBoot 应用改造为模块。

<br/>
<br/>
