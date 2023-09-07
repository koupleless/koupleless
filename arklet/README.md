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