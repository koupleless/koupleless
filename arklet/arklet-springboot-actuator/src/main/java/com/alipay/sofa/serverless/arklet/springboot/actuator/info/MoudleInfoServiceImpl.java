package com.alipay.sofa.serverless.arklet.springboot.actuator.info;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthDetailsModel;
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
    public HealthDetailsModel queryMasterBiz() {
        BizModel bizModel = BizModel.createBizModel(ArkClient.getMasterBiz());
        return new HealthDetailsModel("masterBizInfo",
                JSON.parseObject(toJSONString(bizModel), JSONObject.class));
    }

    @Override
    public HealthDetailsModel queryAllBiz() {
        List<Biz> bizList = ArkClient.getBizManagerService().getBizInOrder();
        List<BizModel> bizModelList = BizModel.createBizModelList(bizList);
        return new HealthDetailsModel("allBizInfo",
                JSON.parseObject(toJSONString(bizModelList), JSONObject.class));
    }

    @Override
    public HealthDetailsModel queryAllPlugin() {
        List<Plugin> pluginList = ArkClient.getPluginManagerService().getPluginsInOrder();
        List<PluginModel> pluginModelList = PluginModel.createPluginModelList(pluginList);
        return new HealthDetailsModel("allPluginInfo",
                JSON.parseObject(toJSONString(pluginModelList), JSONObject.class));
    }

    @Override
    public HealthDetailsModel getBizInfo(BizModel biz) {
        BizModel bizModel = BizModel.createBizModel(
                ArkClient.getBizManagerService().getBiz(biz.getBizName(), biz.getBizVersion()));
        return new HealthDetailsModel("bizInfo",
                JSON.parseObject(toJSONString(bizModel), JSONObject.class));
    }

    @Override
    public HealthDetailsModel getPluginInfo(PluginModel plugin) {
        PluginModel pluginModel = PluginModel.createPluginModel(
                ArkClient.getPluginManagerService().getPluginByName(plugin.getPluginName()));
        return new HealthDetailsModel("pluginInfo",
                JSON.parseObject(toJSONString(pluginModel), JSONObject.class));
    }
}
