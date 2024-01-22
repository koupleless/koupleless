---
title: 常见问题列表
weight: 10
---

#### 问题 1-1：模块 compile 引入 springboot 依赖，模块安装时报错
```text
java.lang.IllegalArgumentException: Cannot instantiate interface org.springframework.context.ApplicationListener : com.alipay.koupleless.common.spring.KouplelessApplicationListener
```
##### 解决方式
模块需要做好瘦身，参考这里：[模块瘦身](/docs/tutorials/module-development/module-slimming.md)

#### 问题 1-2：模块安装找不到 `KouplelessApplicationListener`
报错信息如下：
```text
com.alipay.sofa.ark.exception.ArkLoaderException: [ArkBiz Loader] module1:1.0-SNAPSHOT : can not load class: com.alipay.koupleless.common.spring.KouplelessApplicationListener
```
##### 解决方式
请在模块里面添加如下依赖：
```xml
<dependency>
    <groupId>com.alipay.koupleless</igroupId>
    <artifactId>koupleless-app-starter</artifactId>
    <version>0.5.6</version>
</dependency>
```
或者升级 koupleless 版本到最新版本

#### 问题 1-3: 通过 go install 无法安装 arkctl
执行如下命令，报错
```shell
go install koupleless.alipay.com/koupleless/v1/arkctl@latest
```
报错信息如下：
```text
go: koupleless.alipay.com/koupleless/v1/arkctl@latest: module koupleless.alipay.com/koupleless/v1/arkctl: Get "https://proxy.golang.org/koupleless.alipay.com/koupleless/v1/arkctl/@v/list": dial tcp 142.251.42.241:443: i/o timeout
```
##### 解决方式
arkctl 是作为 koupleless 子目录的方式存在的，所以没法直接 go get，可以从这下面下载执行文件, 请参考[安装 arkctl](https://github.com/koupleless/koupleless/releases/tag/arkctl-release-0.1.0)

#### 问题 1-4：模块安装报 `Master biz environment is null`

##### 解决方式，升级 koupleless 版本到最新版本
```xml
<dependency>
    <groupId>com.alipay.koupleless</igroupId>
    <artifactId>koupleless-app-starter</artifactId>
    <version>${最新版本号}</version>
</dependency>
```

#### 问题 1-5：模块静态合并部署无法从制定的目录里找到模块包
##### 解决方式：升级 koupleless 版本到最新版本
```xml
<dependency>
    <groupId>com.alipay.koupleless</igroupId>
    <artifactId>koupleless-app-starter</artifactId>
    <version>${最新版本号}</version>
</dependency>
```
#### 问题 1-6：用户工程与 koupleless 里 guice 版本不一致，且版本较老
报错信息：
```text
Caused by: java.Lang.ClassNotFoundException: com.google.inject.multibindings.Multibinder
```
![guice_version_incompatibility.png](imgs/guice_version_incompatibility.png)

##### 解决方式：升级 guice 版本到较新版本，如
```xml
<dependency>
    <groupId>com.google.inject</groupId>
    <artifactId>guice</artifactId>
    <version>6.0.0</version>
</dependency>
```

#### 问题 1-7：模块与基座共库时，模块启动了基座的逻辑
例如基座引入了 druid，但是模块里没有引入，按照设计模块应该不需要初始化 dataSource，但是如果遇到模块也初始化了 dataSource，那么该行为是不符合预期的，也可能导致报错。

##### 解决方式
1. 确保模块可以独立构建，也就是可以在模块的目录里执行 `mvn clean package`，并且不会报错
2. 升级 koupleless 版本到最新版本 0.5.7
