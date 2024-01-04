
---
title: 基座与模块并行开发验证
weight: 501
draft: false
---

欢迎使用 **SOFAServerless 完成多 SpringBoot 应用合并部署与动态更新模块**！本文将详细介绍操作流程与方法，希望能够帮助大家节省资源、提高研发效率。
首先，利用 SOFAServerless 完成合并部署与动态更新模块，适用于两种典型场景：

1. **合并部署**
2. **中台应用**_（该场景需要先完成合并部署，再完成中台应用 demo)_

> 本文实验工程代码在：[开源仓库 samples 目录库里](https://github.com/sofastack/sofa-serverless/tree/master/samples/springboot-samples/web/tomcat)

## 场景一：合并部署
先介绍第一个场景**多应用合并部署**，整体流程如下:
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/96256555/1700728867593-5f85b5a6-254c-44f9-9866-03f75cd1a30e.png#clientId=u236ce451-8502-4&from=paste&height=596&id=ud7c32f07&originHeight=1192&originWidth=2386&originalType=binary&ratio=2&rotation=0&showTitle=false&size=2443895&status=done&style=none&taskId=uf8f160d5-4794-4459-aa18-118e3895bff&title=&width=1193)

可以看到，整体上需要完成的动作是**基座/模块接入改造后进行开发与验证**，而基座与模块的合并部署动作都是可以并行的。接下来我们将逐步介绍操作细节。

### 1. 基座接入改造

1. 为 **application.properties **增加应用名（如果没有的话）：

`spring.application.name=${基座应用名}`

2. 在 **pom.xml **里增加必要的依赖
```xml
<properties>
    <sofa.serverless.runtime.version>0.5.6</sofa.serverless.runtime.version>
</properties>
<dependencies>
    <dependency>
        <groupId>com.alipay.sofa.serverless</groupId>
        <artifactId>sofa-serverless-base-starter</artifactId>
        <version>${sofa.serverless.runtime.version}</version>
    </dependency>
</dependencies>
```

---

理论上增加这个依赖就可以了，但由于本 demo 需要演示多个 web 模块应用使用一个端口合并部署，需要再引入 `web-ark-plugin` 依赖，[详细原理查看这里](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-multi-web-component-deploy/)。
```xml
    <dependency>
        <groupId>com.alipay.sofa</groupId>
        <artifactId>web-ark-plugin</artifactId>
    </dependency>
```

3. 点击编译器启动基座。
### 2. 模块 1 接入改造

1. 添加模块需要的依赖和打包插件
```json
<plugins>
    <!--这里添加ark 打包插件-->
    <plugin>
        <groupId>com.alipay.sofa</groupId>
        <artifactId>sofa-ark-maven-plugin</artifactId>
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
            <bizName>${替换为模块名}</bizName>
            <webContextPath>${模块自定义的 web context path，需要与其他模块不同}</webContextPath>
            <declaredMode>true</declaredMode>
            <!--  配置模块自动排包列表，从 github 下载 rules.txt，并放在模块根目录的 conf/ark/ 目录下，下载地址：https://github.com/sofastack/sofa-serverless/blob/master/samples/springboot-samples/slimming/log4j2/biz1/conf/ark/rules.txt  -->
            <packExcludesConfig>rules.txt</packExcludesConfig>
        </configuration>
    </plugin>
    <!--  构建出普通 SpringBoot fatjar，支持独立部署时使用，如果不需要可以删除  -->
    <plugin>
        <!--原来 spring-boot 打包插件 -->
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
    </plugin>
</plugins>
```

2. 参考官网模块瘦身里[自动排包部分](https://sofaserverless.gitee.io/docs/tutorials/module-development/module-slimming/#%E4%B8%80%E9%94%AE%E8%87%AA%E5%8A%A8%E7%98%A6%E8%BA%AB)，下载排包配置文件 **rules.txt**，放在在 **conf/ark/** 目录下

3. 开发模块，例如增加 Rest Controller，提供 Rest 接口
```json
@RestController
public class SampleController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleController.class);

    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {
        String appName = applicationContext.getApplicationName();
        LOGGER.info("{} web test: into sample controller", appName);
        return String.format("hello to %s deploy", appName);
    }
}
```

4. [点击这里下载 Arkctl](https://github.com/sofastack/sofa-serverless/releases/tag/arkctl-release-0.1.0)，mac/linux 电脑放入 `**/usr/local/bin**` 目录中，windows 可以考虑直接放在项目根目录下

5. 执行 `arkctl deploy` 构建部署，成功后 `curl localhost:8080/${模块1 web context path}/` 验证服务返回。显示正常，进入下一步。
```
hello to ${模块1名} deploy
```

### 3. 模块 1 开发与验证
开发与验证需要完成**修改代码并发布 V2 版本**。具体操作如下：

1. 修改 Rest 代码
```json
@RestController
public class SampleController {
    private static final Logger LOGGER = LoggerFactory.getLogger(SampleController.class);

    @Autowired
    private ApplicationContext applicationContext;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {
        String appName = applicationContext.getApplicationName();
        LOGGER.info("{} web test v2: into sample controller", appName);
        return String.format("hello to %s deploy v2", appName);
    }
}
```

2. 执行 `arkctl deploy` 构建部署，成功后 `curl localhost:8080/${模块1 web context path}/` 验证服务返回
```
hello to ${模块1名} deploy v2
```

### 4. 模块 2 接入改造、开发与验证
模块 2 同样采用上述步骤2⃣️3⃣️，即模块 1 接入改造与验证的操作流程。

## 场景二：中台应用
中台应用的特点是**基座有复杂的编排逻辑**去定义**对外暴露服务和业务所需的 SPI**。模块应用来实现这些 SPI 接口，往往会对一个接口在多个模块里定义多个不同的实现。整体流程如下：
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/96256555/1700706388244-f46fbd3d-262d-4a9d-894a-243782860cce.png#clientId=ue666f90d-083a-4&from=paste&height=310&id=u76e59b07&originHeight=620&originWidth=1762&originalType=binary&ratio=2&rotation=0&showTitle=false&size=345022&status=done&style=none&taskId=ue115f7a7-de5a-4c01-91d4-0965c2422dc&title=&width=881)

可以看到，与场景一合并部署操作不同的是，需要在**基座**接入改造与开发验证中间新增一步**通信类和 SPI 的定义**；**模块**接入改造与开发验证中间新增一步**引入通信类基座并实现基座 SPI**。
**接下来我们将介绍与合并部署不同的**_**（即新增的）**_**操作细节。**
### 1. 基座完成通信类和 SPI 的定义
在合并部署接入改造的基础上，需要完成通信类和 SPI 的定义。
通信类需要以 **独立 bundle **的方式存在，才能被模块引入。可参考以下方式：

1. 新建 bundle，定义接口类
```xml
public class ProductInfo {
    private String  name;
    private String  author;
    private String  src;
    private Integer orderCount;
}
```

2. 定义 SPI
```java
public interface StrategyService {
    List<ProductInfo> strategy(List<ProductInfo> products);
    String getAppName();
}
```

### 2. 模块 1 **引入通信类基座并实现基座 SPI**
在上文合并部署模块 1 接入改造 demo 的基础上，引入通信类，然后定义 SPI 实现。

1. 引入通信类和对应 SPI 定义，只需要**在 pom 里引入基座定义的通信 bundle**
2. 定义 SPI 实现
```java
@Service
public class StrategyServiceImpl implements StrategyService {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public List<ProductInfo> strategy(List<ProductInfo> products) {
        return products;
    }

    @Override
    public String getAppName() {
        return applicationContext.getApplicationName();
    }
}
```

3. 执行 `arkctl deploy` 构建部署，成功后用 `curl localhost:8080/${基座服务入口}/biz1/` 验证服务返回

**biz1** 传入是为了使基座根据不同的参数找到不同的 SPI 实现，执行不同的逻辑。传入的方式可以有很多种，这里用最简单方式——从 **path **里传入。
```
默认的 products 列表
```

### 3. 模块 2 **引入通信类基座并实现基座 SPI**
与模块 1 操作相同，需要注意执行 `arkctl deploy` 构建部署时，成功后 `curl localhost:8080/${基座服务入口}/biz2/` 验证服务返回。同理，**biz2 **传入是为了基座根据不同的参数，找到不同的 SPI 实现，执行不同逻辑。
```java
@Service
public class StrategyServiceImpl implements StrategyService {
    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public List<ProductInfo> strategy(List<ProductInfo> products) {
        Collections.sort(products, (m, n) -> n.getOrderCount() - m.getOrderCount());
        products.stream().forEach(p -> p.setName(p.getName()+"("+p.getOrderCount()+")"));
        return products;
    }

    @Override
    public String getAppName() {
        return applicationContext.getApplicationName();
    }
}
```

```
更改排序后的 products 列表
```

基于上述操作，就可以继续进行上文中模块 [**开发与验证**](https://sofaserverless.gitee.io/docs/tutorials/trial_step_by_step/) 的操作了。整体流程丝滑易上手，欢迎试用！

## 文档中的链接地址

1. 本实验工程样例地址：[https://github.com/sofastack/sofa-serverless/tree/master/samples/springboot-samples/web/tomcat](https://github.com/sofastack/sofa-serverless/tree/master/samples/springboot-samples/web/tomcat)
2. `web-ark-plugin` 原理： [https://www.sofastack.tech/projects/sofa-boot/sofa-ark-multi-web-component-deploy/](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-multi-web-component-deploy/)
3. 自动排包原理与配置文件下载：[https://sofaserverless.gitee.io/docs/tutorials/module-development/module-slimming/#%E4%B8%80%E9%94%AE%E8%87%AA%E5%8A%A8%E7%98%A6%E8%BA%AB](https://sofaserverless.gitee.io/docs/tutorials/module-development/module-slimming/#%E4%B8%80%E9%94%AE%E8%87%AA%E5%8A%A8%E7%98%A6%E8%BA%AB)
4. Arkctl 下载地址：[https://github.com/sofastack/sofa-serverless/releases/tag/arkctl-release-0.1.0](https://github.com/sofastack/sofa-serverless/releases/tag/arkctl-release-0.1.0)
5. 本文档地址：[https://sofaserverless.gitee.io/docs/tutorials/trial_step_by_step/](https://sofaserverless.gitee.io/docs/tutorials/trial_step_by_step/)
