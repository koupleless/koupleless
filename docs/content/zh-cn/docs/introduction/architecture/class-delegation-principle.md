---
title: 基座与模块间类委托加载原理介绍
date: 2024-01-25T10:28:32+08:00
description: Koupleless 基座与模块间类委托加载原理介绍
weight: 200
---

## 多模块间类委托加载
SOFAArk 框架是基于多 ClassLoader 的通用类隔离方案，提供类隔离和应用的合并部署能力。本文档并不打算介绍 SOFAArk 类隔离的[原理与机制](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-classloader/)，这里主要介绍多 ClassLoader 当前的最佳实践。<br />当前基座与模块部署在 JVM 上的 ClassLoader 模型如图：<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2022/png/149473/1653304883689-ec30b72b-1620-4a2a-8611-d6c24107afd2.png#clientId=u8aaeb3a3-ec6f-4&from=paste&height=225&id=u1df6aa1c&originHeight=450&originWidth=388&originalType=binary&ratio=1&rotation=0&showTitle=false&size=39808&status=done&style=none&taskId=uf6233ec3-9494-4b6a-b1b6-43546035a43&title=&width=194)

## 当前类委托加载机制
当前一个模块在启动与运行时查找的类，有两个来源：当前模块本身，基座。这两个来源的理想优先级顺序是，优先从模块中查找，如果模块找不到再从基座中查找，但当前存在一些特例：

1. 当前定义了一份白名单，白名单范围内的依赖会强制使用基座里的依赖。
2. 模块可以扫描到基座里的所有类：
   - 优势：模块可以引入较少依赖
   - 劣势：模块会扫描到模块代码里不存在的类，例如会扫描到一些 AutoConfiguration，初始化时由于第四点扫描不到对应资源，所以会报错。
3. 模块不能扫描到基座里的任何资源：
   - 优势：不会与基座重复初始化相同的 Bean
   - 劣势：模块启动如果需要基座的资源，会因为查找不到资源而报错，除非模块里显示引入（Maven 依赖 scope 不设置成 provided）
5. 模块调用基座时，部分内部处理传入模块里的类名到基座，基座如果存在直接从基座 ClassLoader 查找模块传入的类，会查找不到。因为委托只允许模块委托给基座，从基座发起的类查找不会再次查找模块里的。
   
### 使用时需要注意事项
模块要升级委托给基座的依赖时，需要让基座先升级，升级之后模块再升级。

## 类委托的最佳实践
类委托加载的准则是中间件相关的依赖需要放在同一个的 ClassLoader 里进行加载执行，达到这种方式的最佳实践有两种：

### 强制委托加载
由于中间件相关的依赖一般需要在同一个 ClassLoader 里加载运行，所以我们会制定一个中间件依赖的白名单，强制这些依赖委托给基座加载。

#### 使用方法
application.properties 里增加配置 `sofa.ark.plugin.export.class.enable=true`。

#### 优点
模块开发者不需要感知哪些依赖属于需要强制加载由同一个 ClassLoader 加载的依赖。

#### 缺点
白名单里要强制加载的依赖列表需要维护，列表的缺失需要更新基座，较为重要的升级需要推所有的基座升级。


### 自定义委托加载
模块里 pom 通过设置依赖的 scope 为 `provided`主动指定哪些要委托给基座加载。通过模块瘦身把与基座重复的依赖委托给基座加载，并在基座里预置中间件的依赖（可选，虽然模块暂时不会用到，但可以提前引入，以备后续模块需要引入的时候不需再发布基座即可引入）。这里：

1. 基座尽可能的沉淀通用的逻辑和依赖，特别是中间件相关以 `xxx-alipay-sofa-boot-starter `命名的依赖。
2. 基座里预置一些公共依赖（可选）。
3. 模块里的依赖如果基座里面已经有定义，则模块里的依赖尽可能的委托给基座，这样模块会更轻（提供自动模块瘦身的工具）。模块里有两种途径设置为委托给基座：
   1. 依赖里的 scope 设置为 provided，注意通过 mvn dependency:tree 查看是否还有其他依赖设置成了 compile，需要所有的依赖引用的地方都设置为 provided。
   2. biz 打包插件`sofa-ark-maven-plugin`里设置 `excludeGroupIds` 或 `excludeArtifactIds`
