---
title: Arklet 架构设计与接口设计
date: 2023-09-21T10:28:35+08:00
weight: 100
---

# 概述

Arklet 为 SofaArk 基础和模块的交付提供了一个操作接口。有了 Arklet，Ark Biz 的发布和操作可以轻松灵活地进行。

Arklet 是由 **ArkletComponent** 内部构建的

![image](https://github.com/sofastack/sofa-serverless/assets/11410549/a2740422-569e-4dd3-9c9a-1503996bd2f1)
- ApiClient: 负责与外界交互的核心组件
- CommandService: Arklet 对外暴露能力指令定义和扩展
- OperationService: Ark Biz 与 SofaArk 交互，进行添加、删除、修改和封装基本能力
- HealthService: 基于健康和稳定性，计算基础、Biz、系统等其他指标

他们之间的协作如图所示
![overview](https://user-images.githubusercontent.com/11410549/266193839-7865e417-6909-4e89-bd48-c926162eaf83.jpg)


当然，您也可以通过实现 **ArkletComponent** 接口来扩展 Arklet 的组件功能

# 命令扩展
Arklet 外部公开了指令 API，并通过每个 API 映射的 CommandHandler 内部处理指令。
> CommandHandler 相关的扩展属于 CommandService 组件的统一管理

您可以通过继承 **AbstractCommandHandler** 来自定义扩展命令

## 内置命令 API

以下所有的指令 api 都使用 POST(application/json) 请求格式访问 arklet

启用了 http 协议，默认端口是 1238
> 您可以设置 `sofa.serverless.arklet.http.port` JVM 启动参数覆盖默认端口


## 查询支持的命令
- URL: 127.0.0.1:1238/help
- 输入样例:
```json
{}
```
- 输出样例:
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

## 安装一个 biz
- URL: 127.0.0.1:1238/installBiz
- 输入样例:

```json
{
    "bizName": "test",
    "bizVersion": "1.0.0",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///Users/jaimezhang/workspace/github/sofa-ark-dynamic-guides/dynamic-provider/target/dynamic-provider-1.0.0-ark-biz.jar"
}
```

- 输出样例(成功):
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

-输出样例(失败):
```json
{
  "code":"FAILED",
  "data":{
    "code":"REPEAT_BIZ",
    "message":"Biz: dynamic-provider:1.0.0 has been installed or registered."
  }
}
```


## 卸载模块
- URL: 127.0.0.1:1238/uninstallBiz
- 输入样例:
```json
{
    "bizName":"dynamic-provider",
    "bizVersion":"1.0.0"
}
```
-输出样例(成功):
```json
{
  "code":"SUCCESS"
}
```

- 输出样例(失败):
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
- 输出样例:
```json
{
    "bizName":"dynamic-provider",
    "bizVersion":"1.0.0"
}
```
- 输出样例:
```json
{
  "code":"SUCCESS"
}
```

## 查询所有 Biz
- URL: 127.0.0.1:1238/queryAllBiz
- 输入样例:
```json
{}
```
- 输出样例:
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

## 查询健康状况
- URL: 127.0.0.1:1238/health

以下根据不同的输入参数，获取到不同的状态信息

### 查询健康状况
- 输入样例:
```json
{}
```
- 输出样例:
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

### 查询系统健康信息
- 输入样例:

```json
{
  "type": "system",
  // [OPTIONAL] if metrics is null -> query all system health info
  "metrics": ["cpu", "jvm"]
}
```
- 输出样例:
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

### 查询模块健康信息
- 输入样例:

```json
{
  "type": "biz",
  // [OPTIONAL] if moduleName is null and moduleVersion is null -> query all biz
  "moduleName": "bookstore-manager",
  // [OPTIONAL] if moduleVersion is null -> query all biz named moduleName
  "moduleVersion": "1.0.0"
}
```
- 输出样例:
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

### 查询插件健康信息
- 输入样例:

```json
{
  "type": "plugin",
  // [OPTIONAL] if moduleName is null -> query all biz
  "moduleName": "web-ark-plugin"
}
```
- 输出样例:
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

### 使用端点查询健康状况

使用端点获取 k8s 模块的健康信息

**默认配置**
* 端点暴露包括：`*`
* 端点基本路径：`/`
* 端点服务器端口：`8080`

**http 代码结果**
* `HEALTHY(200)`：如果所有健康指标都是健康的，获取健康信息
* `UNHEALTHY(400)`：一旦健康指标不健康，获取健康信息
* `ENDPOINT_NOT_FOUND(404)`：找不到端点路径或参数
* `ENDPOINT_PROCESS_INTERNAL_ERROR(500)`：获取健康过程中抛出错误

### 查询所有健康信息
```shell
curl 127.0.0.1:8080/arkletHealth
```
- 输出样例

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
### 查询所有 biz/plugin 健康信息
```shell
curl: 127.0.0.1:8080/arkletHealth/{moduleType} (moduleType 必须在 ['biz', 'plugin'] 中)
```
- 输出样例
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
### 查询单个 biz/plugin 健康信息
```shell
curl 127.0.0.1:8080/arkletHealth/{moduleType}/moduleName/moduleVersion (moduleType must in ['biz', 'plugin'])
```
- 输出样例：

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

<br/>
