package com.alipay.sofa.serverless.arklet.springboot.actuator.info;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.extension.ArkServiceLoader;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthModel;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.model.BizModel;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.model.PluginModel;
import com.sun.webkit.plugin.PluginManager;

import java.util.List;

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
    public HealthModel queryMasterBiz() {
        return new HealthModel("master-biz-info", ArkClient.getMasterBiz());
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
