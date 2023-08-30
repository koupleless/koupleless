package com.alipay.sofa.serverless.arklet.springboot.actuator.info;

import com.alipay.sofa.serverless.arklet.springboot.actuator.ActuatorComponent;
import com.alipay.sofa.serverless.arklet.springboot.actuator.model.HealthDataModel;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.model.BizModel;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.model.PluginModel;

/**
 * @author Lunarscave
 */
public interface MoudleInfoService extends ActuatorComponent {

    HealthDataModel queryMasterBiz();

    HealthDataModel queryAllBiz();

    HealthDataModel queryAllPlugin();

    HealthDataModel getBizInfo(BizModel biz) throws Throwable;

    HealthDataModel getPluginInfo(PluginModel plugin) throws Throwable;
}
