<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

# Experiment Content
## Experiment Application
### base
The base is built from regular SpringBoot application. The only change you need to do is to add the following dependencies in pom
```xml


<!-- Add dynamic module related dependencies here -->
<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-base-starter</artifactId>
    <!-- The above version supports springboot3 -->
    <version>${koupleless.runtime.version}</version>
</dependency>

<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>web-ark-plugin</artifactId>
    <!-- Exclude log-sofa-boot-starter in web-ark-plugin -->
    <exclusions>
        <exclusion>
            <groupId>com.alipay.sofa</groupId>
            <artifactId>log-sofa-boot-starter</artifactId>
        </exclusion>
    </exclusions>
</dependency>

        <!-- log4j2 related dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>

        <!-- log4j2 asynchronous queue -->
<dependency>
    <groupId>com.lmax</groupId>
    <artifactId>disruptor</artifactId>
    <version>${disruptor.version}</version>
</dependency>
<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-log4j2-starter</artifactId>
    <version>${koupleless.runtime.version}</version>
</dependency>
        <!-- end of log4j2 dependency introduction -->

        <!--Database dependency-->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.31</version>
    <scope>runtime</scope>
</dependency>
        <!--mybatis dependency-->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.3.1</version>
</dependency>
        <!--druid dependency-->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>1.2.9</version>
</dependency>
```

### biz
The biz contains module biz1, which are regular SpringBoot. The packaging plugin method is modified to the sofaArk biz module packaging method, packaged as an ark biz jar package, and the packaging plugin configuration is as follows:
```xml
<!-- The module needs to introduce a special log4j2 adapter -->
<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-adapter-log4j2</artifactId>
    <version>${koupleless.runtime.version}</version>
    <scope>provided</scope>
</dependency>
        <!--Database dependency-->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.31</version>
    <scope>provided</scope>
</dependency>
        <!--mybatis dependency-->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.3.1</version>
    <scope>provided</scope>
</dependency>
        <!--druid dependency-->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>1.2.9</version>
    <scope>provided</scope>
</dependency>

        <!-- Modify the packaging plugin to the sofa-ark biz packaging plugin, package it into an ark biz jar -->
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
        <!-- Change the web context path under a single host -->
        <webContextPath>${bizName}</webContextPath>
        <declaredMode>true</declaredMode>
    </configuration>
</plugin>
```
Note that the web context path of different biz is changed to different values, so that multiple web applications can be successfully installed

## Experiment Steps

### Deploy mysql locally and start

Please create the library, table, etc. required by the code in advance
#### Start the base application base

1. Please modify the datasource configuration in samples/springboot-samples/db/mybatis/base/src/main/resources/application.properties to ensure that it is linked to the correct local database
2. Start the base application

#### Package the module application biz1

1. Please modify the datasource configuration in samples/springboot-samples/db/mybatis/biz1/src/main/resources/application.properties in advance to ensure that it is linked to the correct local databas
2. Execute mvn clean package -Dmaven.test.skip=true to package the module. After the packaging is completed, you can see the packaged ark-biz jar package in the target directory of each bundle

#### Install the module application biz1
```shell
telnet localhost 1234
biz -i file://${your project directory}/samples/springboot-samples/db/mybatis/biz1/target/biz1-mybatis-0.0.1-SNAPSHOT-ark-biz.jar
```

If you want to verify the uninstallation, you can also execute
```shell
biz -u biz1-mybatis:0.0.1-SNAPSHOT
```

### start a request to verify

#### Verify base mybatis and druid

Both annotation mapper and xml mapper methods are supported

```shell
curl http://localhost:8080/hello/haha
```
Return "hello haha to base-mybatis deploy"

```shell
curl http://localhost:8080/mybatis
```
Return the content of the user table, and you can find that the data source used has become DruidDataSource

#### Verify module mybatis and druid

The module supports various druid configurations
```shell
spring.datasource.type=com.alibaba.druid.pool.DruidDataSource
spring.datasource.druid.initial-size=5
spring.datasource.druid.min-idle=5
spring.datasource.druid.max-active=200
spring.datasource.druid.max-wait=60000
spring.datasource.druid.time-between-eviction-runs-millis=60000
spring.datasource.druid.min-evictable-idle-time-millis=300000
spring.datasource.druid.test-while-idle=true
spring.datasource.druid.test-on-borrow=false
spring.datasource.druid.test-on-return=false
spring.datasource.druid.pool-prepared-statements=false
spring.datasource.druid.filters=stat,wall,slf4j
```

Both annotation mapper and xml mapper methods are supported

```shell
curl http://localhost:8080/biz1mybatis/hi
```
Return "hello to biz1-mybatis deploy"

```shell
curl http://localhost:8080/biz1mybatis/testmybatis
```
Return the content of the student table, and you can find that the data source used has become DruidDataSource

## Precautions
Here mainly use simple applications for verification, if complex applications, you need to pay attention to the module to do a good job of slimming, the base has dependencies, the module as much as possible set to provided, as much as possible to use the base dependencies.
