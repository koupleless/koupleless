<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

# Using rocketmqdb in base and module

## Experiment 
1. using different rocketmq producer and consumer in different modules
2. using the same rocketmq producer and consumer from base in different modules

## 实验应用
### 配置 rocketmq db 环境

```shell
docker pull apache/rocketmq:4.9.7
```

```shell
## ref https://juejin.cn/post/7109082879589613575
# start nameServer, default server port 为 -p 9876:9876
docker run -d -p 9876:9876 -p 10909:10909 -p 10910:10910 -p 10911:10911 -p 10912:10912 -v $(pwd)/config/start.sh:/home/rocketmq/rocketmq-4.9.7/bin/start.sh -v $(pwd)/config/broker.conf:/home/rocketmq/rocketmq-4.9.7/bin/broker.conf apache/rocketmq:4.9.7 sh /home/rocketmq/rocketmq-4.9.7/bin/start.sh
```

### base
The biz contains two modules, biz1 and biz2, both are regular SpringBoot. The packaging plugin method is modified to the sofaArk biz module packaging method, packaged as an ark biz jar package, and the packaging plugin configuration is as follows:

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

<!-- add rocketmq and facade dependencies here -->
<dependency>
    <groupId>com.alipay.sofa.db.rocketmq</groupId>
    <artifactId>base-rocketmq-facade</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.rocketmq</groupId>
    <artifactId>rocketmq-spring-boot-starter</artifactId>
    <version>2.0.2</version>
</dependency>
<!-- end -->
```

### biz
biz1 is built from regular SpringBoot application. The packaging plugin method is modified to the sofaArk biz module packaging method, packaged as an ark biz jar package, and the packaging plugin configuration is as follows:
```xml
<!-- add rocketmqdb dependency, and set scope=provided to delegate to base -->
<dependency>
    <groupId>org.apache.rocketmq</groupId>
    <artifactId>rocketmq-spring-boot-starter</artifactId>
    <version>2.0.2</version>
    <scope>provided</scope>
</dependency>

<!-- add facade dependencies for communication with base -->
<dependency>
    <groupId>com.alipay.sofa.db.rocketmq</groupId>
    <artifactId>base-rocketmq-facade</artifactId>
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

## Experiment task
### run `mvn clean package -DskipTests`
we can check the ark-biz jar package in target directory of each bundle
### start base application, and make sure base start successfully
just start in idea like a regular springboot application
### execute curl command to install biz1 and biz2
```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///path/to/springboot-samples/samples/web/tomcat/biz1/target/biz1-rocketmq-0.0.1-SNAPSHOT-ark-biz.jar"
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
### produce message in the base, and base and module consume message at the same time
1. produce message in the base
```shell
curl http://localhost:8080/send/dfadfsdfa
```

we can check that both base and module consume the message
```text
INFO  service.SampleProducer - base producer: dfadfsdfa
INFO  service.SampleConsumer - =================================
INFO  service.SampleConsumer - base receive a message: Greeting(message=base send: dfadfsdfa)
INFO  service.SampleConsumer - =================================
INFO  service.SampleConsumer - biz1 receive a message: Greeting(message=base send: dfadfsdfa)
```

2. produce message in the module, and base and module consume message at the same time
```shell
curl http://localhost:8080/biz1/send/dfadfsdfa
```
we can check that both base and module consume the message

```text
INFO  service.SampleProducer - biz1 producer: dfadfsdfa
INFO  service.SampleConsumer - =================================
INFO  service.SampleConsumer - biz1 receive a message: Greeting(message=biz1 send: dfadfsdfa)
INFO  service.SampleConsumer - =================================
INFO  service.SampleConsumer - base receive a message: Greeting(message=biz1 send: dfadfsdfa)
```

## Precautions
Here mainly use simple applications for verification, if complex applications, need to pay attention to the module to do a good job of slimming, the base has dependencies, the module as much as possible set to provided, as much as possible to use the base dependencies.
