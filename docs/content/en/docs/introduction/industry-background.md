---
title: Industry Background
date: 2024-01-25T10:28:32+08:00
description: Koupleless Background
weight: 200
---

## Issues with Microservices
As application architectures evolved from monolithic to microservices, combined with the development of software engineering from waterfall models to the current DevOps model, various problems such as scalability, distribution, and collaborative work have been addressed, providing enterprises with better agility and execution efficiency, bringing significant value. However, despite solving some problems, the microservices model has gradually exposed some issues that are currently receiving continuous attention:

### Complex Infrastructure

#### High Cognitive Load
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695086284703-13a90661-9735-4daa-a7ec-dfc3a28ca2bd.png#clientId=ue95e757a-3cd6-4&from=paste&height=260&id=ubf4cf860&originHeight=942&originWidth=1738&originalType=binary&ratio=2&rotation=0&showTitle=false&size=404365&status=done&style=none&taskId=udcdc41a4-9949-4f53-98ca-e722e63bfc8&title=&width=479)<br />To fulfill a business requirement, there are actually many dependencies, components, and platforms providing various capabilities behind the scenes. If any component below the business layer encounters an exception that is perceived by the business, it will impose a significant cognitive burden and corresponding time cost on the business development personnel.<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695086591548-8ac5f4b6-b5e2-4ba4-aa1e-35ff6816634a.png#clientId=ue95e757a-3cd6-4&from=paste&height=200&id=ub7a3e5b4&originHeight=596&originWidth=582&originalType=binary&ratio=2&rotation=0&showTitle=false&size=415294&status=done&style=none&taskId=u6e187ff5-dade-4172-83e4-38a90d4ad38&title=&width=195)<br />Various types of exceptions.

#### Heavy Operations Burden
The dependencies included in the business application also undergo continuous iterative upgrades, such as frameworks, middleware, various SDKs, etc. When encountering situations such as:

1. Major feature releases
2. Urgent bug fixes
3. Encountering significant security vulnerabilities

These dependencies' new versions need to be upgraded as quickly as possible by the business. This leads to two problems:

##### For Business Development Personnel
If these dependency upgrades occur only once or twice, it's not a problem. However, a business application relies on many frameworks, middleware, and various SDKs, and each dependency upgrade requires the involvement of business developers. Managing so many dependencies becomes a significant operational burden for business development personnel in the long term. Additionally, it's important to note that the business's common layer also imposes a significant burden on business developers.
##### For Infrastructure Personnel
Similarly, like the developers of various dependencies, each release of such a new version requires the business applications using them to be upgraded as quickly as possible. However, business development personnel are more concerned with delivering business requirements, so pushing them to complete upgrades quickly is not very realistic, especially in enterprises with many developers.

#### Slow Startup
Each business application startup process involves many steps, resulting in long waiting times for functionality verification.

#### Low Release Efficiency
Due to the aforementioned issues of slow startup and numerous exceptions, the deployment process takes a long time, and encountering exceptions that cause delays requires recovery and handling. In addition to platform exceptions, the probability of machine exceptions increases with the increasing number of machines. For example, if the probability of a machine completing a release without any issues (without encountering exceptions) is 99.9%, meaning the success rate in one attempt is 99.9%, then for 100 machines, it becomes 90%, and for 1000 machines, it decreases to only 36.7%. Therefore, applications with many machines often encounter deployment delays, requiring developer intervention, leading to low efficiency.

### High Collaboration and Resource Costs

#### Monolithic/Large Applications are too Big

