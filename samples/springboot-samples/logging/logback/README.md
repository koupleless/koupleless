# 支持基座、模块使用采用独立日志配置打印logback日志
原理详看[这里](https://github.com/sofastack/sofa-serverless/blob/master/docs/content/zh-cn/docs/contribution-guidelines/runtime/logj42.md)

# 实验内容
## 实验应用
### base
base 为普通 springboot 改造成的基座，改造内容为在 pom 里增加如下依赖
```xml
<!-- sofa-serverless 相关依赖 -->
<!--    务必将次依赖放在构建 pom 的第一个依赖引入, 并且设置 type= pom, 
    原理请参考这里 https://sofaserverless.gitee.io/docs/contribution-guidelines/runtime/multi-app-padater/ -->
<dependency>
   <groupId>com.alipay.sofa.serverless</groupId>
   <artifactId>sofa-serverless-base-starter</artifactId>
   <version>${sofa.serverless.runtime.version}</version>
   <type>pom</type>
</dependency>
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>web-ark-plugin</artifactId>
</dependency>
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

注意⚠️：需要基座、模块采用独立日志配置特性，要求，sofa-ark-common 包版本不低于 2.2.6

基座自定义日志配置参考 logback-spring.xml，其中为控制台输出自定义pattern，日志前方添加 ${appname} 000，并且定义appender将日志输出到基座名目录下 ${logging.file.path}/${appname}/app-default.log
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
注意⚠️：基座、模块日志隔离能力，依赖 logback 原生 context selector 特性，需要在jvm启动参数或系统属性中指定 contextSelector

方法一：添加jvm启动参数 -Dlogback.ContextSelector=com.alipay.sofa.ark.common.adapter.ArkLogbackContextSelector

方法二：添加系统属性，需要保证在首次获取 logger 前设置

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
biz 原来是普通 springboot，修改打包插件方式为 sofaArk biz 模块打包方式，打包为 ark biz jar 包，打包插件配置如下：
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
注意这里将不同 biz 的 web context path 修改成不同的值，以此才能成功在一个 tomcat host 里安装多个 web 应用。

模块自定义日志配置见模块项目资源目录中的 logback-spring.xml，其中为控制台输出自定义pattern，日志前方添加 ${appname} 111，并且定义appender将日志输出到模块名目录下 ${logging.file.path}/${appname}/app-default.log

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

## 实验任务
### 执行 mvn clean package -DskipTests
可在各 bundle 的 target 目录里查看到打包生成的 ark-biz jar 包
### 启动基座应用 base，确保基座启动成功
### 执行 curl 命令安装 biz
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

### 验证

1. 先查看基座启动日志，可以见到日志中有"base 000" 字样，满足我们日志配置中的pattern，同时在 logging.file.path=./logging/logback/logs/ 目录下存在基座日志文件
   ![img.png](img.png)
2. 再启动模块后，查看模块启动日志，可以见到日志中有"biz1 111" 字样，满足我们日志配置中的pattern，同时在 logging.file.path=./logging/logback/logs/ 目录下存在模块日志文件
   ![img_1.png](img_1.png)
3. 发起请求验证模块web服务

```shell
curl http://localhost:8080/biz2
```
返回 `hello to /biz1 deploy`，同时查看控制台日志输出，满足我们日志配置中的pattern
```log
biz1 111 2023-12-27 20:05:55,543  INFO  25790 --- [http-nio-8080-exec-1] [c.a.sofa.web.biz1.rest.SampleController ] [com.alipay.sofa.web.biz1.rest.SampleController:21] : [0] /biz1 web test: into sample controller
```

## 注意事项
这里主要使用简单应用做验证，如果复杂应用，需要注意模块做好瘦身，基座有的依赖，模块尽可能设置成 provided，尽可能使用基座的依赖。
