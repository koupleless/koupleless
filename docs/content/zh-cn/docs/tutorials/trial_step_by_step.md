
---
title: 基座与模块并行开发验证
weight: 501
draft: true
---

本文介绍如何使用 SOFAServerless 完成多 SpringBoot 应用合并部署，并动态更新模块，用于省资源与提高研发效率，两种典型场景：
1. 合并部署
2. 中台应用
两种场景需要先完成合并部署，再完成中台应用demo

## 合并部署

先介绍第一个场景多应用合并部署，整体流程如下:
![multi_biz_dev.png](/img/multi_biz_dev.png)

### 基座接入改造
1. application.properties 增加应用名（如果没有的话）， `spring.application.name=${基座应用名}`
2. pom.xml 里增加必要的依赖
```xml
<properties>
    <sofa.serverless.runtime.version>0.5.2</sofa.serverless.runtime.version>
</properties>
<dependencies>
    <dependency>
        <groupId>com.alipay.sofa.serverless</groupId>
        <artifactId>sofa-serverless-base-starter</artifactId>
        <version>${sofa.serverless.runtime.version}</version>
    </dependency>
</dependencies>
```
理论上增加这个依赖就可以了，但由于本demo需要演示多个web模块应用使用一个端口合并部署，需要再引入 web-ark-plugin 依赖，[详细原理查看这里](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-multi-web-component-deploy/)。
```xml
    <dependency>
        <groupId>com.alipay.sofa</groupId>
        <artifactId>web-ark-plugin</artifactId>
    </dependency>
```
3. 点击编译器启动基座。

### 模块1 接入改造
1. 添加模块需要的依赖和打包插件
```xml
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
            <!--  配置模块自动排包列表，从 github 下载 exclude.txt，并放在模块根目录的 conf/ark/ 目录下，下载地址：https://github.com/sofastack/sofa-serverless/blob/master/samples/springboot-samples/slimming/log4j2/biz1/conf/ark/rules.txt  -->
            <packExcludesConfig>exclude.txt</packExcludesConfig>
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
2. 在 conf/ark/ 目录下导入自动排包瘦身的列表 exclude.txt
3. 模块开发，例如增加 rest controller, 提供 rest 接口
```java
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
3. 下载 arkctl, 下载地址 https://github.com/sofastack/sofa-serverless/releases/tag/arkctl-release-0.1.0 ，放入 `/usr/local/bin` 目录中
4. 执行 `arkctl deploy` 构建部署，成功后 `curl localhost:8080/${模块1 web context path}/` 验证服务返回
```text
hello to ${模块1名} deploy
```

### 修改模块1 代码并发布
1. 修改 rest 代码
```java
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
2执行 `arkctl deploy` 构建部署，成功后 `curl localhost:8080/${模块1 web context path}/` 验证服务返回
```text
hello to ${模块1名} deploy v2
```

### 模块2 采用同样方式接入改造与验证

## 中台应用
中台应用的特点是，基座有复杂的编排逻辑，定义对外暴露的服务和业务需要的 SPI，模块应用实现这些 SPI 接口，往往一个 SPI 接口会在多个模块里定义多个不同的实现。整体流程如下:
![middle_biz_dev.png](/img/middle_biz_dev.png)

### 基座接入改造
在上一部分接入改造的基础上，需要完成通信类和 SPI 的定义，通信类需要能被模块引入所以需要以独立 bundle 的方式存在，参考
1. 新建bundle，定义接口类
```java
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


### 模块1 接入改造
在上一个demo基础上，需要引入通信类，然后定义 SPI 实现
1. 引入通信类和对应 SPI 定义，只需要pom里引入基座定义的通信 bundle
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

3. 执行 `arkctl deploy` 构建部署，成功后 `curl localhost:8080/${基座服务入口}/biz2/` 验证服务返回

biz1 传入是为了基座根据不同的参数，找到不同的 SPI 实现，执行不同逻辑，传入 biz1 的方式可以有很多种，这里仅用最简单方式从 path 里传入。

```text
默认的 products 列表
```

### 修改模块1代码并发布
由于该部分已经在上一个 demo中演示过，这里不在单独展示模块代码修改更新

### 模块2 采用同样方式接入改造与验证
与模块1相同，在上一个demo基础上，需要引入通信类，然后定义 SPI 实现
1. 引入通信类和对应 SPI 定义，只需要pom里引入基座定义的通信 bundle
2. 定义 SPI 实现
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

3. 执行 `arkctl deploy` 构建部署，成功后 `curl localhost:8080/${基座服务入口}/biz2/` 验证服务返回

biz2 传入是为了基座根据不同的参数，找到不同的 SPI 实现，执行不同逻辑，传入 biz2 的方式可以有很多种，这里仅用最简单方式从 path 里传入。
```text
更改排序后的 products 列表
```