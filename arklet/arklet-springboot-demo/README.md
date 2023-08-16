## 实验内容  

基于[sofa-ark-dynamic-guides](https://github.com/sofastack-guides/sofa-ark-dynamic-guides/tree/master)的实验模板，使用Sofa-Severless的http调用工具Akrlet，通过 [SOFAArk](https://github.com/sofastack/sofa-ark) 提供的动态模块能力，实现商品列表排序策略的动态变更，以及动态装卸订单查询子应用。能够完成在不重启宿主机，不更改应用配置的情况下实现应用行为改变的任务。

> *注意，本实验由于未将 arklet 上传至 maven repository，因此需要对 arklet 中的 arklet-springboot-starter 执行 `mvn clean install` 打出本地的 jar 包，后供 demo 引用*

## 实验准备

* sofa-serverless-arklet-springboot-demo
* IntellJ IDEA
* Postman（可选，作为HTTP API使用工具，也可以**使用本demo中的API完成测试**）

> *本实验采用SofaBoot启动器，同样地可以使用Springboot完成最基础的应用模块装载，只需要在Maven中取消关于ark web插件中sofa boot的依赖引入即可。比如，下述sofa-boot的依赖即可删除。*

```xml
<!-- bookstore-manager/pom.xml -->

<dependency>  
    <groupId>com.alipay.sofa</groupId>  
    <artifactId>healthcheck-sofa-boot-starter</artifactId>  
</dependency>

<dependency>  
    <groupId>com.alipay.sofa</groupId>  
    <artifactId>runtime-sofa-boot-starter</artifactId>  
</dependency>

<dependency>  
    <groupId>com.alipay.sofa</groupId>  
    <artifactId>runtime-sofa-boot-plugin</artifactId>  
</dependency>
```
## 任务  

### 1、任务准备  

从 github 上将 demo 工程克隆到本地  

```bash  
git clone git@github.com:/sofa-serverless-arklet-springboot-demo
```

然后将工程导入到 IDEA 或者 eclipse，打开工程后，工程目录结构如下：  

```bash  
├── bookstore-manager  
├── bookstore-order-provider
├── bookstore-provider
├── bookstore-service
└── pom.xml  
```

* bookstore-manager 宿主应用，提供基础数据，提供用户端的商品展示 web 页面，用于展示实验效果。
* bookstore-order-provider 子应用，提供订单数展示的行为，独立提供 web 页面，类比单体应用移植。
* bookstore-provider 实现 bookstore-service 定义的接口，并将实现类作为一个服务，类比服务注册行为。
* bookstore-service 定义一个 Java 接口 StrategyService，该接口用于对传入的商品列表进排序并返回。
### 2、相关依赖与打包配置

> 由于本模块在 demo 中已经实现，若仅复现实验可以跳过本节。本节主要介绍 sofa 相关依赖的配置，针对外部已有工程的 sofa 引入，可以参照本节 maven 配置 sofa 依赖，其余微服务相关配置不在此赘述。

宿主应用确定 sofa 版本及 sofa 依赖，关于 SOFAArk 可以参考[SOFABoot 类隔离](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-readme/) 一节进行了解。  其中具体添加的依赖如下

```xml
<!-- bookstore-manager/pom.xml --> 

<!-- sofa-boot 启动器 -->  
<dependency>  
    <groupId>com.alipay.sofa</groupId>  
    <artifactId>runtime-sofa-boot-starter</artifactId>  
</dependency>  
  
<!-- sofa-boot 健康检查 -->  
<dependency>  
    <groupId>com.alipay.sofa</groupId>  
    <artifactId>healthcheck-sofa-boot-starter</artifactId>  
</dependency>  
  
<!-- sofa-core sofa-ark 主要依赖 -->  
<dependency>  
    <groupId>com.alipay.sofa</groupId>  
    <artifactId>sofa-ark-all</artifactId>  
    <version>${sofa.ark.version}</version>  
    <exclusions>  
        <exclusion>  
            <groupId>ch.qos.logback</groupId>  
            <artifactId>logback-core</artifactId>  
        </exclusion>  
        <exclusion>  
            <groupId>ch.qos.logback</groupId>  
            <artifactId>logback-classic</artifactId>  
        </exclusion>  
    </exclusions>  
</dependency>  
  
<!-- sofa-ark 编译时，ArkContainer装载在Springboot内的配置 -->  
<dependency>  
    <groupId>com.alipay.sofa</groupId>  
    <artifactId>sofa-ark-compatible-springboot2</artifactId>  
    <version>${sofa.ark.version}</version>  
</dependency>  
  
<!-- sofa-ark 在springboot启动时添加上下文配置 -->  
<dependency>  
    <groupId>com.alipay.sofa</groupId>  
    <artifactId>sofa-ark-springboot-starter</artifactId>  
    <version>${sofa.ark.version}</version>  
    <exclusions>  
        <exclusion>  
            <groupId>com.alipay.sofa</groupId>  
            <artifactId>sofa-ark-compatible-springboot1</artifactId>  
        </exclusion>  
    </exclusions>  
</dependency>  
  
<!-- sofa-ark-plugin -->  
<dependency>  
    <groupId>com.alipay.sofa</groupId>  
    <artifactId>web-ark-plugin</artifactId>  
</dependency>  
<dependency>  
    <groupId>com.alipay.sofa</groupId>  
    <artifactId>runtime-sofa-boot-plugin</artifactId>  
</dependency>  
  
<!-- end to add sofa-ark-plugin -->  
<dependency>  
    <groupId>com.alipay.sofa</groupId>  
    <artifactId>hessian</artifactId>  
</dependency>  
  
<!-- arklet springboot 启动器 -->  
<dependency>  
    <groupId>com.alipay.sofa</groupId>  
    <artifactId>arklet-springboot-starter</artifactId>  
    <version>1.0.0-SNAPSHOT</version>  
</dependency>
```

在打包配置中，sofa-ark 2.0之后宿主应用可以直接启动。但是更早的版本需要宿主应用需要本身打包一个 masterbiz，在 build 内部添加如下配置。

```xml
<build>  
    <plugins>  
        <plugin>  
            <groupId>org.springframework.boot</groupId>  
            <artifactId>spring-boot-maven-plugin</artifactId>  
            <configuration>  
                <outputDirectory>target</outputDirectory>  
                <classifier>ark-biz</classifier>  
            </configuration>  
            <executions>  
                <execution>  
                    <id>package</id>  
                    <goals>  
                        <goal>repackage</goal>  
                    </goals>  
                </execution>  
            </executions>  
        </plugin>  
    </plugins>  
</build>
```

**在使用 sofaboot 启动的前提下**，若需要对biz包（服务实例）进行健康检查，则需要在相应的服务模块内部加入 healthcheck-sofa-boot-starter 依赖

```xml
<!-- bookstore-provider/pom.xml --> 

<dependency>  
    <groupId>com.alipay.sofa</groupId>  
    <artifactId>healthcheck-sofa-boot-starter</artifactId>  
    <scope>provided</scope>  
</dependency>
```

另外在宿主应用中考虑到服务的注册，需要提前加入相应的 provider 依赖。
### 3、将 dynamic-provider 打包成 ark biz  

在 bookstore-provider/pom.xml 中，增加 ark 打包插件，该模块实现了宿主应用的一个接口，同时暴露一个 rest 服务，添加如下配置。

![image.png](https://picgo-1313342257.cos.ap-nanjing.myqcloud.com/test/20230813164532.png)

```xml  
<plugins>  
    <!--这里添加ark 打包插件-->  
    <plugin>  
        <groupId>com.alipay.sofa</groupId>  
        <artifactId>sofa-ark-maven-plugin</artifactId>  
        <version>2.1.3</version>  
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
            <bizName>bookstore-provider</bizName>  
            <webContextPath>provider</webContextPath>  
            <declaredMode>true</declaredMode>  
        </configuration>  
    </plugin>  
</plugins>
```
### 4、打包 & 启动宿主应用  

sofa-ark 2.0之后宿主应用可以直接启动，命令行与IDEA内部均可以启动，需要额外增设虚拟机选项 `-Dsofa.ark.embed.enable=true`，然后直接启动 BookStoreManagerApplication 类。  

![image.png](https://picgo-1313342257.cos.ap-nanjing.myqcloud.com/test/20230813165200.png)

* sofa-ark 启动成功之后的日志信息如下。

![image.png](https://picgo-1313342257.cos.ap-nanjing.myqcloud.com/test/20230813165609.png)

* sofa-boot 启动成功之后的日志信息如下。

![image.png](https://picgo-1313342257.cos.ap-nanjing.myqcloud.com/test/20230813165807.png)

* arklet 启动成功之后的日志信息如下。

![image.png](https://picgo-1313342257.cos.ap-nanjing.myqcloud.com/test/20230813165929.png)
### 5、 引入默认的排序策略模块  

bookstore-provider 提供的 bookstore.services.StrategyService 实现类返回了默认的价格降序排序模块，现在需要实现在宿主应用启动的情况下完成 biz 模块动态装卸。
执行 `mvn clean package` 进行打包，此时可以额外地打出新版本（**需要在 pom.xml 中给出版本号**） bookstore-provider ark biz 包，其中后续需要对 `*-ark-biz.jar` 的包进行部署与卸载，如下。

![image.png](https://picgo-1313342257.cos.ap-nanjing.myqcloud.com/test/20230813171005.png)

原来的sofa-ark可以使用 telnet 连接来检测 biz 安装情况，现在可以使用 arklet 提供的一套 http API 完成 biz 装卸，关于telnet使用详见 [sofa-ark-dynamic-guides的第5节](https://github.com/sofastack-guides/sofa-ark-dynamic-guides/tree/master)。

这里使用 Postman 进行 http 请求发送，其中具体使用 `http://localhost:1238/...` 格式 POST 请求，具体请求体如下。

使用 `http://localhost:1238/installBiz` 路由安装 bookstore-provider 的 biz 包，其中需要使用 JSON 给出 Request Body（arkBizFilePath，bizname，bizversion 参数必填），并查询响应结果。

```json
{
// Win 下文件路径
"arkBizFilePath": "\\{盘符}:\\{文件路径}\\sofa-serverless-arklet-springboot-demo\\bookstore-provider\\target\\bookstore-provider-1.0.0-ark-biz.jar",
// Mac 下文件路径
// "arkBizFilePath": "/{文件路径}/sofa-serverless-arklet-springboot-demo/bookstore-provider/target/bookstore-provider-1.0.0-ark-biz.jar",
"bizname": "bookstore-provider",
"bizversion": "1.0.0"
}
```

![image.png](https://picgo-1313342257.cos.ap-nanjing.myqcloud.com/test/20230813214801.png)

* 从响应码可以看出宿主应用已经安装了 bookstore-provider 模块，此时使用路由 `http://localhost:1238/queryAllBiz` 查询应用内所有 ark-biz 包，如下。

![image.png](https://picgo-1313342257.cos.ap-nanjing.myqcloud.com/test/20230813215926.png)

* 发现已经成功安装两个访问 http://localhost:8080 ，现在展示的是默认的降序排列顺序，如下所示：  
  

![image.png](https://picgo-1313342257.cos.ap-nanjing.myqcloud.com/test/20230813215102.png)


### 6、新建按照销量排序策略模块  

bookstore-provider 提供的 bookstore.services.StrategyService 实现类返回了默认的**价格降序**排序模块，现在需要开发一个新版本模块，这个新版本模块会按照**价格升序商品列表**。  

首先，修改 bookstoreprovider.serviceImpl.StrategyServiceImpl 实现类如下：  

```java  
@Service  
@SofaService  
public class StrategyServiceImpl implements StrategyService {  
    @Override  
    public List<BookInfo> strategy(List<BookInfo> bookList) {  
        // ascending order        
        bookList.sort((i, j) -> (int) (i.getPrice() - j.getPrice()));  
        return bookList;  
    }  
    
    @Override  
    public String getStrategyName() {  
        return "按价格升序";  
    }  
}  
```

然后，修改 bookstore-provider 版本号 2.0.0:   

```xml  
<version>2.0.0</version>  
```

最后，由于本 demo 引入 web-ark-plugin，所以每个模块会复用同一个 tomcat 实例，所以需要更改server 的 webContextPath，搜索并修改 bookstore-provider 的 pom.xml 

```diff
--- <webContextPath>provider1</webContextPath>  
+++ <webContextPath>provider2</webContextPath>  
```

配置完成之后，执行 `mvn clean package` 进行打包，此时可以打包出新版本 bookstore-provider ark biz包，如下。

![image.png](https://picgo-1313342257.cos.ap-nanjing.myqcloud.com/test/20230813220756.png)  

通过 arklet 中 `http://localhost:1238/installBiz` 完成安装请求，安装新版本 bookstore-provider，Request Body 如下，并查询响应结果。

```JSON
 {
// Win 下文件路径
"arkBizFilePath": "\\{盘符}:\\{文件路径}\\sofa-serverless-arklet-springboot-demo\\bookstore-provider\\target\\bookstore-provider-2.0.0-ark-biz.jar",
// Mac 下文件路径
// "arkBizFilePath": "/{文件路径}/sofa-serverless-arklet-springboot-demo/bookstore-provider/target/bookstore-provider-2.0.0-ark-biz.jar",
"bizname": "bookstore-provider",
"bizversion": "2.0.0"
}
```

![image.png](https://picgo-1313342257.cos.ap-nanjing.myqcloud.com/test/20230813221652.png)


由于应用中已经有了 bookstore-provider 的 biz 包了，因此 2.0.0 版本是未激活的状态，需要将宿主应用切换到最新的模块，使用路由 `http://localhost:1238/switchBiz` 切换到对应版本的 biz 模块，具体Request Body 如下，并查询响应结果。

```JSON
{
"bizname": "bookstore-provider",
"bizversion": "2.0.0"
}
```

![image.png](https://picgo-1313342257.cos.ap-nanjing.myqcloud.com/test/20230813222513.png)

成功切换完模块后，使用路由 `http://localhost:1238/queryAllBiz` 查看当前所有模块的状态，发现 bookstore-provider 2.0.0 版本已经激活。

![image.png](https://picgo-1313342257.cos.ap-nanjing.myqcloud.com/test/20230813224146.png)


访问 http://localhost:8080/index ，现在展示的是列表编程按照**价格升序**进行排序，如下。

![image.png](https://picgo-1313342257.cos.ap-nanjing.myqcloud.com/test/20230813222728.png)

### 7、安装与卸载展示销量策略的独立子应用

现在需要开发一个新版本模块，提供关于销量的展示模块，bookstore-order-provider 提供了关于销量的展示模块，另如果要访问模块中的rest请求，请带上模块 sofa-ark-maven-plugin 里定义的webContextPath。
如同上述安装步骤一样，执行 `mvn clean package` 进行打包，注意在打包的时候需要将原先加入的Spring 等启动器依赖使用 provided ，防止与宿主应用冲突，如下。

![image.png](https://picgo-1313342257.cos.ap-nanjing.myqcloud.com/test/20230813223826.png)

通过 arklet 中 `http://localhost:1238/installBiz` 完成安装请求，安装新版本 bookstore-provider，Request Body 如下，并查询响应结果。

```JSON
{
// Win 下文件路径
"arkBizFilePath": "\\{盘符}:\\{文件路径}\\sofa-serverless-arklet-springboot-demo\\bookstore-provider\\target\\bookstore-order-provider-1.0.0-ark-biz.jar",
// Mac 下文件路径
// "arkBizFilePath": "/{文件路径}/sofa-serverless-arklet-springboot-demo/bookstore-provider/target/bookstore-order-provider-1.0.0-ark-biz.jar",
"bizname": "bookstore-order-provider",
"bizversion": "1.0.0"
}
```

![image.png](https://picgo-1313342257.cos.ap-nanjing.myqcloud.com/test/20230813224405.png)

安装成功后，使用路由 `http://localhost:1238/queryAllBiz` 查看当前所有模块的状态，发现 bookstore-order-provider 1.0.0 已经激活

![image.png](https://picgo-1313342257.cos.ap-nanjing.myqcloud.com/test/20230813225151.png)


访问 http://localhost:8080/order-provider/api-order/index 发现成功展示子应用的 web 页面，同理也可以使用 rest 服务。

![image.png](https://picgo-1313342257.cos.ap-nanjing.myqcloud.com/test/20230813224616.png)


现展示模块卸载工作，调用 `http://localhost:1238/uninstallBiz` 完成 bookstore-order-provider biz 的卸载任务，Request Body 如下，并查询响应结果。

```JSON
{
"bizname": "bookstore-order-provider",
"bizversion": "1.0.0"
}
```

![image.png](https://picgo-1313342257.cos.ap-nanjing.myqcloud.com/test/20230813224945.png)

卸载成功后，使用路由 `http://localhost:1238/queryAllBiz` 查看当前所有模块的状态，发现 bookstore-order-provider 1.0.0 已经删除

![image.png](https://picgo-1313342257.cos.ap-nanjing.myqcloud.com/test/20230813225252.png)
