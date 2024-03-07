[![Coverage Status](https://codecov.io/gh/koupleless/koupleless/branch/main/graph/badge.svg)](https://codecov.io/gh/koupleless/koupleless/branch/main/graph/badge.svg)
![license](https://img.shields.io/badge/license-Apache--2.0-green.svg)
![Maven Central](https://img.shields.io/maven-central/v/com.alipay.sofa.koupleless/koupleless-runtime)

<h1 align="center">Koupleless: Modular Development Framework and Serving Platform</h1>

<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

Would you like your application to start in just 10 seconds, consuming only 20MB of memory? Have you encountered issues with large applications causing collaboration bottlenecks and low release efficiency? Are you struggling with the high resource and maintenance costs associated with numerous small applications? If you're facing these challenges, then Koupleless might be the solution you're looking for. Koupleless approaches application architecture from a modular perspective, offering an extremely low-cost solution to address pain points encountered throughout the entire lifecycle of application development, operation, and execution:

1. Excessive application fragmentation leading to high machine and long-term maintenance costs
2. Insufficient application fragmentation causing collaboration bottlenecks
3. Lengthy application build, startup, and deployment times resulting in low iteration efficiency
4. Severe fragmentation of SDK versions with high upgrade costs and long cycles
5. High costs associated with building platforms and middle platforms, as well as difficulties in business asset precipitation and architectural constraints
6. Long microservice chains leading to poor call performance
7. High costs associated with microservice decomposition and evolution

How does Koupleless address these issues? Koupleless vertically and horizontally splits traditional applications, with the vertical split separating the base and the horizontal split separating multiple modules. The base shields modules from infrastructure concerns, while modules contain only the business-specific portion, enabling quick startup and insulating them from infrastructure concerns, allowing module developers to experience a Serverless-like environment. Koupleless thus evolves into a low-cost, Serverless solution by refining the granularity of development and operations while shielding infrastructure. For detailed explanations of the principles, please refer to the official website.
Further detailed explanations of the principles are available on [the official website](https://koupleless.gitee.io/docs/introduction/architecture/arch-principle/).

![image](https://github.com/koupleless/koupleless/assets/3754074/004c0fa5-62f6-42d7-a77e-f7152ac89248)

The most important aspect is that Koupleless can **assist existing applications** in evolving into a modular development model **at an extremely low cost**, addressing the aforementioned issues and helping businesses reduce costs, increase efficiency, and enhance competitiveness.

## The Advantages of Koupleless

Koupleless is a mature development framework and operational scheduling platform capability that has been refined internally within Ant Group for 5 years. Compared to traditional image-based application models, it offers approximately 10 times improvement in development, operations, and runtime calling. Summarized into 5 key features: Fast, Cost-efficient, Flexible deployment, Smooth evolution, and Production-scale validation.

<img width="788" alt="image" src="https://github.com/sofastack/sofa-serverless/assets/3754074/11d1d662-d33b-482b-946b-bf600aeb34da">

Here are performance data comparing modular development and deployment with traditional image-based approaches for an actual production application.

<img width="600" alt="image" src="https://github.com/koupleless/koupleless/assets/3754074/913a6f11-54cb-4c8b-b417-d014e53c920a"/>

## What is a Module?

Modules utilize extreme sharing and isolation technologies, which enable hot deployment (updating online code without restarting the machine).

Isolation is achieved through ClassLoader class isolation based on [SOFAArk](https://github.com/sofastack/sofa-ark) and object isolation based on [SpringBoot SpringContext](https://github.com/spring-projects/spring-boot).

Sharing is facilitated by class delegation loading based on [SOFAArk](https://github.com/sofastack/sofa-ark) and cross-SpringContext object lookup and invocation based on SpringBootManager.

So, in physical terms, a module can be considered as one ClassLoader + one SpringContext.

## What is the Base？
The base is just a regular application, same with the original app (such as standard SpringBoot).

## Quick start
Please check [the official website Quick Start](https://koupleless.gitee.io/docs/quick-start/).

<video width=100% controls autoplay>
<source src="https://koupleless.oss-cn-shanghai.aliyuncs.com/outer-materials/docs/videos/module_dev_and_deploy.mp4" type="video/mp4">
Your browser does not support the video tag.  
</video>

## Koupleless Components

![image](https://github.com/sofastack/sofa-serverless/assets/101314559/995f1e17-f3be-4672-b1b8-c0c041590fb0)

## Contributing
We appreciate anyone who contribute here together. Please scan the QR code to join the developer collaboration group.

| IAM                        | number      | QR code                                                                                                                          |
|----------------------------|-------------|----------------------------------------------------------------------------------------------------------------------------------|
| DingTalk group (recommand) | 24970018417 | <img width="256" alt="image" src="https://github.com/koupleless/koupleless/assets/3754074/7ba1db74-20c1-43a4-a2ab-d38c99a920cd"> |
| WeChat                     | zzl_ing     | <img width="256" alt="image" src="https://github.com/koupleless/koupleless/assets/3754074/35ebc2bc-86cd-4a24-b12e-e9f44cccc2d7"> |

## Long-term planning and our vision
We hope to further refine and open up these capabilities to be more extreme and applicable to a wider range of scenarios. Help more businesses solve application development problems, achieve cost reduction and efficiency improvement, and ultimately become an excellent research and development framework and solution for global green computing, achieving:

1. Speed as you need
2. Pay as you need
3. Deploy as you need
4. Evolution as you need

<img width="1069" alt="image" src="https://github.com/koupleless/koupleless/assets/3754074/17ebd41d-38c7-46e8-a4ba-b6b8bf8f76dd">
