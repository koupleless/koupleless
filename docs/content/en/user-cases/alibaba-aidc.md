---
title: Alibaba International Digital Commerce Group Middleware Business Efficiency Tripled
date: 2024-01-25T10:28:32+08:00
description: Koupelelss Alibaba International Digital Commerce Group Middleware Business Efficiency Tripled
weight: 1100
type: docs
---
> Authors: Zhu Lin (Feng Yuan), Zhang Jianming (Ming Men)

# Project Background
In the past few years, AIDC (Alibaba Overseas Digital Commerce) business division has expanded in multiple countries and regions globally. The international e-commerce business model is divided into "Cross-border" and "Local to Local," respectively based on AE (Cross-border), Lazada, Daraz, Mirivia, Trendyol, and other e-commerce platforms. The different e-commerce platforms will be collectively referred to as "sites".

<img alt="阿里巴巴国际数字商业背景" src="https://github.com/sofastack/sofa-serverless/assets/11410549/e7bdded0-a6d6-48ae-a373-49429e1bc8ee" width="800"></img>

For the entire e-commerce business, there are certain differences in the core buyer and seller foundation links among the sites, but there are more commonalities. Abstracting a universal platform to achieve low-cost reuse across various sites helps support upper-layer businesses more efficiently. Therefore, in the past few years, the foundation link has been attempting to support business development through a platform-based construction approach, with a model of `1 global business platform + N business sites`; the technical iteration has undergone five stages of development, from the initial centralized middleware integration business architecture model, gradually transitioning to a decentralized architecture integrated by business, which can now basically meet the global sites' business and platform's own closed-loop iteration.

<img alt="全球化业务平台。业务站点。" src="https://github.com/sofastack/sofa-serverless/assets/11410549/8c10464c-6c5a-4fce-a59f-192b809b15bf" width="900"></img>

Each site is logically based on the international middleware (platform) for personalized customization, while in the delivery/operations state, each site is split into independent applications, each carrying its own business traffic. The platform capabilities are integrated into the site applications via secondary package methods, and the platform also possesses an ability expansion mechanism. The R&D at business sites can overwrite platform logic within site applications, maximizing the autonomy of site business development/operations, and to a certain extent, ensuring the reusability of platform capabilities.
However, due to the current e-commerce sites being at different stages of development, and differences in business models between local-to-local and cross-border, as well as continuous changes in business strategies, the rapid iteration of business and the posterior sedimentation of platform capabilities gradually formed contradictions, mainly manifested in the following aspects:

- **Platform Redundancy**: As the platform adopts an open, integrated strategy without certain constraints, demand iterations, even those requiring changes to platform logic, are basically self-contained within sites. There's redundancy in platform capability sedimentation, stability, performance, and openness across all sites, with the differences in platform versions supporting different sites gradually widening;
- **High Site Maintenance Costs**: Independently circled site applications, by maintaining customized platform capabilities and assuming part of "platform team responsibilities," gradually increased the burden on site development teams, leading to higher labor costs;
- **Low Development Iteration Efficiency**: Core application construction and deployment efficiency is low. Taking transaction site applications as an example, system startup time stabilizes at 300s+, compile time at 150s+, image construction time at 30s+, and container reinitialization and other scheduling layer time consumption at about 2 minutes. With over 100 deployments in the development environment per day, reducing construction and deployment time will effectively decrease development waiting time;

Therefore, the next generation of architectural iteration will need to focus on solving how to achieve autonomy in capability iteration and version unity under a decentralized architecture model integrated by business. It will also need to consider how to further reduce site development and operations costs and improve construction and deployment efficiency, allowing business developers to truly focus on their own business logic customization.
The technology concept of Serverless emphasizes separation of concerns, allowing business developers to focus on the development of business logic without paying much attention to the underlying platform. This concept, combined with the problems we face, may be a good solution, upgrading the platform from a secondary package to a platform base application, unifying the iteration of the platform, including the upgrade of the application runtime; making business site applications lightweight, focusing only on the development of customized logic, improving deployment efficiency, reducing maintenance costs, with the overall logic architecture as follows:

