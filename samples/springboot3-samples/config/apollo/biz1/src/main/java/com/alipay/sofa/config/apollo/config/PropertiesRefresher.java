package com.alipay.sofa.config.apollo.config;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @Author: ZYL
 * @Date: 2021/8/17 13:48
 * @Description: Apollo 配置热更新
 */
@Slf4j
@Component
public class PropertiesRefresher {

    @Autowired
    private RefreshScope refreshScope;

    @ApolloConfigChangeListener
    public void changeListener(ConfigChangeEvent changeEvent) {
        changeEvent.changedKeys().stream().forEach(changeKey -> {
            ConfigChange configChange = changeEvent.getChange(changeKey);
            log.info("Apollo biz1 config change,  propertyName:[{}], oldValue:{}, newValue:{}",
                     configChange.getPropertyName(), configChange.getOldValue(), configChange.getNewValue());
        });
        refreshScope.refreshAll();
    }
}