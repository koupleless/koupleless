package com.alipay.sofa.serverless.arklet.core.command;

import com.alipay.sofa.serverless.arklet.core.ArkletComponentRegistry;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author mingmen
 * @date 2023/6/26
 */
public class CommandTest {

    @Test
    public void registerCustomCommand() {
        ArkletComponentRegistry registry = new ArkletComponentRegistry();
        registry.initComponents();
        ArkletComponentRegistry.getCommandServiceInstance().registerCommandHandler(new CustomCommandHandler());
        CustomCommandHandler handler = (CustomCommandHandler)ArkletComponentRegistry.getCommandServiceInstance().getHandler(CustomCommand.HELLO);
        Assert.assertNotNull(handler);
    }
}
