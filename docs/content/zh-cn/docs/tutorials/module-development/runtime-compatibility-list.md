---
title: 模块中官方验证并兼容的中间件客户端
weight: 800
---

在 SOFAServerless 模块中，官方目前支持并兼容常见的中间件客户端。<br />**注意**，这里 “**已经支持**” 需要在基座 POM 中引入相关客户端依赖（**强烈建议使用 SpringBoot Starter 方式引入相关依赖**），同时在模块 POM 中也引入相关依赖并设置 **<scope>provided</scope>** 将依赖委托给基座加载。

<br/>

| 中间件客户端 | 版本号 | 备注 |
| --- | --- | --- |
| JDK | 8.x<br />11.x<br />17.x | - [x] 已经支持<br />8.x 和 11.x 支持 SpringBoot<br />17.x 当前仅支持 SOFABoot，17.x + SpringBoot 正在支持中 |
| SpringBoot | 2.x<br />3.x | - [x] 已经支持<br /> |
| SOFABoot | >= 3.9.0  或<br />>= 4.0.0 | - [x] 已经支持<br /> |
| log4j2 | 任意 | <br />- [x] 已经支持。在基座和模块引入 log4j2，并额外引入依赖：<br/>&lt;dependency&gt;<br/>&nbsp;&nbsp;&lt;groupId&gt;com.alipay.sofa.serverless&lt;/groupId&gt;<br/>&nbsp;&nbsp;&lt;artifactId&gt;sofa-serverless-adapter-log4j2&lt;/artifactId&gt;<br/>&nbsp;&nbsp;&lt;version&gt;${最新版 SOFAServerless 版本}&lt;/version&gt;<br/>&nbsp;&nbsp;&lt;scope&gt;provided&lt;/scope&gt; &lt;!-- 模块需要 provided --&gt;<br/>&nbsp;&nbsp;&lt;/dependency&gt;<br/>基座和模块完整使用样例[参见此处](https://github.com/sofastack/sofa-serverless/blob/master/samples/logging/README.md) |
| slf4j-api | 1.x 且 >= 1.7 | - [x] 已经支持<br /> |
| tomcat | 7.x、8.x、9.x <br />及以上均可 | - [x] 已经支持<br /> |
| netty | 4.x | - [x] 已经支持<br /> |
| sofarpc | >= 5.8.6 | - [x] 已经支持<br /> |
| dubbo | 3.x | - [x] 已经支持<br/>基座和模块完整使用样例及注意事项可[参见此处](https://github.com/sofastack/sofa-serverless/blob/master/samples/dubbo-samples/rpc/grpc) |
| grpc | 1.x 且 >= 1.42 | - [x] 已经支持<br/>基座和模块完整使用样例及注意事项可[参见此处](https://github.com/sofastack/sofa-serverless/blob/master/samples/dubbo-samples/rpc/grpc) |
| protobuf-java | 3.x 且 >= 3.17 | - [x] 已经支持<br/>基座和模块完整使用样例及注意事项可[参见此处](https://github.com/sofastack/sofa-serverless/blob/master/samples/dubbo-samples/rpc/grpc) |
| apollo | 1.x 且 >= 1.6.0 | 验证进行中 |
| kafka-client | >= 2.8.0  或<br />>= 3.4.0 | - [x] 已经支持<br />基座和模块完整使用样例可[参见此处](https://github.com/sofastack/sofa-serverless/blob/master/samples/logging/README.md) |
| rocketmq | 4.x 且 >= 4.3.0 | 验证进行中 |
| xxl-job | 2.x 且 >= 2.1.0 | - [x] 已经支持 |
| mybatis | >= 2.2.2  或<br />>= 3.5.12 | 理论支持，验证进行中 |
| hibernate | 5.x 且 >= 5.6.15 | - [x] 已经支持<br /> |
| druid | 1.x 且 >= 1.2.3 | 理论支持，验证进行中 |
| mysql-connector-java | 8.x | 理论支持，验证进行中 |
| postgresql | 42.x 且 >= 42.3.8 | - [x] 已经支持<br /> |
| jedis | 3.x 且 >= 3.3.0 | - [ ] 验证进行中 |
| opentracing | 0.x 且 >= 0.32.0 | - [x] 已经支持<br /> |
| elasticsearch | 7.x 且 >= 7.6.2 | - [x] 已经支持<br /> |
| jaspyt | 1.x 且 >= 1.9.3 | - [ ] 适配进行中<br /> |
| io.kubernetes:client | 10.x 且 >= 10.0.0 | - [x] 已经支持<br /> |
| net.java.dev.jna | 5.x 且 >= 5.12.1 | - [x] 已经支持<br /> |
