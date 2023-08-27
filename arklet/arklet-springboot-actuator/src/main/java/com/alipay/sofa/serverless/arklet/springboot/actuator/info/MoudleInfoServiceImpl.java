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
        BizModel bizModel = new BizModel();
        bizModel.setBizModel(ArkClient.getMasterBiz());
        return new HealthDetailsModel("masterBizInfo",
                JSON.parseObject(toJSONString(bizModel), JSONObject.class));
    }

    @Override
    public List<Biz> queryBizList() {
        return ArkClient.getBizManagerService().getBizInOrder();
    }

    @Override
    public List<Plugin> queryPluginList() {
        return null;
    }

    @Override
    public Biz getBizInfo(BizModel biz) {
        return ArkClient.getBizManagerService().getBiz(biz.getBizName(), biz.getBizVersion());
    }

    @Override
    public Plugin getPluginInfo(PluginModel plugin) {
        return null;
    }
}
