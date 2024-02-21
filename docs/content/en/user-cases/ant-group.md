---
title: Ant Group's Large-Scale Practice in Cost Reduction and Efficiency Improvement with Serverless
date: 2024-01-25T10:28:32+08:00
description: Koupleless's practice in Ant Group for large-scale Serverless deployment to reduce costs and improve efficiency
weight: 1000
type: docs
---
> Authors: Liu Yu, Zhao Zhenling, Liu Jing, Dai Wei, Sun Ren'en, etc.


# Pain Points in Ant Group's Business Operations

Over the past 20 years, Ant Group has experienced rapid evolution in microservices architecture, alongside explosive growth in the number and complexity of applications, leading to significant cost and efficiency issues:

1. A large number of long-tail applications have a CPU utilization rate of **less than 10%**, yet consume substantial resources due to multi-regional high availability requirements.
2. Slow build and deployment speeds for applications, averaging **10 minutes**, resulting in low development efficiency and lack of rapid **scalability**.
3. Collaborative development of applications forces features to be bundled together in a "**catching the train**" manner, causing iteration blocks and low efficiency in collaboration and delivery.
4. Upgrades to business SDKs and some frameworks cause significant **disturbances** to operations, preventing infrastructure from being minimally or unobtrusive to business operations.
5. Difficulties in capitalizing on business assets, leading to **high costs** in building large and medium platforms.



# Use Cases of Koupleless in Ant Group

## Consolidated Deployment for Cost Reduction

In enterprises, a common observation is that "**80%**" of long-tail applications only cater to "**20%**" of the traffic, and Ant Group is no exception to this trend.<br />Within Ant Group, a significant number of long-tail applications exist, each requiring at least three environments: pre-release, gray release, and production. For each environment, a minimum deployment across three data centers is necessary, with each data center maintaining at least two machines for high availability. As a result, many of these long-tail applications have a CPU utilization rate of "**less than 10%**".<br />By leveraging Koupleless, Ant Group streamlined its server infrastructure for long-tail applications, utilizing class delegation isolation, resource monitoring, and log monitoring technologies. This approach enabled the consolidated deployment of multiple applications, significantly reducing operational and resource costs while ensuring stability.<br /><img alt="合并部署。裁剪机器。" src="https://intranetproxy.alipay.com/skylark/lark/0/2023/png/671/1697010009124-285a0679-2462-434d-8d75-2aa5a7ede7be.png#clientId=u2fc31cce-a2b2-4&from=paste&height=182&id=ub16bde42&originHeight=364&originWidth=1438&originalType=binary&ratio=2&rotation=0&showTitle=false&size=163395&status=done&style=none&taskId=u4de74533-5e86-41e0-bb44-9bc8203b9c9&title=&width=719" width="700"><br />This approach allows small applications to bypass the traditional application launch and server request processes. They can be directly deployed on a common business platform, enabling rapid innovation for low-traffic services.

## Modular Development for Ultimate Efficiency Improvement

Within Ant Group, many departments have applications with a large number of developers. Due to the high headcount, there have been significant issues with environment contention, integration testing contention, and test resource contention, leading to mutual blockages where a delay by one person causes delays for many, resulting in inefficient requirement delivery.<br />By using Koupleless, Ant Group has gradually refactored applications with a large number of collaborators into foundational code and modules for different functionalities. The foundational code consolidates various SDKs and common business interfaces, maintained by dedicated personnel, while module code encapsulates specific business logic unique to a functional domain, capable of calling local foundational interfaces. Modules employ hot deployment to achieve **ten-second level** builds, releases, and scaling, while module developers **do not have to worry at all** about servers and infrastructure, thus enabling ordinary applications to achieve a **Serverless** development experience with **very low access costs**.<br />Taking the finance business of Ant Group as an example, by decomposing applications into a foundation and multiple modules, it has achieved significant efficiency improvements in release operations, organizational collaboration, and cluster traffic isolation across multiple dimensions.<br /><img alt="模块化研发提速。模块化研发提效。" src="https://intranetproxy.alipay.com/skylark/lark/0/2023/png/671/1697011295180-dfc3def2-968b-4456-95f5-447cfe6b8282.png#clientId=u2fc31cce-a2b2-4&from=paste&height=814&id=u32abf9c9&originHeight=1628&originWidth=2924&originalType=binary&ratio=2&rotation=0&showTitle=false&size=1774843&status=done&style=none&taskId=u08c84de5-b5eb-4c19-b724-9826d13e397&title=&width=1462" width="1200">

