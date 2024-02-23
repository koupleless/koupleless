<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

# How to use

1. import the packaging dependency
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
2. The launch method of fat jar remains unchanged, and the Jar Launcher of koupleless-spring-loader will be used by default
```shell
java -jar xxx-executable.jar
```
3. The launch method of unpacking is as follows, and the Launcher needs to be changed to `com.alipay.sofa.koupleless.spring.loader.JarLauncher`
```shell
java -classpath  xxx-executable-unpack com.alipay.sofa.koupleless.spring.loader.JarLauncher
```

# How to maintain

If the code of koupleless-spring-loader changed, you need to manually mvn package first, and then copy the `koupleless-spring-loader/target/koupleless-spring-loader-xxx.jar` to `koupleless-spring-loader-tool/src/main/resources/META-INF/loader`

