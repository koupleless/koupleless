---
title: Introduction to the Principle of Class Delegation Loading between Foundation and Modules
date: 2024-01-25T10:28:32+08:00
description: Introduction to the principle of class delegation loading between Koupleless foundation and modules
weight: 200
---

## Class Delegation Loading between Multiple Modules
The SOFAArk framework is based on a multi-ClassLoader universal class isolation solution, providing class isolation and application merge deployment capabilities. This document does not intend to introduce the [principles and mechanisms](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-classloader/)of SOFAArk class isolation. Instead, it mainly introduces the current best practices of multi-ClassLoader.  <br />The ClassLoader model between the foundation and modules deployed on the JVM at present is as shown in the figure below:<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2022/png/149473/1653304883689-ec30b72b-1620-4a2a-8611-d6c24107afd2.png#clientId=u8aaeb3a3-ec6f-4&from=paste&height=225&id=u1df6aa1c&originHeight=450&originWidth=388&originalType=binary&ratio=1&rotation=0&showTitle=false&size=39808&status=done&style=none&taskId=uf6233ec3-9494-4b6a-b1b6-43546035a43&title=&width=194)

## Current Class Delegation Loading Mechanism
The classes searched by a module during startup and runtime currently come from two sources: the module itself and the foundation. The ideal priority order of these two sources is to search from the module first, and if not found, then from the foundation. However, there are some exceptions currently:

1. A whitelist is defined, and dependencies within the whitelist are forced to use dependencies in the foundation.
2. The module can scan all classes in the foundation:
   - Advantage: The module can introduce fewer dependencies.
   - Disadvantage: The module will scan classes that do not exist in the module code, such as some AutoConfigurations. During initialization, errors may occur due to the inability to scan corresponding resources.
3. The module cannot scan any resources in the foundation:
   - Advantage: It will not initialize the same beans as the foundation repeatedly.
   - Disadvantage: If the module needs resources from the foundation to start, errors will occur due to the inability to find resources unless the module is explicitly introduced (Maven dependency scope is not set to provided).
5. When the module calls the foundation, some internal processes pass the class names from the module to the foundation. If the foundation directly searches for the classes passed by the module from the foundation ClassLoader, it will not find them. This is because delegation only allows the module to delegate to the foundation, and classes initiated from the foundation will not search the module again.

### Points to Note When Using
When a module needs to upgrade the dependencies delegated to the foundation, the foundation needs to be upgraded first, and then the module can be upgraded.

## Best Practices for Class Delegation
The principle of class delegation loading is that middleware-related dependencies need to be loaded and executed in the same ClassLoader. There are two best practices to achieve this:

### Mandatory Delegation Loading
Since middleware-related dependencies generally need to be loaded and executed in the same ClassLoader, we will specify a whitelist of middleware dependency, forcing these dependencies to be delegated to the foundation for loading.

#### Usage
Add the configuration `sofa.ark.plugin.export.class.enable=true` to application.properties.

#### Advantages
Module developers do not need to be aware of which dependencies belong to the middleware that needs to be loaded by the same ClassLoader.

#### Disadvantages
The list of dependencies to be forcibly loaded in the whitelist needs to be maintained. If the list is missing, the foundation needs to be updated. Important upgrades require pushing all foundation upgrades.


### Custom Delegation Loading
In the module's pom, set the scope of the dependency to `provided` to actively specify which dependencies to delegate to the foundation for loading. By slimming down the module, duplicate dependencies with the foundation are delegated to the foundation for loading, and middleware dependencies are pre-deployed in the foundation (optional, although the module may not use them temporarily, they can be introduced in advance in case they are needed by subsequent modules without the need to redeploy the foundation). Here:

