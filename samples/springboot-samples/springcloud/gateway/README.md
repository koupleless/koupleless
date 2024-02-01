<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

# Experiment
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
    <artifactId>netty-ark-plugin</artifactId>
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
        <packExcludesConfig>rules.txt</packExcludesConfig>
    </configuration>
</plugin>
```
Note that the web context path of different biz is changed to different values, so that multiple web applications can be successfully installed in a tomcat host.

## Experiment steps

### start base and deploy bizs
#### run `mvn clean package -DskipTests`
we can check the ark-biz jar package in the target directory of each bundle

#### start base application, make sure base is started successfully
```shell
# add start params
-Dspring.jmx.default-domain=${spring.application.name}
```
#### execute curl command to install biz1 and biz2
cd into gateway directory
```shell
cd samples/springboot-samples/springcloud/gateway
```
install biz1 and biz2 by arkctl
```shell
arkctl deploy biz1/target/biz1-cloud-gateway-0.0.1-SNAPSHOT-ark-biz.jar
arkctl deploy biz2/target/biz2-cloud-gateway-0.0.1-SNAPSHOT-ark-biz.jar
```

#### WebSocket testing
1. start 3 different port webSocket server
```shell
npm install -g wscat
wscat --listen 9000 # 基座 websocket 端口
wscat --listen 9001 # biz1 websocket 端口
wscat --listen 9002 # biz2 websocket 端口
```
2. start webSocket client to test
```shell
wscat --connect ws://localhost:8080/echo
```
input any character, only the webSocket server belongs to base can echo the same character

```shell
wscat --connect ws://localhost:8080/biz1/echo
```
input any character, only the webSocket server belongs to biz1 9001 port can echo the same character

```shell
wscat --connect ws://localhost:8080/biz2/echo
```
input any character, only the webSocket server belongs to biz2 9002 port can echo the same character