The Evolution and Practice of Ant Group's Financial Business Koupleless Architecture, see details: [https://mp.weixin.qq.com/s/uN0SyzkW_elYIwi03gis-Q](https://mp.weixin.qq.com/s/uN0SyzkW_elYIwi03gis-Q)


## General Base to Shield Infrastructure

Within Ant Group, frequent SDK upgrades and slow build/release processes have been pain points. Leveraging the Koupleless universal foundation mode, Ant Group has enabled some applications to achieve micro-sensory upgrades for infrastructure. Concurrently, the build and release speed of applications has been reduced from **600 seconds** to **90 seconds**.<br /><br/><img alt="屏蔽基础设施" src="https://intranetproxy.alipay.com/skylark/lark/0/2023/png/671/1697016458930-17177051-a51f-4a88-956f-6cabfd4a7b97.png#clientId=u2fc31cce-a2b2-4&from=paste&height=265&id=u9661d43a&originHeight=530&originWidth=2370&originalType=binary&ratio=2&rotation=0&showTitle=false&size=450843&status=done&style=none&taskId=uf4bf486d-f806-4164-b786-9cd2e0ff7d3&title=&width=1185" width="800">

In the Koupleless universal base model, the base is pre-started and includes common middleware, second-party, and third-party SDKs. Using the Koupleless build plugin, business applications are built into FatJar packages. For new version releases, the scheduler deploys the FatJar to an empty base without modules, and servers with old modules are asynchronously replaced with new ones (empty bases).<br />A dedicated team maintains and upgrades the base, offering developers seamless infrastructure upgrades and a fast build and release experience.


## Cost-effective and Efficient Central Platforms

Within Ant Group, there are numerous middleware services, typical examples include various business lines' strategies, marketing, charity, search recommendations, and advertising. By utilizing Koupleless, these middleware services have gradually evolved into a foundation + module delivery method. In this architecture, the foundation code consolidates common logic and defines several Service Provider Interfaces (SPIs), while modules are responsible for implementing these SPIs. Traffic enters through the foundation code and calls the module's SPI implementation.<br />In the context of middleware, modules are generally very lightweight, sometimes merely a snippet of code. Most modules can be deployed and scaled up within **5 seconds**, and module developers do not need to concern themselves with the underlying infrastructure, enjoying an ultimate Serverless development experience.<br />Taking Ant Group's **search and recommendation** service middleware as an example, this service sinks common dependencies, general logic, and the workflow engine into the foundation and defines some SPIs. The search and recommendation algorithms are implemented by individual module developers. Currently, the search and recommendation service has integrated over **1000+ modules**, with an average code deployment time of less than **1 day**, truly achieving a "write in the morning, deploy in the evening" capability.<br /><br/><img alt="代码 1 天上线" src="https://intranetproxy.alipay.com/skylark/lark/0/2023/png/671/1697024085963-a8b74e7b-37d5-469f-97da-7ef7b3e6889f.png#clientId=u2fc31cce-a2b2-4&from=paste&height=684&id=u44c95749&originHeight=1368&originWidth=1412&originalType=binary&ratio=2&rotation=0&showTitle=false&size=728809&status=done&style=none&taskId=u34dbef7c-95c4-4e42-9613-0a25f3362a3&title=&width=706" width="700">


# Conclusion and Plans
After over five years of evolution and refinement, Koupleless has been fully integrated across all business lines within Ant Group, supporting a quarter of the group's online traffic and achieving significant cost reduction and efficiency improvement. Ant Group plans to further promote the Koupleless development model, continue building elastic capabilities for an even more extreme elasticity experience and green, low-carbon operations. Additionally, there is a focus on contributing to open-source capabilities, aiming to collaborate with the community to create a top-notch modular technology system, driving technical value creation across industries and helping enterprises to reduce costs and improve efficiency.
