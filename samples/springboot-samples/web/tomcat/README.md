# tomcat 单 host 模式，动态 / 静态部署多个 web 应用

tomcat 单host
模式原理介绍详看[这里](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-multi-web-component-deploy/)

# 实验应用

## base

base 为普通 springboot 改造成的基座，改造内容为在 pom 里增加如下依赖

```xml


<!-- 这里添加动态模块相关依赖 -->
<dependency>
    <groupId>com.alipay.sofa.serverless</groupId>
    <artifactId>sofa-serverless-base-starter</artifactId>
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

biz 包含两个模块，分别为 biz1 和 biz2, 都是普通 springboot，修改打包插件方式为 sofaArk biz 模块打包方式，打包为 ark biz jar 包，打包插件配置如下：

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

# 实验内容(动态部署)

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
    "bizName": "biz1",
    "bizVersion": "0.0.1-SNAPSHOT"
}'
```

4. 发起请求验证

```shell
curl http://localhost:8080/biz1
```

返回 `hello to /biz1 deploy`

```shell
curl http://localhost:8080/biz2
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

# 实验内容(静态合并部署)

## 实验任务

1. cd 到 static-deploy-samples 目录下。
2. 执行 run_static_deploy_on_unix_like.sh 脚本。
   1. 构建 web/tomcat 项目。
   2. 把 biz1 和 biz2 的构建产物移动到 ./biz 目录下。
   3. 在基座启动时扫描该上述目录，完成静态合并部署。
3. 观测日志，进行验证。

观测到如下关键日志，代表静态合并部署开始了：

```
2023-xx-xx xx:xx:xx.xxx  INFO 39753 --- [           main] arklet : start to batch deploy from local dir:./biz
2023-xx-xx xx:xx:xx.xxx  INFO 39753 --- [           main] arklet : Found biz jar file: ~/sofa-serverless/samples/springboot-samples/web/tomcat/static-deploy-samples/./biz/biz1-web-single-host-0.0.1-SNAPSHOT-ark-biz.jar
2023-xx-xx xx:xx:xx.xxx  INFO 39753 --- [           main] arklet : Found biz jar file: ~/sofa-serverless/samples/springboot-samples/web/tomcat/static-deploy-samples/./biz/biz2-web-single-host-0.0.1-SNAPSHOT-ark-biz.jar
```

观测到如下关键日志，代表静态合并部署成功了：

```
2023-xx-xx xx:xx:xx.xxx  INFO 39753 --- [           main] arklet : ~/sofa-serverless/samples/springboot-samples/web/tomcat/static-deploy-samples/./biz/biz1-web-single-host-0.0.1-SNAPSHOT-ark-biz.jar, SUCCESS, Install Biz: biz1:0.0.1-SNAPSHOT success, cost: 4756 ms, started at: xx:xx:xx,xxx, BatchDeployResult
2023-xx-xx xx:xx:xx.xxx  INFO 39753 --- [           main] arklet : ~/sofa-serverless/samples/springboot-samples/web/tomcat/static-deploy-samples/./biz/biz2-web-single-host-0.0.1-SNAPSHOT-ark-biz.jar, SUCCESS, Install Biz: biz2:0.0.1-SNAPSHOT success, cost: 4756 ms, started at: xx:xx:xx,xxx, BatchDeployResult
```

可以通过执行如下 curl 验证是否部署成功:

```shell
curl http://localhost:8080/biz1
curl http://localhost:8080/biz2
```