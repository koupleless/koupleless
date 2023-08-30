package com.alipay.sofa.serverless.arklet.core.ops;

import java.util.List;

import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.serverless.arklet.core.ArkletComponent;
import com.alipay.sofa.serverless.arklet.springboot.actuator.model.HealthDataModel;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public interface UnifiedOperationService extends ArkletComponent {

    ClientResponse install(String bizUrl) throws Throwable;

    ClientResponse uninstall(String bizName, String bizVersion) throws Throwable;

    List<Biz> queryBizList();

    ClientResponse switchBiz(String bizName, String bizVersion) throws Throwable;

    HealthDataModel health();

    HealthDataModel queryAllBizHealth();

    HealthDataModel queryAllPluginHealth();

    HealthDataModel queryBizHealth(String bizName, String bizVersion) ;

    HealthDataModel queryPluginHealth(String pluginName, String pluginVersion) ;

}
