package com.alipay.sofa.serverless.arklet.springboot.actuator.info;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthDataModel;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.model.BizModel;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.model.PluginModel;

import java.util.List;

import static com.alibaba.fastjson.JSON.toJSONString;

/**
 * @author Lunarscave
 */
public class MoudleInfoServiceImpl implements MoudleInfoService{

    @Override
    public void init() {
    }

    @Override
    public void destroy() {

    }

    @Override
    public HealthDataModel queryMasterBiz() {
        BizModel bizModel = BizModel.createBizModel(ArkClient.getMasterBiz());
        return HealthDataModel.createHealthDataModel().putHealthData("masterBizInfo",
                JSON.parseObject(toJSONString(bizModel), JSONObject.class));
    }

    @Override
    public HealthDataModel queryAllBiz() {
        List<Biz> bizList = ArkClient.getBizManagerService().getBizInOrder();
        List<BizModel> bizModelList = BizModel.createBizModelList(bizList);
        return HealthDataModel.createHealthDataModel().putHealthData("allBizInfo",
                JSON.parseObject(toJSONString(bizModelList), JSONArray.class));
    }

    @Override
    public HealthDataModel queryAllPlugin() {
        List<Plugin> pluginList = ArkClient.getPluginManagerService().getPluginsInOrder();
        List<PluginModel> pluginModelList = PluginModel.createPluginModelList(pluginList);
        return HealthDataModel.createHealthDataModel().putHealthData("allPluginInfo",
                JSON.parseObject(toJSONString(pluginModelList), JSONArray.class));
    }

    @Override
    public HealthDataModel getBizInfo(BizModel biz) {
        BizModel bizModel = BizModel.createBizModel(
                ArkClient.getBizManagerService().getBiz(biz.getBizName(), biz.getBizVersion()));
        return HealthDataModel.createHealthDataModel().putHealthData("bizInfo",
                JSON.parseObject(toJSONString(bizModel), JSONObject.class));
    }

    @Override
    public HealthDataModel getPluginInfo(PluginModel plugin) {
        PluginModel pluginModel = PluginModel.createPluginModel(
                ArkClient.getPluginManagerService().getPluginByName(plugin.getPluginName()));
        return HealthDataModel.createHealthDataModel().putHealthData("pluginInfo",
                JSON.parseObject(toJSONString(pluginModel), JSONObject.class));
    }
}
