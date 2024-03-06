<div align="center">
[English](./README.md) | 简体中文
</div>

# SDK Low-cost Upgrade
In the module and base pattern, we move the framework and some middleware SDKs down to the base. Then the base can take over the work of upgrading these SDKs, so the module won't need to worry about SDK upgrades and can focus solely on their business logic.

# Experiment Content

## Experimental Application

### Base
Base is a regular Spring Boot application transformed into a base. The transformation involves adding the following dependencies in the pom file:

```xml
<properties>
    <spring-boot.version>2.6.15</spring-boot.version>
</properties>
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-parent</artifactId>
            <version>${spring-boot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
<!-- Add dynamic module-related dependencies here -->
<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-base-starter</artifactId>
    <version>${koupleless.runtime.version}</version>
    <type>pom</type>
</dependency>
<!-- Add dependencies for deploying multiple web applications in a single Tomcat host mode here -->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>web-ark-plugin</artifactId>
</dependency>
```

### Biz
Biz includes two modules, `biz1` and `biz2`, both of which are regular Spring Boot applications. They are modified to package in the SOFAArk biz module packaging method and packaged into an ark biz jar. The plugin configuration for packaging is as follows:

```xml
<!-- Change the packaging plugin to SOFAArk biz packaging plugin to package into an ark biz jar -->
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
        <!-- Single host requires changing the web context path -->
        <webContextPath>${bizName}</webContextPath>
        <declaredMode>true</declaredMode>
    </configuration>
</plugin>
```

Note that the web context paths for different biz modules need to be changed for successful install in a single Tomcat host.

## Experimental Task, The Spring Boot Version of Base is 2.6.15

### Execute `mvn clean package -DskipTests`
You can see the packed ark-biz jar packages in the target directory of each bundle.

### Start the base application and ensure successful launch

### Execute curl commands to install `biz1` and `biz2`

```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz1",
    "bizVersion": "0.0.1-SNAPSHOT",
    "bizUrl": "file:///path/to/biz1-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

```shell
curl --location --request POST 'localhost:1238/installBiz' \
--header 'Content-Type: application/json' \
--data '{
    "bizName": "biz2",
    "bizVersion": "0.0.1-SNAPSHOT",
    "bizUrl": "file:///path/to/biz2-0.0.1-SNAPSHOT-ark-biz.jar"
}'
```

### Send requests to verify

```shell
curl http://localhost:8080/biz1/
```

Should return `hello to /biz1 deploy`

```shell
curl http://localhost:8080/biz2/
```

Should return `hello to /biz2 deploy`

## Experimental Task, The Spring Boot Version of Base is 2.7.18

Change the Spring Boot version of the base to 2.7.18 and then perform the above tasks to verify whether `biz1` and `biz2` can be installed and operated normally.

## Experimental Results

With the base Spring Boot version at 2.7.18, `biz1` and `biz2` can be installed and operated normally. This shows that after upgrading the base, the module does not need to make any changes to operate well. This way, module developers don't have to worry about SDK upgrades.

Note: If there are some API changes after the Spring Boot version upgrade, the module may need to make changes. Such cases are rare. In most cases, modules don't need any modifications.

## Precautions

This mainly uses simple applications for verification. Complex applications should pay attention to slim down the modules where the base has dependencies the module should mainly set as provided, trying to use the dependencies of the base as much as possible.
