<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>


# Experiment content
Module dynamic installation and dynamic update based on SOFABoot.
and add the ability of base to call module:
base interface: `com.alipay.sofa.dynamicstock.base.facade.StrategyService`
module interfact: `com.alipay.sofa.dynamicstock.biz1.impl.StrategyServiceImpl`

change the implementation of the module interface, you can see different effects.

## Experiment application
### base
Base is built from a normal SOFABoot application, the only thing you need to do is add the following dependencies in the main pom.xml
```xml


<!-- 引入 基于 SOFAServerless 动态模块基础依赖 -->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>runtime-sofa-boot-plugin</artifactId>
</dependency>
<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-base-starter</artifactId>
</dependency>
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>web-ark-plugin</artifactId>
</dependency>
```

### biz
Biz is build from a normal SOFABoot application, you need to change the packaging plugin to sofaArk biz module packaging plugin, the packaging plugin configuration is as follows:
```xml
<!--  引入通信类，这里设置为 provided，委托使用基座的依赖  -->
<dependency>
    <groupId>com.alipay.sofa.dynamicstock</groupId>
    <artifactId>facade</artifactId>
    <scope>provided</scope>
</dependency>

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
Please notice that the web context path of different biz is changed to different values, so that multiple web applications can be successfully installed in a tomcat host.

## Experiment steps
### 1. build
run `mvn clean package -DskipTests`, this step will package the module into ark biz jar in the module directory biz1/target/

### 2. start base
check the log after base started:
![](https://camo.githubusercontent.com/c3ebf0911a214dc2e675bea9fa9a626e479585f7eb75cf1ceb365e2621526e35/68747470733a2f2f67772e616c697061796f626a656374732e636f6d2f6d646e2f726d735f3536356261662f616674732f696d672f412a334e5f6e533650323233494141414141414141414141426b4152516e4151)

### 3. install module(version 1)
```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///Users/path/to/koupleless/samples/sofaboot-samples/dynamic-stock/biz1/target/biz1-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```
check deploy status
```shell
curl --location --request POST 'localhost:1238/queryAllBiz'
```

