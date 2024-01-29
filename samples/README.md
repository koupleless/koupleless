<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

## Samples for middleware in modules

| Framework   | middleware             | explanation                                                     | location of samples                                                                                                                                      |
|-------------|------------------------|-----------------------------------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| springboot  | spring context manager | bean and service invocation across modules                      | [samples/springboot-samples/service](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/service)                              |  
| springboot  | tomcat                 | single host mode                                                | [samples/springboot-samples/web/tomcat/](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/tree/master/samples/web/tomcat)   | 
| springboot  | webflux                | single host mode                                                | [samples/springboot-samples/web/webflux/](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/tree/master/samples/web/webflux) |
| springboot  | log4j2                 | log into different dir for base and modules                     | [samples/springboot-samples/logging/log4j2](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/logging/log4j2)                |
| springboot  | logback                | log into different dir for base and modules                     | [samples/springboot-samples/logging/logback](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/logging/logback)              |  
| springboot  | kafka                  | using kafka in modules                                          | [samples/springboot-samples/msg/kafka](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/msg/kafka/)                         |
| springboot  | rocketmq               | producing and consuming message both in base and modules        | [samples/springboot-samples/msg/rocketmq](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/msg/rocketmq/)                   |
| springboot  | mybatis                | 1. using datasource in modules 2. reusing datasource of base in modules | [samples/springboot-samples/db/mybatis](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/db/mybatis)                        |
| springboot  | mongo                  | 1. using datasource in modules 2. reusing datasource of base in modules | [samples/springboot-samples/db/mongo](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/db/mongo)                            |
| springboot  | apollo 1.x             | 1. using different appId in base and modules 2. using same appId in base and modules | [samples/springboot-samples/config/apollo](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/config/apollo)                  |
| springboot  | slimming               | module slimming by auto exclude dependencies                    | [samples/springboot-samples/slimming/log4j2](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/slimming/log4j2)              |
| springboot  | redis                  | using redis in modules                                          | [samples/springboot-samples/cache/redis](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/cache/redis)                      |
| springboot  | ehcache                | using ehcache in modules                                        | [samples/springboot-samples/cache/ehcache](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/cache/ehcache)                  |
| springboot  | nacos                  | using nacos in modules                                          | [samples/springboot-samples/config/nacos](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/config/nacos)                    |
| springcloud | gateway                | using router of gateway in both base and modules                | [samples/springboot-samples/springcloud/gateway](https://github.com/koupleless/koupleless/tree/main/samples/springboot-samples/springcloud/gateway)      |
| dubbo       | dubbo + grpc           | using grpc in modules                                           | [samples/dubbo-samples/rpc/grpc](https://github.com/koupleless/koupleless/tree/main/samples/dubbo-samples/rpc/grpc)                                      |
| sofaboot    | sofarpc/tomcat         | bean and service invocation from base to modules                | [samples/sofaboot-samples/dynamic-stock](https://github.com/koupleless/koupleless/tree/main/samples/sofaboot-samples/dynamic-stock)                      | 
| springboot3 | springboot3            | springboot3                                                     | [samples/springboot3-samples](https://github.com/koupleless/koupleless/tree/main/samples/springboot3-samples)                                            |       |

### Please import the corresponding samples project in the IDEA separately, such as the koupleless/samples/springboot-samples project, otherwise the BizRuntimeContext Not found error will occur when install bizs.
![biz runtime context not found](bizruntimecontext_not_found.png)

Reason: the samples project and koupleless-runtime are in the same project directory, so the local koupleless-runtime will be used first, instead of the maven dependency koupleless-runtime, which leads to the failure to find the BizRuntimeContext class.
