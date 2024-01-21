# 使用说明

1. 引入打包依赖
```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <version>2.7.15</version>
    <configuration>
        <outputDirectory>../../target/boot</outputDirectory>
        <classifier>executable</classifier>
    </configuration>
    <executions>
        <execution>
            <id>package</id>
            <goals>
                <goal>repackage</goal>
            </goals>
        </execution>
    </executions>
    <dependencies>
        <dependency>
            <groupId>com.alipay.sofa.serverless</groupId>
            <artifactId>sofa-serverless-spring-loader-tool</artifactId>
            <!--最新版本0.5.6-->
            <version>0.5.6</version>
        </dependency>
    </dependencies>
</plugin>
```
2. fat jar启动方式不变，会默认使用sofa-serverless-spring-loader的JarLauncher启动
```shell
java -jar xxx-executable.jar
```
3. 解压启动方式，Launcher需要改成com.alipay.sofa.serverless.spring.loader.JarLauncher
```shell
java -classpath  xxx-executable-unpack com.alipay.sofa.serverless.spring.loader.JarLauncher
```

# 维护说明

如果改了ofa-serverless-spring-loader代码，需要先手动mvn打包，然后将sofa-serverless-spring-loader/target/sofa-serverless-spring-loader-xxx.jar复制到sofa-serverless-spring-loader-tool/src/main/resources/META-INF/loader