<img alt="阿里巴巴国际研发痛点" src="https://github.com/sofastack/sofa-serverless/assets/11410549/8b6d120f-42b3-495d-9879-6be1a8000ebc" width="1200"></img>


# Concept Elaboration

Serverless is commonly understood as "serverless architecture". It is one of the main technologies of cloud-native, where serverless means users do not need to worry about the management of application operation and maintenance. It allows users to develop and use applications without managing the underlying infrastructure. Cloud service providers offer, configure, and manage the underlying infrastructure for computing, decoupling applications from the infrastructure, allowing developers to focus more on business logic, thereby enhancing delivery capabilities and reducing work costs. Traditional Serverless in implementation is actually **FaaS** **+** **BaaS**. FaaS (Function as a Service) carries code snippets (i.e., functions) that can be created, used, and destroyed anytime, anywhere, without carrying state on their own. It is used in conjunction with BaaS (Backend as a Service). Together, they ultimately realize the complete behavior of Serverless services.

<img alt="Serverless 概念" src="https://github.com/sofastack/sofa-serverless/assets/11410549/fe45cf14-0f19-42ed-8a06-6d461c2209f4" width="400"></img>

Under the traditional Serverless technology system, Java application architecture has mostly solved the problems of `IaaS layer + Containerization`, but Serverless itself cannot extend its coverage down into the JVM internals. Therefore, for a complex Java monolithic application, the concept of Serverless can be leveraged to further separate and split the business code under the Java technology stack from the infrastructure (middleware) dependencies. The Serverless transformation in this practice can be abstracted into the following process and objectives:

<img alt="Java Serverless" src="https://github.com/sofastack/sofa-serverless/assets/11410549/1edb95e4-627c-4244-bdf2-844b2bd265a3" width="650"></img>

Horizontally split a monolithic application into two layers:

- **Base**: Some components and Lib packages that **do not change frequently** in business application iterations are sunk into a new application, which we call the `base application`, with the following characteristics:
    - The base can be published and maintained independently
    - Base application developers can uniformly upgrade middleware and component versions, without the upper layer App needing to be aware of the entire upgrade process, provided that compatibility is ensured
    - The base has reusability across different sites; a trading base can be shared by different site Apps like AE, Lazada, Daraz, etc.
- **Serverless App**: To minimize the cost of business transformation, the App maintained by the business team still retains its independence in publishing and operational responsibilities. After Serverless transformation, business developers only need to focus on the business code. The JVM's external service carrier remains the business application.


# Technical Implementation

<img alt="阿里巴巴国际 Serverless 演进" src="https://github.com/sofastack/sofa-serverless/assets/11410549/160e41e5-3227-409d-b81a-05800f24a306" width="550"></img>

The implementation process of the Serverless architecture evolution is divided into two parts:

1. Redesign the application architecture layering and responsibility division under the Serverless architecture model to reduce the burden on the business and improve the efficiency of SRE (Site Reliability Engineering).
2. Adopt new development frameworks, delivery models, and release & operations products in the areas of R&D, publishing, and operations to support rapid business iteration.


## Application Architecture
Taking the Daraz foundational link as an example, the application architecture's layered structure, interaction relationships, and team responsibilities are as follows:

<img alt="阿里巴巴国际 Serverless 应用架构" src="https://github.com/sofastack/sofa-serverless/assets/11410549/7af3a82f-31b8-4ed4-ae18-e7d2606a261a" width="650"></img>

We logically layer the supporting architecture required for the complete delivery of a Serverless application and divide the development responsibilities, clearly defining the protocol standards for interaction between the App and the base.

## Development Domain

<img alt="阿里巴巴国际 Serverless 研发运维平台" src="https://github.com/sofastack/sofa-serverless/assets/11410549/e7957e02-0f14-4f79-9b00-d748ce806722" width="700"></img>

- Constructed a Serverless runtime framework to drive the operation and interaction of "Base-Serverless App"
- Collaborated with the Aone development platform team to build a complete set of release & operations product systems for the base and App under the Serverless model

