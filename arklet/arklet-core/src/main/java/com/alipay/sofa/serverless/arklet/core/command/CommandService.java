package com.alipay.sofa.serverless.arklet.core.command;

import java.util.List;
import java.util.Map;

import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.ArkletComponent;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.command.meta.CommandType;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;

/**
 * @author mingmen
 * @date 2023/6/14
 */
@SuppressWarnings("rawtypes")
public interface CommandService extends ArkletComponent {

    /**
     * register command handler
     * @param handler handler
     */
    void registerCommandHandler(AbstractCommandHandler handler);

    /**
     * get command handler
     * @param command command
     * @return handler
     */
    AbstractCommandHandler getHandler(Command command);

    /**
     * get command handler
     * @param commandId commandId
     * @return handler
     */
    AbstractCommandHandler getHandler(String commandId);

    /**
     * core method
     * @param cmd command handler
     * @param content detail for this command
     * @return process result
     */
    Output<?> process(String cmd, Map content) throws InterruptedException;

    /**
     * if the cmd supported
     * @param cmd command handler
     * @return whether supported
     */
    boolean supported(String cmd);

    /**
     * get all handlers
     * @return handlers
     */
    List<AbstractCommandHandler> listAllHandlers();

}
