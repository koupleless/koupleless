<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>


# Experiment: Base and Module use mybatis、mysql、druid
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

<!-- add log4j2 dependencies -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>

<!-- add log4j2 async queue dependencies -->
<dependency>
    <groupId>com.lmax</groupId>
    <artifactId>disruptor</artifactId>
    <version>${disruptor.version}</version>
</dependency>
<dependency>
    <groupId>com.alipay.sofa.koupleless</groupId>
    <artifactId>koupleless-log4j2-starter</artifactId>
    <!--    <version>${koupleless.runtime.version}</version>-->
    <version>1.0.0</version>
</dependency>
<!-- end of log4j2 -->

<!-- add db dependencies -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.31</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.3.1</version>
</dependency>
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>1.2.9</version>
</dependency>
<!-- end of db dependencies -->
```

### biz
The biz contains module biz1, which is regular SpringBoot. The packaging plugin method is modified to the sofaArk biz module packaging method, packaged as an ark biz jar package, and the packaging plugin configuration is as follows:
```xml

<!-- add db dependencies -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.31</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.3.1</version>
    <scope>provided</scope>
</dependency>
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid-spring-boot-starter</artifactId>
    <version>1.2.9</version>
    <scope>provided</scope>
</dependency>
<!-- end of db dependencies -->

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
    </configuration>
</plugin>
```
Note that the web context path of different biz is changed to different values, so that multiple web applications can be successfully installed in a tomcat host.

## Define data source separately for base and module

### Start mysql locally

please create the database, table, etc. required by the code in advance

```shell
# 1. cd into config directory
cd config
# 2. add execution permission to init_mysql.sh
chmod +x ./init_mysql.s
# 3. run init_mysql.sh
./init_mysql.sh
```

### start base application

1. please modify the datasource configuration in samples/springboot-samples/db/mybatis/base/src/main/resources/application.properties, and ensure that it is connected to the correct local database

2. start base application

### package module application biz1

1. please modify the datasource configuration in samples/springboot-samples/db/mybatis/biz1/src/main/resources/application.properties, and ensure that it is connected to the correct local database

2. execute mvn clean package -Dmaven.test.skip=true to package the module. After the packaging is completed, you can see the ark-biz jar package generated in the target directory of each bundle

### Install module application biz1
```shell
telnet localhost 1234
biz -i file://${your project directory}/samples/springboot-samples/db/mybatis/biz1/target/biz1-mybatis-0.0.1-SNAPSHOT-ark-biz.jar
```

If you want to verify hot deployment, you can uninstall and deploy multiple times
```shell
biz -u biz1-mybatis:0.0.1-SNAPSHOT
```

### Start verify request

#### Verify base mybatis and druid

support both annotation mapper and xml mapper

```shell
curl http://localhost:8080/hello/haha
```
return "hello haha to base-mybatis deploy"

```shell
curl http://localhost:8080/mybatis
```
return the content of user table, and you can see that the data source used has changed to DruidDataSource

#### verify mybatis and druid in module

support various druid configuration in module
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

support both annotation mapper and xml mapper

```shell
curl http://localhost:8080/biz1mybatis/hi
```
return "hello to biz1-mybatis deploy"

```shell
curl http://localhost:8080/biz1mybatis/testmybatis
```
return the content of student table, and you can see that the data source used has changed to DruidDataSource

## Module reuse base data source

### Modify module configuration

based on the previous section "base and module define data source separately"

1. remove module data source configuration, comment out the data source datasource related configuration in the application.properties
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
   
2. add module MybatisConfig
```java
@Configuration
@MapperScan(basePackages = "com.alipay.sofa.biz1.mapper", sqlSessionFactoryRef = "mysqlSqlFactory")
@EnableTransactionManagement
public class MybatisConfig {

    // tips: Do not initialize a base DataSource. When the module is unloaded, the base data source will be destroyed. There is no problem that transactionManager, transactionTemplate, mysqlSqlFactory are destroyed

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
        // datasource cannot be declared as a bean in the spring context of the module, because the close method of datasource will be triggered when the module is unloaded
        DataSource dataSource = (DataSource) getBaseBean("dataSource");
        SqlSessionFactoryBean mysqlSqlFactory = new SqlSessionFactoryBean();
        mysqlSqlFactory.setDataSource(dataSource);
        mysqlSqlFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:mappers/*.xml"));
        return mysqlSqlFactory;
    }
}
```

same as the previous section "base and module define data source separately" start the base, deploy the module, and start the verification.

## Precautions
Here mainly use simple applications for verification, if complex applications, need to pay attention to the module to do a good job of slimming, the base has dependencies, the module as much as possible set to provided, as much as possible to use the base dependencies.
