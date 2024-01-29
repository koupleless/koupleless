---
title: 复用基座拦截器
date: 2024-01-25T10:28:32+08:00
description: Koupleless 模块复用基座拦截器
weight: 500
---

# 诉求
基座中会定义很多 Aspect 切面（Spring 拦截器），你可能希望复用到模块中，但是模块和基座的 Spring 上下文是隔离的，就导致 Aspect 切面不会在模块中生效。<br/><br/>

# 解法
为原有的切面类创建一个代理对象，让模块能调用到这个代理对象，然后模块通过 AutoConfiguration 注解初始化出这个代理对象。完整步骤和示例代码如下：

### 步骤 1：
基座代码定义一个接口，定义切面的执行方法。这个接口需要对模块可见（在模块里引用相关依赖）：
```java
public interface AnnotionService {
    Object doAround(ProceedingJoinPoint joinPoint) throws Throwable;
}
```

### 步骤 2：
在基座中编写切面的具体实现，这个实现类需要加上 @SofaService 注解（SOFABoot）或者 @SpringService 注解（SpringBoot，_建设中_）：
```java
@Service
@SofaService(uniqueId = "facadeAroundHandler")
public class FacadeAroundHandler implements AnnotionService {

    private final static Logger LOG = LoggerConst.MY_LOGGER;

    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("开始执行")
        joinPoint.proceed();
        log.info("执行完成")
    }
}
```

### 步骤 3：
在模块里使用 @Aspect 注解实现一个 Aspect，SOFABoot 通过 @SofaReference 注入基座上的 FacadeAroundHandler。<br />**注意**：这里不要声明成一个 Bean，不要加 @Component 或者 @Service 注解，主需要 @Aspect 注解。
```java
//注意，这里不必申明成一个bean，不要加@Component或者@Service
@Aspect
public class FacadeAroundAspect {

    @SofaReference(uniqueId = "facadeAroundHandler")
    private AnnotionService facadeAroundHandler;

    @Pointcut("@annotation(com.alipay.linglongmng.presentation.mvc.interceptor.FacadeAround)")
    public void facadeAroundPointcut() {
    }

    @Around("facadeAroundPointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        return facadeAroundHandler.doAround(joinPoint);
    }
}
```

### 步骤 4：
使用 @Configuration 注解写个 Configuration 配置类，把模块需要的 aspectj 对象都声明成 Spring Bean。<br />**注意**：这个 Configuration 类需要对模块可见，相关 Spring Jar 依赖需要以 <scope>provided</scope> 方式引进来。
```java
@Configuration
public class MngAspectConfiguration {
    @Bean
    public FacadeAroundAspect facadeAroundAspect() {
        return new FacadeAroundAspect();
    }
    @Bean
    public EnvRouteAspect envRouteAspect() {
        return new EnvRouteAspect();
    }
    @Bean
    public FacadeAroundAspect facadeAroundAspect() {
        return new FacadeAroundAspect();
    }
    
}
```

### 步骤 5：
模块代码中显示的依赖步骤 4 创建的 Configuration 配置类 MngAspectConfiguration。
```java
@SpringBootApplication
@ImportResource("classpath*:META-INF/spring/*.xml")
@ImportAutoConfiguration(value = {MngAspectConfiguration.class})
public class ModuleBootstrapApplication {
    public static void main(String[] args) {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(ModuleBootstrapApplication.class)
        	.web(WebApplicationType.NONE);
        builder.build().run(args);
    }
}
```

<br/>
<br/>
