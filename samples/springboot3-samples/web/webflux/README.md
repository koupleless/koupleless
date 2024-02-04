
# 实验内容：基座、模块使用 webflux

## 背景

基座、模块合并部署，使用 webflux 支持两种模式：

### 多host模式
1. 直接将基座和模块的server.port设置为不同端口
2. 基座、模块通过不同端口访问各自web服务

### 单host模式
1. 基座和模块使用相同的server.port，默认如8080
2. 基座、模块通过相同端口、不同context path访问各自web服务

该实验验证基座、模块采用单host，多context path模式使用webflux

## 实验应用
### base
base 为普通 springboot 改造成的基座，改造内容为在 pom 里增加如下依赖，注意 ⚠️ netty-ark-plugin 版本要求 >= 2.2.5
```xml

<!-- begin sofa-serverless相关依赖 -->
<!--    务必将次依赖放在构建 pom 的第一个依赖引入, 并且设置 type= pom, 
    原理请参考这里 https://sofaserverless.gitee.io/docs/contribution-guidelines/runtime/multi-app-padater/ -->
<dependency>
    <groupId>com.alipay.sofa.serverless</groupId>
    <artifactId>sofa-serverless-base-starter</artifactId>
    <version>${sofa.serverless.runtime.version}</version>
    <type>pom</type>
</dependency>
<!-- end sofa-serverless相关依赖 -->

<!-- begin spring boot webflux 相关依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
<!-- end spring boot webflux 相关依赖 -->

<!-- begin netty 单 host 模式部署多web应用的依赖 -->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>netty-ark-plugin</artifactId>
    <!-- netty-ark-plugin 版本要求 >= 2.2.5 -->
    <version>${sofa.ark.version}</version>
</dependency>
<!-- end netty 单 host 部署的依赖 -->

```

### biz
biz 是普通 springboot，修改打包插件方式为 sofaArk biz 模块打包方式，打包为 ark biz jar 包，打包插件配置如下：
```xml
<dependency>
    <groupId>com.alipay.sofa.serverless</groupId>
    <artifactId>sofa-serverless-app-starter</artifactId>
    <scope>provided</scope>
</dependency>

<!-- 修改打包插件为 sofa-ark biz 打包插件，打包成 ark biz jar -->
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
        <!-- 单host下需更换 web context path, 必须指定，否则将和基座 web context 冲突，导致启动失败 -->
        <webContextPath>/${bizName}</webContextPath>
        <declaredMode>true</declaredMode>
    </configuration>
</plugin>
```
注意这里将不同 biz 的web context path 修改成不同的值，以此才能成功在一个 netty host 里安装多个 web 应用。


## 实验步骤

### 启动基座应用 base

可以使用 IDEA run 启动基座应用

### 打包模块应用 biz

在xx/samples/springboot-samples/web/webflux/biz 目录下执行 mvn clean package -Dmaven.test.skip=true 进行模块打包， 打包完成后可在各 bundle 的 target 目录里查看到打包生成的 ark-biz jar 包

### 安装模块应用 biz

#### 执行 curl 命令安装 biz

```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///Users/xxxxx/sofa-serverless/samples/springboot-samples/web/webflux/biz/target/bizwebflux-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

### 发起请求验证

#### 1. 验证基座服务

访问基座 base 的 web 服务
```shell
curl http://localhost:8080/hello
```
返回 `Hello, city!`
```shell
curl curl http://localhost:8080/village
```
返回 `Hello, village`

且日志里能看到调用成功返回

#### 2. 验证模块服务

访问 biz 的 web 服务，由于是单host模式，模块服务也发布在8080端口，需要在path中添加在模块打包插件中配置的 <webContextPath>/${bizName}</webContextPath> 作为前缀访问
```shell
curl http://localhost:8080/biz/biz
```
返回 `Hello, biz webflux!`

且日志里能看到调用成功返回

#### 3. 验证模块卸载后，基座服务可正常访问，模块服务无法再访问

访问基座 base 的 web 服务
```shell
curl http://localhost:8080/hello
```
返回 `Hello, city!`

访问 biz 的 web 服务
```shell
curl http://localhost:8080/biz/biz
```
返回 `{"timestamp":"2023-11-22T08:56:11.508+00:00","path":"/biz/biz","status":404,"error":"Not Found","message":null,"requestId":"a7917dd5-6"}`
模块服务已经无法再访问，"status":404 

#### 4. 模块重新安装后，基座服务可正常访问，模块新服务可正常访问

#### 执行 curl 命令安装 biz

```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///Users/xxxxx/sofa-serverless/samples/springboot-samples/web/webflux/biz/target/bizwebflux-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```
访问基座 base 的 web 服务
```shell
curl http://localhost:8080/hello
```
返回 `Hello, city!`

访问 biz 的 web 服务
```shell
curl http://localhost:8080/biz/biz
```
返回 `Hello, biz webflux!`


## 注意事项
1. 支持基座、模块合并部署时采用单host多context path需要 netty-ark-plugin 依赖版本 >= 2.2.5
2. 支持模块多次安装、多次卸载
3. 支持模块先卸后装，不支持先装后卸