1. The foundation tries to precipitate common logic and dependencies, especially those related to middleware named `xxx-alipay-sofa-boot-starter`.
2. Pre-deploy some common dependencies in the foundation (optional).
3. If the dependencies in the module are already defined in the foundation, the dependencies in the module should be delegated to the foundation as much as possible. This will make the module lighter (providing tools for automatic module slimming). There are two ways for the module to delegate to the foundation:
   1. Set the scope of the dependency to provided, and check whether there are other dependencies set to compile through `mvn dependency:tree`, and all places where dependencies are referenced need to be set to provided.
   2. Set `excludeGroupIds` or `excludeArtifactIds` in the `sofa-ark-maven-plugin` biz packaging plugin.
```xml
            <plugin>
                <groupId>com.alipay.sofa</groupId>
                <artifactId>sofa-ark-maven-plugin</artifactId>
                <configuration> 
                    <excludeGroupIds>io.netty,org.apache.commons,......</excludeGroupIds>
                    <excludeArtifactIds>validation-api,fastjson,hessian,slf4j-api,junit,velocity,......</excludeArtifactIds>
                    <declaredMode>true</declaredMode>
                </configuration>
            </plugin>
```
Using Method 2.b To ensure that all declarations are set to `provided` scope, it is recommended to use Method 2.b, where you only need to specify once.

4. Only dependencies declared by the module can be delegated to the foundation for loading.

During module startup, the Spring framework has some scanning logic. If these scans are not restricted, they will search for all resources of both the module and the foundation, causing some modules to attempt to initialize functions they clearly do not need, resulting in errors. Since SOFAArk 2.0.3, the `declaredMode` of modules has been added to limit only dependencies declared within the module can be delegated to the foundation for loading. Simply add `<declaredMode>true</declaredMode>` to the module's packaging plugin configurations.

#### Advantages
- No need to maintain a forced loading list for plugins. When some dependencies that need to be loaded by the same ClassLoader are not set for uniform loading, you can fix them by modifying the module without redeploying the foundation (unless the foundation does require it).

#### Disadvantages
- Strong dependency on slimming down modules.


### Comparison and Summary
|                     | Dependency Missing Investigation Cost | Repair Cost                                 | Module Refactoring Cost | Maintenance Cost |
|---------------------|----------------------------------------|---------------------------------------------|-------------------------|------------------|
| Forced Loading      | Moderate                               | Update plugin, deploy foundation, high      | Low                     | High             |
| Custom Delegation   | Moderate                               | Update module dependencies, update foundation if dependencies are insufficient and deploy, moderate | High                    | Low              |
| Custom Delegation + Foundation Preloaded Dependencies + Module Slimming | Moderate                               | Update module dependencies, set to provided, low | Low                     | Low              |

#### Conclusion: Recommend Custom Delegation Loading Method

1. Module custom delegation loading + module slimming.
2. Module enabling declaredMode.
3. Preload dependencies in the base.


## declaredMode 开启方式

## declaredMode Activation Procedure

### Activation Conditions
The purpose of declaredMode is to enable modules to be deployed to the foundation. Therefore, before activation, ensure that the module can start locally successfully.<br />If it is a SOFABoot application and involves module calls to foundation services, local startup can be skipped by adding these two parameters to the module's application.properties (SpringBoot applications do not need to care):
```properties
# If it is SOFABoot, then:
# Configure health check to skip JVM service check
com.alipay.sofa.boot.skip-jvm-reference-health-check=true
# Ignore unresolved placeholders
com.alipay.sofa.ignore.unresolvable.placeholders=true
```

### Activation Method
Add the following configuration to the module's packaging plugin: <br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2022/png/149473/1668428226653-d1ad571e-a580-42fa-9ca0-ff63c199dfb1.png#clientId=u664f9b10-526b-4&from=paste&height=399&id=uf9e74e96&originHeight=798&originWidth=975&originalType=binary&ratio=1&rotation=0&showTitle=false&size=116831&status=done&style=none&taskId=u2287fc36-ca94-4018-94f5-5a33dcb87b2&title=&width=487.5)

### Side Effects After Activation
If the dependencies delegated to the foundation by the module include published services, then the foundation and the module will publish two copies simultaneously.

<br/>
