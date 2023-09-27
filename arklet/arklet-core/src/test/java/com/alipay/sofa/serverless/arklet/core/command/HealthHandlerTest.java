package com.alipay.sofa.serverless.arklet.core.command;

import com.alipay.sofa.serverless.arklet.core.BaseTest;
import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.HealthHandler;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.HealthHandler.Input;
import com.alipay.sofa.serverless.arklet.core.common.exception.CommandValidationException;
import org.junit.Test;

/**
 * @author lunarscave
 */
public class HealthHandlerTest extends BaseTest {

    private void testValidate(Input input) throws CommandValidationException{
        HealthHandler handler = (HealthHandler) commandService.getHandler(BuiltinCommand.HEALTH);
        handler.validate(input);
    }

    @Test(expected = CommandValidationException.class)
    public void testValidate_InvalidType() throws CommandValidationException {
        Input input = new Input();
        input.setType("non type");
        testValidate(input);
    }

}
