package com.alipay.sofa.serverless.arklet.core.command.handler;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.ark.spi.model.BizOperation;
import com.alipay.sofa.serverless.arklet.core.BaseTest;
import com.alipay.sofa.serverless.arklet.core.health.custom.CustomBizManagerService;
import com.alipay.sofa.serverless.arklet.core.health.custom.CustomPluginManagerService;
import org.junit.After;
import org.junit.Before;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mockStatic;

/**
 * @author lunarscave
 */
public class BaseHandlerTest extends BaseTest {

    public final ClientResponse success = new ClientResponse();
    public final ClientResponse failed = new ClientResponse();
    public MockedStatic<ArkClient> arkClient;

    @Before
    public void setupHandler() {
        success.setCode(ResponseCode.SUCCESS);
        failed.setCode(ResponseCode.FAILED);

        arkClient = mockStatic(ArkClient.class);
        arkClient.when(() -> {
            ArkClient.installOperation(new BizOperation());
        }).thenReturn(success);
        arkClient.when(ArkClient::getBizManagerService).thenReturn(new CustomBizManagerService());
        arkClient.when(ArkClient::getPluginManagerService).thenReturn(new CustomPluginManagerService());
        arkClient.when(ArkClient::getMasterBiz).thenReturn(new CustomBizManagerService().getMasterBiz());
    }

    @After
    public void tearDown() {
        arkClient.close();
    }

}
