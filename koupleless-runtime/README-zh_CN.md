<div align="center">

[English](./README.md) | 简体中文

</div>

[![codecov](https://codecov.io/gh/sofastack/sofa-serverless/graph/badge.svg?token=q8SGKKa58G)](https://codecov.io/gh/sofastack/sofa-serverless)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.alipay.sofa/sofa-serverless/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.alipay.sofa/sofa-serverless/)
[![GitHub release](https://img.shields.io/github/release/sofastack/sofa-serverless.svg)](https://github.com/sofastack/sofa-serverless/releases)

[//]: # (翻译成中文)
# Overview
# 概述

Arklet 提供了 SOFAArk 基座和模块的运维接口，通过 Arklet 可以轻松灵活的进行 Ark Biz 的发布和运维。

Arklet 内部由 **ArkletComponent** 构成

![image](https://github.com/sofastack/sofa-serverless/assets/11410549/a2740422-569e-4dd3-9c9a-1503996bd2f1)
- ApiClient: The core components responsible for interacting with the outside world
- ApiClient: 负责与外界交互的核心组件
- CommandService: Arklet 暴露能力指令定义和扩展
- OperationService: Ark Biz 与 SOFAArk 交互，增删改查，封装基础能力
- HealthService: 基于基座、模块系统等指标统计健康和稳定性

这些组件之间的关联关系如下图
![overview](https://user-images.githubusercontent.com/11410549/266193839-7865e417-6909-4e89-bd48-c926162eaf83.jpg)


当然，您也可以通过实现 **ArkletComponent** 接口来扩展 Arklet 的组件功能。

# Command Extension
# 指令扩展
Arklet 通过外部暴露指令 API，通过每个 API 映射的 CommandHandler 内部处理指令。
> CommandHandler 相关扩展属于 CommandService 组件统一管理

你可以通过继承 **AbstractCommandHandler** 来自定义扩展指令

## 内置指令 API

下面所有的指令 API 都是通过 POST(application/json) 请求格式访问 arklet

使用的是 http 协议，1238 端口
> 你可以通过设置 `sofa.serverless.arklet.http.port` JVM 启动参数覆盖默认端口

## 查询支持的指令
- URL: 127.0.0.1:1238/help
- input sample:
```json
{}
```
- output sample:
```json
{
    "code":"SUCCESS",
    "data":[
        {
            "desc":"query all ark biz(including master biz)",
            "id":"queryAllBiz"
        },
        {
            "desc":"list all supported commands",
            "id":"help"
        },
        {
            "desc":"uninstall one ark biz",
            "id":"uninstallBiz"
        },
        {
            "desc":"switch one ark biz",
            "id":"switchBiz"
        },
        {
            "desc":"install one ark biz",
            "id":"installBiz"
        }
    ]
}
```

## 安装一个模块
- URL: 127.0.0.1:1238/installBiz
- 输入例子:
```json
{
    "bizName": "test",
    "bizVersion": "1.0.0",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///Users/jaimezhang/workspace/github/sofa-ark-dynamic-guides/dynamic-provider/target/dynamic-provider-1.0.0-ark-biz.jar"
}
```

- 输出例子(success):
```json
{
  "code":"SUCCESS",
  "data":{
    "bizInfos":[
      {
        "bizName":"dynamic-provider",
        "bizState":"ACTIVATED",
        "bizVersion":"1.0.0",
        "declaredMode":true,
        "identity":"dynamic-provider:1.0.0",
        "mainClass":"io.sofastack.dynamic.provider.ProviderApplication",
        "priority":100,
        "webContextPath":"provider"
      }
    ],
    "code":"SUCCESS",
    "message":"Install Biz: dynamic-provider:1.0.0 success, cost: 1092 ms, started at: 16:07:47,769"
  }
}
```

- 输出例子(failed):
```json
{
  "code":"FAILED",
  "data":{
    "code":"REPEAT_BIZ",
    "message":"Biz: dynamic-provider:1.0.0 has been installed or registered."
  }
}
```

## 卸载一个模块
- URL: 127.0.0.1:1238/uninstallBiz
- 输入例子:
```json
{
    "bizName":"dynamic-provider",
    "bizVersion":"1.0.0"
}
```
- 输出例子(success):
```json
{
  "code":"SUCCESS"
}
```

- 输出例子(failed):
```json
{
  "code":"FAILED",
  "data":{
    "code":"NOT_FOUND_BIZ",
    "message":"Uninstall biz: test:1.0.0 not found."
  }
}
```

## 切换一个模块
- URL: 127.0.0.1:1238/switchBiz
- 输入例子:
```json
{
    "bizName":"dynamic-provider",
    "bizVersion":"1.0.0"
}
```
- 输出例子:
```json
{
  "code":"SUCCESS"
}
```

## 查询所有模块
- URL: 127.0.0.1:1238/queryAllBiz
- 输入例子:
```json
{}
```
- 输出例子:
```json
{
  "code":"SUCCESS",
  "data":[
    {
      "bizName":"dynamic-provider",
      "bizState":"ACTIVATED",
      "bizVersion":"1.0.0",
      "mainClass":"io.sofastack.dynamic.provider.ProviderApplication",
      "webContextPath":"provider"
    },
    {
      "bizName":"stock-mng",
      "bizState":"ACTIVATED",
      "bizVersion":"1.0.0",
      "mainClass":"embed main",
      "webContextPath":"/"
    }
  ]
}
```

## 查询健康与状态信息
- URL: 127.0.0.1:1238/health

### 查询所有健康与状态信息

- 输入信息:
```json
{}
```
- 输出信息:
```json
{
  "code": "SUCCESS",
  "data": {
    "healthData": {
      "jvm": {
        "max non heap memory(M)": -9.5367431640625E-7,
        "java version": "1.8.0_331",
        "max memory(M)": 885.5,
        "max heap memory(M)": 885.5,
        "used heap memory(M)": 137.14127349853516,
        "used non heap memory(M)": 62.54662322998047,
        "loaded class count": 10063,
        "init non heap memory(M)": 2.4375,
        "total memory(M)": 174.5,
        "free memory(M)": 37.358726501464844,
        "unload class count": 0,
        "total class count": 10063,
        "committed heap memory(M)": 174.5,
        "java home": "****\\jre",
        "init heap memory(M)": 64.0,
        "committed non heap memory(M)": 66.203125,
        "run time(s)": 34.432
      },
      "cpu": {
        "count": 4,
        "total used (%)": 131749.0,
        "type": "****",
        "user used (%)": 9.926451054656962,
        "free (%)": 81.46475495070172,
        "system used (%)": 6.249762806548817
      },
      "masterBizInfo": {
        "webContextPath": "/",
        "bizName": "bookstore-manager",
        "bizState": "ACTIVATED",
        "bizVersion": "1.0.0"
      },
      "pluginListInfo": [
        {
          "artifactId": "web-ark-plugin",
          "groupId": "com.alipay.sofa",
          "pluginActivator": "com.alipay.sofa.ark.web.embed.WebPluginActivator",
          "pluginName": "web-ark-plugin",
          "pluginUrl": "file:/****/2.2.3-SNAPSHOT/web-ark-plugin-2.2.3-20230901.090402-2.jar!/",
          "pluginVersion": "2.2.3-SNAPSHOT"
        },
        {
          "artifactId": "runtime-sofa-boot-plugin",
          "groupId": "com.alipay.sofa",
          "pluginActivator": "com.alipay.sofa.runtime.ark.plugin.SofaRuntimeActivator",
          "pluginName": "runtime-sofa-boot-plugin",
          "pluginUrl": "file:/****/runtime-sofa-boot-plugin-3.11.0.jar!/",
          "pluginVersion": "3.11.0"
        }
      ],
      "masterBizHealth": {
        "readinessState": "ACCEPTING_TRAFFIC"
      },
      "bizListInfo": [
        {
          "bizName": "bookstore-manager",
          "bizState": "ACTIVATED",
          "bizVersion": "1.0.0",
          "webContextPath": "/"
        }
      ]
    }
  }
}
```

### 查询系统健康与状态信息
- 输入例子:

```json
{
  "type": "system",
  // [OPTIONAL] if metrics is null -> query all system health info
  "metrics": ["cpu", "jvm"]
}
```
- 输出例子:
```json
{
  "code": "SUCCESS",
  "data": {
    "healthData": {
      "jvm": {...},
      "cpu": {...},
//      "masterBizHealth": {...}
    }
  }
}
```

### 查询模块健康与状态信息
- 输入例子:

```json
{
  "type": "biz",
  // [OPTIONAL] if moduleName is null and moduleVersion is null -> query all biz
  "moduleName": "bookstore-manager",
  // [OPTIONAL] if moduleVersion is null -> query all biz named moduleName
  "moduleVersion": "1.0.0"
}
```
- 输出例子:
```json
{
  "code": "SUCCESS",
  "data": {
    "healthData": {
      "bizInfo": {
        "bizName": "bookstore-manager",
        "bizState": "ACTIVATED",
        "bizVersion": "1.0.0",
        "webContextPath": "/"
      }
//      "bizListInfo": [
//        {
//          "bizName": "bookstore-manager",
//          "bizState": "ACTIVATED",
//          "bizVersion": "1.0.0",
//          "webContextPath": "/"
//        }
//      ]
    }
  }
}
```

### 查询插件健康与状态信息
- 输入例子:

```json
{
  "type": "plugin",
  // [OPTIONAL] if moduleName is null -> query all biz
  "moduleName": "web-ark-plugin"
}
```
- 输出例子:
```json
{
  "code": "SUCCESS",
  "data": {
    "healthData": {
      "pluginListInfo": [
        {
          "artifactId": "web-ark-plugin",
          "groupId": "com.alipay.sofa",
          "pluginActivator": "com.alipay.sofa.ark.web.embed.WebPluginActivator",
          "pluginName": "web-ark-plugin",
          "pluginUrl": "file:/****/web-ark-plugin-2.2.3-20230901.090402-2.jar!/",
          "pluginVersion": "2.2.3-SNAPSHOT"
        }
      ]
    }
  }
}
```

### 使用 Endpoint 来查询健康信息

使用 endpoint 来查询 k8s 模块的健康信息

** 默认配置 **
* endpoints path: `/`
* endpoints 服务端口: `8080`

** http 结果码 **
* `HEALTHY(200)`: 所有健康指标都健康
* `UNHEALTHY(400)`: 至少有一个健康指标已经不健康
* `ENDPOINT_NOT_FOUND(404)`: 路径或参数不存在
* `ENDPOINT_PROCESS_INTERNAL_ERROR(500)`: 遇到异常

### 查询所有健康信息
- url: 127.0.0.1:8080/arkletHealth
- method: GET
- 输出例子

```json  
{   
    "healthy": true,
    "code": 200,    
    "codeType": "HEALTHY",    
    "data": {        
        "jvm": {...},        
        "masterBizHealth": {...},        
        "cpu": {...},        
        "masterBizInfo": {...},        
        "bizListInfo": [...],        
        "pluginListInfo": [...]    
    }
}  
```    
### 查询所有模块的健康信息
- url: 127.0.0.1:8080/arkletHealth/{moduleType} (moduleType must in ['biz', 'plugin'])
- method: GET
- 输出例子
 ```json  
{   
    "healthy": true,
    "code": 200,    
    "codeType": "HEALTHY",    
    "data": {        
        "bizListInfo": [...],  
        // "pluginListInfo": [...]      
    }
}  
```      
### 查询单个模块的健康信息
- url: 127.0.0.1:8080/arkletHealth/{moduleType}/moduleName/moduleVersion (moduleType must in ['biz', 'plugin'])
- method: GET
- 输出例子

 ```json  
{   
    "healthy": true,
    "code": 200,    
    "codeType": "HEALTHY",    
    "data": {        
        "bizInfo": {...},  
        // "pluginInfo": {...}      
    }
}  
```  


