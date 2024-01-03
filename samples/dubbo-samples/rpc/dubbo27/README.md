# Dubbo 2.7.x 在模块中使用
## 基座新增依赖
base 为普通 dubbo 应用改造而成，改造内容只需在主 pom 里增加如下依赖
```xml
<!--覆盖dubbo 2.7同名类,一定要放在dubbo的依赖前面-->
<dependency>
    <groupId>com.alipay.sofa.serverless</groupId>
    <artifactId>sofa-serverless-adapter-dubbo2.7</artifactId>
    <version>0.5.6-SNAPSHOT</version>
</dependency>
<!--sofa serverless 依赖-->
<dependency>
    <groupId>com.alipay.sofa.serverless</groupId>
    <artifactId>sofa-serverless-base-starter</artifactId>
</dependency>
<!--如果是 web 应用，并且希望后面模块部署与基座使用同一个 tomcat host，则引入如下依赖。详细查看[这里](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-multi-web-component-deploy/)-->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>web-ark-plugin</artifactId>
</dependency>
<!-- 通信类-->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>dubbo27model</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

这里基座发布了 RPC 服务
```xml
<bean id="demoService" class="com.alipay.sofa.rpc.dubbo27.base.service.MasterDemoServiceImpl"/>
<dubbo:service interface="com.alipay.sofa.rpc.dubbo27.model.DemoService" ref="demoService" group="masterBiz"/>    <!-- 和本地bean一样实现服务 -->
```

## 模块中使用
用例提供了 biz 和 biz2 两个模块动态安装到基座JVM中，也是普通 dubbo 应用，进行如下改造即可变成可合并部署的 ark biz 模块

### 1.修改模块打包插件
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
        <!--biz和biz2必须声明不同的bizName-->
        <bizName>biz</bizName>
        <!--biz和biz2必须声明不同的webContextPath-->
        <webContextPath>/biz</webContextPath>
        <declaredMode>true</declaredMode>
    </configuration>
</plugin>
```
### 2. 添加依赖
另外模块还额外将基座里有的依赖，设置为了 provided，这样可以尽可能的复用基座的spring、dubbo依赖等。通信类 dubbo27model 不用 provided，让biz类加载器自己加载
```xml
<!--提供 log4j2 适配，提供基座、模块日志隔离，不需要可以不添加-->
<dependency>
    <groupId>com.alipay.sofa.serverless</groupId>
    <artifactId>sofa-serverless-adapter-log4j2</artifactId>
    <version>${sofa.serverless.runtime.version}</version>
    <scope>provided</scope>
</dependency>
<!--提供 dubbo2.7 适配-->
<dependency>
    <groupId>com.alipay.sofa.serverless</groupId>
    <artifactId>sofa-serverless-adapter-dubbo2.7</artifactId>
    <version>${sofa.serverless.runtime.version}</version>
    <scope>provided</scope>
</dependency>
<!-- 通信类-->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>dubbo27model</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```
### 3. 声明dubbo服务和引用
biz1声明了三个rpc服务:
> biz1/com.alipay.sofa.rpc.dubbo27.model.DemoService
> biz1Second/com.alipay.sofa.rpc.dubbo27.model.DemoService
> biz1/com.alipay.sofa.rpc.dubbo27.model.HelloService

三个rpc引用，remote调用biz2发布的rpc服务
> biz2/com.alipay.sofa.rpc.dubbo27.model.DemoService
> biz2Second/com.alipay.sofa.rpc.dubbo27.model.DemoService
> biz2/com.alipay.sofa.rpc.dubbo27.model.HelloService

服务发布与引用配置如下：
```xml
<bean id="demoService" class="com.alipay.sofa.rpc.dubbo27.biz.service.BizDemoServiceImpl"/>
<dubbo:service interface="com.alipay.sofa.rpc.dubbo27.model.DemoService" ref="demoService" group="biz1"/> 

<bean id="secondDemoService" class="com.alipay.sofa.rpc.dubbo27.biz.service.SecondDemoServiceImpl"/>
<dubbo:service interface="com.alipay.sofa.rpc.dubbo27.model.DemoService" ref="secondDemoService" group="biz1Second"/>

<bean id="helloService" class="com.alipay.sofa.rpc.dubbo27.biz.service.HelloServiceImpl"/>
<dubbo:service interface="com.alipay.sofa.rpc.dubbo27.model.HelloService" ref="helloService" group="biz1"/>

<dubbo:reference id="demoServiceRef" interface="com.alipay.sofa.rpc.dubbo27.model.DemoService" scope="remote" group="biz2" check="false"/>
<dubbo:reference id="secondDemoServiceRef" interface="com.alipay.sofa.rpc.dubbo27.model.DemoService" scope="remote" group="biz2Second" check="false"/>
<dubbo:reference id="helloServiceRef" interface="com.alipay.sofa.rpc.dubbo27.model.HelloService" scope="remote" group="biz2" check="false"/>
```

类似的，biz2声明了三个rpc服务：
> biz2/com.alipay.sofa.rpc.dubbo27.model.DemoService
> biz2Second/com.alipay.sofa.rpc.dubbo27.model.DemoService
> biz2/com.alipay.sofa.rpc.dubbo27.model.HelloService

三个rpc引用，remote调用biz1发布的rpc服务
> biz1/com.alipay.sofa.rpc.dubbo27.model.DemoService
> biz1Second/com.alipay.sofa.rpc.dubbo27.model.DemoService
> biz1/com.alipay.sofa.rpc.dubbo27.model.HelloService






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


### 运行代码
1. 【重要】首次运行，先编译安装下用于通信包
```shell
cd samples/dubbo-samples/rpc/dubbo27/dubbo27model
mvn clean install
```

2. 进入目录`samples/dubbo-samples/rpc/dubbo27` 编译打包模块的代码
```shell
cd ../
mvn clean package
```
3. 启动基座应用Dubbo26BaseApplication.java，为了方便本地测试用，启动基座时，默认也启动模块
```java 
public static void run(String... args) throws Exception {
    try {
        installBiz("dubbo27biz/target/dubbo27biz-0.0.1-SNAPSHOT-ark-biz.jar");
        installBiz("dubbo27biz2/target/dubbo27biz2-0.0.1-SNAPSHOT-ark-biz.jar");
    } catch (Throwable e) {
        LOGGER.error("Install biz failed", e);
    }
}
```
也可以用curl、telnet、arkctl等部署工具手动安装，此处不再赘述。确保基座和模块启动成功。
4. 查看模块安装是否成功
```shell
curl --location --request POST 'localhost:1238/queryAllBiz'
```
可以查看到所有安装好的模块列表

5. 验证模块的RPC调用
模块biz远程调用biz2发布的dubbo服务（因为有dubbo网络调用，执行前请关闭vpn，否则可能出现调用超时）
```shell
curl localhost:8080/biz
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

dubbo2.7 暂时只支持java序列化，不支持热部署能力，如有需要请提一个issue告诉我们。

