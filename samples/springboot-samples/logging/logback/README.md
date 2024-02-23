<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

# logback logging with independent configuration for base and module
check principle [here](https://github.com/koupleless/koupleless/blob/master/docs/content/zh-cn/docs/contribution-guidelines/runtime/logj42.md)

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
   <!--    <version>${koupleless.runtime.version}</version>-->
    <version>1.0.0</version>
   <type>pom</type>
</dependency>
        <!-- end of dynamic module related dependencies -->

        <!-- Add dependencies for deploying multiple web applications in tomcat single host mode here -->
<dependency>
<groupId>com.alipay.sofa</groupId>
<artifactId>web-ark-plugin</artifactId>
</dependency>
        <!-- end of dependencies for single host deployment -->

<!-- spring boot 相关依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <!-- spring boot 默认日志实现框架 logback -->
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-logging</artifactId>
</dependency>
```
Attention⚠️: base and module use independent log configuration feature, sofa-ark-common version should be no less than 2.2.6

Log config for base defined in `logback-spring.xml`, which will output log to console with custom pattern, and add `${appname} 000` to log prefix, and define appender to output log to base name directory `${logging.file.path}/${appname}/app-default.log`

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<configuration>
    <springProperty scope="context" name="appname" source="spring.application.name"/>
    <springProperty scope="context" name="logging.file.path"  source="logging.file.path"/>
    <property name="level" value="${logLevel:-info}"/>
    <property name="the3rdLevel" value="${the3rdLevel:-WARN}"/>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${appname} 000 %date %5level %6relative --- [%15thread] [%-40logger{40}] [%C:%L] : [%X{traceId:-0}] %msg%n</pattern>
        </encoder>
    </appender>
    <appender name="APP-APPENDER" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <append>true</append>
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>${level}</level>
        </filter>
        <file>${logging.file.path}/${appname}/app-default.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <FileNamePattern>${logging.file.path}/${appname}/app-default.log.%d{yyyy-MM-dd}</FileNamePattern>
            <MaxHistory>30</MaxHistory>
        </rollingPolicy>
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>
    <logger name="org.hibernate" level="${the3rdLevel}"/>
    <logger name="org.springframework" level="${the3rdLevel}"/>
    <logger name="com.alibaba" level="${the3rdLevel}"/>
    <logger name="org.apache" level="${the3rdLevel}"/>
    <root level="${level}">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="APP-APPENDER"/>
    </root>
</configuration>
```
Attention⚠️: base and module use independent log configuration feature, which depends on logback native context selector feature, so we need to specify contextSelector in jvm startup parameters or system properties

1. add jvm start parameter `-Dlogback.ContextSelector=com.alipay.sofa.ark.common.adapter.ArkLogbackContextSelector`
2. add system properties, need to ensure set before first time get logger

```java
@ImportResource({ "classpath*:META-INF/spring/service.xml"})
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class})
public class BaseApplication {

	static {
		ArkConfigs.setEmbedEnable(true);
		// 建议加到jvm 参数中
		// 需要保证在 slf4j static bind 之前，（如，首次 getLogger、类加载 SpringApplication 之前）
		System.setProperty(ClassicConstants.LOGBACK_CONTEXT_SELECTOR,
				"com.alipay.sofa.ark.common.adapter.ArkLogbackContextSelector");
	}

	private static Logger LOGGER = LoggerFactory.getLogger(BaseApplication.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(
				BaseApplication.class, args);
	}
}
```

### biz
The biz contains module biz1, which is regular SpringBoot. The packaging plugin method is modified to the sofaArk biz module packaging method, packaged as an ark biz jar package, and the packaging plugin configuration is as follows:
```xml
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
        <bizName>biz1-logback</bizName>
        <webContextPath>biz1</webContextPath>
        <declaredMode>true</declaredMode>
    </configuration>
</plugin>
```
Note that the web context path of different biz is changed to different values, so that multiple web applications can be successfully installed in a tomcat host.

log config for module defined in `logback-spring.xml`, which will output log to console with custom pattern, and add `${appname} 111` to log prefix, and define appender to output log to module name directory `${logging.file.path}/${appname}/app-default.log`

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<configuration>
    <springProperty scope="context" name="appname" source="spring.application.name"/>
    <springProperty scope="context" name="logging.file.path"  source="logging.file.path"/>
    <property name="level" value="${logLevel:-info}"/>
    <!--    <property name="appid" value="${appname}"/>-->
    <property name="the3rdLevel" value="${the3rdLevel:-WARN}"/>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>${appname} 111 %date %5level %6relative --- [%15thread] [%-40logger{40}] [%C:%L] : [%X{traceId:-0}] %msg%n</pattern>
        </encoder>
    </appender>
    <appender name="APP-APPENDER" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <append>true</append>
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>${level}</level>
        </filter>
        <file>${logging.file.path}/${appname}/app-default.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <FileNamePattern>${logging.file.path}/${appname}/app-default.log.%d{yyyy-MM-dd}</FileNamePattern>
            <MaxHistory>30</MaxHistory>
        </rollingPolicy>
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n</pattern>
            <charset>UTF-8</charset>
        </encoder>
    </appender>
    <logger name="org.hibernate" level="${the3rdLevel}"/>
    <logger name="org.springframework" level="${the3rdLevel}"/>
    <logger name="com.alibaba" level="${the3rdLevel}"/>
    <logger name="org.apache" level="${the3rdLevel}"/>
    <root level="${level}">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="APP-APPENDER"/>
    </root>
</configuration>
```

## Experiment Task
### run `mvn clean package -DskipTests`
we can check the ark-biz jar package in target directory of each bundle
### start base application, and make sure base start successfully
### execute curl command to install biz1
```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1-logback",
    "bizVersion": "0.0.1-SNAPSHOT",
    // local path should start with file://, alse support remote url which can be downloaded
    "bizUrl": "file:///xxxx/samples/springboot-samples/logging/logback/biz1/target/biz1-logback-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

### verification
1. check log of base, we can see "base 000", which is satisfied with our log pattern, and log file of base is in `./logging/logback/logs/` directory
   ![img.png](img.png)
2. check log of module, we can see "biz1 111", which is satisfied with our log pattern, and log file of module is in `./logging/logback/logs/${appName}` directory
   ![img_1.png](img_1.png)
3. start verification request

```shell
curl http://localhost:8080/biz1/
```
return `hello to /biz1 deploy`, and check console log, which is satisfied with our log pattern

```log
biz1 111 2023-12-27 20:05:55,543  INFO  25790 --- [http-nio-8080-exec-1] [c.a.sofa.web.biz1.rest.SampleController ] [com.alipay.sofa.web.biz1.rest.SampleController:21] : [0] /biz1 web test: into sample controller
```

## Precautions
Here mainly use simple applications for verification, if complex applications, need to pay attention to the module to do a good job of slimming, the base has dependencies, the module as much as possible set to provided, as much as possible to use the base dependencies.