deploy succeed when return response like this
```json
{
    "code": "SUCCESS",
    "data": [
        {
            "bizName": "biz1",
            "bizState": "ACTIVATED",
            "bizVersion": "0.0.1-SNAPSHOT",
            "mainClass": "com.alipay.sofa.dynamicstock.biz1.Biz1Application",
            "webContextPath": "provider"
        }
    ]
}
```
access http://localhost:8080/ , you can see the page info
![](https://camo.githubusercontent.com/dcf5adbe9a2a5967801d20347d484d113ffad426866f6894cb60a64d5dd44ff2/68747470733a2f2f67772e616c697061796f626a656374732e636f6d2f6d646e2f726d735f6336396531662f616674732f696d672f412a48704b755237576e3434554141414141414141414141426b4152516e4151)

### 4. install module(version 2)
Now we want to develop a new version of the module, this new version of the module will return the product list according to the sales.

First, modify `com.alipay.sofa.dynamicstock.biz1.impl.StrategyServiceImpl`, uncomment the 2 lines of code, the implementation class is as follows:
```java
@Service
@SofaService
public class StrategyServiceImpl implements StrategyService {
    @Override
    public List<ProductInfo> strategy(List<ProductInfo> products) {
        Collections.sort(products, (m, n) -> n.getOrderCount() - m.getOrderCount());
        products.stream().forEach(p -> p.setName(p.getName()+"("+p.getOrderCount()+")"));
        return products;
    }
}
```
build the module in jar file.

we have two kind of deploy: one is uninstall the old version and install the new version, the other is install the new version directly.
#### Only one version at a time (uninstall the old version first, then install the new version)

1. uninstall the old version
```shell
curl --location --request POST 'localhost:1238/uninstallBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1",
    "bizVersion": "0.0.1-SNAPSHOT"
}'
```

2. install the new version
```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///Users/path/to/koupleless/samples/sofaboot-samples/dynamic-stock/biz1/target/biz1-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```
3. check the deploy status
```shell
curl --location --request POST 'localhost:1238/queryAllBiz'
```

4. deploy success when return response like this
```json
{
    "code": "SUCCESS",
    "data": [
        {
            "bizName": "biz1",
            "bizState": "ACTIVATED",
            "bizVersion": "0.0.1-SNAPSHOT",
            "mainClass": "com.alipay.sofa.dynamicstock.biz1.Biz1Application",
            "webContextPath": "provider"
        }
    ]
}
```
Now we access http://localhost:8080/ , you can see the page
![](https://camo.githubusercontent.com/afc9437351c0c467ebe203db4954629fa149ba8be28b15867386aeaf2260c594/68747470733a2f2f67772e616c697061796f626a656374732e636f6d2f6d646e2f726d735f6336396531662f616674732f696d672f412a7671454a513437373575344141414141414141414141426b4152516e4151)


5. compare the two versions, you can see the order is different

#### Multiple versions coexist, can be used for AB testing, fast gray verification, etc.

1. modify the version of biz1
```xml
<groupId>com.alipay.sofa.dynamicstock</groupId>
<artifactId>biz1</artifactId>
<version>0.0.2-SNAPSHOT</version>
```

2. Since this demo introduces web-ark-plugin, each module will reuse the same tomcat instance, see [here](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-multi-web-component-deploy/) for details, so you need to change the webContextPath of the server, search and modify the web context path in the Ark module packaging plugin of the pom of biz1 to any different value, from the original
```xml
<webContextPath>provider</webContextPath>
```
to
```xml
<webContextPath>provider-1</webContextPath>
```
3. run mvn command to build the new module biz package
```mvn clean package -DskipTests```

4. install the new version directly without uninstalling the old version
```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1",
    "bizVersion": "0.0.2-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///Users/path/to/koupleless/samples/sofaboot-samples/dynamic-stock/biz1/target/biz1-0.0.2-SNAPSHOT-ark-biz.jar"
}'
```
5. check the status of deployment
```shell
curl --location --request POST 'localhost:1238/queryAllBiz'
```

6. Success when return response like this, we can see that there are two versions of biz1
```json
{
  "code": "SUCCESS",
  "data": [
    {
      "bizName": "biz1",
      "bizState": "ACTIVATED",
      "bizVersion": "0.0.1-SNAPSHOT",
      "mainClass": "com.alipay.sofa.dynamicstock.biz1.Biz1Application",
      "webContextPath": "provider"
    },
    {
      "bizName": "biz1",
      "bizState": "DEACTIVATED",
      "bizVersion": "0.0.2-SNAPSHOT",
      "mainClass": "com.alipay.sofa.dynamicstock.biz1.Biz1Application",
      "webContextPath": "provider-1"
    }
  ]
}
```
7. But because Koupleless does not provide the ability to switch versions, the old version is still used to provide services at this time. Access http://localhost:8080/ and check the page information. You can see that the order is still the order of version 1
![](https://camo.githubusercontent.com/dcf5adbe9a2a5967801d20347d484d113ffad426866f6894cb60a64d5dd44ff2/68747470733a2f2f67772e616c697061796f626a656374732e636f6d2f6d646e2f726d735f6336396531662f616674732f696d672f412a48704b755237576e3434554141414141414141414141426b4152516e4151)
8. Switch the service to the latest version 2
```shell
curl --location --request POST '127.0.0.1:1238/switchBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName":"biz1",
    "bizVersion":"0.0.2-SNAPSHOT"
}'
```
return response
```json
{
    "code": "SUCCESS",
    "data": {
        "code": "SUCCESS",
        "message": "Switch biz: biz1:0.0.2-SNAPSHOT is activated."
    }
}
```
9. check the status of deployment
```shell
curl --location --request POST 'localhost:1238/queryAllBiz'
```
According the return response, we can see that the status of module version 2 has become ACTIVATED

```json
{
    "code": "SUCCESS",
    "data": [
        {
            "bizName": "biz1",
            "bizState": "DEACTIVATED",
            "bizVersion": "0.0.1-SNAPSHOT",
            "mainClass": "com.alipay.sofa.dynamicstock.biz1.Biz1Application",
            "webContextPath": "provider"
        },
        {
            "bizName": "biz1",
            "bizState": "ACTIVATED",
            "bizVersion": "0.0.2-SNAPSHOT",
            "mainClass": "com.alipay.sofa.dynamicstock.biz1.Biz1Application",
            "webContextPath": "provider-1"
        }
    ]
}
```
10. Access the service, we can see that the order returned at this time has been changed to the order of module version 2
![](https://camo.githubusercontent.com/afc9437351c0c467ebe203db4954629fa149ba8be28b15867386aeaf2260c594/68747470733a2f2f67772e616c697061796f626a656374732e636f6d2f6d646e2f726d735f6336396531662f616674732f696d672f412a7671454a513437373575344141414141414141414141426b4152516e4151)

## Attention
1. Here we use simple applications for verification. If it is a complex application, pay attention to the module to be slim: the dependencies of module should be set to provided as much as possible if it had been imported from the base.
2. If you have any questions about the use of web context path, you can check [here](https://github.com/koupleless/koupleless/tree/master/samples/springboot-samples/web/tomcat)
