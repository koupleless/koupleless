# Overview

# Cross-context calls

There is spring context isolation between the base and the module, and between the module and the module, and each other's beans will not conflict or be invisible. However, in many application scenarios, such as mid-end mode and independent module mode, there are scenarios where the base calls the module, the module calls the base, and the modules call each other.

## Base calls module

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

## Module calls base

### @AutowiredFromBase

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

### SpringServiceFinder

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

## Module calls module

Reference module calling base, @AutowiredFromBiz and SpringServiceFinder can be used.


> See the use case demo：
> 
> base:
> 
> git:  https://github.com/sofastack-guides/sofa-ark-spring-guides.git
> 
> branch: test_spring_service
> 
> module:
> 
> git:  https://github.com/sofastack-guides/spring-boot-ark-biz.git
> 
> branch: test_spring_service

