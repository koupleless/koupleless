/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            log.info("Apollo config change, property namespace: [{}], propertyName:[{}], oldValue:{}, newValue:{}",
                    configChange.getNamespace(), configChange.getPropertyName(), configChange.getOldValue(), configChange.getNewValue());
        });
        refreshScope.refreshAll();
    }
}