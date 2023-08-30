package com.alipay.sofa.serverless.arklet.springboot.actuator.api;

import com.alipay.sofa.serverless.arklet.springboot.actuator.ActuatorComponent;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.HealthActuatorService;
import com.alipay.sofa.serverless.arklet.springboot.actuator.model.HealthDataModel;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.MoudleInfoService;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.model.BizModel;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.model.PluginModel;


/**
 * @author Lunarscave
 */
public class ActuatorClient implements ActuatorComponent {

    private static MoudleInfoService moudleInfoService;
    private static HealthActuatorService healthActuatorService;

    public static HealthDataModel getHealth(HealthQueryType queryType) {
        HealthDataModel healthDataModel = HealthDataModel.createHealthDataModel();
        if (queryType == HealthQueryType.ALL) {
            healthDataModel.putAllHealthData(moudleInfoService.queryMasterBiz());
        }
        if (queryType == HealthQueryType.ALL || queryType == HealthQueryType.BIZ_LIST) {
            healthDataModel.putAllHealthData(moudleInfoService.queryAllBiz());
        }
        if (queryType == HealthQueryType.ALL || queryType == HealthQueryType.PLUGIN_LIST) {
            healthDataModel.putAllHealthData(moudleInfoService.queryAllPlugin());
        }
        return healthDataModel.putAllHealthData(getSystemHealth());
    }

    public static HealthDataModel getHealth(BizModel biz){
        try {
            return HealthDataModel.createHealthDataModel()
                    .putAllHealthData(moudleInfoService.getBizInfo(biz))
                    .putAllHealthData(getSystemHealth());
        } catch (Throwable e) {
            return HealthDataModel.createHealthDataModel()
                    .putHealthData("error", e.getMessage());
        }

    }

    public static HealthDataModel getHealth(PluginModel plugin) {
        try {
            return HealthDataModel.createHealthDataModel()
                    .putAllHealthData(moudleInfoService.getPluginInfo(plugin))
                    .putAllHealthData(getSystemHealth());
        } catch (Throwable e) {
            return HealthDataModel.createHealthDataModel()
                    .putHealthData("error", e.getMessage());
        }
    }

    public static HealthDataModel getSystemHealth() {
        return HealthDataModel.createHealthDataModel()
                .putAllHealthData(healthActuatorService.getMasterBizHealth())
                .putAllHealthData(healthActuatorService.getCpuHealth())
                .putAllHealthData(healthActuatorService.getJvmHealth());
    }

    public static void setMoudleInfoService(MoudleInfoService moudleInfoService) {
        ActuatorClient.moudleInfoService = moudleInfoService;
    }

    public static void setHealthActuatorService(HealthActuatorService healthActuatorService) {
        ActuatorClient.healthActuatorService = healthActuatorService;
    }

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }
}
