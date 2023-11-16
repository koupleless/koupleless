
# 实验内容  模块与基座相互调用
## 实验应用
### base
base 为普通 springboot 改造成的基座，改造内容为在 pom 里增加如下依赖
```xml

<!-- 这里添加动态模块相关依赖 -->
<dependency>
    <groupId>com.alipay.sofa.serverless</groupId>
    <artifactId>sofa-serverless-base-starter</artifactId>
</dependency>
<!-- end 动态模块相关依赖 -->

<!-- 这里添加 tomcat 单 host 模式部署多web应用的依赖 -->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>web-ark-plugin</artifactId>
</dependency>
<!-- end 单 host 部署的依赖 -->

```

### biz
biz 包含两个模块，分别为 biz1 和 biz2, 都是普通 springboot，修改打包插件方式为 sofaArk biz 模块打包方式，打包为 ark biz jar 包，打包插件配置如下：
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
        <!-- 单host下需更换 web context path -->
        <webContextPath>${bizName}</webContextPath>
        <declaredMode>true</declaredMode>
    </configuration>
</plugin>
```
注意这里将不同 biz 的web context path 修改成不同的值，以此才能成功在一个 tomcat host 里安装多个 web 应用。


## 实验步骤

### 启动基座应用 base

可以使用 IDEA run 启动基座应用

### 打包模块应用 biz1、biz2

在samples/springboot-samples/service/sample-service-biz 和 samples/springboot-samples/service/sample-service-biz2 目录下分别执行 mvn clean package -Dmaven.test.skip=true 进行模块打包， 打包完成后可在各 bundle 的 target 目录里查看到打包生成的 ark-biz jar 包

### 安装模块应用 biz1、biz2

#### 执行 curl 命令安装 biz1

```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///Users/xxxx/xxxx/Code/sofa-serverless/samples/springboot-samples/service/sample-service-biz/biz-bootstrap/target/biz-bootstrap-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

#### 执行 curl 命令安装 biz2

```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz2",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///Users/xxxx/xxxx/Code/sofa-serverless/samples/springboot-samples/service/sample-service-biz2/biz2-bootstrap/target/biz2-bootstrap-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

### 发起请求验证

#### 验证基座调用模块

访问基座 base 的 web 服务
```shell
curl http://localhost:8080
```
返回 `hello to ark master biz`

且日志里能看到对模块biz的调用都是成功的，证明基座通过 SpringServiceFinder.getModuleService 方式调用模块是成功的

#### 验证模块调用基座

访问 biz2 的 web 服务
```shell
curl http://localhost:8080/biz2
```
返回 `hello to ark2 dynamic deploy`

且日志里能看到对基座base的调用都是成功的，证明模块通过 @AutowiredFromBase 或者 SpringServiceFinder.getBaseService() 方式调用基座是成功的

#### 验证模块调用模块

访问 biz2 的 web 服务
```shell
curl http://localhost:8080/biz2
```
返回 `hello to ark2 dynamic deploy`

且日志里能看到对模块biz的调用都是成功的，证明模块通过 @AutowiredFromBiz 或者 SpringServiceFinder.getModuleService 方式调用模块biz是成功的

## 注意事项
这里主要使用简单应用做验证，如果复杂应用，需要注意模块做好瘦身，基座有的依赖，模块尽可能设置成 provided，尽可能使用基座的依赖。

