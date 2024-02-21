---
title: Koupleless Multi-Application Governance Patch Management
date: 2024-01-25T10:28:32+08:00
description: Koupleless Multi-Application Governance Patch Management
weight: 1
---

# Why Koupleless Needs Multi-Application Governance Patching?
Koupleless is a multi-application architecture, and traditional middleware may only consider scenarios for a single application. Therefore, in some cases, it is incompatible with multi-application coexistence, leading to problems such as shared variable contamination, classloader loading exceptions, and unexpected class judgments.
Thus, when using Koupleless middleware, we need to patch some potential issues, covering the original middleware implementation, allowing open-source middleware to be compatible with the multi-application mode.

# Research on Multi-Application Governance Patching Solutions for Koupleless
In multi-application compatibility governance, we not only consider production deployment but also need to consider compatibility with local user development (IDEA click Debug), compatibility with unit testing (e.g., @SpringbootTest), and more.

<br />Below is a comparison table of different solutions.
## Solution Comparison

| Solution Name                                                            | Access Cost | Maintainability | Deployment Compatibility | IDE Compatibility | Unit Testing Compatibility |
|------------------------------------------------------------------------| ----------- | --------------- | ------------------------ | ------------------ | -------------------------- |
| A: Place the patch package dependency at the beginning of maven dependency to ensure that the patch class is loaded first by the classLoader.   | Low.<br>Users only need to control the order of Maven dependencies. | Low<br>Users need to ensure that the relevant dependencies are at the front, and the classpath is not manually passed during startup. | Compatible✅ | Compatible✅ | Compatible✅ |
| B: Modify the indexing file order of spring boot build artifacts using maven plugins.                       | Low.<br>Just need to add a package cycle maven plugin, user perception is low. | Medium<br>Users need to ensure that the classpath is not manually passed during startup. | Compatible✅ | Not compatible❌<br>JetBrains cannot be compatible, JetBrains will build the CLI command line by itself to pass the classpath according to the order of Maven dependencies, which may lead to suboptimal loading order of the adapter. | Not compatible❌<br>Unit tests do not go through the repackage cycle and do not depend on the classpath.idx file. |
| C: Add a custom spring boot jarlaunch starter to control the classLoader loading behavior through the starter. | High.<br>Users need to modify their own base startup logic to use Koupleless' custom jarlaunch. | High<br>Custom jarlaunch can control the code loading order through hooks. | Compatible✅ | Compatible✅<br>But IDE needs to be configured to use custom jarlaunch. | Not compatible❌<br>Because unit tests do not go through the jarlaunch logic. |
| D: Enhance the base classloader to ensure priority searching and loading of patch classes.                             | High.<br>Users need to initialize enhanced code, and this mode also has an impact on the sofa-ark recognition logic of the master biz, and needs to be refactored to support. | High<br>The base classloader can programmatically control the loading order of dependencies. | Compatible✅ | Compatible✅ | Compatible✅ |
| E: Configure the maven plugin to copy patch class code to the current project, and the files in the current project will be loaded first.                 | High.<br>Maven's current copy plugin cannot use wildcards, so adding an adapter requires additional configuration. | High<br>As long as users configure it, they can ensure that dependencies are loaded first (because the classes of the local project are loaded first). | Compatible✅ | Compatible✅ | Not compatible❌<br>Because unit tests do not go through the package cycle, and the maven copy plugin takes effect during the package cycle. |

## Conclusion
Overall, it is not possible to achieve user 0 perception access completely, and each method requires minor business refactoring.
Among many solutions, A and D can achieve full compatibility. However, the A solution does not require business code changes, nor does it intrude into runtime logic. It only requires users to add the following dependency at the beginning of the maven dependency:
```xml
<dependency>
  <groupId>com.alipay.koupleless</groupId>
  <artifactId>koupleless-base-starter</artifactId>
  <version>${koupleless.runtime.version}</version>
  <type>pom</type>
</dependency>
```
Therefore, we will adopt solution A.
<br/>If you have more ideas or input, welcome to discuss them with the open-source community!
