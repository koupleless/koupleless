# Dubbo 2.6.x 在模块中使用
## 基座新增依赖
base 为普通 dubbo 应用改造而成，改造内容只需在主 pom 里增加如下依赖
```
<!--覆盖dubbo 2.6同名类,一定要放在dubbo的依赖前面-->
<dependency>
    <groupId>com.alipay.sofa.serverless</groupId>
    <artifactId>sofa-serverless-adapter-dubbo2.6</artifactId>
    <version>${sofa.serverless.runtime.version}</version>
</dependency>
<dependency>
    <groupId>com.alipay.sofa.serverless</groupId>
    <artifactId>sofa-serverless-base-starter</artifactId>
</dependency>
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

这里基座发布了 RPC 服务（包括一个injvm服务）
```shell
base/com.alipay.sofa.rpc.dubbo26.model.DemoService
```

## 模块中使用
### 修改模块打包插件
这是动态安装到基座的模块应用，也是普通 dubbo 应用，只需修改打包插件方式即可变成可合并部署的 ark biz 模块
```xml
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
        <bizName>biz</bizName>
        <webContextPath>/biz</webContextPath>
        <declaredMode>true</declaredMode>
        <!--					打包、安装和发布 ark biz-->
        <!--					静态合并部署需要配置-->
        <!--					<attach>true</attach>-->
    </configuration>
</plugin>
```
### 复用基座依赖
另外模块还额外将基座里有的依赖，设置为了 provided，这样可以尽可能的复用基座的model、dubbo 等。
```xml
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

<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>
```
如果需要基座和模块间通信，用于通信的模块需要委托给基座去加载（基座compile引入，模块provided引入），否则会报ClassCastException
```xml
<!-- 模块和基座通信，需要共享一个类-->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>dubbo26model</artifactId>
    <version>0.0.1-SNAPSHOT</version>
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
发布一个服务
``` xml
<!-- 和本地bean一样实现服务 -->
<bean id="demoService" class="com.alipay.sofa.rpc.dubbo26.biz.service.BizDemoServiceImpl"/>
<!-- 声明需要暴露的服务接口 -->
<dubbo:service interface="com.alipay.sofa.rpc.dubbo26.model.DemoService" ref="demoService" group="biz"/>    <!-- 和本地bean一样实现服务 -->
```

在 BizController 里引用了模块自己发布的RPC，基座发布的injvm服务。
``` xml
<!-- 生成服务代理，调用基座的injvm服务-->
<dubbo:reference id="baseDemoServiceRef" interface="com.alipay.sofa.rpc.dubbo26.model.DemoService" scope="local" group="base" check="false"/>
<!-- 生成远程服务代理，调用服务biz1/com.alipay.sofa.rpc.dubbo26.model.DemoService-->
<dubbo:reference id="selfDemoServiceRef" interface="com.alipay.sofa.rpc.dubbo26.model.DemoService" scope="remote" group="biz" check="false"/>
```
### 基座启动时默认安装模块（仅本地测试），以下两种方式可以二选一
为了方便本地测试用，启动基座时，默认也启动模块(静态合并部署)
```java 
/**
     * 方便本地测试用，启动基座时，默认也启动模块
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        try {
            installBiz("dubbo26biz/target/dubbo26biz-0.0.1-SNAPSHOT-ark-biz.jar");
            installBiz("dubbo26biz2/target/dubbo26biz2-0.0.1-SNAPSHOT-ark-biz.jar");
        } catch (Throwable e) {
            LOGGER.error("Install biz failed", e);
        }
    }
```
也可以用curl命令安装，本地 path 以 file://开始, 也支持远程url下载
```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz",
    "bizVersion": "0.0.1-SNAPSHOT",
    "bizUrl": "file:////path/to/project/sofa-serverless/samples/dubbo-samples/rpc/dubbo3/dubbo26biz/target/dubbo26biz-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

### 运行代码
1. 【重要】首次运行，先编译安装下用于基座模块间通信的包
```shell
cd samples/dubbo-samples/rpc/dubbo26/dubbo26model
mvn clean install
```

2. 进入目录`samples/dubbo-samples/rpc/dubbo26/` 编译打包模块的代码
```shell
cd ../
mvn clean package
```
3. 启动基座应用Dubbo26BaseApplication.java，确保基座和模块启动成功
4. 查看模块安装是否成功
```shell
curl --location --request POST 'localhost:1238/queryAllBiz'
```
可以查看到所有安装好的模块列表

5. 验证模块的 RPC/JVM调用
模块远程调用自己发布的dubbo服务（因为有dubbo网络调用，执行前请关闭vpn，否则可能出现调用超时）
```shell
curl localhost:8080/biz/selfRemote
```
返回
```shell
{
  "result": "biz->com.alipay.sofa.rpc.dubbo26.biz.service.BizDemoServiceImpl"
}
```
模块injvm调用基座发布的服务
```shell
curl localhost:8080/biz/baseInJvm
```
返回
```json
{
  "result": "biz->com.alipay.sofa.rpc.dubbo26.base.service.BaseDemoService"
}
```
6. 验证基座的 RPC/JVM调用
基座调用biz模块发布的injvm服务
```shell
curl http://localhost:8080/bizInJvm
```
返回
```shell
{
  "result": "base->com.alipay.sofa.rpc.dubbo26.biz.service.BizDemoServiceImpl"
}
```

### 说明
因为有dubbo网络调用，执行前请关闭vpn，否则可能出现调用超时。

dubbo2.6 暂时只支持java序列化，不支持热部署能力，如有需要请提一个issue告诉我们。

