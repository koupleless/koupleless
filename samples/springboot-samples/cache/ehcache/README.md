<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

# Experiment: Base and Module use Redis

## Experiment principle
check details [here](https://koupleless.gitee.io/docs/contribution-guidelines/runtime/ehcache/)

## Experiment application
### base
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

<!-- Add ehcache dependencies here -->
<dependency>
    <groupId>net.sf.ehcache</groupId>
    <artifactId>ehcache</artifactId>
</dependency>
```

### biz1
The biz contains module biz1, which are regular SpringBoot. The packaging plugin method is modified to the sofaArk biz module packaging method, packaged as an ark biz jar package, and the packaging plugin configuration is as follows:


```xml
<dependency>
    <groupId>net.sf.ehcache</groupId>
    <artifactId>ehcache</artifactId>
</dependency>

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
        <packExcludesConfig>rules.txt</packExcludesConfig>
    </configuration>
</plugin>
```
Note that here by introducing rules.txt to complete the automatic slimming of the module, including the dependency of ehcache will also be automatically delegated to the base for loading. Also, you need to change the web context path of different biz to different values, so that multiple web applications can be successfully installed in a tomcat host.

### biz2
same as biz1


## Experiment tasks
1. run `mvn clean package -DskipTests`
2. start base application and make sure it starts successfully
3. Execute the curl command to install biz1 and biz2, or use arkctl to install

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

If you want to verify hot deployment, you can uninstall and deploy multiple times, and then verify whether the request is normal

```shell
curl --location --request POST 'localhost:1238/uninstallBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1-ehcache",
    "bizVersion": "0.0.1-SNAPSHOT"
}'
```

4. Start a base request to verify whether the base ehcache capability is normal
```shell
curl -X POST http://localhost:8080/getUserById?id=111
```

Return `user_base-ehcache_111`, and you can see the log when the first request is initiated
```text
add user into cache: {"id": 111, "value": user_base-ehcache_111}
```
Subsequent requests return user_base-ehcache_111, but you can't see the log, because the value is directly obtained from ehcache.

5. Initiate a base request to verify whether the base ehcache capability is normal
```shell
curl -X POST http://localhost:8080/biz1/getUserById?id=111
```

return `user_biz1-ehcache_111`, and you can see the log when the first request is initiated
```text
add user into cache: {"id": 111, "value": user_biz1-ehcache_111}
```
Subsequent requests return user_biz1-ehcache_111, but you can't see the log, because the value is directly obtained from ehcache.

6. Start a base request to verify whether the base ehcache capability is normal
```shell
curl -X POST http://localhost:8080/biz2/getUserById?id=111
```

return `user_biz2-ehcache_111`, and you can see the log when the first request is initiated
```text
add user into cache: {"id": 111, "value": user_biz2-ehcache_111}
```
Subsequent requests return user_biz2-ehcache_111, but you can't see the log, because the value is directly obtained from ehcache.
