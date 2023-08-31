package com.alipay.sofa.serverless.arklet.core.actuator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.serverless.arklet.core.actuator.health.indicator.ArkletBaseIndicator;
import com.alipay.sofa.serverless.arklet.core.actuator.health.indicator.CpuIndicator;
import com.alipay.sofa.serverless.arklet.core.actuator.health.indicator.JvmIndicator;
import com.alipay.sofa.serverless.arklet.core.actuator.model.BizHealthModel;
import com.alipay.sofa.serverless.arklet.core.actuator.model.HealthModel;
import com.alipay.sofa.serverless.arklet.core.actuator.model.PluginHealthModel;
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
public class ActuatorServiceImpl implements ActuatorService {

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
    public HealthModel getHealth() {
        HealthModel healthModel = HealthModel.createHealthModel();
        for (ArkletBaseIndicator indicator : this.indicators.values()) {
            healthModel.putAllHealthData(indicator.getHealthModel());
        }
        return healthModel;
    }

    @Override
    public HealthModel getHealth(String indicatorId) {
        HealthModel healthModel = HealthModel.createHealthModel();
        try {
            AssertUtils.assertNotNull(indicators.get(indicatorId), "indicator not registered");
            healthModel.putAllHealthData(indicators.get(indicatorId).getHealthModel());
        } catch (Throwable e) {
            healthModel.putErrorData("error", e.getMessage());
        }
        return healthModel;
    }

    @Override
    public HealthModel queryModuleInfo() {
        return HealthModel.createHealthModel()
                .putAllHealthData(queryMasterBiz())
                .putAllHealthData(queryModuleInfo(new BizModel()))
                .putAllHealthData(queryModuleInfo(new PluginModel()));
    }

    @Override
    public HealthModel queryModuleInfo(BizModel bizModel) {
        String bizName = bizModel.getBizName(),
                bizVersion = bizModel.getBizVersion();
        HealthModel healthModel = HealthModel.createHealthModel();
        try {
            if (bizName == null && bizVersion == null ) {
                List<BizHealthModel> bizHealthModelList = BizHealthModel.createBizModelList(
                        ArkClient.getBizManagerService().getBizInOrder());
                healthModel.putHealthData("bizListInfo", bizHealthModelList);
            } else if (bizVersion == null) {
                List<Biz> bizList =  ArkClient.getBizManagerService().getBiz(bizName);
                AssertUtils.isTrue(bizList.size() > 0, "can not find biz: %s", bizName);
                List<BizHealthModel> bizHealthModelList = BizHealthModel.createBizModelList(bizList);
                healthModel.putHealthData("bizListInfo", bizHealthModelList);
            } else {
                BizHealthModel bizHealthModel = BizHealthModel.createBizModel(
                        ArkClient.getBizManagerService().getBiz(bizName, bizVersion));
                healthModel.putHealthData("bizInfo", bizHealthModel);
            }
        } catch (Throwable e) {
            healthModel.putErrorData("error", e.getMessage());
        }
        return healthModel;
    }

    @Override
    public HealthModel queryModuleInfo(PluginModel pluginModel) {
        String pluginName = pluginModel.getPluginName();
        HealthModel healthModel = HealthModel.createHealthModel();
        try {
            if (pluginName == null) {
                List<PluginHealthModel> pluginHealthModelList = PluginHealthModel.createPluginModelList(
                        ArkClient.getPluginManagerService().getPluginsInOrder());
                healthModel.putHealthData("pluginListInfo", pluginHealthModelList);
            } else {
                PluginHealthModel pluginHealthModel = PluginHealthModel.createPluginModel(
                        ArkClient.getPluginManagerService().getPluginByName(pluginName));
                healthModel.putHealthData("pluginInfo", pluginHealthModel);
            }
        } catch (Throwable e) {
            healthModel.putErrorData("error", e.getMessage());
        }
        return healthModel;
    }

    @Override
    public HealthModel queryMasterBiz() {
        BizHealthModel bizHealthModel = BizHealthModel.createBizModel(ArkClient.getMasterBiz());
        return HealthModel.createHealthModel().putHealthData("masterBizInfo",
                JSON.parseObject(toJSONString(bizHealthModel), JSONObject.class));
    }

    @Override
    public void registerIndicator(ArkletBaseIndicator indicator) {
        this.indicators.put(indicator.getIndicatorId(), indicator);
        LOGGER.info("register indicator " + indicator.getIndicatorId());
    }

    public HealthModel getHealth(ArkletBaseIndicator indicator) {
        return indicator.getHealthModel();
    }

    private void initIndicators() {
        registerIndicator(new CpuIndicator());
        registerIndicator(new JvmIndicator());
    }
}
