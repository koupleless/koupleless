package com.alipay.sofa.serverless.arklet.springboot.actuator.info;

import com.alipay.sofa.serverless.arklet.springboot.actuator.ActuatorComponent;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthDataModel;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.model.BizModel;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.model.PluginModel;

/**
 * @author Lunarscave
 */
public interface MoudleInfoService extends ActuatorComponent {

    HealthDataModel queryMasterBiz();

    HealthDataModel queryAllBiz();

    HealthDataModel queryAllPlugin();

    HealthDataModel getBizInfo(BizModel biz);

    HealthDataModel getPluginInfo(PluginModel plugin);
}
