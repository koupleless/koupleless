---
title: 模块测试
description: Show your user how to work through some end to end examples.
date: 2017-01-04
weight: 8
---

## 本地调试

您可以使用 SOFAArk 自带的 telnet 命令在本地启动基座、安装模块，结合给模块代码打断点，即可实现模块在本地调试。
在本地启动基座后，在终端执行如下命令，即可进入 SOFAArk telnet 命令行：

```properties
telnet localhost 1234
```

SOFAArk telnet 命令行提供了以下能力：
1. 本地安装、卸载、查询场景。
2. 安装、卸载、查询模块。

### 获取帮助

```shell
sofa-ark>biz -h
Biz Command Tips:
  USAGE: biz [option...] [arguments...]
  SAMPLE: biz -m bizIdentityA bizIdentityB.
  -h  Shows the help message.
  -a  Shows all biz.
  -m  Shows the meta info of specified bizIdentity.
  -s  Shows the service info of specified bizIdentity.
  -d  Shows the detail info of specified bizIdentity.
  -i  Install biz of specified bizIdentity or bizUrl.
  -u  Uninstall biz of specified bizIdentity.
  -o  Switch biz of specified bizIdentity.
```

### 模块查询

```shell
biz -a
```

### 模块安装
```shell
biz -i file:///Users/yuan/Code/module-demo/target/demomodule-1.0.0-20230810194809-ark-biz.jar
```

### 模块卸载
```shell
biz -u bizName:bizVersion
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
模块支持使用标准 JUnit4 和 TestNG 编写和执行单元测试。