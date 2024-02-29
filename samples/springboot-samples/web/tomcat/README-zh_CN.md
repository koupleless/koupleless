<div align="center">

[English](./README.md) | 简体中文

</div>

# tomcat 单 host 模式，动态 / 静态部署多个 web 应用

tomcat 单host
模式原理介绍详看[这里](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-multi-web-component-deploy/)

# 实验应用

## base

base 为普通 springboot 改造成的基座，改造内容为在 pom 里增加如下依赖

```xml


<!-- 这里添加动态模块相关依赖 -->
<!--    务必将次依赖放在构建 pom 的第一个依赖引入, 并且设置 type= pom, 
    原理请参考这里 https://koupleless.gitee.io/docs/contribution-guidelines/runtime/multi-app-padater/ -->
<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-base-starter</artifactId>
    <version>${koupleless.runtime.version}</version>
    <type>pom</type>
</dependency>
        <!-- end 动态模块相关依赖 -->

        <!-- 这里添加 tomcat 单 host 模式部署多web应用的依赖 -->
<dependency>
<groupId>com.alipay.sofa</groupId>
<artifactId>web-ark-plugin</artifactId>
</dependency>
        <!-- end 单 host 部署的依赖 -->
```

## biz

biz 包含两个模块，分别为 biz1 和 biz2, 都是普通 springboot，修改打包插件方式为 sofaArk biz 模块打包方式，打包为 ark biz jar
包，打包插件配置如下：

```xml
<!-- 修改打包插件为 sofa-ark biz 打包插件，打包成 ark biz jar -->
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
    <configuration>
        <skipArkExecutable>true</skipArkExecutable>
        <outputDirectory>./target</outputDirectory>
        <bizName>${bizName}</bizName>
        <!-- 单host下需更换 web context path -->
        <webContextPath>${bizName}</webContextPath>
        <declaredMode>true</declaredMode>
    </configuration>
</plugin>
```

注意这里将不同 biz 的web context path 修改成不同的值，以此才能成功在一个 tomcat host 里安装多个 web 应用。

## forward

非k8s部署的情况下，尤其是在针对存量应用改造的时候，各业务模块，可能没有预留contextPath。

此时，请求打到基座应用，需要进行forward。

**暂时需要自行安装master分支的最新sofa-ark，预计在 sofa-ark 2.2.8 版本可用。**

文件中，配置的是forward规则的list，单条forward规则数据结构如下：

| 字段名                | 字段类型   | 可否为空 | 说明                     |
|--------------------|--------|------|------------------------|
| contextPath        | string | 否    | biz module的contextPath |
| hosts              | array  | 可    | 域名前缀，为空表示不限制域名         |
| paths              | array  | 可    | 路径前缀，为空表示不限制路径         |
| &nbsp;└─&nbsp;from | string | 否    | 原请求路径前缀                |
| &nbsp;└─&nbsp;to   | string | 否    | 目标路径前缀                 |

properties示例如下：

```properties
# host in [a.xxx,b.xxx,c.xxx] path /abc --forward to--> biz1/abc
koupleless.web.gateway.forwards[0].contextPath=biz1
koupleless.web.gateway.forwards[0].hosts[0]=a
koupleless.web.gateway.forwards[0].hosts[1]=b
koupleless.web.gateway.forwards[0].hosts[2]=c
# /idx2/** -> /biz2/**, /t2/** -> /biz2/timestamp/**
koupleless.web.gateway.forwards[1].contextPath=biz2
koupleless.web.gateway.forwards[1].paths[0].from=/idx2
koupleless.web.gateway.forwards[1].paths[0].to=/
koupleless.web.gateway.forwards[1].paths[1].from=/t2
koupleless.web.gateway.forwards[1].paths[1].to=/timestamp
# /idx1/** -> /biz1/**, /t1/** -> /biz1/timestamp/**
koupleless.web.gateway.forwards[2].contextPath=biz1
koupleless.web.gateway.forwards[2].paths[0].from=/idx1
koupleless.web.gateway.forwards[2].paths[0].to=/
koupleless.web.gateway.forwards[2].paths[1].from=/t1
koupleless.web.gateway.forwards[2].paths[1].to=/timestamp
```

yaml示例如下：

```yaml
#a.xxx b.xxx c.xxx域名的请求，路由到biz1
koupleless:
  web:
    gateway:
      forwards:
        - contextPath: biz1
          hosts:
            - a
            - b
            - c
        # /idx2/** -> /biz2/**, /t2/** -> /biz2/timestamp/**
        - contextPath: biz2
          paths:
            - from: /idx2
              to: /
            - from: /t2
              to: /timestamp
        # /idx1/** -> /biz1/**, /t1/** -> /biz1/timestamp/**
        - contextPath: biz1
          paths:
            - from: /idx1
              to: /
            - from: /t1
              to: /timestamp
        #a.xxx b.xxx c.xxx域名的请求，/idx2/** -> /biz2/**, /t2/** -> /biz2/timestamp/**    
        - contextPath: biz2
          hosts:
            - a
            - b
            - c
          paths:
            - from: /idx2
              to: /
            - from: /t2
              to: /timestamp
```

