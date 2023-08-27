package com.alipay.sofa.serverless.arklet.springboot.actuator.api;

import com.alipay.sofa.serverless.arklet.springboot.actuator.ActuatorComponent;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.HealthActuatorService;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthDetailsModel;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthResponseModel;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.MoudleInfoService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lunarscave
 */
public class ActuatorClient implements ActuatorComponent {

    private static MoudleInfoService moudleInfoService;
    private static HealthActuatorService healthActuatorService;

    public static HealthResponseModel getAllHealth() {
        HealthResponseModel model = new HealthResponseModel();
        model.putHealthInfo(moudleInfoService.queryMasterBiz());
        model.putHealthInfo(healthActuatorService.getMasterBizHealth());
        model.putHealthInfo(healthActuatorService.getCpuHealth());
        model.putHealthInfo(healthActuatorService.getJvmHealth());
        return model;
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
