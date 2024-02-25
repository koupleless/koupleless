<div align="center">

[English](./README.md) | 简体中文

</div>

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
            <groupId>com.alipay.sofa.koupleless</groupId>
            <artifactId>koupleless-spring-loader-tool</artifactId>
            <!--最新版本1.0.0-->
            <version>${koupleless.runtime.version}</version>
        </dependency>
    </dependencies>
</plugin>
```
2. fat jar启动方式不变，会默认使用koupleless-spring-loader的JarLauncher启动
```shell
java -jar xxx-executable.jar
```
3. 解压启动方式，Launcher需要改成com.alipay.sofa.koupleless.spring.loader.JarLauncher
```shell
java -classpath  xxx-executable-unpack com.alipay.sofa.koupleless.spring.loader.JarLauncher
```

# 维护说明

如果改了 koupleless-spring-loader 代码，需要先手动mvn打包，然后将 koupleless-spring-loader/target/koupleless-spring-loader-xxx.jar复制到 koupleless-spring-loader-tool/src/main/resources/META-INF/loader