```xml
            <plugin>
                <groupId>com.alipay.sofa</groupId>
                <artifactId>sofa-ark-maven-plugin</artifactId>
                <configuration> 
                    <excludeGroupIds>io.netty,org.apache.commons,......</excludeGroupIds>
                    <excludeArtifactIds>validation-api,fastjson,hessian,slf4j-api,junit,velocity,......</excludeArtifactIds>
                    <declaredMode>true</declaredMode>
                </configuration>
            </plugin>
```
通过 2.a 的方法需要确保所有声明的地方 scope 都设置为provided，通过2.b的方法只要指定一次即可，建议使用方法 2.b。

4. 只有模块声明过的依赖才可以委托给基座加载。

模块启动的时候，Spring 框架会有一些扫描逻辑，这些扫描如果不做限制会查找到模块和基座的所有资源，导致一些模块明明不需要的功能尝试去初始化，从而报错。SOFAArk 2.0.3 之后新增了模块的 declaredMode, 来限制只有模块里声明过的依赖才可以委托给基座加载。只需在模块的打包插件的 Configurations 里增加  `<declaredMode>true</declaredMode>`即可。

#### 优点
不需要维护 plugin 的强制加载列表，当部分需要由同一 ClassLoader 加载的依赖没有设置为统一加载时，可以修改模块就可以修复，不需要发布基座（除非基座确实依赖）。

#### 缺点
对模块瘦身的依赖较强。


### 对比与总结
|  | 依赖缺失排查成本 | 修复成本 | 模块改造成本 | 维护成本 |
| --- | --- | --- | --- | --- |
| 强制加载 | 类转换失败或类查找失败，成本中 | 更新 plugin，发布基座，高 | 低 | 高 |
| 自定义委托加载 | 类转换失败或类查找失败，成本中 | 更新模块依赖，如果基座依赖不足，需要更新基座并发布，中 | 高 | 低 |
| 自定义委托加载 + 基座预置依赖 + 模块瘦身 | 类转换失败或类查找失败，成本中 | 更新模块依赖，设置为 provided，低 | 低 | 低 |


#### 结论：推荐自定义委托加载方式

1. 模块自定义委托加载 + 模块瘦身。
2. 模块开启 declaredMode。
3. 基座预置依赖。


## declaredMode 开启方式

### 开启条件
declaredMode 的本意是让模块能合并部署到基座上，所以开启前需要确保模块能本地启动成功。<br />如果是 SOFABoot 应用且涉及到模块调用基座服务的，本地启动因为没有基座服务，可以通过在模块 application.properties 添加这两个参数进行跳过（SpringBoot 应用无需关心）：
```properties
# 如果是 SOFABoot，则：
# 配置健康检查跳过 JVM 服务检查
com.alipay.sofa.boot.skip-jvm-reference-health-check=true
# 忽略未解析的占位符
com.alipay.sofa.ignore.unresolvable.placeholders=true
```

### 开启方式
模块打包插件里增加如下配置：<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2022/png/149473/1668428226653-d1ad571e-a580-42fa-9ca0-ff63c199dfb1.png#clientId=u664f9b10-526b-4&from=paste&height=399&id=uf9e74e96&originHeight=798&originWidth=975&originalType=binary&ratio=1&rotation=0&showTitle=false&size=116831&status=done&style=none&taskId=u2287fc36-ca94-4018-94f5-5a33dcb87b2&title=&width=487.5)

### 开启后的副作用
如果模块委托给基座的依赖里有发布服务，那么基座和模块会同时发布两份。

<br/>
