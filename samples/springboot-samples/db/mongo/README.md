<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

# Using mongobd in base and module

## Experiment
1. using different mongodb collection in different modules
2. using the same mongodb collection from base in different modules

## Experiment Application
### config mongo db environment

```shell
docker pull mongo:7.0.2-jammy
docker run --name mongodb -d -p 27017:27017 -v $(pwd)/data:/data/db ${mongodb_image}
```

### base
The base is built from regular SpringBoot application. The only change you need to do is to add the following dependencies in pom

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
<artifactId>web-ark-plugin</artifactId>
</dependency>
<!-- end of dependencies for single host deployment -->

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>

<!-- add mongo and facade dependencies here -->
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
The biz contains two modules, biz1 and biz2, both are regular SpringBoot. The packaging plugin method is modified to the sofaArk biz module packaging method, packaged as an ark biz jar package, and the packaging plugin configuration is as follows:

```xml
<!-- add mongodb dependency, and set scope=provided to delegate to base -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
    <scope>provided</scope>
</dependency>

<!-- add koupleless app starter for communication with base -->
<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-app-starter</artifactId>
    <scope>provided</scope>
</dependency>

<!-- add communication package for communication with base -->
<dependency>
    <groupId>com.alipay.sofa.db.mongo</groupId>
    <artifactId>base-mongo-facade</artifactId>
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
        <webContextPath>${bizName}</webContextPath>
        <declaredMode>true</declaredMode>
    </configuration>
</plugin>
```
Note that the web context path of different biz is changed to different values, so that multiple web applications can be successfully installed in a tomcat host.

## Experiment Task
### run `mvn clean package -DskipTests`
we can check the ark-biz jar package in target directory of each bundle

### start base application, and make sure base is started successfully
start base application in idea as a regular springboot application
### execute curl command to install biz1 and biz2

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

If you want to verify hot deployment, you can uninstall and deploy multiple times
```shell
curl --location --request POST 'localhost:1238/uninstallBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1",
    "bizVersion": "0.0.1-SNAPSHOT"
}'
```
### using mongo db in module
1. insert data to collection order in module1
```shell
curl --location --request POST 'localhost:8080/biz1/add' \
--header 'Content-Type: application/json' \
--data '{
    "title": "order1",
    "content": "dafdfadaf"
}'
```
2. query data from collection order in module1
```shell
curl --location --request GET 'localhost:8080/biz1/listOrders'
```
return 
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

3. insert data to collection user in module2
```shell
curl --location --request POST 'localhost:8080/biz2/add' \
--header 'Content-Type: application/json' \
--data '{
    "name": "me",
    "age": 10,
    "gender": "male"
}'
```

4. query data from collection user in module2
```shell
curl --location --request GET 'localhost:8080/biz2/listUsers'
```

return
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

### reuse base mongodb collection in module
1. insert some commonModule from base
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

2. query data from collection user in module1
```shell
curl --location --request GET 'localhost:8080/biz1/listCommons'
```
return
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

3. query data from collection user in module2
```shell
curl --location --request GET 'localhost:8080/biz2/listCommons'
```
return
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

## Precautions
Here mainly use simple applications for verification, if complex applications, need to pay attention to the module to do a good job of slimming, the base has dependencies, the module as much as possible set to provided, as much as possible to use the base dependencies.
