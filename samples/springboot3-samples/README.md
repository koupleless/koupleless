## SOFAServerless 支持中间件列表与样例


| 中间件                    | 说明           | 代码样例                                                                                                                                           |
|------------------------|--------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| tomcat                 | 单host模式      | [samples/springboot3-samples/web/tomcat/](https://github.com/sofastack-guides/springboot3-samples/tree/master/samples/web/tomcat)                | 
| log4j2                 | 基座与模块独立日志目录  | [samples/springboot3-samples/logging/log4j2](https://github.com/sofastack/sofa-serverless/tree/master/samples/springboot3-samples/logging/log4j2) |
| kafka                  | 模块独立使用 kafka | [samples/springboot3-samples/msg/kafka](https://github.com/sofastack/sofa-serverless/tree/master/samples/springboot3-samples/msg/kafka/)         |
| mybatis                | 模块使用独立数据源    | [samples/springboot3-samples/db/mybatis](https://github.com/sofastack/sofa-serverless/tree/master/samples/springboot3-samples/db/mybatis)   |

### 注意请在编译器单独导入sofa-serverless/samples/springboot-samples 工程，否则会出现 BizRuntieContext Not found 的报错。
![biz runtime context not found](bizruntimecontext_not_found.png)
原因：samples 工程与 sofa-serverless-runtime 在一个工程目录里，会优先使用本地的 sofa-serverless-runtime，而不是 maven 依赖的 sofa-serverless-runtime，导致找不到 BizRuntimeContext 类。

### 支持 Java EE/Jakarta EE
随着 Oracle 将 Java EE 交给 Eclipse 开始 Java EE 就成为了历史，取而代之的是新的 Jakarta EE 系列。对于开发者而言，最大的改变便是从 jakarta EE 9 开始， EE 相关的类的命名空间从 javax.* 迁移到了 jakarta.*。
对于这一变更，开源社区的组件也均在不同进度的进行升级适配：例如 Tomcat 从 10.x 版本开始便迁移到了 jakarta 命名空间。而 SpringBoot 3.x 版本也不再支持 Java EE 8，而是基于 Jakarta EE 9 构建。 为了支持 Jakarta EE，sofa-ark 将从 2.2.4 开始发布后缀为 `-jakarta`的包。

sofa-ark 中 `sofa-ark-springboot-starter` 原依赖写法
```
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>sofa-ark-springboot-starter</artifactId>
    <version>2.2.4-SNAPSHOT</version>
</dependency>
```
现依赖写法
```
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>sofa-ark-springboot-starter</artifactId>
    <version>2.2.4-jakarta-SNAPSHOT</version>
</dependency>
```
