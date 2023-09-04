package com.alipay.sofa.serverless.arklet.core.health;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.common.utils.ArrayUtil;
import com.alipay.sofa.serverless.arklet.core.health.indicator.ArkletBaseIndicator;
import com.alipay.sofa.serverless.arklet.core.health.indicator.CpuIndicator;
import com.alipay.sofa.serverless.arklet.core.health.indicator.JvmIndicator;
import com.alipay.sofa.serverless.arklet.core.health.model.BizHealthMeta;
import com.alipay.sofa.serverless.arklet.core.health.model.Constants;
import com.alipay.sofa.serverless.arklet.core.health.model.Health;
import com.alipay.sofa.serverless.arklet.core.health.model.PluginHealthMeta;
import com.alipay.sofa.serverless.arklet.core.command.builtin.model.BizModel;
import com.alipay.sofa.serverless.arklet.core.command.builtin.model.PluginModel;
import com.alipay.sofa.serverless.arklet.core.common.log.ArkletLogger;
import com.alipay.sofa.serverless.arklet.core.common.log.ArkletLoggerFactory;
import com.google.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.alibaba.fastjson.JSON.toJSONString;

/**
 * @author Lunarscave
 */
@Singleton
public class HealthServiceImpl implements HealthService {

    private static final ArkletLogger LOGGER = ArkletLoggerFactory.getDefaultLogger();

    private final Map<String, ArkletBaseIndicator> indicators = new ConcurrentHashMap<>(3);

    @Override
    public void init() {
        initIndicators();
    }

    @Override
    public void destroy() {
    }

    @Override
    public Health getHealth() {
        Health health = Health.createHealth();
        for (ArkletBaseIndicator indicator : this.indicators.values()) {
            health.putAllHealthData(indicator.getHealthModel());
        }
        return health;
    }

    @Override
    public Health getHealth(String indicatorId) {
        Health health = Health.createHealth();
        try {
            AssertUtils.assertNotNull(indicators.get(indicatorId), "indicator not registered");
            health.putAllHealthData(indicators.get(indicatorId).getHealthModel());
        } catch (Throwable e) {
            health.putErrorData(Constants.HEALTH_ERROR, e.getMessage());
        }
        return health;
    }

    @Override
    public Health getHealth(String[] indicatorIds) {
        Health health = Health.createHealth();
        if (ArrayUtil.isEmpty(indicatorIds)) {
            health.putAllHealthData(getHealth());
        } else {
            for (String indicatorId : indicatorIds) {
                health.putAllHealthData(getHealth(indicatorId));
            }
        }
        return health;
    }

    @Override
    public Health queryModuleInfo() {
        return Health.createHealth()
                .putAllHealthData(queryMasterBiz())
                .putAllHealthData(queryModuleInfo(new BizModel()))
                .putAllHealthData(queryModuleInfo(new PluginModel()));
    }

    @Override
    public Health queryModuleInfo(String type, String name, String version) {
        Health health = Health.createHealth();
        try {
            AssertUtils.isTrue(StringUtils.isEmpty(type) || Constants.typeOfInfo(type), "illegal type: %s", type);
            if (StringUtils.isEmpty(type) || Constants.BIZ.equals(type)) {
                BizModel bizModel = new BizModel();
                bizModel.setBizName(name);
                bizModel.setBizVersion(version);
                health.putAllHealthData(queryModuleInfo(bizModel));
            }
            if (StringUtils.isEmpty(type) || Constants.PLUGIN.equals(type)) {
                PluginModel pluginModel = new PluginModel();
                pluginModel.setPluginName(name);
                pluginModel.setPluginVersion(version);
                health.putAllHealthData(queryModuleInfo(pluginModel));
            }
        } catch (Throwable e) {
            health.putErrorData(Constants.HEALTH_ERROR, e.getMessage());
        }
        return health;
    }

    @Override
    public Health queryModuleInfo(BizModel bizModel) {
        String bizName = bizModel.getBizName(),
                bizVersion = bizModel.getBizVersion();
        Health health = Health.createHealth();
        try {
            if (StringUtils.isEmpty(bizName) && StringUtils.isEmpty(bizVersion)) {
                List<BizHealthMeta> bizHealthMetaList = BizHealthMeta.createBizMetaList(
                        ArkClient.getBizManagerService().getBizInOrder());
                health.putHealthData("bizListInfo", bizHealthMetaList);
            } else if (StringUtils.isEmpty(bizVersion)) {
                List<Biz> bizList =  ArkClient.getBizManagerService().getBiz(bizName);
                AssertUtils.isTrue(bizList.size() > 0, "can not find biz: %s", bizName);
                List<BizHealthMeta> bizHealthMetaList = BizHealthMeta.createBizMetaList(bizList);
                health.putHealthData("bizListInfo", bizHealthMetaList);
            } else {
                BizHealthMeta bizHealthMeta = BizHealthMeta.createBizMeta(
                        ArkClient.getBizManagerService().getBiz(bizName, bizVersion));
                health.putHealthData("bizInfo", bizHealthMeta);
            }
        } catch (Throwable e) {
            health.putErrorData(Constants.HEALTH_ERROR, e.getMessage());
        }
        return health;
    }

    @Override
    public Health queryModuleInfo(PluginModel pluginModel) {
        String pluginName = pluginModel.getPluginName();
        Health health = Health.createHealth();
        try {
            if (StringUtils.isEmpty(pluginName)) {
                List<PluginHealthMeta> pluginHealthMetaList = PluginHealthMeta.createPluginMetaList(
                        ArkClient.getPluginManagerService().getPluginsInOrder());
                health.putHealthData("pluginListInfo", pluginHealthMetaList);
            } else {
                PluginHealthMeta pluginHealthMeta = PluginHealthMeta.createPluginMeta(
                        ArkClient.getPluginManagerService().getPluginByName(pluginName));
                health.putHealthData("pluginInfo", pluginHealthMeta);
            }
        } catch (Throwable e) {
            health.putErrorData(Constants.HEALTH_ERROR, e.getMessage());
        }
        return health;
    }

    @Override
    public Health queryMasterBiz() {
        BizHealthMeta bizHealthMeta = BizHealthMeta.createBizMeta(ArkClient.getMasterBiz());
        return Health.createHealth().putHealthData("masterBizInfo",
                JSON.parseObject(toJSONString(bizHealthMeta), JSONObject.class));
    }

    @Override
    public void registerIndicator(ArkletBaseIndicator indicator) {
        this.indicators.put(indicator.getIndicatorId(), indicator);
        LOGGER.info("register indicator " + indicator.getIndicatorId());
    }

    public Health getHealth(ArkletBaseIndicator indicator) {
        return indicator.getHealthModel();
    }

    private void initIndicators() {
        registerIndicator(new CpuIndicator());
        registerIndicator(new JvmIndicator());
    }
}
