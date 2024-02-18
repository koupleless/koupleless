---
title: 模块测试
date: 2024-01-25T10:28:32+08:00
description: Koupleless 模块测试
weight: 400
---

## 本地调试
您可以在本地或远程先启动基座，然后使用客户端 Arklet 暴露的 HTTP 接口在本地或远程部署模块，并且可以给模块代码打断点实现模块的本地或远程 Debug。<br />Arklet HTTP 接口主要提供了以下能力：

1. 部署和卸载模块。
2. 查询所有已部署的模块信息。
3. 查询各项系统和业务指标。

### 部署模块
```shell
curl -X POST -H "Content-Type: application/json" http://127.0.0.1:1238/installBiz 
```
请求体样例：
```json
{
    "bizName": "test",
    "bizVersion": "1.0.0",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///Users/jaimezhang/workspace/github/sofa-ark-dynamic-guides/dynamic-provider/target/dynamic-provider-1.0.0-ark-biz.jar"
}
```
部署成功返回结果样例：
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
部署失败返回结果样例：
```json
{
  "code":"FAILED",
  "data":{
    "code":"REPEAT_BIZ",
    "message":"Biz: dynamic-provider:1.0.0 has been installed or registered."
  }
}
```

### 卸载模块
```shell
curl -X POST -H "Content-Type: application/json" http://127.0.0.1:1238/uninstallBiz 
```
请求体样例：
```json
{
    "bizName":"dynamic-provider",
    "bizVersion":"1.0.0"
}
```
卸载成功返回结果样例：
```json
{
  "code":"SUCCESS"
}
```
卸载失败返回结果样例：
```json
{
  "code":"FAILED",
  "data":{
    "code":"NOT_FOUND_BIZ",
    "message":"Uninstall biz: test:1.0.0 not found."
  }
}
```

### 查询模块
```shell
curl -X POST -H "Content-Type: application/json" http://127.0.0.1:1238/queryAllBiz 
```
请求体样例：
```json
{}
```
返回结果样例：
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

### 获取帮助
Arklet 暴露的所有对外 HTTP 接口，可以查看 Arklet 接口帮助：
```shell
curl -X POST -H "Content-Type: application/json" http://127.0.0.1:1238/help 
```
请求体样例：
```json
{}
```
返回结果样例：
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

## 本地构建如何不改变模块版本号
添加以下 maven profile，本地构建模块使用命令 mvn clean package -Plocal
```xml
<profile>
    <id>local</id>
    <build>
        <plugins>
            <plugin>
                <groupId>com.alipay.sofa</groupId>
                <artifactId>sofa-ark-maven-plugin</artifactId>
                <configuration>
                    <finalName>${project.artifactId}-${project.version}</finalName>
                    <bizVersion>${project.version}</bizVersion>
                </configuration>
            </plugin>
        </plugins>
    </build>
</profile>
```

## 单元测试
模块里支持使用标准 JUnit4 和 TestNG 编写和执行单元测试。

<br/>
<br/>
