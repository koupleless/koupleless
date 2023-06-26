package com.alipay.sofa.serverless.arklet.core.component;

import com.alipay.sofa.serverless.arklet.core.ArkletComponentRegistry;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author mingmen
 * @date 2023/6/26
 */
public class ComponentRegistryTest {

    @Test
    public void run() {
        ArkletComponentRegistry registry = new ArkletComponentRegistry();
        registry.initComponents();
        Assert.assertNotNull(ArkletComponentRegistry.getCommandServiceInstance());
        Assert.assertNotNull(ArkletComponentRegistry.getOperationServiceInstance());
        Assert.assertNotNull(ArkletComponentRegistry.getApiClientInstance());
        Assert.assertTrue(ArkletComponentRegistry.getApiClientInstance().getTunnels().size() > 0);
    }
}
