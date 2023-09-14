package com.alipay.sofa.serverless.arklet.core.health;

import com.alipay.sofa.serverless.arklet.core.ArkletComponent;
import com.alipay.sofa.serverless.arklet.core.health.indicator.Indicator;
import com.alipay.sofa.serverless.arklet.core.health.model.Health;
import com.alipay.sofa.serverless.arklet.core.command.builtin.model.BizInfo;
import com.alipay.sofa.serverless.arklet.core.command.builtin.model.PluginInfo;

/**
 * @author Lunarscave
 */
public interface HealthService extends ArkletComponent {

    /**
     * get system health with all indicators
     * @return health with all details of indicators
     */
    Health getHealth();

    /**
     * get system health with indicator id
     * @param indicatorId indicator ids
     * @return health with indicator detail
     */
    Health getHealth(String indicatorId);

    /**
     * get system health with indicator ids
     * @param indicatorIds indicator ids
     * @return health with indicator detail(s)
     */
    Health getHealth(String[] indicatorIds);

    /**
     * query all module info
     * @return health with module infos
     */
    Health queryModuleInfo();

    /**
     * query module info with type, name and version
     * @param type module type, must in ("biz", "plugin")
     * @param name module name
     * @param version module version
     * @return health with module info(s)
     */
    Health queryModuleInfo(String type, String name, String version);

    /**
     * query biz info
     * @param bizInfo input plugin info
     * @return health with biz info(list)
     */
    Health queryModuleInfo(BizInfo bizInfo);

    /**
     * query plugin info
     * @param pluginInfo input plugin info
     * @return health with plugin info(list)
     */
    Health queryModuleInfo(PluginInfo pluginInfo);

    /**
     * query master biz info
     * @return health with master biz
     */
    Health queryMasterBiz();

    /**
     * get indicator by indicator id
     * @param indicatorId indicator id
     * @return indicator or null
     */
    Indicator getIndicator(String indicatorId);

    /**
     * register indicator
     * @param indicator input indicator
     */
    void registerIndicator(Indicator indicator);
}
