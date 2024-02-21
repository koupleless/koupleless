---
title: 快速性测 sofArk 多 classLoader 兼容性。
date: 2024-02-21T28:18:00+08:00
description: Koupleless 快速测试 sofaArk 多 classLoader 模式兼容性。
weight: 700
---

## 介绍

SOFAArk 是一个多 ClassLoader 的框架。
想要测试中间件等和它的兼容性，首先要解决的问题是构建一个多模块的项目, 产出多个 jar 包。
如此，才能在不同的 ClassLoader 中加载不同的类。
但这个过程的人工成本太高了。
为了简化兼容性验证的过程，我们提供了一套测试工具，能在不拆分项目的前提下，模拟多 SOFAArk ClassLoader
的环境，以最小的成本测试兼容性。

## 包名

需要用户引入如下包：

```xml

<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-test-suite</artifactId>
    <version>${latestVersion}</version>
</dependency>
```

## 使用方法

该测试工具的使用方法主要有 2 种模式。

### 模式一：手动写单测。

用户可以构建 SOFAArkTestBiz 类来快速构建一个 Biz 模块，让目标验证逻辑跑在模块中。
具体的工程样例可以参考: [手动写单测](https://github.com/koupleless/koupleless/tree/main/samples/compatibility-samples/compatibility-test/manual-write-test/src/README_zh-CN.md)

### 模式二：通过配置文件 + 命令行运行已有单测。

很多时候，我们只是希望教研现有的单测是否能兼容 SOFAArk 的多 ClassLoader 模式，而不希望写新的单测。
测试，我们可以通过配置文件 + 命令行的方式来运行已有的单测。
具体的工程样例可以参考: [自动运行已有单测](https://github.com/koupleless/koupleless/tree/main/samples/compatibility-samples/compatibility-test/run-existing-test-by-simple-config/src/README_zh-CN.md)
