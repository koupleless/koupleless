---
title: What happens if a module independently introduces part of the SpringBoot framework?
date: 2024-01-25T10:28:32+08:00
description: What happens if a module independently introduces part of the SpringBoot framework in Koupleless?
weight: 100
---

Since the logic of multi-module runtime is introduced and loaded in the base, such as some Spring Listeners. If the module starts using its own SpringBoot entirely, there may be some class conversion or assignment judgment failures, for example:

## CreateSpringFactoriesInstances
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695020207040-788742b8-a1b1-4dc8-8ac2-f0675cf070d5.png#clientId=ufd4bb4ce-38f3-4&from=paste&height=280&id=EvWYQ&originHeight=560&originWidth=2778&originalType=binary&ratio=2&rotation=0&showTitle=false&size=201445&status=done&style=none&taskId=u342062f0-1d50-4344-9990-2377b42e6ca&title=&width=1389)

name = 'com.alipay.sofa.ark.springboot.listener.ArkApplicationStartListener', ClassUtils.forName gets the class from the base ClassLoader.<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695020357927-660e3462-1bd7-4ede-9955-541b63caf650.png#clientId=ufd4bb4ce-38f3-4&from=paste&height=308&id=DwdJg&originHeight=616&originWidth=1786&originalType=binary&ratio=2&rotation=0&showTitle=false&size=132500&status=done&style=none&taskId=u870534d8-591d-4685-bdef-19aeb287535&title=&width=893)<br />However, the type is loaded when the module starts, which means it is loaded using the module's BizClassLoader.<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695020334793-d8be43cc-b791-4aef-bb75-b8c890cbe82c.png#clientId=ufd4bb4ce-38f3-4&from=paste&height=400&id=q2juJ&originHeight=800&originWidth=1612&originalType=binary&ratio=2&rotation=0&showTitle=false&size=165924&status=done&style=none&taskId=u52077367-d352-49fc-9d49-a6ac0cf539b&title=&width=806)<br />At this point, performing an isAssignable check here will cause an error.
```xml
com.alipay.sofa.koupleless.plugin.spring.ServerlessApplicationListener is not assignable to interface org.springframework.context.ApplicationListener
```

So the module framework part needs to be delegated to the base to load.


<br/>
<br/>
