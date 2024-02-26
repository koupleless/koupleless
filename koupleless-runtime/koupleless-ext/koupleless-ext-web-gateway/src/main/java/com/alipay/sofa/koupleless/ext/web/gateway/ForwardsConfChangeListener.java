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
package com.alipay.sofa.koupleless.ext.web.gateway;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@ConditionalOnClass(ConfigChangeListener.class)
public class ForwardsConfChangeListener implements ConfigChangeListener {
    @Autowired
    private Forwards            forwards;
    @Autowired
    private GatewayProperties   gatewayProperties;
    private static final String WATCH_KEY_PREFIX = "koupleless.web.gateway";

    @Override
    public void onChange(ConfigChangeEvent event) {
        Set<String> keys = event.changedKeys();
        boolean watchKeyChanged = false;
        for (String key : keys) {
            if (key.contains(WATCH_KEY_PREFIX)) {
                watchKeyChanged = true;
                break;
            }
        }
        if (watchKeyChanged) {
            ForwardItems.init(forwards, gatewayProperties);
        }
    }
}
