package com.alipay.sofa.serverless.arklet.springboot.actuator.api;

import com.alipay.sofa.serverless.arklet.springboot.actuator.ActuatorComponent;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.HealthActuatorService;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthModel;
import com.alipay.sofa.serverless.arklet.springboot.actuator.info.MoudleInfoService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lunarscave
 */
public class ActuatorClient implements ActuatorComponent {

    private static MoudleInfoService moudleInfoService;
    private static HealthActuatorService healthActuatorService;

    public static List<HealthModel> getAllHealth() {
        List<HealthModel> models = new ArrayList<>();
        models.add(moudleInfoService.queryMasterBiz());
        models.add(healthActuatorService.getMasterBizHealth());
        return models;
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
