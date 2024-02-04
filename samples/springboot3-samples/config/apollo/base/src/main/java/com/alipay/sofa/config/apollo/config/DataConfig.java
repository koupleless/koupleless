package com.alipay.sofa.config.apollo.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

@ConfigurationProperties(prefix = "data")
@Service
@Data
@RefreshScope
@Slf4j
public class DataConfig implements InitializingBean {

    private String name;

    @Override
    public void afterPropertiesSet() {
        log.info("DataConfig: {}", this);
    }
}
