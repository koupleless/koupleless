# Dubbo 使用 triple 进行 RPC 调用

## 实验内容
### 实验应用
#### base
base 为普通 dubbo 应用改造而成，改造内容只需在主 pom 里增加如下依赖
```xml
<!-- 这里添加动态模块相关依赖 -->
<dependency>
    <groupId>com.alipay.sofa.serverless</groupId>
    <artifactId>sofa-serverless-base-starter</artifactId>
</dependency>
<!-- end 动态模块相关依赖 -->
```
如果是 web 应用，并且希望后面模块部署与基座使用同一个 tomcat host，则引入如下依赖。详细查看[这里](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-multi-web-component-deploy/)
```xml
<!-- 这里添加 tomcat 单 host 模式部署多web应用的依赖, 非 web 应用可忽略 -->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>web-ark-plugin</artifactId>
</dependency>
        <!-- end 单 host 部署的依赖 -->
```

这里基座发布了 RPC 服务, DemoService

#### biz1
这是动态安装到基座的模块应用，也是普通 dubbo 应用，只需修改打包插件方式即可变成可合并部署的 ark biz 模块
```xml
<plugin>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>sofa-ark-maven-plugin</artifactId>
    <version>2.2.3</version>
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
        <webContextPath>biz1</webContextPath>
        <declaredMode>true</declaredMode>
        <!--					打包、安装和发布 ark biz-->
        <!--					静态合并部署需要配置-->
        <!--					<attach>true</attach>-->
    </configuration>
</plugin>
```
另外模块还额外将基座里有的依赖，设置为了 provided，这样可以尽可能的服用基座的类、rpc client 等。

这里模块在 RPCController 里引用了基座里定义的 RPC 服务。

#### model
改应用只是作为通信包，里面只放了 RPC 通信需要的通信接口类。

另外，为了让基座和模块日志打印到不同的目录下，基座和模块还额外引入了 log4j2 adapter，如果不关心基座和模块日志是否打印在一起还是分开打印，那么这个依赖可以不加。
```xml
<dependency>
    <groupId>com.alipay.sofa.serverless</groupId>
    <artifactId>sofa-serverless-adapter-log4j2</artifactId>
    <version>${sofa.serverless.runtime.version}</version>
    <scope>provided</scope>
</dependency>
```

### 实验步骤
1. 进入目录 `sofa-serverless/samples/dubbo-samples/rpc/grpc/model/`，执行 `mvn instll`，将通信包安装到本地仓库
1. 执行 `mvn clean package -DskipTests`
2. 启动基座应用 base，确保基座启动成功
3. 启动 zookeeper
```shell
docker run -p 2181:2181 -it --name zookeeper --restart always -d zookeeper:3.9.0
```
4. 执行 curl 命令安装 biz1
```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1-triple",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///path/to/sofa-ark-spring-guides/samples/web/tomcat/biz1/target/biz1-triple-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

如果要验证热部署的能力，也可以通过卸载命令卸载模块，然后重新安装模块，发起多次模块安装
```shell
curl --location --request POST 'localhost:1238/uninstallBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1",
    "bizVersion": "0.0.1-SNAPSHOT"
}'
```

5. 查看模块安装是否成功
```shell
curl --location --request POST 'localhost:1238/queryAllBiz'
```
可以查看到所有安装好的模块列表

6. 验证模块的 RPC 调用

   1. 直接调用基座 rest 接口，验证 injvm 的 rpc 调用

```shell
curl localhost:8080
```
返回 `Receive result ======> Hello base`
    2. 调用模块的 rest 接口，验证 rpc 调用
```shell
curl localhost:8080/biz1
```
返回 `Receive result ======> Hello biz1`
