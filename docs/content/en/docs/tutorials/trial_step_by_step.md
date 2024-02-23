---
title: Parallel Development Verification of Base and Modules
weight: 501
description: Koupleless Base and Module Parallel Development Verification
date: 2024-01-25T10:28:32+08:00
---

Welcome to **Koupleless for Combined Deployment and Dynamic Module Updates with multiple SpringBoot applications**! This document will provide a detailed guide on the operation process and methods, aiming to help save resources and improve development efficiency.

Firstly, Koupleless is utilized to achieve combined deployment and dynamic module updates, suitable for two typical scenarios:

1. **Combined Deployment**
2. **Middleware Applications** _ (This scenario requires completing combined deployment first, followed by demonstrating middleware application demos)_

> The experimental project code in this document is available at: [samples directory in the open-source repository](https://github.com/koupleless/koupleless/tree/master/samples/springboot-samples/web/tomcat)

## Scenario One: Combined Deployment
Let's start with the first scenario: **Combined Deployment of Multiple Applications**. The overall process is as follows:
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/96256555/1700728867593-5f85b5a6-254c-44f9-9866-03f75cd1a30e.png#clientId=u236ce451-8502-4&from=paste&height=596&id=ud7c32f07&originHeight=1192&originWidth=2386&originalType=binary&ratio=2&rotation=0&showTitle=false&size=2443895&status=done&style=none&taskId=uf8f160d5-4794-4459-aa18-118e3895bff&title=&width=1193)

It can be observed that the main actions to be completed are **integration and deployment of base/module after access transformation**, and the merging deployment of base and module can be done in parallel. Next, we will gradually introduce the operational details.

### 1. Base Access Transformation

1. Add the application name to **application.properties** (if not already present):

`spring.application.name=${base application name}`

2. Add necessary dependencies to **pom.xml **
```xml
<properties>
    <koupleless.runtime.version>0.5.6</koupleless.runtime.version>
</properties>
<dependencies>
    <dependency>
        <groupId>com.alipay.koupleless</groupId>
        <artifactId>koupleless-base-starter</artifactId>
        <version>${koupleless.runtime.version}</version>
    </dependency>
</dependencies>
```

---

Theoretically, adding this dependency should be enough. However, because this demo needs to demonstrate the deployment of multiple web module applications using a single port, the `web-ark-plugin` dependency needs to be added. For detailed principles, please check [here](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-multi-web-component-deploy/)。
```xml
    <dependency>
        <groupId>com.alipay.sofa</groupId>
        <artifactId>web-ark-plugin</artifactId>
    </dependency>
```

3. Click on the compiler to start the base.
### 2. Module 1 Access Transformation

1. Add dependencies and packaging plugins required for the module
```xml
<plugins>
    <!-- Add the ark packaging plugin here -->
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
            <bizName>${Replace with module name}</bizName>
            <webContextPath>${Module's custom web context path, needs to be different from other modules}</webContextPath>
            <declaredMode>true</declaredMode>
            <!-- Configure the module's automatic package exclusion list, download rules.txt from github and place it in the conf/ark/ directory of the module's root directory, download link: https://github.com/koupleless/koupleless/blob/master/samples/springboot-samples/slimming/log4j2/biz1/conf/ark/rules.txt -->
            <packExcludesConfig>rules.txt</packExcludesConfig>
        </configuration>
    </plugin>
    <!-- Build a regular SpringBoot fatjar, which supports independent deployment. If not needed, it can be deleted -->
    <plugin>
        <!-- Original spring-boot packaging plugin -->
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
    </plugin>
</plugins>
```

2. Refer to the official website's module slimming section for the [automatic packaging part](https://koupleless.gitee.io/docs/tutorials/module-development/module-slimming/#%E4%B8%80%E9%94%AE%E8%87%AA%E5%8A%A8%E7%98%A6%E8%BA%AB), download the packaging configuration file **rules.txt**, and place it in the **conf/ark/** directory.

3. Develop the module, for example, add a Rest Controller to provide a REST interface.
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

4. [Click here to download Arkctl](https://github.com/koupleless/koupleless/releases/tag/arkctl-release-0.1.0), For Mac/Linux computers, place it in the `/usr/local/bin` directory. For Windows, consider placing it directly in the project root directory.

5. Execute `arkctl deploy` to build and deploy. After successful deployment, verify the service response by `curl localhost:8080/${module1 web context path}/`. If it displays correctly, proceed to the next step.
```
hello to ${module1 name} deploy
```

### 3. Module 1 Development and Validation
Development and validation require **modifying code and publishing version 2**. Follow these steps:

1. Modify the Rest code
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

2. Execute `arkctl deploy` to build and deploy. After successful deployment, verify the service response by `curl localhost:8080/${module1 web context path}/`.
```
hello to ${module1 name} deploy v2
```

### 4. Module 2 Access Transformation, Development, and Validation
Module 2 follows the same steps as mentioned in steps 2️⃣ and 3️⃣ for Module 1, i.e., Module 2 Access Transformation and Validation.

## Scenario Two: Middleware Application
The characteristic of a middleware application is that the foundation has complex orchestration logic to define the SPI (Service Provider Interface) needed for external exposure of services and business. Module applications implement these SPI interfaces, often defining multiple different implementations for one interface in multiple modules. The overall process is as follows:
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/96256555/1700706388244-f46fbd3d-262d-4a9d-894a-243782860cce.png#clientId=ue666f90d-083a-4&from=paste&height=310&id=u76e59b07&originHeight=620&originWidth=1762&originalType=binary&ratio=2&rotation=0&showTitle=false&size=345022&status=done&style=none&taskId=ue115f7a7-de5a-4c01-91d4-0965c2422dc&title=&width=881)

Unlike the combined deployment operation in Scenario One, an additional step is required in the middle of the Basic and Module access transformation and development verification, which is the definition of communication classes and SPI.
### 1. Basic Completion of Communication Class and SPI Definitions
Building on the combined deployment access transformation, communication class and SPI definitions need to be completed.
Communication classes need to exist as independent bundles to be imported by modules. You can refer to the following approach:

1. Create a new bundle and define interface classes.
```java
public class ProductInfo {
    private String  name;
    private String  author;
    private String  src;
    private Integer orderCount;
}
```

### 2. Define SPI
```java
public interface StrategyService {
    List<ProductInfo> strategy(List<ProductInfo> products);
    String getAppName();
}
```

### 2. Module 1 Integrate Communication Class Foundation and Implement Foundation SPI
Building on the previous demonstration of Module 1's access transformation for combined deployment, we will now introduce communication classes and define SPI implementations.

1. Integrate the communication class and corresponding SPI definition. Only **import the communication bundle defined by the foundation into the pom.xml**.
2. Define the SPI implementation.
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

3. Execute `arkctl deploy` for building and deployment. After successful deployment, verify the service return using `curl localhost:8080/${foundation service entrance}/biz1/`.

The parameter **biz1** is used to allow the foundation to find different SPI implementations based on different parameters and execute different logic. There are many ways to pass parameters, and here we use the simplest method—passing it from the **path**.
```
Default products list
```

### 3. Module 2 **Integrate Communication Class Foundation and Implement Foundation SPI**
The steps for Module 2 are the same as Module 1. It is essential to note that after executing `arkctl deploy` for building and deployment, successful deployment should be verified using `curl localhost:8080/${foundation service entrance}/biz2/`. Similarly, **biz2** is passed to enable the foundation to find different SPI implementations based on different parameters and execute different logic.
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
Modified products list after sorting
```

With these steps completed, you can proceed with the Module [**Development and Verification**](https://koupleless.gitee.io/docs/tutorials/trial_step_by_step/) mentioned earlier in the document. The overall process is smooth and easy to follow. Feel free to give it a try!

## Links in the Document

1. Sample project address: [https://github.com/koupleless/koupleless/tree/master/samples/springboot-samples/web/tomcat](https://github.com/koupleless/koupleless/tree/master/samples/springboot-samples/web/tomcat)
2. `web-ark-plugin` principles: [https://www.sofastack.tech/projects/sofa-boot/sofa-ark-multi-web-component-deploy/](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-multi-web-component-deploy/)
3. Principles and configuration file download for automatic slimming: [https://koupleless.gitee.io/docs/tutorials/module-development/module-slimming/#%E4%B8%80%E9%94%AE%E8%87%AA%E5%8A%A8%E7%98%A6%E8%BA%AB](https://koupleless.gitee.io/docs/tutorials/module-development/module-slimming/#%E4%B8%80%E9%94%AE%E8%87%AA%E5%8A%A8%E7%98%A6%E8%BA%AB)
4. Arkctl download address: [https://github.com/koupleless/koupleless/releases/tag/arkctl-release-0.1.0](https://github.com/koupleless/koupleless/releases/tag/arkctl-release-0.1.0)
5. Document address: [https://koupleless.gitee.io/docs/tutorials/trial_step_by_step/](https://koupleless.gitee.io/docs/tutorials/trial_step_by_step/)
