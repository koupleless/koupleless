<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

# Experiment: Using webflux in both Base and Module

## Background
we support two kinds of mode for base and module merge deploy in one process:

### multi host mode
1. set `server.port` to different port for base and module
2. access web service of base and module by different port

### single host mode
1. set `server.prot` to same port for base and biz, like `8080`
2. access web service of base and module with same port but different context path, like `/base` and `/biz`

Here we will verify the single host mode with multi web context path

## Experiment application
### base
The base is built from regular SpringBoot application. The only change you need to do is to add the following dependencies in pom. Note that the netty-ark-plugin version must be >= 2.2.5

```xml
<!-- Add dynamic module related dependencies here -->
<!--    Be sure to put this dependency as the first dependency in the build pom, and set type= pom,
    The principle can be found here https://koupleless.gitee.io/docs/contribution-guidelines/runtime/multi-app-padater/ -->
<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-base-starter</artifactId>
    <version>${koupleless.runtime.version}</version>
    <type>pom</type>
</dependency>
<!-- end of dynamic module related dependencies -->

<!-- Add dependencies for deploying multiple web applications in tomcat single host mode here -->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>netty-ark-plugin</artifactId>
</dependency>
<!-- end of dependencies for single host deployment -->

<!-- begin spring boot 3 webflux -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
    <version>3.2.1</version>
</dependency>
<!-- end spring boot webflux -->
```

### biz
biz is built from regular SpringBoot, which change packing plugin to sofa ark maven plugin, and the packaging plugin configuration is as follows:
```xml
<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-app-starter</artifactId>
    <scope>provided</scope>
</dependency>

<!-- change the packaging plugin to sofa-ark biz packaging plugin, packaged as ark biz jar -->
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
        <bizName>${bizName}</bizName>
        <!-- single host mode, need to change web context path -->
        <webContextPath>/${bizName}</webContextPath>
        <declaredMode>true</declaredMode>
    </configuration>
</plugin>
```
Note that the web context path of different biz is changed to different values, so that multiple web applications can be successfully installed in a netty host.

## Experiment steps

### start base
just run the base application in IDEA like a regular SpringBoot application

### packaging module application biz

executing `mvn clean package -DskipTest=true` in `samples/springboot-samples/web/webflux/biz` directory to package biz, and you can find the ark-biz jar package in the target directory of each bundle

### install module application biz
#### execute curl command to install biz

```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///Users/xxxxx/koupleless/samples/springboot-samples/web/webflux/biz/target/bizwebflux-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

### start verification request
#### 1. verify services in base

access web service in base
```shell
curl http://localhost:8080/hello
```
return `Hello, city!`
```shell
curl curl http://localhost:8080/village
```
return `Hello, village`

且日志里能看到调用成功返回
and also we can check the success log in base

#### 2. versify services in module

access web services in biz, since it is single host mode, module service is also published on port 8080, we need to add <webContextPath>/${bizName}</webContextPath> configured in module packaging plugin as prefix to access
```shell
curl http://localhost:8080/biz/biz
```
return `Hello, biz webflux!`

and also we can check the success log in biz

#### 3. verify base services after uninstalling module, module services can no longer be accessed

access web services in base
```shell
curl http://localhost:8080/hello
```
return `Hello, city!`

access web services in biz
```shell
curl http://localhost:8080/biz/biz
```
return  `{"timestamp":"2023-11-22T08:56:11.508+00:00","path":"/biz/biz","status":404,"error":"Not Found","message":null,"requestId":"a7917dd5-6"}`
this means module service can no longer be accessed, "status":404

#### 4. verify base services after reinstalling module, module services can be accessed successfully

#### execute curl command to install biz

```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///Users/xxxxx/koupleless/samples/springboot-samples/web/webflux/biz/target/bizwebflux-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```
access web services in base
```shell
curl http://localhost:8080/hello
```
return `Hello, city!`

access web services in biz
```shell
curl http://localhost:8080/biz/biz
```
return `Hello, biz webflux!`


## Precautions
1. require netty-ark-plugin version >= 2.2.5 when using single host mode with multi web context path
2. support module uninstall first, then install, but not support install first, then uninstall
3. support install and uninstall modules multiple times