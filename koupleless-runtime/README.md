<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

[![codecov](https://codecov.io/gh/sofastack/sofa-serverless/graph/badge.svg?token=q8SGKKa58G)](https://codecov.io/gh/sofastack/sofa-serverless)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.alipay.sofa/sofa-serverless/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.alipay.sofa/sofa-serverless/)
[![GitHub release](https://img.shields.io/github/release/sofastack/sofa-serverless.svg)](https://github.com/sofastack/sofa-serverless/releases)
# Overview

Arklet provides an operational interface for delivery of SofaArk bases and modules. With Arklet, the release and operation of Ark Biz can be easily and flexibly operated.

Arklet is internally constructed by **ArkletComponent**

![image](https://github.com/sofastack/sofa-serverless/assets/11410549/a2740422-569e-4dd3-9c9a-1503996bd2f1)
- ApiClient: The core components responsible for interacting with the outside world
- CommandService: Arklet exposes capability instruction definition and extension
- OperationService: Ark Biz interacts with SofaArk to add, delete, modify, and encapsulate basic capabilities
- HealthService: Based on health and stability, base, Biz, system and other indicators are calculated

The collaboration between them is shown in the figure
![overview](https://user-images.githubusercontent.com/11410549/266193839-7865e417-6909-4e89-bd48-c926162eaf83.jpg)


Of course, you can also extend Arklet's component capabilities by implementing the **ArkletComponent** interface

# Command Extension
The Arklet exposes the instruction API externally and handles the instruction internally through a CommandHandler mapped from each API.
> CommandHandler related extensions belong to the unified management of the CommandService component

You can customize extension commands by inheriting **AbstractCommandHandler**

## Build-in Command API

All of the following instruction apis access the arklet using the POST(application/json) request format

The http protocol is enabled and the default port is 1238
> You can set `sofa.serverless.arklet.http.port` JVM startup parameters override the default port


## Query the supported commands
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

## Install a biz
- URL: 127.0.0.1:1238/installBiz
- input sample:
```json
{
    "bizName": "test",
    "bizVersion": "1.0.0",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///Users/jaimezhang/workspace/github/sofa-ark-dynamic-guides/dynamic-provider/target/dynamic-provider-1.0.0-ark-biz.jar"
}
```

- output sample(success):
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

- output sample(failed):
```json
{
  "code":"FAILED",
  "data":{
    "code":"REPEAT_BIZ",
    "message":"Biz: dynamic-provider:1.0.0 has been installed or registered."
  }
}
```


## Uninstall a biz
- URL: 127.0.0.1:1238/uninstallBiz
- input sample:
```json
{
    "bizName":"dynamic-provider",
    "bizVersion":"1.0.0"
}
```
- output sample(success):
```json
{
  "code":"SUCCESS"
}
```

- output sample(failed):
```json
{
  "code":"FAILED",
  "data":{
    "code":"NOT_FOUND_BIZ",
    "message":"Uninstall biz: test:1.0.0 not found."
  }
}
```

## Switch a biz
- URL: 127.0.0.1:1238/switchBiz
- input sample:
```json
{
    "bizName":"dynamic-provider",
    "bizVersion":"1.0.0"
}
```
- output sample:
```json
{
  "code":"SUCCESS"
}
```

## Query all Biz
- URL: 127.0.0.1:1238/queryAllBiz
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

## Query Health
- URL: 127.0.0.1:1238/health

### Query All Health Info
- input sample:
```json
{}
```
- output sample:
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

### Query System Health Info
- input sample:

```json
{
  "type": "system",
  // [OPTIONAL] if metrics is null -> query all system health info
  "metrics": ["cpu", "jvm"]
}
```
- output sample:
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

### Query Biz Health Info
- input sample:

```json
{
  "type": "biz",
  // [OPTIONAL] if moduleName is null and moduleVersion is null -> query all biz
  "moduleName": "bookstore-manager",
  // [OPTIONAL] if moduleVersion is null -> query all biz named moduleName
  "moduleVersion": "1.0.0"
}
```
- output sample:
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

### Query Plugin Health Info
- input sample:

```json
{
  "type": "plugin",
  // [OPTIONAL] if moduleName is null -> query all biz
  "moduleName": "web-ark-plugin"
}
```
- output sample:
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

### Query Health Using Endpoint

use endpoint for k8s module to get helath info

**default config**
* endpoints exposure include: `*`
* endpoints base path: `/`
* endpoints sever port: `8080`

**http code result**
* `HEALTHY(200)`: get health if all health indicator is healthy
* `UNHEALTHY(400)`: get health once a health indicator is unhealthy
* `ENDPOINT_NOT_FOUND(404)`: endpoint path or params not found
* `ENDPOINT_PROCESS_INTERNAL_ERROR(500)`:  get health process throw an error

### query all health info
- url: 127.0.0.1:8080/arkletHealth
- method: GET
- output sample

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
### query all biz/plugin health info
- url: 127.0.0.1:8080/arkletHealth/{moduleType} (moduleType must in ['biz', 'plugin'])
- method: GET
- output sample
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
### query single biz/plugin health info
- url: 127.0.0.1:8080/arkletHealth/{moduleType}/moduleName/moduleVersion (moduleType must in ['biz', 'plugin'])
- method: GET
- output sample

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


