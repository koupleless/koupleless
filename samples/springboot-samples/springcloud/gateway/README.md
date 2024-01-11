# 实验内容
## 实验应用
### base
base 为普通 springboot cloud改造成的基座，改造内容为在 pom 里增加如下依赖
```xml
<!--    务必将次依赖放在构建 pom 的第一个依赖引入, 并且设置 type= pom, 
    原理请参考这里 https://sofaserverless.gitee.io/docs/contribution-guidelines/runtime/multi-app-padater/ -->
<dependency>
    <groupId>com.alipay.sofa.serverless</groupId>
    <artifactId>sofa-serverless-base-starter</artifactId>
    <version>${sofa.serverless.runtime.version}</version>
    <type>pom</type>
</dependency>

<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>netty-ark-plugin</artifactId>
</dependency>
```

### biz
biz 包含两个模块，分别为 biz1 和 biz2, 都是普通 springboot cloud，修改打包插件方式为 sofaArk biz 模块打包方式，打包为 ark biz jar 包，打包插件配置如下：

```xml
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
        <packExcludesConfig>rules.txt</packExcludesConfig>
    </configuration>
</plugin>
```
注意这里将不同 biz 的web context path 修改成不同的值，以此才能成功在一个 netty host 里安装多个 web 应用。

## 实验步骤

### 基座启动与模块部署
#### 执行 mvn clean package -DskipTests
可在各 bundle 的 target 目录里查看到打包生成的 ark-biz jar 包

#### 启动基座应用 base，确保基座启动成功
#### 执行 curl 命令安装 biz1 和 biz2
到 gateway 目录
```shell
cd samples/springboot-samples/springcloud/gateway
```
通过 arkctl 部署模块 1 和模块 2
```shell
arkctl deploy biz1/target/biz1-cloud-gateway-0.0.1-SNAPSHOT-ark-biz.jar
arkctl deploy biz2/target/biz2-cloud-gateway-0.0.1-SNAPSHOT-ark-biz.jar
```

#### WebSocket 测试
1. 启动 3个不同端口的 webSocket 服务端
```shell
npm install -g wscat
wscat --listen 9000 # 基座 websocket 端口
wscat --listen 9001 # biz1 websocket 端口
wscat --listen 9002 # biz2 websocket 端口
```
2. 启动 webSocket 客户端链接测试
```shell
wscat --connect ws://localhost:8080/echo
```
输入任意字符， 只有属于基座的 webSocket 服务终端能 echo 出相同字符

```shell
wscat --connect ws://localhost:8080/biz1/echo
```
输入任意字符，只有属于 biz1 9001 端口的 webSocket 服务终端能 echo 出相同字符

```shell
wscat --connect ws://localhost:8080/biz2/echo
```
输入任意字符，只有属于 biz2 9002 端口的 webSocket 服务终端能 echo 出相同字符
