<div align="center">

[English](./README.md) | 简体中文

</div>

# 实验内容：基座、模块使用 mybatis、mysql、druid
## 实验应用
### base
base 为普通 springboot 改造成的基座，改造内容为在 pom 里增加如下依赖
```xml
<!-- 这里添加动态模块相关依赖 -->
<!--    务必将次依赖放在构建 pom 的第一个依赖引入, 并且设置 type= pom, 
    原理请参考这里 https://koupleless.gitee.io/docs/contribution-guidelines/runtime/multi-app-padater/ -->
<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-base-starter</artifactId>
    <version>${koupleless.runtime.version}</version>
    <type>pom</type>
</dependency>
<!-- end 动态模块相关依赖 -->

<!-- 这里添加 tomcat 单 host 模式部署多web应用的依赖 -->
<dependency>
    <groupId>com.alipay.sofa</groupId>
    <artifactId>web-ark-plugin</artifactId>
</dependency>
<!-- end 单 host 部署的依赖 -->

<!-- log4j2 相关依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>

<!-- log4j2 异步队列 -->
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
<!-- end log4j2 依赖引入 -->

        <!--数据库依赖-->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.31</version>
    <scope>runtime</scope>
</dependency>
            <!--mybatis依赖-->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.3.1</version>
</dependency>
            <!--druid依赖-->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>1.2.9</version>
</dependency>
```

### biz
biz 包含两个模块，分别为 biz1 和 biz2, 都是普通 springboot，修改打包插件方式为 sofaArk biz 模块打包方式，打包为 ark biz jar 包，打包插件配置如下：
```xml
<!-- 模块需要引入专门的 log4j2 adapter -->
<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-adapter-log4j2</artifactId>
    <version>${koupleless.runtime.version}</version>
    <scope>provided</scope>
</dependency>
        <!--数据库依赖-->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.31</version>
    <scope>provided</scope>
</dependency>
        <!--mybatis依赖-->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.3.1</version>
    <scope>provided</scope>
</dependency>
        <!--druid依赖-->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>1.2.9</version>
    <scope>provided</scope>
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
    </configuration>
</plugin>
```
注意这里将不同 biz 的web context path 修改成不同的值，以此才能成功在一个 tomcat host 里安装多个 web 应用。


## 基座、模块各自定义数据源

### 本地部署 mysql 并启动

请提前创建代码所需要的库、表等

```shell
# 1. cd 进入 config 目录
cd config
# 2. 给脚本添加执行权限
chmod +x ./init_mysql.sh
# 3. 执行 init_mysql.sh
./init_mysql.sh
```

### 启动基座应用 base

1. 请修改 samples/springboot-samples/db/mybatis/base/src/main/resources/application.properties 中的 datasource 配置，确保链接到正确的本地数据库

2. 启动基座应用

### 打包模块应用 biz1

1. 请提前修改 samples/springboot-samples/db/mybatis/biz1/src/main/resources/application.properties 中的 datasource 配置，确保链接到正确的本地数据库

2. 执行 mvn clean package -Dmaven.test.skip=true 进行模块打包， 打包完成后可在各 bundle 的 target 目录里查看到打包生成的 ark-biz jar 包

### 安装模块应用 biz1
```shell
telnet localhost 1234
biz -i file://${你的项目目录}/samples/springboot-samples/db/mybatis/biz1/target/biz1-mybatis-0.0.1-SNAPSHOT-ark-biz.jar
```

如果想验证卸载也可以执行
```shell
biz -u biz1-mybatis:0.0.1-SNAPSHOT
```

### 发起请求验证

#### 验证基座 mybatis 和 druid

注解mapper 和 xml mapper的方式均支持

```shell
curl http://localhost:8080/hello/haha
```
返回 "hello haha to base-mybatis deploy"

```shell
curl http://localhost:8080/mybatis
```
返回 user 表中的内容，且可以发现使用的数据源已经变为 DruidDataSource

#### 验证模块 mybatis 和 druid

模块支持多样 druid 配置
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

注解 mapper 和 xml mapper的方式均支持

```shell
curl http://localhost:8080/biz1mybatis/hi
```
返回 "hello to biz1-mybatis deploy"

```shell
curl http://localhost:8080/biz1mybatis/testmybatis
```
返回 student 表中的内容，且可以发现使用的数据源已经变为 DruidDataSource

## 模块复用基座数据源

### 修改模块配置

在上一节「基座、模块各自定义数据源」的基础上

1. 移除模块数据源配置，在biz的application.properties文件中注释掉数据源datasource相关配置项
```properties
#spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
#spring.datasource.username=root
#spring.datasource.password=Zfj1995!
#spring.datasource.url=jdbc:mysql://localhost:3306/test?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
#
#mybatis.mapper-locations=classpath:mappers/*.xml
#
#spring.datasource.type=com.alibaba.druid.pool.DruidDataSource
#spring.datasource.druid.initial-size=5
#spring.datasource.druid.min-idle=5
#spring.datasource.druid.max-active=200
#spring.datasource.druid.max-wait=60000
#spring.datasource.druid.time-between-eviction-runs-millis=60000
#spring.datasource.druid.min-evictable-idle-time-millis=300000
#spring.datasource.druid.test-while-idle=true
#spring.datasource.druid.test-on-borrow=false
#spring.datasource.druid.test-on-return=false
#spring.datasource.druid.pool-prepared-statements=false
#spring.datasource.druid.filters=stat,wall,slf4j
```
   
2. 添加模块MybatisConfig
```java
@Configuration
@MapperScan(basePackages = "com.alipay.sofa.biz1.mapper", sqlSessionFactoryRef = "mysqlSqlFactory")
@EnableTransactionManagement
public class MybatisConfig {

    //tips:不要初始化一个基座的DataSource，当模块被卸载的是，基座数据源会被销毁，transactionManager，transactionTemplate，mysqlSqlFactory被销毁没有问题

    @Bean(name = "transactionManager")
    public PlatformTransactionManager platformTransactionManager() {
        return (PlatformTransactionManager) getBaseBean("transactionManager");
    }

    @Bean(name = "transactionTemplate")
    public TransactionTemplate transactionTemplate() {
        return (TransactionTemplate) getBaseBean("transactionTemplate");
    }

    @Bean(name = "mysqlSqlFactory")
    public SqlSessionFactoryBean mysqlSqlFactory() throws IOException {
        //数据源不能申明成模块spring上下文中的bean，因为模块卸载时会触发close方法

        DataSource dataSource = (DataSource) getBaseBean("dataSource");
        SqlSessionFactoryBean mysqlSqlFactory = new SqlSessionFactoryBean();
        mysqlSqlFactory.setDataSource(dataSource);
        mysqlSqlFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:mappers/*.xml"));
        return mysqlSqlFactory;
    }
}
```

同上一节「基座、模块各自定义数据源」启动基座、部署模块、发起验证即可。


## 注意事项
这里主要使用简单应用做验证，如果复杂应用，需要注意模块做好瘦身，基座有的依赖，模块尽可能设置成 provided，尽可能使用基座的依赖。