### voyager-serverless framework

<img alt="voyager-serverless" src="https://github.com/sofastack/sofa-serverless/assets/11410549/92bb0727-abad-49a0-a175-9cba64ffdbbb" width="350"></img>

**voyager-serverless framework** is a self-developed R&D framework based on **[Koupleless](https://github.com/koupleless/koupleless/)** technology, providing a `Serverless programming interface`, allowing business Apps to be dynamically loaded into a running base container (ArkContainer). Based on the module isolation capability of  [Koupleless](https://github.com/koupleless/koupleless/) , we have made in-depth customization for the Alibaba Group technology stack.

The entire framework provides the following key capabilities:

<img alt="阿里巴巴国际 Serverless 框架关键能力" src="https://github.com/sofastack/sofa-serverless/assets/11410549/b2bc97cb-2a0c-4d6d-a971-0dbc78dd2959" width="600"></img>

#### Isolation and Runtime Principles

<img alt="Serverless 隔离性与运行时原理" src="https://github.com/sofastack/sofa-serverless/assets/11410549/3cc5dfda-b3e4-4edf-a553-34577425771a" width="1300"></img>

The framework implements `ClassLoader isolation` and `SpringContext isolation` between the base and application modules. The startup process is divided into `two stages and three steps`, with the startup sequence from bottom to top:

- **Phase One: Base Startup**
    - **Step One**: Bootstrap startup, including Kondyle and Pandora containers, loading `Kondyle plugins` and `Pandora middleware plugins` classes or objects
    - **Step Two**: `Base application startup`, internally ordered as follows:
        - Start ArkContainer, initialize Serverless-related components
        - Base application SpringContext initialization, loading `base-owned classes, base Plugin classes, dependency package classes, middleware SDK classes`, etc.
- **Phase Two: App Startup**
    - **Step Three**: `Serverless App startup`, where the ArkContainer internal component accepts `Fiber` scheduling requests to download App artifacts and trigger App Launch
        - Create BizClassLoader as a thread ClassLoader to initialize SpringContext, loading `App-owned classes, base Plugin classes, dependency package classes, middleware SDK classes`, etc.
        - 
#### Communication Mechanism

In the Serverless mode, communication between the base and App can be achieved through `in-process communication`. Currently, two communication models are provided: **SPI** and **Base Bean Service Export**.
> SPI is essentially an internal special implementation based on the standard Java SPI extension, which is not elaborated further in this article. Here, we focus on introducing `Base Bean Service Export`.

In general, the SpringContext of the base and the SpringContext of the App are completely isolated and have no parent-child inheritance relationship. Therefore, the App side cannot reference beans in the lower-level base SpringContext through regular `@Autowired` methods.
In addition to sinking classes, in some scenarios, the base can also sink its already initialized bean objects, declaring and exposing them for use by the upper-level App. After this, when the App starts, it can directly obtain the initialized beans in the base SpringContext to accelerate the startup speed of the App. The process is as follows:

<img alt="加快 Java 应用启动" src="https://github.com/sofastack/sofa-serverless/assets/11410549/99036b5c-1081-4869-8e6e-84127250f063" width="400"></img>

   1. Users declare the beans that need to be exported in the base either through configuration or annotation.
   2. After the base startup is complete, the isolated container will automatically export the beans marked by the user to a buffer area, waiting for the App to start.
   3. When the App starts on the base, during the initialization process of the App's SpringContext, it will read the beans that need to be imported from the buffer area during the initialization phase.
   4. Other components in the App's SpringContext can then `@Autowired` these exported beans normally.

#### Plugin Mechanism

The Serverless plugin provides a mechanism for classes required by the App runtime to be loaded from the base by default. The framework supports packaging SDKs/secondary packages required by the platform base for upper-level App use into a plugin (Ark Plugin), ultimately achieving the sinking of mid-platform controlled packages into the base without requiring changes to upper-level business:

<img alt="Serverless 插件机制" src="https://github.com/sofastack/sofa-serverless/assets/11410549/7bbbcbb9-46d4-46d0-b82d-1e9f21c754b0" width="500"></img>

### Middleware Adaptation

In the evolution of Serverless architecture, as the startup process of a complete application is split into base startup and App startup, the initialization logic of related middleware in phases one and two has also changed. We have tested and adapted commonly used middleware and product components on the international side.
In summary, most issues arise from the fact that some middleware processes are not designed for scenarios with multiple ClassLoaders. Many classes/methods do not pass the ClassLoader object as a parameter, causing errors when initializing model objects, leading to abnormal context interactions.

### Development Support

We also provide a complete and easy-to-use set of supporting tools to facilitate developers in quickly integrating into the Serverless ecosystem:

<img alt="Serverless 研发配套" src="https://github.com/sofastack/sofa-serverless/assets/11410549/2d7c3c88-0924-4a9b-9d01-c6c718fcd4b0" width="600"></img>


## Release & Operations Domain

In addition to the development domain, the Serverless architecture also brings many new changes to the release and operations domain. Firstly, there is the splitting of development and operations layers, achieving separation of concerns and reducing development complexity:

<img alt="Serverless 运维配套" src="https://github.com/sofastack/sofa-serverless/assets/11410549/1e06ba4e-ee6f-42cd-8beb-d848fe0c7b6d" width="1100"></img>

- **Logical Splitting**: Splitting the original application, isolating business code from infrastructure, sinking basic capabilities. For example, sinking time-consuming middleware, some rich secondary libraries, and controllable secondary libraries into the base, achieving lightweighting of business applications.
- **Independent Evolution**: After splitting into layers, the base and business applications iterate independently. SREs can control and upgrade infrastructure uniformly on the base, reducing or even eliminating the cost of business upgrades.
<img alt="Serverless 运维配套" src="https://github.com/sofastack/sofa-serverless/assets/11410549/42d72ce3-e7c4-49c1-8154-ba1aac07bdfc" width="600"></img>

We also collaborate with Aone, and voyager-serverless integrates into the Aone Serverless product technology system using the `OSR (Open Serverless Runtime)` standard protocol. With the help of new release models and deployment strategies, significant improvements have been achieved in App building speed and startup efficiency.

<img alt="Serverless 运维配套" src="https://github.com/sofastack/sofa-serverless/assets/11410549/a2637716-0883-48fd-bad2-928aca1f5af3" width="700"></img>

### Improvement in Build Efficiency

- **Maven Build Optimization**: Since many dependencies have been sunk into the ready-made base, the number of secondary packages and class files that need to be built can be reduced for the App, thereby optimizing the overall artifact size and improving build efficiency.
- **Removal of Docker Builds**: Since the artifacts deployed for business Apps under Serverless mode are lightweight Fat Jars, there is no need for Docker builds.

### Improvement in Release Efficiency

In Serverless mode, we use `Surge+streaming release` instead of traditional batch releases to further improve the release efficiency of the App.

| **Term** | **Description** |
| --- | --- |
| **Batched Release** &nbsp;&nbsp;  | The strategy of releasing in batches involves moving to the next batch after a certain number of new nodes are reached in each batch. For example, with 100 nodes and 10 batches, the first batch releases 10 new nodes, the second batch releases 20 new nodes, and so on.  |
| **Surge** &nbsp;&nbsp; | Surge release strategy accelerates business release efficiency without affecting service availability:<br/> 1) During release, a proportionate number of nodes are added according to the Surge configuration. For instance, if there are 10 machines in the business and Surge is configured at 50%, 5 machines are first added for release.<br/> 2) If the base is configured with an appropriate-sized buffer, these 5 machines can be directly obtained from the buffer to release the new version of the code.<br/> 3) Once the overall number of new version nodes reaches the expected number (in this example, 10 machines), the old nodes are directly taken offline, completing the entire release process.<br/> When Surge is used in conjunction with streaming release and an appropriate number of buffers in the Runtime, it can maximize business release efficiency. |

