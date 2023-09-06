package com.alipay.sofa.serverless.arklet.core.command;

import java.util.List;

import com.alipay.sofa.serverless.arklet.core.BaseTest;
import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.HelpHandler;
import com.alipay.sofa.serverless.arklet.core.command.builtin.model.CommandModel;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.command.meta.InputMeta;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.common.exception.CommandValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

/**
 * @author mingmen
 * @date 2023/9/6
 */
public class HelpHandlerTests extends BaseTest {

    private HelpHandler helpHandler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        helpHandler = (HelpHandler)commandService.getHandler(BuiltinCommand.HELP);
    }

    @Test
    public void testHandle() {
        Output<List<CommandModel>> result = helpHandler.handle(new InputMeta());
        Assert.assertTrue(result.getData() != null && !result.getData().isEmpty());
    }

    @Test
    public void testCommand() {
        // Act
        Command result = helpHandler.command();

        // Assert
        assert result == BuiltinCommand.HELP;
    }

    @Test
    public void testValidate() {
        // Arrange
        InputMeta input = new InputMeta();

        // Act
        try {
            helpHandler.validate(input);
        } catch (CommandValidationException e) {
            assert false;
        }
    }

}
