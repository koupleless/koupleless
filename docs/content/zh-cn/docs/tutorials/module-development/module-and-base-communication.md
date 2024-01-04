---
title: 模块与模块、模块与基座通信
weight: 300
---

基座与模块之间、模块与模块之间存在 spring 上下文隔离，互相的 bean 不会冲突、不可见。然而很多应用场景比如中台模式、独立模块模式等存在基座调用模块、模块调用基座、模块与模块互相调用的场景。
当前支持3种方式调用，@AutowiredFromBiz, @AutowiredFromBase, SpringServiceFinder 方法调用，注意三个方式使用的情况不同。

# Spring 环境

## 模块里引入依赖

```xml
<dependency>
    <groupId>com.alipay.sofa.serverless</groupId>
    <artifactId>sofa-serverless-app-starter</artifactId>
    <version>0.5.6</version>
    <scope>provided</scope>
</dependency>
```

## 基座调用模块

只能使用 SpringServiceFinder 

```java
@RestController
public class SampleController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {

        Provider studentProvider = SpringServiceFinder.getModuleService("biz", "0.0.1-SNAPSHOT",
                "studentProvider", Provider.class);
        Result result = studentProvider.provide(new Param());

        Provider teacherProvider = SpringServiceFinder.getModuleService("biz", "0.0.1-SNAPSHOT",
                "teacherProvider", Provider.class);
        Result result1 = teacherProvider.provide(new Param());
        
        Map<String, Provider> providerMap = SpringServiceFinder.listModuleServices("biz", "0.0.1-SNAPSHOT",
                Provider.class);
        for (String beanName : providerMap.keySet()) {
            Result result2 = providerMap.get(beanName).provide(new Param());
        }

        return "hello to ark master biz";
    }
}
```

## 模块调用基座

### 方式一：注解 @AutowiredFromBase

```java
@RestController
public class SampleController {

    @AutowiredFromBase(name = "sampleServiceImplNew")
    private SampleService sampleServiceImplNew;

    @AutowiredFromBase(name = "sampleServiceImpl")
    private SampleService sampleServiceImpl;

    @AutowiredFromBase
    private List<SampleService> sampleServiceList;

    @AutowiredFromBase
    private Map<String, SampleService> sampleServiceMap;

    @AutowiredFromBase
    private AppService appService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {

        sampleServiceImplNew.service();

        sampleServiceImpl.service();

        for (SampleService sampleService : sampleServiceList) {
            sampleService.service();
        }

        for (String beanName : sampleServiceMap.keySet()) {
            sampleServiceMap.get(beanName).service();
        }

        appService.getAppName();

        return "hello to ark2 dynamic deploy";
    }
}
```

### 方式二：编程API SpringServiceFinder

```java
@RestController
public class SampleController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {

        SampleService sampleServiceImplFromFinder = SpringServiceFinder.getBaseService("sampleServiceImpl", SampleService.class);
        String result = sampleServiceImplFromFinder.service();
        System.out.println(result);

        Map<String, SampleService> sampleServiceMapFromFinder = SpringServiceFinder.listBaseServices(SampleService.class);
        for (String beanName : sampleServiceMapFromFinder.keySet()) {
            String result1 = sampleServiceMapFromFinder.get(beanName).service();
            System.out.println(result1);
        }

        return "hello to ark2 dynamic deploy";
    }
}
```

## 模块调用模块

参考模块调用基座，注解使用 @AutowiredFromBiz 和 编程API支持 SpringServiceFinder。

### 方式一：注解 @AutowiredFromBiz

```java
@RestController
public class SampleController {

    @AutowiredFromBiz(bizName = "biz", bizVersion = "0.0.1-SNAPSHOT", name = "studentProvider")
    private Provider studentProvider;

    @AutowiredFromBiz(bizName = "biz", name = "teacherProvider")
    private Provider teacherProvider;

    @AutowiredFromBiz(bizName = "biz", bizVersion = "0.0.1-SNAPSHOT")
    private List<Provider> providers;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {

        Result provide = studentProvider.provide(new Param());

        Result provide1 = teacherProvider.provide(new Param());

        for (Provider provider : providers) {
            Result provide2 = provider.provide(new Param());
        }

        return "hello to ark2 dynamic deploy";
    }
}
```

### 方式二：编程API SpringServiceFinder

```java
@RestController
public class SampleController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {

        Provider teacherProvider1 = SpringServiceFinder.getModuleService("biz", "0.0.1-SNAPSHOT", "teacherProvider", Provider.class);
        Result result1 = teacherProvider1.provide(new Param());

        Map<String, Provider> providerMap = SpringServiceFinder.listModuleServices("biz", "0.0.1-SNAPSHOT", Provider.class);
        for (String beanName : providerMap.keySet()) {
            Result result2 = providerMap.get(beanName).provide(new Param());
        }

        return "hello to ark2 dynamic deploy";
    }
}
```

[完整样例](https://github.com/sofastack/sofa-serverless/tree/master/samples)

# SOFABoot 环境

[请参考该文档](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-ark-jvm/)

<br/>
<br/>
