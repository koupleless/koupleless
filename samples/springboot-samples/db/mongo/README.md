# 基座与模块使用 mongodb

## 实验内容
1. 模块独立使用不同的 mongodb collection
2. 模块复用基座的 mongodb collection

## 实验应用
### 配置 mongo db 环境

```shell
docker pull mongo:7.0.2-jammy
docker run --name mongodb -d -p 27017:27017 -v $(pwd)/data:/data/db ${mongodb_image}
```

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

<!-- log4j2 相关依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>

<!-- mongo 相关，通信包和springboot starter -->
<dependency>
    <groupId>com.alipay.sofa.db.mongo</groupId>
    <artifactId>base-mongo-facade</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
<!-- end -->
```

### biz
biz 包含两个模块，分别为 biz1 和 biz2, 都是普通 springboot，修改打包插件方式为 sofaArk biz 模块打包方式，打包为 ark biz jar 包，打包插件配置如下：
```xml
<!-- 引入 mongodb 依赖，通过设置 scope=provided 委托给基座 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
    <scope>provided</scope>
</dependency>

<!-- 引入模块 starter，主要用于和基座通信 -->
<dependency>
    <groupId>com.alipay.sofa.serverless</groupId>
    <artifactId>sofa-serverless-app-starter</artifactId>
    <scope>provided</scope>
</dependency>

<!-- 引入和基座通信的通信包 -->
<dependency>
    <groupId>com.alipay.sofa.db.mongo</groupId>
    <artifactId>base-mongo-facade</artifactId>
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



## 实验任务
### 执行 mvn clean package -DskipTests
可在各 bundle 的 target 目录里查看到打包生成的 ark-biz jar 包
### 启动基座应用 base，确保基座启动成功
idea 里正常启动即可
### 执行 curl 命令安装 biz1 和 biz2
```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///path/to/springboot-samples/samples/web/tomcat/biz1/target/biz1-mongo-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz2",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///path/to/springboot-samples/samples/web/tomcat/biz2/target/biz2-mongo-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

如果想验证卸载也可以执行
```shell
curl --location --request POST 'localhost:1238/uninstallBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1",
    "bizVersion": "0.0.1-SNAPSHOT"
}'
```
### 模块独立使用 mongo db
1. 模块1 插入数据到 collection order
```shell
curl --location --request POST 'localhost:8080/biz1/add' \
--header 'Content-Type: application/json' \
--data '{
    "title": "order1",
    "content": "dafdfadaf"
}'
```
2. 查询模块1 插入的数据
```shell
curl --location --request GET 'localhost:8080/biz1/listOrders'
```
返回
```json
[
    {
        "id": "654854ecd0bd140975f40cf7",
        "title": null,
        "content": null
    },
    {
        "id": "654855abeaf8d711d87b5621",
        "title": "order1",
        "content": "dafdfadaf"
    },
    {
        "id": "6548594fd43af222223aa230",
        "title": "order1",
        "content": "dafdfadaf"
    },
    {
        "id": "65486741cb71c22f0dfe1e7b",
        "title": "order1",
        "content": "dafdfadaf"
    }
]
```


3. 模块2 插入数据到 collection user
```shell
curl --location --request POST 'localhost:8080/biz2/add' \
--header 'Content-Type: application/json' \
--data '{
    "name": "me",
    "age": 10,
    "gender": "male"
}'
```

4. 查询模块2 插入的数据
```shell
curl --location --request GET 'localhost:8080/biz2/listUsers'
```

返回
```json
[
    {
        "id": "6548592fd43af222223aa22f",
        "name": "me",
        "age": 10,
        "gender": "male"
    },
    {
        "id": "6548af2009609217c4172269",
        "name": "me",
        "age": 10,
        "gender": "male"
    }
]
```

### 模块复用基座数据源
1. 从基座插入几条 commonModule
```shell
curl --location --request POST 'localhost:8080/add' \
--header 'Content-Type: application/json' \
--data '{
    "name": "common1"
}'
```

```shell
curl --location --request POST 'localhost:8080/add' \
--header 'Content-Type: application/json' \
--data '{
    "name": "common2"
}'
```

2. 从模块1 里复用基座数据源
```shell
curl --location --request GET 'localhost:8080/biz1/listCommons'
```
返回
```json
[
    {
        "id": "6548693baa1123089d0e695f",
        "name": null
    },
    {
        "id": "65489a1f09609217c4172268",
        "name": "common2"
    }
]
```

3. 从模块2 里复用基座数据源
```shell
curl --location --request GET 'localhost:8080/biz2/listCommons'
```
返回
```json
[
    {
        "id": "6548693baa1123089d0e695f",
        "name": null
    },
    {
        "id": "65489a1f09609217c4172268",
        "name": "common2"
    }
]
```

## 注意事项
这里主要使用简单应用做验证，如果复杂应用，需要注意模块做好瘦身，基座有的依赖，模块尽可能设置成 provided，尽可能使用基座的依赖。