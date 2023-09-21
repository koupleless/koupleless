package com.alipay.sofa.serverless.arklet.core.ops;

import java.util.List;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizOperation;
import com.google.inject.Singleton;

/**
 * @author mingmen
 * @date 2023/6/14
 */
@Singleton
public class UnifiedOperationServiceImpl implements UnifiedOperationService {

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public ClientResponse install(String bizUrl) throws Throwable {
        BizOperation bizOperation = new BizOperation().setOperationType(BizOperation.OperationType.INSTALL);
        bizOperation.putParameter(Constants.CONFIG_BIZ_URL, bizUrl);
        return ArkClient.installOperation(bizOperation);
    }

    @Override
    public ClientResponse uninstall(String bizName, String bizVersion) throws Throwable {
        return ArkClient.uninstallBiz(bizName, bizVersion);
    }

    @Override
    public List<Biz> queryBizList() {
        return ArkClient.getBizManagerService().getBizInOrder();
    }

    @Override
    public ClientResponse switchBiz(String bizName, String bizVersion) throws Throwable {
        return ArkClient.switchBiz(bizName, bizVersion);
    }
}
