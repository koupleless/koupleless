package com.alipay.sofa.serverless.arklet.core.ops;

import java.util.List;

import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.serverless.arklet.core.ArkletComponent;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public interface UnifiedOperationService extends ArkletComponent {

    ClientResponse install(String bizPath) throws Throwable;

    ClientResponse uninstall(String bizName, String bizVersion) throws Throwable;

    List<Biz> queryBizList();

    ClientResponse switchBiz(String bizName, String bizVersion) throws Throwable;

}
