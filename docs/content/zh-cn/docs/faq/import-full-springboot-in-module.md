---
title: 如果模块独立引入 SpringBoot 框架部分会怎样？
date: 2024-01-25T10:28:32+08:00
description: Koupleless 模块独立引入 SpringBoot 框架部分会怎样？
weight: 100
---

由于多模块运行时的逻辑在基座引入和加载，例如一些 Spring 的 Listener。如果模块启动使用完全自己的 SpringBoot，则会出现一些类的转换或赋值判断失败，例如：

## CreateSpringFactoriesInstances
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695020207040-788742b8-a1b1-4dc8-8ac2-f0675cf070d5.png#clientId=ufd4bb4ce-38f3-4&from=paste&height=280&id=EvWYQ&originHeight=560&originWidth=2778&originalType=binary&ratio=2&rotation=0&showTitle=false&size=201445&status=done&style=none&taskId=u342062f0-1d50-4344-9990-2377b42e6ca&title=&width=1389)

name = 'com.alipay.sofa.ark.springboot.listener.ArkApplicationStartListener', ClassUtils.forName 获取到的是从基座 ClassLoader 的类<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695020357927-660e3462-1bd7-4ede-9955-541b63caf650.png#clientId=ufd4bb4ce-38f3-4&from=paste&height=308&id=DwdJg&originHeight=616&originWidth=1786&originalType=binary&ratio=2&rotation=0&showTitle=false&size=132500&status=done&style=none&taskId=u870534d8-591d-4685-bdef-19aeb287535&title=&width=893)<br />而 type 是模块启动时加载的，也就是使用模块 BizClassLoader 加载。<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695020334793-d8be43cc-b791-4aef-bb75-b8c890cbe82c.png#clientId=ufd4bb4ce-38f3-4&from=paste&height=400&id=q2juJ&originHeight=800&originWidth=1612&originalType=binary&ratio=2&rotation=0&showTitle=false&size=165924&status=done&style=none&taskId=u52077367-d352-49fc-9d49-a6ac0cf539b&title=&width=806)<br />此时这里做 isAssignable 判断，则会报错。
```xml
com.alipay.sofa.koupleless.plugin.spring.ServerlessApplicationListener is not assignable to interface org.springframework.context.ApplicationListener
```

所以模块框架这部分需要委托给基座加载。


<br/>
<br/>
