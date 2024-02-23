<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

# Dubbo 2.6.x in module
## Add dependencies in the base
base 为普通 dubbo 应用改造而成，改造内容只需在主 pom 里增加如下依赖
The base is build from a normal dubbo application, the only thing you need to do is add the following dependencies in the build pom.xml
```xml
<!--覆盖dubbo 2.6同名类,一定要放在dubbo的依赖前面-->
<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-adapter-dubbo2.6</artifactId>
    <!--    <version>${koupleless.runtime.version}</version>-->
    <version>1.0.0</version>
</dependency>
<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-base-starter</artifactId>
</dependency>
```
If the module is a web application and you want to deploy the module in the same tomcat host with base, add the following dependency. For more details, please refer to [here](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-multi-web-component-deploy/)
```xml
<!-- 这里添加 tomcat 单 host 模式部署多web应用的依赖, 非 web 应用可忽略 -->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>web-ark-plugin</artifactId>
</dependency>
        <!-- end 单 host 部署的依赖 -->
```

Here the base published a RPC service and also a Injvm service
```shell
base/com.alipay.sofa.rpc.dubbo26.model.DemoService
```

## How to use in the modules
### Add the module packaging plugin
This is a module application that is dynamically installed to the base, and it is also a normal dubbo application. It only needs to modify the packaging plugin to become an ark biz module that can be merged and deployed.
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
### Reuse dependencies in the base
Furthermore, the module also sets the dependencies as provided scope which is imported in base, so that the biz can reused the libs like model, dubbo etc in the base as much as possible.


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
If you need to communicate between the base and the module, the module classes that is used for communication needs to be delegated to the base for loading (base compile import, module provided import), otherwise ClassCastException will be reported
```xml
<!-- 模块和基座通信，需要共享一个类-->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>dubbo26model</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```
### Module and Base log path isolation
- To make the base and module log print to different directories, the base and module also introduce log4j2 adapter.

```xml
<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-adapter-log4j2</artifactId>
    <!--    <version>${koupleless.runtime.version}</version>-->
    <version>1.0.0</version>
    <scope>provided</scope>
</dependency>
```

### Testing code
publish a service
``` xml
<!-- 和本地bean一样实现服务 -->
<bean id="demoService" class="com.alipay.sofa.rpc.dubbo26.biz.service.BizDemoServiceImpl"/>
<!-- 声明需要暴露的服务接口 -->
<dubbo:service interface="com.alipay.sofa.rpc.dubbo26.model.DemoService" ref="demoService" group="biz"/>    <!-- 和本地bean一样实现服务 -->
```

BizController referenced the RPC published by the module itself and the injvm service published by the base.

``` xml
<!-- 生成服务代理，调用基座的injvm服务-->
<dubbo:reference id="baseDemoServiceRef" interface="com.alipay.sofa.rpc.dubbo26.model.DemoService" scope="local" group="base" check="false"/>
<!-- 生成远程服务代理，调用服务biz1/com.alipay.sofa.rpc.dubbo26.model.DemoService-->
<dubbo:reference id="selfDemoServiceRef" interface="com.alipay.sofa.rpc.dubbo26.model.DemoService" scope="remote" group="biz" check="false"/>
```
### Install the module when the base is started (only for local test), you can choose one of the following two methods
1. We use static merge deployment here, so the module is installed when the base is started
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
2. You can also install the module by curl command, the bizUrl support local file by `file://` and also remote url

```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz",
    "bizVersion": "0.0.1-SNAPSHOT",
    "bizUrl": "file:////path/to/project/koupleless/samples/dubbo-samples/rpc/dubbo3/dubbo26biz/target/dubbo26biz-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

### 运行代码
### Run the code
1. [Important] For the first run, compile and install the package used for communication between the base and the module
```shell
cd samples/dubbo-samples/rpc/dubbo26/dubbo26model
mvn clean install
```

2. cd into `samples/dubbo-samples/rpc/dubbo26/` and compile and package the module code
```shell
cd ../
mvn clean package
```
3. start base application Dubbo26BaseApplication.java, make sure the base and module started successfully

4. check if the module is installed successfully

```shell
curl --location --request POST 'localhost:1238/queryAllBiz'
```
you can see the list of all installed modules

5. verify the RPC/JVM call of the module
Remoting calling the dubbo service published by the module itself, because there is dubbo network call, please close vpn before execution, otherwise there may be call timeout
```shell
curl localhost:8080/biz/selfRemote
```
return
```shell
{
  "result": "biz->com.alipay.sofa.rpc.dubbo26.biz.service.BizDemoServiceImpl"
}
```
Injvm calling the dubbo service published by the base
```shell
curl localhost:8080/biz/baseInJvm
```
return
```json
{
  "result": "biz->com.alipay.sofa.rpc.dubbo26.base.service.BaseDemoService"
}
```
6. verify the RPC/JVM call of the base
The base calling injvm service published by the biz module
```shell
curl http://localhost:8080/bizInJvm
```
return
```shell
{
  "result": "base->com.alipay.sofa.rpc.dubbo26.biz.service.BizDemoServiceImpl"
}
```

### Description
Because there is dubbo network call, please close vpn before execution, otherwise there may be call timeout.

dubbo2.6 only supports java serialization and does not support hot deployment capability. If you need it, please submit an issue to tell us.