- **Waterfall Batched Release**: In waterfall batched release strategy, all machines in each batch are deployed online before moving on to the next batch. Machines within a batch are deployed in parallel, while batches are deployed sequentially. For example, if there are 100 machines and the release is divided into 10 batches, with each batch deploying 10 machines, the total deployment time would be:

<img alt="Serverless 流式发布" src="https://github.com/sofastack/sofa-serverless/assets/11410549/ccd21281-e158-4af9-a6b7-0a297005b3c7" width="850"></img>

- **Surge Streaming Release**: During the release process, it allows for the allocation of additional machines to participate in the update. The core principle is to increase the number of machines participating in the update in a single round, under the condition of `ensuring availability`. For example, with 100 machines, and ensuring availability ≥ 90%, meaning at any time at least 90 (100 * 90%) machines are online, the release scheduling with a surge of 5% would proceed as follows:

<img alt="Serverless 流式发布" src="https://github.com/sofastack/sofa-serverless/assets/11410549/a5d34726-e4b9-4ce9-956a-ca91ba35be95" width="850"></img>

<img alt="Serverless 流式发布" src="https://github.com/sofastack/sofa-serverless/assets/11410549/b7298771-81f3-4aaf-a4ff-30b0805a0875" width="900"></img>

Using this new release model, we are fully implementing Surge releases in the daily and staging environments where development changes are most frequent, to accelerate the deployment of business apps.

