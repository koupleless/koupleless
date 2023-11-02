---
title: 如何发布 Arklet 版本
date: 2023-09-21T10:28:35+08:00
weight: 200
---


### 触发 github Action 发布到 snapshot staging
版本发布到 maven 中央仓库，发布能力集成到了 github action 里：<br />
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1694917403997-feefe1c0-9aff-440c-afca-58f7dce10c19.png#clientId=u0869f56c-af1d-4&from=paste&height=469&id=u1538d344&originHeight=938&originWidth=2114&originalType=binary&ratio=2&rotation=0&showTitle=false&size=193270&status=done&style=none&taskId=u851fd385-a349-4f24-a37a-ded4c025441&title=&width=1057)

**该 action 需要手动触发执行**：<br />
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1694917427074-e50ce7a6-44f7-4f7b-abc8-f78142727b28.png#clientId=u0869f56c-af1d-4&from=paste&height=283&id=u51c5094e&originHeight=566&originWidth=1004&originalType=binary&ratio=2&rotation=0&showTitle=false&size=55468&status=done&style=none&taskId=u83b68510-dcdd-4dae-9ab1-c1d2d7b34df&title=&width=502)<br />
执行成功后，只会发布到 snapshot staging，如果是 SNAPSHOT 版本，则这里执行完就可以结束。如果是正式版本，发布到 snapshot staging 之后，还需要推送到 release staging。


### 发布到 Release staging
打开  [https://oss.sonatype.org](https://oss.sonatype.org) ，点击右上角的 Log In, 登陆信息可找管理员。<br />
点击左侧的 Staging Repositories：<br />
![](https://gw.alipayobjects.com/zos/skylark/4f0bacb1-599d-4a3b-90f4-8d88bba2f660/2018/png/d27b8f43-c7eb-41a9-a9c4-49b0308c44d8.png#height=250&id=kxutu&originHeight=431&originWidth=1294&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=&width=752)

搜索刚才记录的 ID：<br />
![](https://gw.alipayobjects.com/zos/skylark/2c91c79e-fce1-4483-8c35-c712d5367805/2018/png/ae5425f2-a572-4c50-9307-fd9618286689.png#height=394&id=O3wmg&originHeight=749&originWidth=1428&originalType=binary&ratio=1&rotation=0&showTitle=false&status=done&style=none&title=&width=752)

钩上之后就可以进行 Release (发布) 或者 Drop (放弃) 的操作。

**当看不到这两个选项，只有 Close 选项时，则先选择 Close 操作，这时候如果包没有问题，则接下来可以 Release 或者 Drop。如果有问题，下面的内容中的 Activity 中会显示包不能正常 Close 的原因, 按照提示进行修改就可以了。**


### 仓库包同步与搜索
在包发布到 release 仓库之后， 10 分钟后包会更新，在 [http://central.maven.org/maven2/com/alipay/sofa/](http://central.maven.org/maven2/com/alipay/sofa/) 能看到包。2 小时之后，可通过 [搜索](http://search.maven.org/) 查询到包。


<br/>
