---
title: 模块瘦身
weight: 200
---

## 基本原理
SOFAServerless 底层借助 SOFAArk 框架，实现了模块之间、模块和基座之间的相互隔离，以下两个核心逻辑对编码非常重要，需要深刻理解：

1. 基座有独立的类加载器和 Spring 上下文，模块也有**独立的类加载器**和** Spring 上下文**，相互之间 Spring 上下文都是**隔离的**。
2. 模块启动时会初始化各种对象，会**优先使用模块的类加载器**去加载构建产物 FatJar 中的 class、resource 和 Jar 包，**找不到的类会委托基座的类加载器**去查找。

![](https://intranetproxy.alipay.com/skylark/lark/0/2023/jpeg/8276/1678275655551-75bf283f-3817-447a-84b2-7f6f7f773300.jpeg)

基于这套类委托的加载机制，让基座和模块共用的 class、resource 和 Jar 包**通通下沉**到基座中，可以让模块构建产物**非常小**，更重要的是还能让模块在运行中大量复用基座已有的 class、bean、service、IO 连接池、线程池等资源，从而模块消耗的内存**非常少**，启动也能**非常快**。<br />所谓模块瘦身，就是让基座已经有的 Jar 依赖务必在模块中剔除干净，在主 pom.xml 和 bootstrap/pom.xml 将共用的 Jar 包 **scope 都声明为 provided**，让其不参与打包构建。

## 为什么要瘦身
为了让模块安装更快、内存消耗更小：

- 提高模块安装的速度，减少模块包大小，减少启动依赖，控制模块安装耗时 < 30秒，甚至 < 5秒。
- 模块启动后 Spring 上下文中会创建很多对象，如果启用了模块热卸载，可能无法完全回收，安装次数过多会造成 Old 区、Metaspace 区开销大，触发频繁 FullGC，所有要控制单模块包大小 < 5MB。**这样不替换或重启基座也能热部署热卸载数百次。**

## 手动排包瘦身
模块运行时装载类时，会优先从自己的依赖里找，找不到的话再委托基座的 ClassLoader 去加载。<br />所以对于基座已经存在的依赖，在模块 pom 里将其 scope 设置成 provided，避免其参与模块打包。<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/8276/1678276103445-036d226e-4f88-40bc-937d-90fd4c60b83d.png#clientId=udf1ce5b3-f5a9-4&from=paste&height=521&id=jFiln&originHeight=1042&originWidth=1848&originalType=binary&ratio=2&rotation=0&showTitle=false&size=957278&status=done&style=none&taskId=u254c8709-de81-4175-bcf8-f1c4a26bc49&title=&width=924)

如果要排除的依赖无法找到，可以利用 **maven helper 插件**找到其直接依赖。举个例子，图示中要排除的依赖为 spring-boot-autoconfigure，右边的直接依赖有 sofa-boot-alipay-runtime，ddcs-alipay-sofa-boot-starter等（只需要看 scope 为 compile 的依赖）：<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/191604/1691733668683-34a9d11f-3ca6-4b66-a4e3-22ade9413094.png#clientId=u05d65c58-49f7-4&from=paste&height=869&id=u467da8b5&originHeight=1738&originWidth=2644&originalType=binary&ratio=2&rotation=0&showTitle=false&size=1043897&status=done&style=none&taskId=u70530c01-d7a5-4ca9-875d-3785f59242b&title=&width=1322)<br />确定自己代码 pom.xml 中有 ddcs-alipay-sofa-boot-starter，增加 exlcusions 来排除依赖：<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/191604/1691735644585-9201c203-b749-46e9-ab96-49ecc8090098.png#clientId=uda997d0f-c9aa-4&from=paste&height=244&id=ub08bbabe&originHeight=488&originWidth=1476&originalType=binary&ratio=2&rotation=0&showTitle=false&size=85290&status=done&style=none&taskId=u7f72a9d1-a1cd-422e-a50a-beafc4a9c4a&title=&width=738)


## 在 pom 中统一排包（更彻底）
有些依赖引入了过多的间接依赖，手动排查比较困难，此时可以通过通配符匹配，把那些中间件、基座的依赖全部剔除掉，如 org.apache.commons、org.springframework 等等，这种方式会把间接依赖都排除掉，相比使用 sofa-ark-maven-plugin 排包效率会更高：
```xml
<dependency>
    <groupId>com.serverless.mymodule</groupId>
    <artifactId>mymodule-core</artifactId>
    <exclusions>
          <exclusion>
              <groupId>org.springframework</groupId>
              <artifactId>*</artifactId>
          </exclusion>
          <exclusion>
              <groupId>org.apache.commons</groupId>
              <artifactId>*</artifactId>
          </exclusion>
          <exclusion>
              <groupId>......</groupId>
              <artifactId>*</artifactId>
          </exclusion>
    </exclusions>
</dependency>
```


## 在 sofa-ark-maven-plugin 中指定排包
通过使用 **excludeGroupIds、excludeGroupIds **能够排除大量基座上已有的公共依赖：
```xml
 <plugin>
      <groupId>com.alipay.sofa</groupId>
      <artifactId>sofa-ark-maven-plugin</artifactId>
      <executions>
          <execution>
              <id>default-cli</id>
              <goals>
                  <goal>repackage</goal>
              </goals>
          </execution>
      </executions>
      <configuration>
          <excludeGroupIds>io.netty,org.apache.commons,......</excludeGroupIds>
          <excludeArtifactIds>validation-api,fastjson,hessian,slf4j-api,junit,velocity,......</excludeArtifactIds>
          <outputDirectory>../../target</outputDirectory>
          <bizName>mymodule</bizName>
          <finalName>mymodule-${project.version}-${timestamp}</finalName>
          <bizVersion>${project.version}-${timestamp}</bizVersion>
          <webContextPath>/mymodule</webContextPath>
      </configuration>
  </plugin>
```

<br/>
<br/>