说明事项：

1. 限制域名的规则，优先级高于未限制域名的规则
2. 路径越长，优先级越高
3. 路径长度相同，且都限制域名时，域名限制越长，优先级越高
4. 域名以 `.` 为分割点，路径以 `/` 为分割点

# 实验内容1: 动态部署

## 实验任务

1. 执行 mvn clean package -DskipTests
2. 启动基座应用 base，确保基座启动成功
3. 执行 curl 命令安装 biz1 和 biz2

```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///path/to/springboot-samples/samples/web/tomcat/biz1/target/biz1-web-single-host-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz2",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///path/to/springboot-samples/samples/web/tomcat/biz2/target/biz2-web-single-host-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

如果想验证热部署，可以通过多次卸载多次部署，然后验证请求是否正常

```shell
curl --location --request POST 'localhost:1238/uninstallBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1-web-single-host",
    "bizVersion": "0.0.1-SNAPSHOT"
}'
```

4. 发起请求验证

```shell
curl http://localhost:8080/biz1/
```

返回 `hello to /biz1 deploy`

```shell
curl http://localhost:8080/biz2/
```

返回 `hello to /biz2 deploy`

说明，单host模式应用多次热部署正常。

5. 也可以验证基座调用模块能力

```shell
curl http://localhost:8080/order1
```

返回模块1里定义的书籍顺序
![](https://camo.githubusercontent.com/dcf5adbe9a2a5967801d20347d484d113ffad426866f6894cb60a64d5dd44ff2/68747470733a2f2f67772e616c697061796f626a656374732e636f6d2f6d646e2f726d735f6336396531662f616674732f696d672f412a48704b755237576e3434554141414141414141414141426b4152516e4151)

```shell
curl http://localhost:8080/order2
```

返回模块2里定义的书籍顺序
![](https://camo.githubusercontent.com/afc9437351c0c467ebe203db4954629fa149ba8be28b15867386aeaf2260c594/68747470733a2f2f67772e616c697061796f626a656374732e636f6d2f6d646e2f726d735f6336396531662f616674732f696d672f412a7671454a513437373575344141414141414141414141426b4152516e4151)

## 注意事项

这里主要使用简单应用做验证，如果复杂应用，需要注意模块做好瘦身，基座有的依赖，模块尽可能设置成 provided，尽可能使用基座的依赖。

# 实验内容2: 静态合并部署

## 实验任务

1. cd 到 static-deploy-demo 目录下。
2. 执行 run_static_deploy_on_unix_like.sh 脚本, 该脚本会做如下几件事情：
    1. 构建 web/tomcat 项目。
    2. 把 biz1 和 biz2 的构建产物移动到 ./biz 目录下。
    3. 在基座启动时扫描该上述目录，完成静态合并部署。
3. 观测日志，进行验证。

观测到如下关键日志，代表静态合并部署开始了：

```
2023-xx-xx xx:xx:xx.xxx  INFO 39753 --- [           main] arklet : start to batch deploy from local dir:./biz
2023-xx-xx xx:xx:xx.xxx  INFO 39753 --- [           main] arklet : Found biz jar file: ~/koupleless/samples/springboot-samples/web/tomcat/static-deploy-demo/./biz/biz1-web-single-host-0.0.1-SNAPSHOT-ark-biz.jar
2023-xx-xx xx:xx:xx.xxx  INFO 39753 --- [           main] arklet : Found biz jar file: ~/koupleless/samples/springboot-samples/web/tomcat/static-deploy-demo/./biz/biz2-web-single-host-0.0.1-SNAPSHOT-ark-biz.jar
```

观测到如下关键日志，代表静态合并部署成功了：

```
2023-xx-xx xx:xx:xx.xxx  INFO 39753 --- [           main] arklet : ~/koupleless/samples/springboot-samples/web/tomcat/static-deploy-demo/./biz/biz1-web-single-host-0.0.1-SNAPSHOT-ark-biz.jar, SUCCESS, Install Biz: biz1:0.0.1-SNAPSHOT success, cost: 4756 ms, started at: xx:xx:xx,xxx, BatchDeployResult
2023-xx-xx xx:xx:xx.xxx  INFO 39753 --- [           main] arklet : ~/koupleless/samples/springboot-samples/web/tomcat/static-deploy-demo/./biz/biz2-web-single-host-0.0.1-SNAPSHOT-ark-biz.jar, SUCCESS, Install Biz: biz2:0.0.1-SNAPSHOT success, cost: 4756 ms, started at: xx:xx:xx,xxx, BatchDeployResult
```

可以通过执行如下 curl 验证是否部署成功:

```shell
curl http://localhost:8080/biz1/
curl http://localhost:8080/biz2/
```

# 实验内容3：内部转发

部署成功之后，就可以开始验证内部转发了。

```shell
curl localhost:8080/idx1

hello to /biz1 deploy
```

```shell
curl localhost:8080/idx2

hello to /biz2 deploy
```

```shell
curl localhost:8080/t1

/biz1 now is $now
```

```shell
curl localhost:8080/t2

/biz2 now is $now
```