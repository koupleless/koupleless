---
title: 复用基座数据源
weight: 600
---

## 建议
强烈建议使用本文档方式，在模块中尽可能**复用基座数据源**，否则模块反复部署就会反复创建、消耗数据源连接，导致模块发布运维会变慢，同时也会额外消耗内存。<br/>

## SpringBoot 解法
在模块的代码中写个 MybatisConfig 类即可，这样事务模板都是复用基座的，只有 Mybatis 的 SqlSessionFactoryBean 需要新创建。<br /> 参考 demo：/sofa-serverless/samples/springboot-samples/db/mybatis/biz1

通过`SpringBeanFinder.getBaseBean`获取到基座的 Bean 对象，然后注册成模块的 Bean：

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

## SOFABoot 解法
如果 SOFABoot 基座没有开启多 bundle（Package 里没有 MANIFEST.MF 文件），则解法和上文 SpringBoot 完全一致。<br />如果有 MANIFEST.MF 文件，需要调用`BaseAppUtils.getBeanOfBundle`获取基座的 Bean，其中**BASE_DAL_BUNDLE_NAME** 为 MANIFEST.MF 里面的`Module-Name`：<br />![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2022/png/38696/1661758587977-7a499d0d-d5ca-4a68-9925-fa7258679d9b.png#clientId=ue6b6f4dc-5527-4&errorMessage=unknown%20error&from=paste&height=458&id=u531b3c3e&originHeight=916&originWidth=2042&originalType=binary&ratio=1&rotation=0&showTitle=false&size=383535&status=error&style=none&taskId=ua403e261-49af-4d10-99e6-12edf669677&title=&width=1021)
```java

@Configuration
@MapperScan(basePackages = "com.alipay.serverless.dal.dao", sqlSessionFactoryRef = "mysqlSqlFactory")
@EnableTransactionManagement
public class MybatisConfig {

    // 注意：不要初始化一个基座的 DataSource，会导致模块被热卸载的时候，基座的数据源被销毁，不符合预期。
    // 但是 transactionManager，transactionTemplate，mysqlSqlFactory 这些资源被销毁没有问题
    
    private static final String BASE_DAL_BUNDLE_NAME = "com.alipay.serverless.dal"

    @Bean(name = "transactionManager")
    public PlatformTransactionManager platformTransactionManager() {
        return (PlatformTransactionManager) BaseAppUtils.getBeanOfBundle("transactionManager",BASE_DAL_BUNDLE_NAME);
    }

    @Bean(name = "transactionTemplate")
    public TransactionTemplate transactionTemplate() {
        return (TransactionTemplate) BaseAppUtils.getBeanOfBundle("transactionTemplate",BASE_DAL_BUNDLE_NAME);
    }

    @Bean(name = "mysqlSqlFactory")
    public SqlSessionFactoryBean mysqlSqlFactory() throws IOException {
        //数据源不能申明成模块spring上下文中的bean，因为模块卸载时会触发close方法
        ZdalDataSource dataSource = (ZdalDataSource) BaseAppUtils.getBeanOfBundle("dataSource",BASE_DAL_BUNDLE_NAME);
        SqlSessionFactoryBean mysqlSqlFactory = new SqlSessionFactoryBean();
        mysqlSqlFactory.setDataSource(dataSource);
        mysqlSqlFactory.setMapperLocations(new PathMatchingResourcePatternResolver()
                .getResources("classpath:mapper/*.xml"));
        return mysqlSqlFactory;
    }
}

```

<br/>
<br/>
