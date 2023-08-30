# API

All of the following instruction apis access the arklet using the POST(application/json) request format

The http protocol is enabled and the default port is 1238
> You can set `sofa.serverless.arklet.http.port` JVM startup parameters override the default port

**8.30 updated**
* add health check commands
* add health check actuator
* add health check endpoint

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
    "arkBizFilePath": "/Users/jaimezhang/workspace/github/sofa-ark-dynamic-guides/dynamic-provider/target/dynamic-provider-1.0.0-ark-biz.jar"  
}  
```  
- output sample:
```json  
{  
  "code":"SUCCESS",  
  "data":[  
    {  
      "bizName":"dynamic-provider",  
      "bizVersion":"1.0.0",  
      ...  
    }  
  ]  
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
- output sample:
```json  
{  
  "code":"SUCCESS"  
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

## Query all health info
- URL: 127.0.0.1:1238/health
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
        "total memory(M)": 175.0,
        "free memory(M)": 37.3826904296875,
        "java version": "1.8.0_331",
        "max memory(M)": 885.5,
        "health": {
          "description": "",
          "code": "UP"
        },
        "java home": "D:\\******\\jre",
        "run time(s)": 37.605
      },
      "masterBiz": {
        "readinessState": "ACCEPTING_TRAFFIC",
        "health": {
          "description": "",
          "code": "UP"
        }
      },
      "cpu": {
        "user used (%)": 2.348825587206397,
        "system used (%)": 4.747626186906547,
        "count": 4,
        "health": {
          "description": "",
          "code": "UP"
        },
        "total used (%)": 2001.0,
        "type": "******* CPU @ ****GHz",
        "free (%)": 92.10394802598701
      },
      "masterBizInfo": {
        "webContextPath": "/",
        "bizName": "bookstore-manager",
        "bizState": "ACTIVATED",
        "bizVersion": "1.0.0"
      },
      "allBizInfo": [
        {
          "webContextPath": "/",
          "bizName": "bookstore-manager",
          "bizState": "ACTIVATED",
          "bizVersion": "1.0.0"
        }
      ],
      "allPluginInfo": [
        {
          "pluginVersion": "2.2.2-SNAPSHOT",
          "pluginActivator": "com.alipay.sofa.ark.web.embed.WebPluginActivator",
          "pluginName": "web-ark-plugin",
          "groupId": "com.alipay.sofa",
          "artifactId": "web-ark-plugin",
          "pluginUrl": "file:/****/web-ark-plugin-2.2.2-SNAPSHOT.jar!/"
        },
        {
          "pluginVersion": "3.11.0",
          "pluginActivator": "com.alipay.sofa.****.SofaRuntimeActivator",
          "pluginName": "runtime-sofa-boot-plugin",
          "groupId": "com.alipay.sofa",
          "artifactId": "runtime-sofa-boot-plugin",
          "pluginUrl": "file:/****/runtime-sofa-boot-plugin-3.11.0.jar!/"
        }
      ]
    }
  }
}
```  

## Query all biz health info
- URL: 127.0.0.1:1238/queryAllBizHealth
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
      "jvm": {...},
      "cpu": {...},
      "allBizInfo": [
        {
          "webContextPath": "/",
          "bizName": "bookstore-manager",
          "bizState": "ACTIVATED",
          "bizVersion": "1.0.0"
        }
      ],
    }
  }
}
```  

## Query all plugin health info
- URL: 127.0.0.1:1238/queryAllPluginHealth
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
      "jvm": {...},
      "cpu": {...},
      "allPluginInfo": [
        {
          "pluginName": "web-ark-plugin",
          "groupId": "com.alipay.sofa",
          "artifactId": "web-ark-plugin",
          ...,
        },
        {
          "pluginName": "runtime-sofa-boot-plugin",
          "groupId": "com.alipay.sofa",
          "artifactId": "runtime-sofa-boot-plugin",
          ...,
        }
      ]
    }
  }
}
```  

## Query single biz health info
- URL: 127.0.0.1:1238/queryBizHealth
- input sample:
```json  
{
  "bizname": "bookstore-manager",
  "bizversion": "1.0.0"
}
```  
- output sample:
```json  
{
  "code": "SUCCESS",
  "data": {
    "healthData": {
      "jvm": {...},
      "bizInfo": {
         "webContextPath": "/",
         "bizName": "bookstore-manager",
         "bizState": "ACTIVATED",
         "bizVersion": "1.0.0"
       },
      "cpu": {...},
    }
  }
}
```  

## Query single plugin health info
- URL: 127.0.0.1:1238/queryPluginHealth
- input sample:
```json  
{
  "pluginVersion": "2.2.2-SNAPSHOT",
  "pluginName": "web-ark-plugin"
}
```  
- output sample:
```json  
{
  "code": "SUCCESS",
  "data": {
    "healthData": {
      "jvm": {...},
      "pluginInfo": {
        "pluginVersion": "2.2.2-SNAPSHOT",
        "pluginActivator": "com.alipay.sofa.ark.web.embed.WebPluginActivator",
        "pluginName": "web-ark-plugin",
        "groupId": "com.alipay.sofa",
        "artifactId": "web-ark-plugin",
        "pluginUrl": "file:/****/web-ark-plugin-2.2.2-SNAPSHOT.jar!/"
      },
      "cpu": {...},
    }
  }
}
```  

