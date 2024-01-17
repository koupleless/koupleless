# 基座与模块使用 rocketmqdb

## 实验内容
1. 模块独立使用不同的 rocketmq 生产和消费
2. 模块复用基座的 rocketmq 生产和消费

## 实验应用
### 配置 rocketmq db 环境

```shell
docker pull apache/rocketmq:4.9.7
```

```shell
## ref https://juejin.cn/post/7109082879589613575
# start nameServer, 默认端口为 -p 9876:9876
docker run -d -p 9876:9876 -p 10909:10909 -p 10910:10910 -p 10911:10911 -p 10912:10912 -v $(pwd)/config/start.sh:/home/rocketmq/rocketmq-4.9.7/bin/start.sh -v $(pwd)/config/broker.conf:/home/rocketmq/rocketmq-4.9.7/bin/broker.conf apache/rocketmq:4.9.7 sh /home/rocketmq/rocketmq-4.9.7/bin/start.sh
```

### base
base 为普通 springboot 改造成的基座，改造内容为在 pom 里增加如下依赖
```xml
<!-- 这里添加动态模块相关依赖 -->
<!--    务必将次依赖放在构建 pom 的第一个依赖引入, 并且设置 type= pom, 
    原理请参考这里 https://sofaserverless.gitee.io/docs/contribution-guidelines/runtime/multi-app-padater/ -->
<dependency>
    <groupId>com.alipay.sofa.serverless</groupId>
    <artifactId>sofa-serverless-base-starter</artifactId>
    <version>${sofa.serverless.runtime.version}</version>
    <type>pom</type>
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

<!-- rocketmq 相关，通信包和springboot starter -->
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
biz1 是普通 springboot，修改打包插件方式为 sofaArk biz 模块打包方式，打包为 ark biz jar 包，打包插件配置如下：
```xml
<!-- 引入 rocketmqdb 依赖，通过设置 scope=provided 委托给基座 -->
<dependency>
    <groupId>org.apache.rocketmq</groupId>
    <artifactId>rocketmq-spring-boot-starter</artifactId>
    <version>2.0.2</version>
    <scope>provided</scope>
</dependency>

<!-- 引入和基座通信的通信包 -->
<dependency>
    <groupId>com.alipay.sofa.db.rocketmq</groupId>
    <artifactId>base-rocketmq-facade</artifactId>
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
    "bizUrl": "file:///path/to/springboot-samples/samples/web/tomcat/biz1/target/biz1-rocketmq-0.0.1-SNAPSHOT-ark-biz.jar"
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
### 基座生产消息，基座与模块同时消费
1. 基座生产消息
```shell
curl http://localhost:8080/send/dfadfsdfa
```

可以看到基座和模块都消费到了消息
```text
INFO  service.SampleProducer - base producer: dfadfsdfa
INFO  service.SampleConsumer - =================================
INFO  service.SampleConsumer - base receive a message: Greeting(message=base send: dfadfsdfa)
INFO  service.SampleConsumer - =================================
INFO  service.SampleConsumer - biz1 receive a message: Greeting(message=base send: dfadfsdfa)
```

2. 模块生产消息，基座和模块同时消费
```shell
curl http://localhost:8080/biz1/send/dfadfsdfa
```
可以看到基座和模块都消费了消息

```text
INFO  service.SampleProducer - biz1 producer: dfadfsdfa
INFO  service.SampleConsumer - =================================
INFO  service.SampleConsumer - biz1 receive a message: Greeting(message=biz1 send: dfadfsdfa)
INFO  service.SampleConsumer - =================================
INFO  service.SampleConsumer - base receive a message: Greeting(message=biz1 send: dfadfsdfa)
```

## 注意事项
这里主要使用简单应用做验证，如果复杂应用，需要注意模块做好瘦身，基座有的依赖，模块尽可能设置成 provided，尽可能使用基座的依赖。
