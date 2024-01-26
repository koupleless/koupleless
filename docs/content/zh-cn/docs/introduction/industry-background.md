---
title: 行业背景
date: 2024-01-25T10:28:32+08:00
description: Koupleless 背景
weight: 200
---

## 微服务的问题
应用架构从单体应用发展到微服务，结合软件工程从瀑布模式到当前的 DevOps 模式的发展，解决了可扩展、分布式、分工协作等问题，为企业提供较好的敏捷性与执行效率，带来了明显的价值。但该模式发展至今，虽然解决了一些问题，也有微服务的一些问题慢慢暴露出来，在当前已经得到持续关注：

### 基础设施复杂

#### 认知负载高
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695086284703-13a90661-9735-4daa-a7ec-dfc3a28ca2bd.png#clientId=ue95e757a-3cd6-4&from=paste&height=260&id=ubf4cf860&originHeight=942&originWidth=1738&originalType=binary&ratio=2&rotation=0&showTitle=false&size=404365&status=done&style=none&taskId=udcdc41a4-9949-4f53-98ca-e722e63bfc8&title=&width=479)<br />当前业务要完成一个需求，背后实际上有非常多的依赖、组件和平台在提供各种各样的能力，只要这些业务以下的某一个组件出现异常被业务感知到，都会对业务研发人员带来较大认知负担和对应恢复的时间成本。<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695086591548-8ac5f4b6-b5e2-4ba4-aa1e-35ff6816634a.png#clientId=ue95e757a-3cd6-4&from=paste&height=200&id=ub7a3e5b4&originHeight=596&originWidth=582&originalType=binary&ratio=2&rotation=0&showTitle=false&size=415294&status=done&style=none&taskId=u6e187ff5-dade-4172-83e4-38a90d4ad38&title=&width=195)<br />异常种类繁多

#### 运维负担重
业务包含的各个依赖也会不断迭代升级，例如框架、中间件、各种 sdk 等，在遇到

1. 重要功能版本发布
2. 修复紧急 bug
3. 遇到重大安全漏洞

等情况时，这些依赖的新版本就需要业务尽可能快的完成升级，这造成了两方面的问题：

##### 对于业务研发人员
这些依赖的升级如果只是一次两次那么就不算是问题，但是一个业务应用背后依赖的框架、中间件与各类 sdk 是很多的，每一个依赖发布这些升级都需要业务同学来操作，这么多个依赖的话长期上就会对业务研发同学来说是不小的运维负担。另外这里也需要注意到业务公共层对业务开发者来说也是不小的负担。

##### 对于基础设施人员
类似的对于各个依赖的开发人员自身，每发布一个这样的新版本，需要尽可能快的让使用的业务应用完成升级。但是业务研发人员更关注业务需求交付，想要推动业务研发人员快速完成升级是不太现实的，特别是在研发人员较多的企业里。

#### 启动慢
每个业务应用启动过程都需要涉及较多过程，造成一个功能验证需要花费较长等待时间。<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695088271232-52d649a0-0e21-46b3-aaf4-43d0d908d279.png#clientId=ue95e757a-3cd6-4&from=paste&height=83&id=uf009ae3a&originHeight=180&originWidth=1234&originalType=binary&ratio=2&rotation=0&showTitle=false&size=52685&status=done&style=none&taskId=u56e65597-48ba-47f8-b8b6-c69a8ceebf3&title=&width=570)

#### 发布效率低
由于上面提到的启动慢、异常多的问题，在发布上线过程中需要较长时间，出现异常导致卡单需要恢复处理。发布过程中除了平台异常外，机器异常发生的概率会随着机器数量的增多而增多，假如一台机器正常完成发布（不发生异常）的概率是 99.9%，也就是一次性成功率为 99.9%，那么100台则是 90%，1000台则降低到了只有 36.7%，所以对于机器较多的应用发布上线会经常遇到卡单的问题，这些都需要研发人员介入处理，导致效率低。

### 协作与资源成本高

#### 单体应用/大应用过大

