package com.alipay.sofa.serverless.arklet.core.health;

import com.alipay.sofa.serverless.arklet.core.ArkletComponent;
import com.alipay.sofa.serverless.arklet.core.health.indicator.ArkletBaseIndicator;
import com.alipay.sofa.serverless.arklet.core.health.model.Health;
import com.alipay.sofa.serverless.arklet.core.command.builtin.model.BizInfo;
import com.alipay.sofa.serverless.arklet.core.command.builtin.model.PluginModel;

/**
 * @author Lunarscave
 */
public interface HealthService extends ArkletComponent {

    Health getHealth();

    Health getHealth(String indicatorId);

    Health getHealth(String[] indicatorIds);

    Health queryModuleInfo();

    Health queryModuleInfo(String type, String name, String version);

    Health queryModuleInfo(BizInfo bizInfo);

    Health queryModuleInfo(PluginModel pluginModel);

    Health queryMasterBiz();

    ArkletBaseIndicator getIndicator(String indicatorId);

    void registerIndicator(ArkletBaseIndicator arkletBaseIndicator);
}
