package com.alipay.sofa.biz1;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.io.IOException;

import static com.alipay.sofa.serverless.common.api.SpringBeanFinder.getBaseBean;

/**
 * @author: yuanyuan
 * @date: 2023/12/5 3:37 下午
 * 模块复用基座 transactionManager、transactionTemplate、以及 dataSource
 */
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
