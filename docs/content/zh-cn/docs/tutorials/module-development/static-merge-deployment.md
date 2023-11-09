---
title: 静态合并部署
weight: 700
---


## 介绍
SOFAArk 提供了静态合并部署能力，在开发阶段，应用可以将其他应用构建成的** Biz 包（模块应用）**通过 Maven 依赖的方式引入，而当自身被构建成可执行 FatJar 时，可以将其他应用 Biz 包一并打入。在启动时，SOFAArk 会根据优先级依次启动各应用的 Biz 包（模块应用），每个 Biz 包使用独立的 BizClassLoader 加载，不需要考虑相互之间依赖冲突问题，Biz 之间则通过 @SofaService 和 @SofaReference 发布/引用 JVM 服务（SOFABoot，_SpringBoot 还在建设中_）进行交互。


## 步骤 1：模块应用打包成 Ark Biz
如果开发者希望自己应用的 Ark Biz 包能够被其他应用直接当成 Jar 包依赖，进而运行在同一个 SOFAArk 容器之上，那么就需要打包发布 Ark Biz 包，详见 [Ark Biz 介绍](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-ark-biz/)。 Ark Biz 包使用 Maven 插件 sofa-ark-maven-plugin 打包生成。模块应用在配置该插件时，需要配置参数 attach 为 true：
```xml
<build>
    <plugin>
        <groupId>com.alipay.sofa</groupId>
        <artifactId>sofa-ark-maven-plugin</artifactId>
        <version>${sofa.ark.version}</version>
        <executions>
            <execution>
                <id>default-cli</id>
                <goals>
                    <goal>repackage</goal>
                </goals>
            </execution>
        </executions>
        <configuration> <!--同时打包发布 Ark Biz-->
            <attach>true</attach>
        </configuration>
    </plugin>
</build>
```

在执行 **mvn clean install** 后，Ark Biz 包会发布到本地 .m2 仓库中，并带有 ark-biz 后缀，如：${artifactId}-${version}-ark-biz.jar 。


## 步骤 2：基座应用依赖模块 Ark Biz
宿主应用（基座）静态合并部署其它 Ark Biz 包（模块应用）时，只需要在基座应用 pom.xml 中添加依赖，并设置 ark-biz 后缀：
```xml
<dependency>
    <groupId>xxx</groupId>
    <artifactId>xxx</artifactId>
    <classifier>ark-biz</classifier>
    <version>xxx</version>
</dependency>
```
在 Master Biz（基座）的启动过程中，当 Master Biz（基座）启动成功后触发 ApplicationReadyEvent 时，一般情况下会根据优先级依次启动依赖的 Ark Biz 包。

## 步骤 3：验证 Ark Biz（模块）启动
在 Master Biz（基座）启动成功后，可以通过 telnet 启动 SOFAArk 客户端交互界面：
```shell
telnet localhost 1234
```
然后执行如下命令查看模块列表：
```shell
biz -a
```
此时应当可以看到 Master Biz（基座）和所有静态合并部署的 Ark Biz（模块）。

<br/>
<br/>
