<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

# Experiment
1. tomcat with single host mode, for detail principle please refer [here](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-multi-web-component-deploy/)
2. dynamic deploy / static deploy for multi web app

# Experiment application

## base

The base is built from regular SpringBoot application. The only change you need to do is to add the following dependencies in pom

```xml
<!-- Add dynamic module related dependencies here -->
<!--    Be sure to put this dependency as the first dependency in the build pom, and set type= pom,
    The principle can be found here https://koupleless.gitee.io/docs/contribution-guidelines/runtime/multi-app-padater/ -->
<dependency>
   <groupId>com.alipay.sofa.koupleless</groupId>
   <artifactId>koupleless-base-starter</artifactId>
   <version>${koupleless.runtime.version}</version>
   <type>pom</type>
</dependency>
<!-- end of dynamic module related dependencies -->

<!-- Add dependencies for deploying multiple web applications in tomcat single host mode here -->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>web-ark-plugin</artifactId>
</dependency>
<!-- end of dependencies for single host deployment -->
```

## biz
The biz contains two modules, biz1 and biz2, both are regular SpringBoot. The packaging plugin method is modified to the sofaArk biz module packaging method, packaged as an ark biz jar package, and the packaging plugin configuration is as follows:

```xml
<!-- change the packaging plugin to sofa-ark biz packaging plugin, packaged as ark biz jar -->
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
        <!-- single host mode, need to change web context path -->
        <webContextPath>${bizName}</webContextPath>
        <declaredMode>true</declaredMode>
    </configuration>
</plugin>
```
Note that the web context path of different biz is changed to different values, so that multiple web applications can be successfully installed in a tomcat host.


# Experiment1: dynamic deploy multi app in one jvm
## Experiment steps

1. run `mvn clean package -DskipTests`
2. start base application, make sure base started successfully
3. execute curl command to install biz1 and biz2

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

If you want to verify hot deployment, you can uninstall and deploy multiple times

```shell
curl --location --request POST 'localhost:1238/uninstallBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1-web-single-host",
    "bizVersion": "0.0.1-SNAPSHOT"
}'
```

4. start verification request

```shell
curl http://localhost:8080/biz1/
```

return `hello to /biz1 deploy`

```shell
curl http://localhost:8080/biz2/
```

return `hello to /biz2 deploy`

so, hot deployments of single host mode applications are succeed.

5. we can also verify the ability of the base to call the module

```shell
curl http://localhost:8080/order1
```

It will return the book in order which defined by biz1
![](https://camo.githubusercontent.com/dcf5adbe9a2a5967801d20347d484d113ffad426866f6894cb60a64d5dd44ff2/68747470733a2f2f67772e616c697061796f626a656374732e636f6d2f6d646e2f726d735f6336396531662f616674732f696d672f412a48704b755237576e3434554141414141414141414141426b4152516e4151)

```shell
curl http://localhost:8080/order2
```

It will return the book in order which defined by biz2
![](https://camo.githubusercontent.com/afc9437351c0c467ebe203db4954629fa149ba8be28b15867386aeaf2260c594/68747470733a2f2f67772e616c697061796f626a656374732e636f6d2f6d646e2f726d735f6336396531662f616674732f696d672f412a7671454a513437373575344141414141414141414141426b4152516e4151)

## Precautions
Here mainly use simple applications for verification, if complex applications, need to pay attention to the module to do a good job of slimming, the base has dependencies, the module as much as possible set to provided, as much as possible to use the base dependencies.

# Experiment2: static deploy multi app in one jvm

## Experiment steps

1. cd into `static-deploy-demo`
2. run `run_static_deploy_on_unix_like.sh` script, this script will do the following things:
   1. build web/tomcat project
   2. copy artifacts of biz1 and biz2 to ./biz directory
   3. scan the above directory when the base starts, and complete the static merge deployment.
3. chceck the log to verify how the bizs deployed.

if you see the following log, it means that the static merge deployment has started:
```
2023-xx-xx xx:xx:xx.xxx  INFO 39753 --- [           main] arklet : start to batch deploy from local dir:./biz
2023-xx-xx xx:xx:xx.xxx  INFO 39753 --- [           main] arklet : Found biz jar file: ~/koupleless/samples/springboot-samples/web/tomcat/static-deploy-demo/./biz/biz1-web-single-host-0.0.1-SNAPSHOT-ark-biz.jar
2023-xx-xx xx:xx:xx.xxx  INFO 39753 --- [           main] arklet : Found biz jar file: ~/koupleless/samples/springboot-samples/web/tomcat/static-deploy-demo/./biz/biz2-web-single-host-0.0.1-SNAPSHOT-ark-biz.jar
```

if you see the following log, it means that the static merge deployment has succeed:
```
2023-xx-xx xx:xx:xx.xxx  INFO 39753 --- [           main] arklet : ~/koupleless/samples/springboot-samples/web/tomcat/static-deploy-demo/./biz/biz1-web-single-host-0.0.1-SNAPSHOT-ark-biz.jar, SUCCESS, Install Biz: biz1:0.0.1-SNAPSHOT success, cost: 4756 ms, started at: xx:xx:xx,xxx, BatchDeployResult
2023-xx-xx xx:xx:xx.xxx  INFO 39753 --- [           main] arklet : ~/koupleless/samples/springboot-samples/web/tomcat/static-deploy-demo/./biz/biz2-web-single-host-0.0.1-SNAPSHOT-ark-biz.jar, SUCCESS, Install Biz: biz2:0.0.1-SNAPSHOT success, cost: 4756 ms, started at: xx:xx:xx,xxx, BatchDeployResult
```

and also we can verify the status of deployment by executing the following curl:
```shell
curl http://localhost:8080/biz1/
curl http://localhost:8080/biz2/
```
