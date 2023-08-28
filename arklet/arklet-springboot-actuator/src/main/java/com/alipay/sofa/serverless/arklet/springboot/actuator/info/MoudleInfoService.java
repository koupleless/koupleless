package com.alipay.sofa.serverless.arklet.springboot.actuator.info;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.serverless.arklet.springboot.actuator.ActuatorComponent;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthDetailsModel;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.model.BizModel;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.model.PluginModel;

import java.util.List;

/**
 * @author Lunarscave
 */
public interface MoudleInfoService extends ActuatorComponent {

    HealthDetailsModel queryMasterBiz();

    HealthDetailsModel queryAllBiz();

    HealthDetailsModel queryAllPlugin();

    HealthDetailsModel getBizInfo(BizModel biz);

    HealthDetailsModel getPluginInfo(PluginModel plugin);
}
