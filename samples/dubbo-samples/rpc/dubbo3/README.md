# Dubbo 3.x 在模块中使用
## 当前支持情况
| provider/consumer | protocol |                         序列化                          | 是否支持 |
|:-----------------:|:--------:|:----------------------------------------------------:|:----:|
|     provider      |   tri    |                       hessian2                       |  支持  |
|     consumer      |   tri    |                       hessian2                       |  支持  |
|     provider      |   tri    |                      fastjson2                       |  支持  |
|     consumer      |   tri    |                      fastjson2                       | 不支持  |
|     provider      |   tri    |            java/compactedjava/nativejava             |  不支持  |
|     consumer      |   tri    |            java/compactedjava/nativejava             | 不支持  |

## 启动基座
base 为普通 dubbo 应用改造而成，改造内容只需在主 pom 里增加如下依赖
```
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

这里基座发布了 RPC 服务
```shell
base/com.alipay.sofa.rpc.dubbo3.model.CommonService
```
引用了模块的RPC，JVM服务
```java
 /**
     * 通过远程rpc调用模块triplebiz的CommonService服务
     * check = false是因为启动时还没安装模块
     */
    @DubboReference(group = "triplebiz", scope = Constants.SCOPE_LOCAL, check = false)
    private CommonService commonServiceLocal;

    /**
     * 通过远程rpc调用模块triplebiz的CommonService服务
     * check = false是因为启动时还没安装模块
     */
    @DubboReference(group = "triplebiz", scope = Constants.SCOPE_REMOTE, check = false)
    private CommonService commonServiceRemote;

```

## 模块中使用tri协议示例：triplebiz模块
### 修改模块打包插件
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
        <bizName>triplebiz</bizName>
        <webContextPath>/triplebiz</webContextPath>
        <declaredMode>true</declaredMode>
        <!--					打包、安装和发布 ark biz-->
        <!--					静态合并部署需要配置-->
        <!--					<attach>true</attach>-->
    </configuration>
</plugin>
```
### 复用基座依赖
另外模块还额外将基座里有的依赖，设置为了 provided，这样可以尽可能的服用基座的model、dubbo 等。
```xml
<!--和基座通信-->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>common-model</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>

<dependency>
    <groupId>com.google.protobuf</groupId>
    <artifactId>protobuf-java</artifactId>
    <version>${protobuf.version}</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.apache.dubbo</groupId>
    <artifactId>dubbo-dependencies-zookeeper</artifactId>
    <scope>provided</scope>
    <type>pom</type>
    <exclusions>
        <exclusion>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-reload4j</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.apache.dubbo</groupId>
    <artifactId>dubbo-spring-boot-starter</artifactId>
    <scope>provided</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <scope>provided</scope>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
    <scope>provided</scope>
</dependency>
```
### 模块日志路径和基座隔离
- 为了让基座和模块日志打印到不同的目录下，基座和模块还额外引入了 log4j2 adapter。
- 如果不关心基座和模块日志是否打印在一起还是分开打印，那么这个依赖可以不加。
```xml
<dependency>
    <groupId>com.alipay.sofa.serverless</groupId>
    <artifactId>sofa-serverless-adapter-log4j2</artifactId>
    <version>${sofa.serverless.runtime.version}</version>
    <scope>provided</scope>
</dependency>
```

### 测试调用代码
这里模块在 RPCController 里引用了模块/基座里定义的 RPC/JVM 服务。
```java
 /**
     * tri协议，远程调用，默认走hessian序列化
     */
    @DubboReference(group = "triplebiz", scope = "remote")
    private DemoService demoService;

    /**
     * tri协议，injvm调用，scope默认走injvm
     */
    @DubboReference(group = "base")
    private CommonService commonServiceInJvm;
```

### 运行代码
1. 进入目录 `sofa-serverless/samples/dubbo-samples/rpc/dubbo3/`
2. 执行 `mvn clean install -DskipTests`
3. 启动 zookeeper（用于Dubbo服务注册）
```shell
docker run -p 2181:2181 -it --name zookeeper --restart always -d zookeeper:3.9.0
```
4. 启动基座应用Dubbo3BaseApplication.java，确保基座启动成功
5. 执行 curl 命令安装 triplebiz
本地 path 以 file://开始, 也支持远程url下载
```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "triplebiz",
    "bizVersion": "0.0.1-SNAPSHOT",
    "bizUrl": "file:////path/to/project/sofa-serverless/samples/dubbo-samples/rpc/dubbo3/triplebiz/target/triplebiz-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

如果要验证热部署的能力，也可以通过卸载命令卸载模块，然后重新安装模块，发起多次模块安装
```shell
curl --location --request POST 'localhost:1238/uninstallBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "triplebiz",
    "bizVersion": "0.0.1-SNAPSHOT"
}'
```

5. 查看模块安装是否成功
```shell
curl --location --request POST 'localhost:1238/queryAllBiz'
```
可以查看到所有安装好的模块列表

6. 验证模块的 RPC/JVM调用
模块远程调用自己发布的tri服务
```shell
curl localhost:8080/triplebiz/remote
```
返回
```shell
com.alipay.sofa.rpc.dubbo3.triplebiz.service.DemoServiceImpl: Hello,trpilebiz
```
模块injvm调用基座发布的服务
```shell
curl localhost:8080/triplebiz/local
```
返回
```shell
com.alipay.sofa.rpc.dubbo3.base.service.BaseCommonService: Hello,triplebiz
```
7. 验证基座的 RPC/JVM调用
基座调用triplebiz模块发布的injvm服务
```shell
curl http://localhost:8080/base/triplebiz/injvm
```
返回
```shell
com.alipay.sofa.rpc.dubbo3.triplebiz.service.TripleBizCommonService: Hello,base
```
基座调用triplebiz模块发布的rpc服务
```shell
curl http://localhost:8080/base/triplebiz/remote
```
返回
```shell
com.alipay.sofa.rpc.dubbo3.triplebiz.service.TripleBizCommonService: Hello,base
```