![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695109775918-de436da0-8187-45a8-a30a-62177a55181e.png#clientId=u02591eed-2e18-4&from=paste&height=106&id=u28baf164&originHeight=304&originWidth=1412&originalType=binary&ratio=2&rotation=0&showTitle=false&size=97660&status=done&style=none&taskId=u468dfc48-8b76-484e-abb6-36aed56dfd8&title=&width=494)

##### 多人协作阻塞
业务不断发展，应用会不断变大，这主要体现在开发人员不断增多，出现多人协作阻塞问题。

##### 变更影响面大，风险高
业务不断发展，线上流量不断增大，机器数量也不断增多，但当前一个变更可能影响全部代码和机器流量，变更影响面大风险高。

#### 小应用过多
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695111071288-b27e64a3-ff6f-4457-9353-5a4b337faccf.png#clientId=u02591eed-2e18-4&from=paste&height=110&id=ua230cdfe&originHeight=302&originWidth=1404&originalType=binary&ratio=2&rotation=0&showTitle=false&size=76471&status=done&style=none&taskId=ua211c1f6-fe53-43fa-8be8-7da9a92e8cb&title=&width=512)<br />在微服务发展过程中，随着时间的推移，例如部分应用拆分过多、某些业务萎缩、组织架构调整等，都会出现线上小应用或者长尾应用不断积累，数量越来越多，像蚂蚁过去3年应用数量增长了 3倍。<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695111122234-8a85eee7-bdf5-40c6-85e2-5955413f9c7d.png#clientId=u02591eed-2e18-4&from=paste&height=177&id=uf7c75dd0&originHeight=1182&originWidth=1538&originalType=binary&ratio=2&rotation=0&showTitle=false&size=140920&status=done&style=none&taskId=uaadf29d5-7052-4316-9073-5ce5a4f92d4&title=&width=230)


##### 资源成本高
这些应用每个机房都需要几台机器，但其实流量也不大，cpu 使用率很低，造成资源浪费。

##### 长期维护成本
这些应用同样需要人员来维度，例如升级 SDK，修复安全漏洞等，长期维护成本高。

#### 问题必然性
微服务系统是个生态，在一个公司内发展演进几年后，参考28定律，少数的大应用占有大量的流量，不可避免的会出现大应用过大和小应用过多的问题。<br />然而大应用多大算大，小应用多少算多，这没有定义的标准，所以这类问题造成的研发人员的痛点是隐匿的，没有痛到一定程度是较难引起公司管理层面的关注和行动。


### 如何合理拆分微服务
微服务如何合理拆分始终是个老大难的问题，合理拆分始终没有清晰的标准，这也是为何会存在上述的大应用过大、小应用过多问题的原因。而这些问题背后的根因是业务与组织灵活，与微服务拆分的成本高，两者的敏捷度不一致。

#### 微服务的拆分与业务和组织发展敏捷度不一致
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695113016136-8d591312-1300-496e-9df8-a5ed1a49abe4.png#clientId=u02591eed-2e18-4&from=paste&height=201&id=u7ce79cce&originHeight=554&originWidth=1222&originalType=binary&ratio=2&rotation=0&showTitle=false&size=182342&status=done&style=none&taskId=uf3c867d4-2d82-4922-a6d9-6572ca3a1f7&title=&width=443)<br />业务发展灵活，组织架构也在不断调整，而微服务拆分需要机器与长期维护的成本，两者的敏捷度不一致，导致容易出现未拆或过度拆分问题，从而出现大应用过大和小应用过多问题。这类问题不从根本上解决，会导致微服务应用治理过一波之后还会再次出现问题，导致研发同学始终处于低效的痛苦与治理的痛苦循环中。


### 不同体量企业面对的问题
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695131026232-1e25044b-d0d4-4a58-9d03-ef665365fbc6.png#clientId=ucec7e736-7c4f-4&from=paste&height=511&id=uc85ea670&originHeight=1022&originWidth=3766&originalType=binary&ratio=2&rotation=0&showTitle=false&size=244352&status=done&style=none&taskId=u18416169-fc43-47a4-8486-9e5e328552c&title=&width=1883)

### 行业尝试的解法
当前行业里也有很多不错的思路和项目在尝试解决这些问题，例如服务网格、应用运行时、平台工程，Spring Modulith、Google ServiceWeaver，有一定的效果，但也存在一定的局限性：

1. 从业务研发人员角度看，只屏蔽部分基础设施，未屏蔽业务公共部分
2. 只解决其中部分问题
3. 存量应用接入改造成本高

Koupleless 的目的是为了解决这些问题而不断演进出来的一套研发框架与平台能力。

<br/>
<br/>
