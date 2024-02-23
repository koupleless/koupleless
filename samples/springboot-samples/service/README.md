<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

# Experiment: Base and Module call each other
## Experiment application
### base
The base is built from regular SpringBoot application. The only change you need to do is to add the following dependencies in pom

```xml
<!-- Add dynamic module related dependencies here -->
<!--    Be sure to put this dependency as the first dependency in the build pom, and set type= pom,
    The principle can be found here https://koupleless.gitee.io/docs/contribution-guidelines/runtime/multi-app-padater/ -->
<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-base-starter</artifactId>
    <!--    <version>${koupleless.runtime.version}</version>-->
    <version>1.0.0</version>
    <type>pom</type>
</dependency>
<!-- end of dynamic module related dependencies -->

<!-- Add dependencies for deploying multiple web applications in tomcat single host mode here -->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>web-ark-plugin</artifactId>
</dependency>
<!-- end of dependencies for single host deployment -->
```

### biz
The biz contains two modules, biz1 and biz2, both are regular SpringBoot. The packaging plugin method is modified to the sofaArk biz module packaging method, packaged as an ark biz jar package, and the packaging plugin configuration is as follows:

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
        <webContextPath>${bizName}</webContextPath>
        <declaredMode>true</declaredMode>
    </configuration>
</plugin>
```
Note that the web context path of different biz is changed to different values, so that multiple web applications can be successfully installed in a tomcat host.

## Experiment steps

### start base application, and make sure it is started successfully
just start in idea like a regular springboot application

### package module application biz1、biz2

packaging in `samples/springboot-samples/service/sample-service-biz` and `samples/springboot-samples/service/sample-service-biz2` directory by executing `mvn clean package -Dmaven.test.skip=true`, then we can check the generated ark-biz jar in each bundle target

### install module application biz1、biz2

#### execute curl command to install biz1

```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///Users/xxxx/xxxx/Code/koupleless/samples/springboot-samples/service/sample-service-biz/biz-bootstrap/target/biz-bootstrap-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

#### execute curl command to install biz2

```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz2",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///Users/xxxx/xxxx/Code/koupleless/samples/springboot-samples/service/sample-service-biz2/biz2-bootstrap/target/biz2-bootstrap-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

### start verification request

#### verify call module from base

access web service of base
```shell
curl http://localhost:8080
```
return `hello to ark master biz`

we can check that the call to module biz is successful in the log, which proves that the base calls the module successfully through SpringServiceFinder.getModuleService

#### verify call base from module

access web service of biz1
```shell
curl http://localhost:8080/biz1/
```
return `hello to ark biz1 dynamic deploy`

we can check that the call to base is successful in the log, which proves that the module calls the base successfully through @AutowiredFromBase or SpringServiceFinder.getBaseService()

#### verify call module from module

access web service of biz2
```shell
curl http://localhost:8080/biz2/
```
return `hello to ark biz2 dynamic deploy`

we can check that the call to module biz is successful in the log, which proves that the module calls the module biz successfully through @AutowiredFromBiz or SpringServiceFinder.getModuleService()

## Precautions

1. Here mainly use simple applications for verification, if complex applications, need to pay attention to the module to do a good job of slimming, the base has dependencies, the module as much as possible set to provided, as much as possible to use the base dependencies.


2. When verifying the module function here, the web interface needs to be followed by a slash, for example, curl http://localhost:8080/biz1/, instead of http://localhost:8080/biz1

