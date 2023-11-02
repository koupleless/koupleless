
# 实验内容
基于 SOFABoot 完成模块动态安装，动态更新。
并实现基座调用模块的功能：
基座定义接口 `com.alipay.sofa.dynamicstock.base.facade.StrategyService`
模块实现接口 `com.alipay.sofa.dynamicstock.biz1.impl.StrategyServiceImpl`

修改模块接口的实现，可以看到不同的效果。

## 实验应用
### base
base 为普通 SOFABoot 改造成的基座，改造内容为在 pom 里增加如下依赖
```xml


<!-- 引入 基于 SOFAServerless 动态模块基础依赖 -->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>runtime-sofa-boot-plugin</artifactId>
</dependency>
<dependency>
    <groupId>com.alipay.sofa.serverless</groupId>
    <artifactId>sofa-serverless-base-starter</artifactId>
</dependency>
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>web-ark-plugin</artifactId>
</dependency>
```

### biz
biz 也是普通 SOFABoot，修改打包插件方式为 sofaArk biz 模块打包方式，打包为 ark biz jar 包，打包插件配置如下：
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
    <version>2.2.4-SNAPSHOT</version>
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


## 实验步骤
### 1. 构建
执行 `mvn clean package -DskipTests`，该步骤会在模块目录 biz1/target/ 下将模块打包出 ark biz 的 jar 包

### 启动基座 
启动成功之后日志信息如下：
![](https://camo.githubusercontent.com/c3ebf0911a214dc2e675bea9fa9a626e479585f7eb75cf1ceb365e2621526e35/68747470733a2f2f67772e616c697061796f626a656374732e636f6d2f6d646e2f726d735f3536356261662f616674732f696d672f412a334e5f6e533650323233494141414141414141414141426b4152516e4151)

### 部署模块（版本1）
```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///Users/path/to/sofa-serverless/samples/sofaboot-samples/dynamic-stock/biz1/target/biz1-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```
查看部署状态
```shell
curl --location --request POST 'localhost:1238/queryAllBiz'
```

返回如下信息表示部署成功
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
此时访问 http://localhost:8080/ ，查看页面信息
![](https://camo.githubusercontent.com/dcf5adbe9a2a5967801d20347d484d113ffad426866f6894cb60a64d5dd44ff2/68747470733a2f2f67772e616c697061796f626a656374732e636f6d2f6d646e2f726d735f6336396531662f616674732f696d672f412a48704b755237576e3434554141414141414141414141426b4152516e4151)

### 部署模块（版本2）
现在我们要开发一个新版本模块，这个新版本模块会按照销量高低返回商品列表。

首先，修改 `com.alipay.sofa.dynamicstock.biz1.impl.StrategyServiceImpl`，解注释掉2行代码即可，实现类如下：
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
打包构建出新的模块jar包

#### 始终只有一个版本（先卸载老版本，再安装新版本）


1. 卸载老模块
```shell
curl --location --request POST 'localhost:1238/uninstallBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1",
    "bizVersion": "0.0.1-SNAPSHOT"
}'
```

2. 安装新模块
```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///Users/path/to/sofa-serverless/samples/sofaboot-samples/dynamic-stock/biz1/target/biz1-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```
3. 查看部署状态
```shell
curl --location --request POST 'localhost:1238/queryAllBiz'
```

4. 返回如下信息表示部署成功
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
此时访问 http://localhost:8080/ ，查看页面信息
![](https://camo.githubusercontent.com/afc9437351c0c467ebe203db4954629fa149ba8be28b15867386aeaf2260c594/68747470733a2f2f67772e616c697061796f626a656374732e636f6d2f6d646e2f726d735f6336396531662f616674732f696d672f412a7671454a513437373575344141414141414141414141426b4152516e4151)


5. 对比两个版本，可以看到排序已经不同

#### 多版本共存的部署方式，可用于 AB 测试，快速灰度验证等

1. 然后修改biz1 的版本号
```xml
<groupId>com.alipay.sofa.dynamicstock</groupId>
<artifactId>biz1</artifactId>
<version>0.0.2-SNAPSHOT</version>
```

2. 由于本Demo引入web-ark-plugin，所以每个模块会复用同一个tomcat实例，详看这里[原理介绍](https://www.sofastack.tech/projects/sofa-boot/sofa-ark-multi-web-component-deploy/)，所以需要更改server的webContextPath，搜索并修改 biz1 的 pom 里 Ark 模块打包插件里的 web context path 为任意不同的值，由原来的
```xml
<webContextPath>provider</webContextPath>
```
修改为
```xml
<webContextPath>provider-1</webContextPath>
```
3. 执行 mvn 命令构建出新的模块 biz 包
```mvn clean package -DskipTests```

4. 在不卸载原来版本的基础上，直接部署新版本
```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1",
    "bizVersion": "0.0.2-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///Users/path/to/sofa-serverless/samples/sofaboot-samples/dynamic-stock/biz1/target/biz1-0.0.2-SNAPSHOT-ark-biz.jar"
}'
```
5. 查看部署状态
```shell
curl --location --request POST 'localhost:1238/queryAllBiz'
```

6. 返回如下信息表示部署成功，可以看到 biz1 存在两个版本
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
7. 但是由于当前 SOFAServerless 未提供切换版本的能力，当前提供服务的还是老版本，此时访问 http://localhost:8080/ ，查看页面信息，可以看到排序还是版本1的顺序
![](https://camo.githubusercontent.com/dcf5adbe9a2a5967801d20347d484d113ffad426866f6894cb60a64d5dd44ff2/68747470733a2f2f67772e616c697061796f626a656374732e636f6d2f6d646e2f726d735f6336396531662f616674732f696d672f412a48704b755237576e3434554141414141414141414141426b4152516e4151)
8. 切换服务到最新版本2上
```shell
curl --location --request POST '127.0.0.1:1238/switchBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName":"biz1",
    "bizVersion":"0.0.2-SNAPSHOT"
}'
```
返回 response
```json
{
    "code": "SUCCESS",
    "data": {
        "code": "SUCCESS",
        "message": "Switch biz: biz1:0.0.2-SNAPSHOT is activated."
    }
}
```
9. 查看此时模块的状态信息
发起查看状态请求
```shell
curl --location --request POST 'localhost:1238/queryAllBiz'
```
返回信息，可以看到此时模块版本2的状态已经变成了 ACTIVATED
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
10. 发起流量调用，可以看到此时返回的书籍顺序已经更改成模块版本2 的顺序了
![](https://camo.githubusercontent.com/afc9437351c0c467ebe203db4954629fa149ba8be28b15867386aeaf2260c594/68747470733a2f2f67772e616c697061796f626a656374732e636f6d2f6d646e2f726d735f6336396531662f616674732f696d672f412a7671454a513437373575344141414141414141414141426b4152516e4151)

## 注意事项
1. 这里主要使用简单应用做验证，如果复杂应用，需要注意模块做好瘦身，基座有的依赖，模块尽可能设置成 provided，尽可能使用基座的依赖。
2. 如果你对 web context path 的使用方式有疑问可以查看[这里](https://github.com/sofastack/sofa-serverless/tree/master/samples/springboot-samples/web/tomcat)