![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695109775918-de436da0-8187-45a8-a30a-62177a55181e.png#clientId=u02591eed-2e18-4&from=paste&height=106&id=u28baf164&originHeight=304&originWidth=1412&originalType=binary&ratio=2&rotation=0&showTitle=false&size=97660&status=done&style=none&taskId=u468dfc48-8b76-484e-abb6-36aed56dfd8&title=&width=494)

##### Blockage in Multilateral Cooperation
As businesses continue to grow, applications become larger, mainly reflected in the increasing number of developers, resulting in blockages in multilateral cooperation.

##### Large Impact of Changes, High Risk
As businesses continue to grow, online traffic increases, and the number of machines grows. However, a single change can affect all code and machine traffic, resulting in a large impact and high risk from changes.

#### Too Many Small Applications
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695111071288-b27e64a3-ff6f-4457-9353-5a4b337faccf.png#clientId=u02591eed-2e18-4&from=paste&height=110&id=ua230cdfe&originHeight=302&originWidth=1404&originalType=binary&ratio=2&rotation=0&showTitle=false&size=76471&status=done&style=none&taskId=ua211c1f6-fe53-43fa-8be8-7da9a92e8cb&title=&width=512)<br />During the evolution of microservices, over time, due to factors such as excessive application splitting, some businesses shrinking, or organizational restructuring, there is a continuous accumulation of small or long-tail applications online, resulting in an increasing number of applications. For example, in the past three years, the number of applications at Ant Group has tripled.<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695111122234-8a85eee7-bdf5-40c6-85e2-5955413f9c7d.png#clientId=u02591eed-2e18-4&from=paste&height=177&id=uf7c75dd0&originHeight=1182&originWidth=1538&originalType=binary&ratio=2&rotation=0&showTitle=false&size=140920&status=done&style=none&taskId=uaadf29d5-7052-4316-9073-5ce5a4f92d4&title=&width=230)


##### High Resource Costs
These applications require several machines in each data center, but in reality, the traffic is not significant, and CPU usage is very low, resulting in resource waste.

##### High Long-Term Maintenance Costs
These applications also require personnel for maintenance tasks, such as upgrading SDKs and fixing security vulnerabilities, leading to high long-term maintenance costs.

#### Inevitability of the Problem
A microservices system is an ecosystem, and after several years of evolution within a company, according to the 28 Law, a few large applications occupy a significant portion of the traffic. It is inevitable that problems such as oversized large applications and too many small applications will arise.
However, there is no defined standard for what constitutes a large application or how many small applications are too many. Therefore, the pain points experienced by developers due to these problems are subtle, and unless the pain reaches a certain threshold, it is difficult to attract the attention and action of the company's management.

### How to Properly Decompose Microservices
The proper decomposition of microservices has always been a challenging problem, as there are no clear standards. This is also why the issues of oversized large applications and too many small applications exist. The root cause behind these problems is the flexibility of business and organization and the high cost of microservice decomposition, which results in inconsistent agility between the two.

#### Misalignment between Microservices Decomposition and Business/Organizational Agility
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695113016136-8d591312-1300-496e-9df8-a5ed1a49abe4.png#clientId=u02591eed-2e18-4&from=paste&height=201&id=u7ce79cce&originHeight=554&originWidth=1222&originalType=binary&ratio=2&rotation=0&showTitle=false&size=182342&status=done&style=none&taskId=uf3c867d4-2d82-4922-a6d9-6572ca3a1f7&title=&width=443)<br />Business development is flexible, and organizational structures are constantly adjusting. However, microservice decomposition requires machine resources and incurs long-term maintenance costs. The misalignment in agility between the two leads to problems such as under-decomposition or over-decomposition, resulting in oversized large applications and too many small applications. If these problems are not fundamentally addressed, microservices governance will continue to encounter issues, causing developers to remain stuck in a cycle of low efficiency and governance challenges.


### Problems Faced by Enterprises of Different Sizes
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1695131026232-1e25044b-d0d4-4a58-9d03-ef665365fbc6.png#clientId=ucec7e736-7c4f-4&from=paste&height=511&id=uc85ea670&originHeight=1022&originWidth=3766&originalType=binary&ratio=2&rotation=0&showTitle=false&size=244352&status=done&style=none&taskId=u18416169-fc43-47a4-8486-9e5e328552c&title=&width=1883)

### Industry Attempts at Solutions
The industry has many good ideas and projects attempting to solve these problems, such as service meshes, runtime applications, platform engineering, Spring Modulith, and Google ServiceWeaver. These solutions have had some effect but also come with limitations:

1. From the perspective of business developers, only part of the infrastructure is shielded, and the business's common parts are not shielded.
2. Only some of the problems are addressed.
3. High cost of retrofitting existing applications.

Koupleless is evolving as a development framework and platform capability to address these issues.

<br/>
<br/>
