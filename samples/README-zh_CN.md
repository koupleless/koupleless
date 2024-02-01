<div align="center">

[English](./README.md) | 简体中文

</div>

## Koupleless 模块中间件使用样例清单

| 框架          | 中间件                    | 说明                                          | 代码样例                                                                                                                                                     |
|-------------|------------------------|---------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| springboot  | spring context manager | 跨模块bean调用                                   | [samples/springboot-samples/service](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/service)                              |  
| springboot  | tomcat                 | 单host模式                                     | [samples/springboot-samples/web/tomcat/](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/tree/master/samples/web/tomcat)                          | 
| springboot  | webflux                | 单 host 模式                                   | [samples/springboot-samples/web/webflux/](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/tree/master/samples/web/webflux) |
| springboot  | log4j2                 | 基座与模块独立日志目录                                 | [samples/springboot-samples/logging/log4j2](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/logging/log4j2)                |
| springboot  | logback                | 基座与模块独立日志目录                                 | [samples/springboot-samples/logging/logback](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/logging/logback)              |  
| springboot  | kafka                  | 模块独立使用 kafka                                | [samples/springboot-samples/msg/kafka](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/msg/kafka/)                         |
| springboot  | rocketmq               | 模块与基座可以共同生产和消费消息                            | [samples/springboot-samples/msg/rocketmq](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/msg/rocketmq/)                   |
| springboot  | mybatis                | 1. 模块使用独立数据源 2. 模块复用基座数据源                   | [samples/springboot-samples/db/mybatis](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/db/mybatis)                        |
| springboot  | mongo                  | 1. 模块独立使用数据源， 2. 模块复用基座数据源                  | [samples/springboot-samples/db/mongo](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/db/mongo)                            |
| springboot  | apollo 1.x             | 1. 模块独立使用不同 appId 的配置， 2. 模块与基座共用 appId 的配置 | [samples/springboot-samples/config/apollo](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/config/apollo)                  |
| springboot  | slimming               | 模块瘦身                                        | [samples/springboot-samples/slimming/log4j2](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/slimming/log4j2)              |
| springboot  | redis                  | 模块使用 redis                                  | [samples/springboot-samples/cache/redis](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/cache/redis)                      |
| springboot  | ehcache                | 模块使用 ehcache                                | [samples/springboot-samples/cache/ehcache](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/cache/ehcache)                  |
| springboot  | nacos                  | 模块使用 nacos                                  | [samples/springboot-samples/config/nacos](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/config/nacos)                    |
| springcloud | gateway                | 模块与基座同时使用 gateway 配置路由规则                    | [samples/springboot-samples/springcloud/gateway](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/springcloud/gateway)      |
| dubbo       | dubbo + grpc           | 模块使用 grpc                                   | [samples/dubbo-samples/rpc/grpc](https://github.com/koupleless/koupleless/tree/main/samples/dubbo-samples/rpc/grpc)                                      |
| sofaboot    | sofarpc/tomcat         | 基座调用模块、中台模式                                 | [samples/sofaboot-samples/dynamic-stock](https://github.com/koupleless/koupleless/tree/main/samples/sofaboot-samples/dynamic-stock)                      | 
| springboot3 | springboot3            | springboot3                                 | [samples/springboot3-samples](https://github.com/koupleless/koupleless/tree/main/samples/springboot3-samples)                                            |       |
### 注意请在编译器单独导入对应 samples 工程，例如 koupleless/samples/springboot-samples 工程，否则会出现 BizRuntimeContext Not found 的报错。
![biz runtime context not found](bizruntimecontext_not_found.png)
原因：samples 工程与 koupleless-runtime 在一个工程目录里，会优先使用本地的 koupleless-runtime，而不是 maven 依赖的 koupleless-runtime，导致找不到 BizRuntimeContext 类。
