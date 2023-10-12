---
title: 模块与模块、模块与基座通信
weight: 300
---

基座与模块之间、模块与模块之间存在 spring 上下文隔离，互相的 bean 不会冲突、不可见。然而很多应用场景比如中台模式、独立模块模式等存在基座调用模块、模块调用基座、模块与模块互相调用的场景。

# Spring 环境

## 基座调用模块

```java
@RestController
public class SampleController {
    
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {

        Provider studentProvider = SpringServiceFinder.getModuleService("spring-boot-ark-biz", "0.0.1-SNAPSHOT",
                "studentProvider");
        Result result = studentProvider.provide(new Param());
        
        Provider teacherProvider = SpringServiceFinder.getModuleService("spring-boot-ark-biz", "0.0.1-SNAPSHOT",
                TeacherProvider.class);
        Result result1 = teacherProvider.provide(new Param());

        Map<String, TeacherProvider> teacherProviderMap = SpringServiceFinder.listModuleServices("spring-boot-ark-biz", "0.0.1-SNAPSHOT",
                TeacherProvider.class);
        for (String beanName : teacherProviderMap.keySet()) {
            Result result2 = teacherProviderMap.get(beanName).provide(new Param());
            System.out.println(result2.getClass());
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

    @AutowiredFromBase
    private EnvClient envClient;

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

        envClient.getEnv();

        return "hello to ark dynamic deploy";
    }
}
```

### 方式二：编程API SpringServiceFinder

```java
@RestController
public class SampleController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {

        SampleService sampleServiceImplFromFinder = SpringServiceFinder.getBaseService("sampleServiceImpl");
        String result = sampleServiceImplFromFinder.service();
        System.out.println(result);

        Map<String, SampleService> sampleServiceMapFromFinder = SpringServiceFinder.listBaseServices(SampleService.class);
        for (String beanName : sampleServiceMapFromFinder.keySet()) {
            String result1 = sampleServiceMapFromFinder.get(beanName).service();
            System.out.println(result1);
        }

        return "hello to ark dynamic deploy";
    }
}
```

## 模块调用模块

参考模块调用基座，注解使用 @AutowiredFromBiz 和 编程API支持 SpringServiceFinder。

### 方式一：注解 @AutowiredFromBiz

```java
@RestController
public class SampleController {

    @AutowiredFromBiz(name = "studentProvider", bizName = "spring-boot-ark-biz", bizVersion = "0.0.1-SNAPSHOT")
    private Provider studentProvider;

    @AutowiredFromBiz(name = "teacherProvider", bizName = "spring-boot-ark-biz")
    private Provider teacherProvider;

    @AutowiredFromBiz
    private List<Provider> providerList;

    @AutowiredFromBiz
    private Map<String, Provider> providerMap;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {

        Result result = studentProvider.provide(new Param());

        Result result1 = teacherProvider.provide(new Param());

        for (Provider provider : providerList) {
            Result result = studentProvider.provide(new Param());
        }

        for (String beanName : providerMap.keySet()) {
            Result result = providerMap.get(beanName).provide(new Param());
        }

        return "hello to ark dynamic deploy";
    }
}
```

### 方式二：编程API SpringServiceFinder

```java
import java.security.Provider;

@RestController
public class SampleController {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String hello() {

        Provider studentProvider = SpringServiceFinder.getModuleService("spring-boot-ark-biz", "0.0.1-SNAPSHOT", "studentProvider");
        Result result = studentProvider.provide(new Param());

        Map<String, Provider> providerMap = SpringServiceFinder.listModuleServices("spring-boot-ark-biz", "0.0.1-SNAPSHOT", Provider.class);
        for (String beanName : providerMap.keySet()) {
            Result result1 = providerMap.get(beanName).provide(new Param());
        }

        return "hello to ark dynamic deploy";
    }
}
```

# SOFABoot 环境

[请参考该文档](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-ark-jvm/)

<br/>
<br/>
