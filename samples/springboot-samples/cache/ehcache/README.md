
# 实验内容： 基座、模块使用 Redis

## 实验原理
原理[请看这里](https://sofaserverless.gitee.io/docs/contribution-guidelines/runtime/ehcache/) 

## 实验应用
### base
base 为普通 springboot 改造成的基座，改造内容为在 pom 里增加如下依赖
```xml

<!-- 这里添加动态模块相关依赖 -->
<!--    务必将次依赖放在构建 pom 的第一个依赖引入, 并且设置 type= pom, 
    原理请参考这里 https://sofaserverless.gitee.io/docs/contribution-guidelines/runtime/multi-app-padater/ -->
<dependency>
    <groupId>com.alipay.sofa.serverless</groupId>
    <artifactId>sofa-serverless-base-starter</artifactId>
    <version>${sofa.serverless.runtime.version}</version>
    <type>pom</type>
</dependency>
<!-- end 动态模块相关依赖 -->

<!-- 这里添加 tomcat 单 host 模式部署多web应用的依赖 -->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>web-ark-plugin</artifactId>
</dependency>
<!-- end 单 host 部署的依赖 -->

<!-- 引入 ehcache 依赖 -->
<dependency>
    <groupId>net.sf.ehcache</groupId>
    <artifactId>ehcache</artifactId>
</dependency>

```

### biz1
biz1 是普通 springboot 应用，修改打包插件方式为 sofaArk biz 模块打包方式，打包为 ark biz jar 包，打包插件配置如下：
```xml
<dependency>
    <groupId>net.sf.ehcache</groupId>
    <artifactId>ehcache</artifactId>
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
        <packExcludesConfig>rules.txt</packExcludesConfig>
    </configuration>
</plugin>
```
注意这里通过引入 rules.txt 来完成模块自动瘦身，其中包括 ehcache 的依赖也会自动委托给基座加载。另外也需将不同 biz 的web context path 修改成不同的值，以此才能成功在一个 tomcat host 里安装多个 web 应用。

### biz2
同 biz1


## 实验任务
1. 执行 mvn clean package -DskipTests
2. 启动基座应用 base，确保基座启动成功
3. 执行 curl 命令安装 biz1 和 biz2, 也可以使用 arkctl 安装
```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///path/to/springboot-samples/web/tomcat/biz1/target/biz1-ehcache-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz2",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///path/to/springboot-samples/samples/web/tomcat/biz2/target/biz2-ehcache-0.0.1-SNAPSHOT-ark-biz.jar"
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

4. 发起基座请求验证基座 ehcache 能力是否正常 
```shell
curl -X POST http://localhost:8080/getUserById?id=111
```

返回 `user_base-ehcache_111`, 并在第一次发起请求是能看到日志 
```text
add user into cache: {"id": 111, "value": user_base-ehcache_111}
```
后续请求返回 `user_base-ehcache_111`, 但看不到日志，因为已经直接从 ehcache 获取值。

5. 发起基座请求验证基座 ehcache 能力是否正常
```shell
curl -X POST http://localhost:8080/biz1/getUserById?id=111
```

返回 `user_biz1-ehcache_111`, 并在第一次发起请求是能看到日志
```text
add user into cache: {"id": 111, "value": user_biz1-ehcache_111}
```
后续请求返回 `user_biz1-ehcache_111`, 但看不到日志，因为已经直接从 ehcache 获取值。


6. 发起基座请求验证基座 ehcache 能力是否正常
```shell
curl -X POST http://localhost:8080/biz2/getUserById?id=111
```

返回 `user_biz2-ehcache_111`, 并在第一次发起请求是能看到日志
```text
add user into cache: {"id": 111, "value": user_biz2-ehcache_111}
```
后续请求返回 `user_biz2-ehcache_111`, 但看不到日志，因为已经直接从 ehcache 获取值。