- Before the Serverless transformation:
    - To ensure that traffic is not affected during deployment, a staging environment typically retains two machines (replica = 2) and follows traditional batched releases (batch = 2), meaning each machine is updated in turn.
    - Here, let's assume the application startup time is 5 minutes, with frequent changes in business code taking 1 minute, and platform and middleware components loading taking 4 minutes.
    - The total deployment time is 5 minutes (for business code changes) + 5 minutes (for platform and middleware loading) = **10 minutes**.

<img alt="Serverless 速度收益" src="https://github.com/sofastack/sofa-serverless/assets/11410549/2e7e7d83-5784-40dc-b60f-370819c390f6" width="1200"></img>

- After completing the Serverless transformation and adopting Surge streaming release:
    - The staging environment for the App only needs to retain one machine (replica = 1), and the base is configured with a buffer of 1, meaning one empty base is retained for scheduling use by the App.
    - In terms of release strategy, the Surge percentage for the App environment is set to 100%.
    - Since only updates to the App's Biz code are being released, the total deployment time is **1 minute**, and the total cost of machines remains unchanged throughout the process.

<img alt="Serverless 速度收益" src="https://github.com/sofastack/sofa-serverless/assets/11410549/2b9dda94-acf8-4846-8e21-821cda4a56cd" width="1150"></img>

Additionally, we have configured a certain number of base buffers in the production environment to support rapid elastic scaling of site apps.


# Summary and Outlook

We have completed the Serverless upgrade and transformation of Daraz business site's transaction, marketing, fulfillment, membership, and reverse applications. Significant optimization effects have been achieved in three indicators: `build time`, `single application startup time`, and `staging environment deployment time`. In some areas, we have even achieved **10-second** level application startup.

<img alt="阿里巴巴国际 Serverless 展望" src="https://github.com/sofastack/sofa-serverless/assets/11410549/298650c3-ef2f-48b3-8b85-3754bd62e5cb" width="1100"></img>


It can be seen that the upgrade of the Serverless architecture in this iteration has brought significant positive benefits and efficiency improvements, both in theoretical deduction and practical results. This brings much convenience to the rapid iteration of subsequent business apps. Meanwhile, since the platform code is sunk as a base application, it also has the ability to release orthogonal to the business site, basically achieving the goal of unified platform version for the basic chain link. "Focus separation" has also liberated business developers, allowing them to focus more on their business code. However, there are still some challenges and issues to be further addressed, such as the maturity of development support, problem diagnosis, and optimization of production environment costs with the optimal configuration of the base. We will also deeply participate in the co-construction of the **[Koupleless](https://github.com/koupleless/koupleless/)** open-source community to explore more landing scenarios and practical experience.

Serverless has never been a single architecture form; it brings more of a concept and production mode. Understanding and utilizing it help us broaden new ideas and problem-solving methods.