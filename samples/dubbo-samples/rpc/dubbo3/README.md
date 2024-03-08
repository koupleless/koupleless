<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

# Use Dubbo 3.x in module
## Supported features
| provider/consumer | protocol |           serialize           | support or not? |
|:-----------------:|:--------:|:-----------------------------:|:---------------:|
|     provider      |   tri    |           hessian2            |       yes       |
|     consumer      |   tri    |           hessian2            |       yes       |
|     provider      |   tri    |           fastjson2           |       yes       |
|     consumer      |   tri    |           fastjson2           |       no        |
|     provider      |   tri    | java/compactedjava/nativejava |       no        |
|     consumer      |   tri    | java/compactedjava/nativejava |       no        |

## Start base
Base is build from a normal dubbo application, the only thing you need to do is add the following dependencies in the main pom.xml
```xml
<!-- 这里添加动态模块相关依赖 -->
<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-base-starter</artifactId>
</dependency>
<!-- end 动态模块相关依赖 -->
```

If the module is a web application and you want to deploy the module in the same tomcat host with base, add the following dependency. For more details, please refer to [here](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-multi-web-component-deploy/)
```xml
<!-- 这里添加 tomcat 单 host 模式部署多web应用的依赖, 非 web 应用可忽略 -->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>web-ark-plugin</artifactId>
</dependency>
        <!-- end 单 host 部署的依赖 -->
```

Here base publish a RPC service
```shell
base/com.alipay.sofa.rpc.dubbo3.model.CommonService
```

引用了模块的RPC，JVM服务
and reference a RPC service and a JVM service from module

```java
 /**
     * 通过远程rpc调用模块triplebiz的CommonService服务
     * check = false是因为启动时还没安装模块
     */
    @DubboReference(group = "triplebiz", scope = Constants.SCOPE_LOCAL, check = false)
    private CommonService commonServiceLocal;

    /**
     * 通过远程rpc调用模块triplebiz的CommonService服务
     * check = false是因为启动时还没安装模块
     */
    @DubboReference(group = "triplebiz", scope = Constants.SCOPE_REMOTE, check = false)
    private CommonService commonServiceRemote;

```

## How to use tri protocol in module: triplebiz
### Add module packaging plugin

This is a module application that is dynamically installed to the base, and it is also a normal dubbo application. You only need to modify the packaging plugin to make it a ark biz module that can be merged and deployed.
```xml
<plugin>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>sofa-ark-maven-plugin</artifactId>
    <version>${sofa.ark.version}</version>
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
        <bizName>triplebiz</bizName>
        <webContextPath>/triplebiz</webContextPath>
        <declaredMode>true</declaredMode>
        <!--					打包、安装和发布 ark biz-->
        <!--					静态合并部署需要配置-->
        <!--					<attach>true</attach>-->
    </configuration>
</plugin>
```
### Reuse base dependencies
Furthermore, the module also sets the dependencies as provided scope which is imported in base, so that the biz can reused the libs like model, dubbo etc in the base as much as possible.

```xml
<!--和基座通信-->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>common-model</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>

<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>${protobuf.version}</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.apache.dubbo</groupId>
    <artifactId>dubbo-dependencies-zookeeper</artifactId>
    <scope>provided</scope>
    <type>pom</type>
    <exclusions>
        <exclusion>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-reload4j</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.apache.dubbo</groupId>
    <artifactId>dubbo-spring-boot-starter</artifactId>
    <scope>provided</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <scope>provided</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
    <scope>provided</scope>
</dependency>
```
### Isolate module log path from base
- To make the base and module log print to different directories, the base and module also introduce log4j2 adapter.
```xml
<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-adapter-log4j2</artifactId>
    <version>${koupleless.runtime.version}</version>
    <scope>provided</scope>
</dependency>
```

### Testing code

Here the module RPCController references the RPC/JVM service defined in the module or base.

```java
 /**
     * tri协议，远程调用，默认走hessian序列化
     */
    @DubboReference(group = "triplebiz", scope = "remote")
    private DemoService demoService;

    /**
     * tri协议，injvm调用，scope默认走injvm
     */
    @DubboReference(group = "base")
    private CommonService commonServiceInJvm;
```

### Run the code
1. cd into `koupleless/samples/dubbo-samples/rpc/dubbo3/`
2. run `mvn clean install -DskipTests`
3. start zookeeper (for Dubbo service registration)
```shell
docker run -p 2181:2181 -it --name zookeeper --restart always -d zookeeper:3.9.0
```
4. start base application Dubbo3BaseApplication.java
5. run curl command to install triplebiz, the bizUrl support local file by `file://` and also remote url

```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "triplebiz",
    "bizVersion": "0.0.1-SNAPSHOT",
    "bizUrl": "file:////path/to/project/koupleless/samples/dubbo-samples/rpc/dubbo3/triplebiz/target/triplebiz-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

If you want to verify the hot deployment capability, you can also uninstall the module through the uninstall command, and then reinstall the module many times.

```shell
curl --location --request POST 'localhost:1238/uninstallBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "triplebiz",
    "bizVersion": "0.0.1-SNAPSHOT"
}'
```

5. check if the module is installed successfully
```shell
curl --location --request POST 'localhost:1238/queryAllBiz'
```
You can see the list of all installed modules

6. verify the RPC/JVM call of the module
Remoting calling the tri service published by the module itself
```shell
curl localhost:8080/triplebiz/remote
```
return
```shell
com.alipay.sofa.rpc.dubbo3.triplebiz.service.DemoServiceImpl: Hello,trpilebiz
```
Injvm calling the tri service published by the base
```shell
curl localhost:8080/triplebiz/injvm
```
return
```shell
com.alipay.sofa.rpc.dubbo3.base.service.BaseCommonService: Hello,triplebiz
```
7. verify the RPC/JVM call of the base
The base calls the injvm service published by the triplebiz module
```shell
curl http://localhost:8080/base/triplebiz/injvm
```
return
```shell
com.alipay.sofa.rpc.dubbo3.triplebiz.service.TripleBizCommonService: Hello,base
```
The base calls the rpc service published by the triplebiz module
```shell
curl http://localhost:8080/base/triplebiz/remote
```
return
```shell
com.alipay.sofa.rpc.dubbo3.triplebiz.service.TripleBizCommonService: Hello,base
```
