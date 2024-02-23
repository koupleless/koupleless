<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

# Experiment: module slimming by auto excluding dependencies
## precautions
how to slim ark biz jar: delegate the framework, middleware and other common dependencies to base, and reuse the dependencies in base, so that the ark-biz jar will be very small. We recommend to exclude those dependencies in module by slimming configuration (conf/ark/rules.txt).
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

<!-- add log4j2 dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>

<!-- add log4j2 async dependencies -->
<dependency>
    <groupId>com.lmax</groupId>
    <artifactId>disruptor</artifactId>
    <version>${disruptor.version}</version>
</dependency>
<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-log4j2-starter</artifactId>
    <version>${koupleless.runtime.version}</version>
</dependency>
<!-- end of log4j2 -->
```

### biz1
biz1 contains two packaging plugin, one for regular springboot fatjar plugin, another for biz module plugin, packaging plugin configuration is as follows:

**Special Note**： we must import sofa ark maven plugin before spring boot maven plugin;
```xml
<!-- the following plugin configuration is the key content of this experiment -->
<build>
<plugins>
    <!-- plugin1: packaging plugin for sofa-ark biz module, packaged as ark biz jar -->
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
            <bizName>biz1</bizName>
            <!-- packExcludesConfig is for module slimming, file name can be customized,
            for example rlues.txt means config files biz1/conf/ark/rules.txt -->
            <packExcludesConfig>rules.txt</packExcludesConfig>
            <webContextPath>biz1</webContextPath>
            <declaredMode>true</declaredMode>
            <!-- packaging、install and deploy ark biz -->
            <!-- static merge deployment need set attach = true -->
            <!--					<attach>true</attach>-->
        </configuration>
    </plugin>
    <!-- plugin2: packaging plugin for regular springboot fatjar plugin, packaged as regular springboot fatjar -->
    <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
        <configuration>
            <finalName>springboot-application</finalName>
        </configuration>
    </plugin>
</plugins>
</build>
```

## Experiment task
### run `mvn clean package -DskipTests`
可在各 biz1 bundle 的 target 目录里查看到打包生成的 ark-biz jar 包 和 普通 springboot 包, 明显经过模块瘦身的 ark-biz jar 包大小更小
We can found the ark-biz jar package in target directory of each bundle, and also the springboot fatjar. and also the ark-biz jar is much smaller than springboot fatjar.

![img.png](imgs/biz1-target.png)

### start base application, and make sure base start successfully
### execute curl command to install biz1
```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///path/to/springboot-samples/samples/web/tomcat/biz1/target/biz1-log4j2-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

### 发起请求验证
### start verification request
```shell
curl http://localhost:8080/biz1/
```
return `hello to /biz1 deploy`

### stop base application
### start biz1 as regular springboot application
![img.png](imgs/biz1-springboot.png)

### start verification request
```shell
curl http://localhost:8080/
```
return `hello to /biz1 deploy`
![img.png](imgs/biz1-springboot-res.png)
