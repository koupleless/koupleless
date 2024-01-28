<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

# single host mode of tomcat, deploy multiple web applications dynamically
the principle of tomcat single host mode is introduced in detail [here](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-multi-web-component-deploy/)

# Experiment Content
## Experiment Application
### base
The base is built from regular SpringBoot application. The only change you need to do is to add the following dependencies in pom
```xml
<!-- add libs for dynamic module -->
<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-base-starter</artifactId>
    <!-- The above version supports springboot3 -->
    <version>0.5.5-jdk17</version>
</dependency>
<!-- end of dynamic module related dependencies -->

<!-- add dependencies for deploying multiple web applications in single host mode of tomcat here -->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>web-ark-plugin</artifactId>
    <!-- Exclude log-sofa-boot-starter in web-ark-plugin -->
    <exclusions>
        <exclusion>
            <groupId>com.alipay.sofa</groupId>
            <artifactId>log-sofa-boot-starter</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<!-- end of dependencies for single host deployment -->
```

### biz
The biz contains two modules, biz1 and biz2, both are regular SpringBoot. The packaging plugin method is modified to the sofaArk biz module packaging method, packaged as an ark biz jar package, and the packaging plugin configuration is as follows:
```xml
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

## 实验任务
##　Experiment Task
1. Execute `mvn clean package -DskipTests`
2. Start the base application and ensure that the base starts successfully
3. Execute the curl command to install biz1 and biz2
```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///path/to/springboot-samples/samples/web/tomcat/biz1/target/biz1-web-single-host-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz2",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///path/to/springboot-samples/samples/web/tomcat/biz2/target/biz2-web-single-host-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

If you want to verify hot deployment, you can uninstall and deploy multiple times, and then verify whether the request is normal
```shell
curl --location --request POST 'localhost:1238/uninstallBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1",
    "bizVersion": "0.0.1-SNAPSHOT"
}'
```

4. Start a request to verify
```shell
curl http://localhost:8080/biz1
```
return `hello to /biz1 deploy`

```shell
curl http://localhost:8080/biz2
```
return `hello to /biz2 deploy`

说明，单host模式应用多次热部署正常。
This means that the hot deployment of single host mode application succeed.

## Precautions
Here mainly use simple applications for verification, if complex applications, need to pay attention to the module to do a good job of slimming, the base has dependencies, the module as much as possible set to provided, as much as possible to use the base dependencies.