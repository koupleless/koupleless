---
title: log4j2 Multi-Module Adaptation
date: 2024-01-25T10:28:32+08:00
description: Koupleless log4j2 Multi-Module Adaptation
weight: 1
---

## Why Adaptation is Needed
In its native state, log4j2 does not provide individual log directories for modules in a multi-module environment. Instead, it logs uniformly to the base directory, which makes it challenging to isolate logs and corresponding monitoring for each module. The purpose of this adaptation is to enable each module to have its own independent log directory.

## Initialization of log4j2 in Regular Applications
Before Spring starts, log4j2 initializes various logContexts and configurations using default values. During the Spring startup process, it listens for Spring events to finalize initialization. This process involves invoking the Log4j2LoggingSystem.initialize method via `org.springframework.boot.context.logging.LoggingApplicationListener`.

![](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1696930949183-9519451c-be76-4d9b-bb6b-28a1b21e7fa7.png)

The method determines whether it has already been initialized based on the `loggerContext`.

> Here, a problem arises in a multi-module environment.
>
> The `getLoggerContext` method retrieves the `LoggerContext` based on the classLoader of `org.apache.logging.log4j.LogManager`. Relying on the classLoader of a specific class to extract the `LoggerContext` can be unstable in a multi-module setup. This instability arises because some classes in modules can be configured to delegate loading to the base, so when a module starts, it might obtain the `LoggerContext` from the base. Consequently, if `isAlreadyInitialized` returns true, the log4j2 logging for the module cannot be further configured based on user configuration files.

If it hasn't been initialized yet, it enters `super.initialize`, which involves two tasks:

1. Retrieving the log configuration file.
2. Parsing the variable values in the log configuration file.
   Both of these tasks may encounter issues in a multi-module setup. Let's first examine how these two steps are completed in a regular application.

### Retrieving the Log Configuration File
![](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1696931678652-81a19dc2-f618-48b0-add3-d098d3781966.png?x-oss-process=image%2Fresize%2Cw_1500%2Climit_0)

You can see that the location corresponding to the log configuration file's URL is obtained through ResourceUtils.getURL. Here, the URL is obtained by retrieving the current thread's context ClassLoader, which works fine in a multi-module environment (since each module's startup thread context is already its own ClassLoader).

![](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1696931908899-f1fac1bb-f365-49f9-81a2-3e2d924c2b7d.png?x-oss-process=image%2Fresize%2Cw_1500%2Climit_0)

### Parsing Log Configuration Values

The configuration file contains various variables, such as these:

![](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1696932148670-d04bde21-e46b-476c-9cf5-53e43cc4dbe2.png)

These variables are parsed in the specific implementation of `org.apache.logging.log4j.core.lookup.AbstractLookup`, including:


||Variable Syntax |	Implementation Class |
|-|-|-|
| ${bundle:application:logging.file.path} |	org.apache.logging.log4j.core.lookup.ResourceBundleLookup | Locates application.properties based on the ClassLoader of ResourceBundleLookup and reads the values inside. |
| ${ctx:logging.file.path} | org.apache.logging.log4j.core.lookup.ContextMapLookup | Retrieves values stored in the LoggerContext ThreadContext. It's necessary to set the values from application.properties into the ThreadContext. |

Based on the above analysis, configuring via bundle method might not be feasible in a multi-module setup because ResourceBundleLookup might only exist in the base module, leading to always obtaining application.properties from the base module. Consequently, the logging configuration path of the modules would be the same as that of the base module, causing all module logs to be logged into the base module. Therefore, it needs to be modified to use ContextMapLookup.

## Expected Logging in a Multi-Module Consolidation Scenario
Both the base module and individual modules should be able to use independent logging configurations and values, completely isolated from each other. However, due to the potential issues identified in the analysis above, which could prevent module initialization, additional adaptation of log4j2 is required.

### Multi-Module Adaptation Points
1. Ensure `getLoggerContext()` can retrieve the LoggerContext of the module itself.
![](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1696938182575-51ce1066-21f0-47bb-8bdb-c3c7d0814ca3.png)
2. It's necessary to adjust to use ContextMapLookup so that module logs can retrieve the module application name and be logged into the module directory.

   a. Set the values of application.properties to ThreadContext when the module starts.
   b. During logging configuration, only use the ctx:xxx:xxx configuration format.

## Module Refactoring Approach
[Check the source code for detailed information](https://github.com/koupleless/koupleless/blob/main/koupleless-runtime/koupleless-adapter-ext/koupleless-adapter-log4j2/src/main/java/org/springframework/boot/logging/log4j2)

