package com.alipay.sofa.serverless.arklet.core.actuator;

import com.alipay.sofa.serverless.arklet.core.ArkletComponent;
import com.alipay.sofa.serverless.arklet.core.actuator.health.indicator.ArkletBaseIndicator;
import com.alipay.sofa.serverless.arklet.core.actuator.model.HealthModel;
import com.alipay.sofa.serverless.arklet.core.command.builtin.model.BizModel;
import com.alipay.sofa.serverless.arklet.core.command.builtin.model.PluginModel;

import java.util.Map;

/**
 * @author Lunarscave
 */
public interface ActuatorService extends ArkletComponent {

    HealthModel getHealth();

    HealthModel getHealth(String indicatorId);

    HealthModel queryModuleInfo();

    HealthModel queryModuleInfo(BizModel bizModel);

    HealthModel queryModuleInfo(PluginModel pluginModel);

    HealthModel queryMasterBiz();

    void registerIndicator(ArkletBaseIndicator arkletBaseIndicator);
}
