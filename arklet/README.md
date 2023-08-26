# API

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
    "arkBizFilePath": "file:///Users/jaimezhang/workspace/github/sofa-ark-dynamic-guides/dynamic-provider/target/dynamic-provider-1.0.0-ark-biz.jar"
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