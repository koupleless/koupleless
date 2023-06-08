package com.alipay.sofa.serverless.arklet.core.ops;

import java.util.List;

import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.serverless.arklet.core.ArkletComponent;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public interface UnifiedOperationService extends ArkletComponent {

    ResponseCode install(String bizPath) throws Throwable;

    ResponseCode uninstall(String bizName, String bizVersion) throws Throwable;

    List<Biz> queryBizList();

    ResponseCode switchBiz(String bizName, String bizVersion) throws Throwable;

}
