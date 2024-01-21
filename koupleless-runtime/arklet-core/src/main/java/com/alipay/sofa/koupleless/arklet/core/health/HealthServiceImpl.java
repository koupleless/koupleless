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
package com.alipay.sofa.koupleless.arklet.core.health;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.common.utils.ArrayUtil;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.model.BizInfo;
import com.alipay.sofa.koupleless.arklet.core.health.indicator.Indicator;
import com.alipay.sofa.koupleless.arklet.core.health.indicator.CpuIndicator;
import com.alipay.sofa.koupleless.arklet.core.health.indicator.JvmIndicator;
import com.alipay.sofa.koupleless.arklet.core.health.model.BizHealthMeta;
import com.alipay.sofa.koupleless.arklet.core.health.model.Constants;
import com.alipay.sofa.koupleless.arklet.core.health.model.Health;
import com.alipay.sofa.koupleless.arklet.core.health.model.Health.HealthBuilder;
import com.alipay.sofa.koupleless.arklet.core.health.model.PluginHealthMeta;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.model.PluginInfo;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLogger;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;
import com.google.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Lunarscave
 */
@Singleton
public class HealthServiceImpl implements HealthService {

    private final HealthBuilder          healthBuilder = new HealthBuilder();

    private final Map<String, Indicator> indicators    = new ConcurrentHashMap<>(3);

    @Override
    public void init() {
        initIndicators();
        healthBuilder.init();
    }

    @Override
    public void destroy() {
    }

    @Override
    public Health getHealth() {
        HealthBuilder builder = new HealthBuilder();
        for (Indicator indicator : this.indicators.values()) {
            builder.putAllHealthData(indicator.getHealthModel(healthBuilder));
        }
        return builder.build();
    }

    @Override
    public Health getHealth(String indicatorId) {
        try {
            healthBuilder.init();
            AssertUtils.assertNotNull(indicators.get(indicatorId), "indicator not registered");
            healthBuilder.putAllHealthData(indicators.get(indicatorId)
                .getHealthModel(healthBuilder));
        } catch (Throwable e) {
            healthBuilder.putErrorData(Constants.HEALTH_ERROR, e.getMessage());
        }
        return healthBuilder.build();
    }

    @Override
    public Health getHealth(String[] indicatorIds) {
        HealthBuilder builder = new HealthBuilder();
        if (ArrayUtil.isEmpty(indicatorIds)) {
            builder.putAllHealthData(getHealth());
        } else {
            for (String indicatorId : indicatorIds) {
                builder.putAllHealthData(getHealth(indicatorId));
            }
        }
        return builder.build();
    }

    @Override
    public Health queryModuleInfo() {
        HealthBuilder builder = new HealthBuilder();
        return builder.init().putAllHealthData(queryMasterBiz())
            .putAllHealthData(queryModuleInfo(new BizInfo()))
            .putAllHealthData(queryModuleInfo(new PluginInfo())).build();
    }

    @Override
    public Health queryModuleInfo(String type, String name, String version) {
        HealthBuilder builder = new HealthBuilder();
        try {
            AssertUtils.isTrue(StringUtils.isEmpty(type) || Constants.typeOfInfo(type),
                "illegal type: %s", type);
            if (StringUtils.isEmpty(type) || Constants.BIZ.equals(type)) {
                BizInfo bizInfo = new BizInfo();
                bizInfo.setBizName(name);
                bizInfo.setBizVersion(version);
                builder.putAllHealthData(queryModuleInfo(bizInfo));
            }
            if (StringUtils.isEmpty(type) || Constants.PLUGIN.equals(type)) {
                PluginInfo pluginInfo = new PluginInfo();
                pluginInfo.setPluginName(name);
                pluginInfo.setPluginVersion(version);
                builder.putAllHealthData(queryModuleInfo(pluginInfo));
            }
        } catch (Throwable e) {
            builder.putErrorData(Constants.HEALTH_ERROR, e.getMessage());
        }
        return builder.build();
    }

    @Override
    public Health queryModuleInfo(BizInfo bizInfo) {
        String bizName = bizInfo.getBizName(), bizVersion = bizInfo.getBizVersion();
        healthBuilder.init();
        try {
            if (StringUtils.isEmpty(bizName) && StringUtils.isEmpty(bizVersion)) {
                List<BizHealthMeta> bizHealthMetaList = BizHealthMeta.createBizMetaList(ArkClient
                    .getBizManagerService().getBizInOrder());
                healthBuilder.putHealthData(Constants.BIZ_LIST_INFO, bizHealthMetaList);
            } else if (StringUtils.isEmpty(bizVersion)) {
                List<Biz> bizList = ArkClient.getBizManagerService().getBiz(bizName);
                AssertUtils.isTrue(bizList.size() > 0, "can not find biz: %s", bizName);
                List<BizHealthMeta> bizHealthMetaList = BizHealthMeta.createBizMetaList(bizList);
                healthBuilder.putHealthData(Constants.BIZ_LIST_INFO, bizHealthMetaList);
            } else {
                BizHealthMeta bizHealthMeta = BizHealthMeta.createBizMeta(ArkClient
                    .getBizManagerService().getBiz(bizName, bizVersion));
                healthBuilder.putHealthData(Constants.BIZ_INFO, bizHealthMeta);
            }
        } catch (Throwable e) {
            healthBuilder.putErrorData(Constants.HEALTH_ERROR, e.getMessage());
        }
        return healthBuilder.build();
    }

    @Override
    public Health queryModuleInfo(PluginInfo pluginInfo) {
        String pluginName = pluginInfo.getPluginName();
        healthBuilder.init();
        try {
            if (StringUtils.isEmpty(pluginName)) {
                List<PluginHealthMeta> pluginHealthMetaList = PluginHealthMeta
                    .createPluginMetaList(ArkClient.getPluginManagerService().getPluginsInOrder());
                healthBuilder.putHealthData(Constants.PLUGIN_LIST_INFO, pluginHealthMetaList);
            } else {
                PluginHealthMeta pluginHealthMeta = PluginHealthMeta.createPluginMeta(ArkClient
                    .getPluginManagerService().getPluginByName(pluginName));
                healthBuilder.putHealthData(Constants.PLUGIN_INFO, pluginHealthMeta);
            }
        } catch (Throwable e) {
            healthBuilder.putErrorData(Constants.HEALTH_ERROR, e.getMessage());
        }
        return healthBuilder.build();
    }

    @Override
    public Health queryMasterBiz() {
        return healthBuilder
            .init()
            .putHealthData(Constants.MASTER_BIZ_INFO,
                BizHealthMeta.createBizMeta(ArkClient.getMasterBiz())).build();
    }

    @Override
    public Indicator getIndicator(String indicatorId) {
        return indicators.get(indicatorId);
    }

    @Override
    public void registerIndicator(Indicator indicator) {
        this.indicators.put(indicator.getIndicatorId(), indicator);
        ArkletLoggerFactory.getDefaultLogger().info(
            "register indicator " + indicator.getIndicatorId());
    }

    private void initIndicators() {
        registerIndicator(new CpuIndicator());
        registerIndicator(new JvmIndicator());
    }
}
