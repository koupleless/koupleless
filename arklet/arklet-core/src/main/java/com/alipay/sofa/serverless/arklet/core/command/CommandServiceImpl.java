package com.alipay.sofa.serverless.arklet.core.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSONObject;

import com.alipay.sofa.common.utils.StringUtil;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.HelpHandler;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.InstallBizHandler;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.QueryAllBizHandler;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.SwitchBizHandler;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.UninstallBizHandler;
import com.alipay.sofa.serverless.arklet.core.command.coordinate.CommandMutexException;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.command.meta.InputMeta;
import com.alipay.sofa.serverless.arklet.core.command.meta.Output;
import com.alipay.sofa.serverless.arklet.core.common.exception.ArkletInitException;
import com.alipay.sofa.serverless.arklet.core.common.exception.CommandValidationException;
import com.alipay.sofa.serverless.arklet.core.common.log.ArkletLogger;
import com.alipay.sofa.serverless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.serverless.arklet.core.util.AssertUtils;
import com.google.inject.Singleton;

/**
 * @author mingmen
 * @date 2023/6/8
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Singleton
public class CommandServiceImpl implements CommandService {

    private static final ArkletLogger LOGGER = ArkletLoggerFactory.getDefaultLogger();

    private final Map<String, AbstractCommandHandler> handlerMap = new ConcurrentHashMap<>(16);

    @Override
    public void registerCommandHandler(AbstractCommandHandler handler) {
        AssertUtils.isTrue(StringUtil.isNotBlank(handler.command().getId()), "command handler id should not be blank");
        AssertUtils.isTrue(StringUtil.isNotBlank(handler.command().getDesc()), "command handler desc should not be blank");
        if (handlerMap.containsKey(handler.command().getId())) {
            throw new ArkletInitException("handler id (" + handler.command().getId() + ") duplicated");
        }
        handlerMap.put(handler.command().getId(), handler);
        LOGGER.info("registered command:{}", handler.command().getId());
    }

    @Override
    public void init() {
        registerBuiltInCommands();
    }

    @Override
    public void destroy() {

    }

    @Override
    public Output<?> process(String cmd, Map content) throws CommandValidationException, CommandMutexException {
        AbstractCommandHandler handler = getHandler(cmd);
        InputMeta input = toJavaBean(handler.getInputClass(), content);
        handler.validate(input);
        return handler.handle(input);
    }

    @Override
    public boolean supported(String cmd) {
        return handlerMap.containsKey(cmd);
    }

    @Override
    public List<AbstractCommandHandler> listAllHandlers() {
        return new ArrayList<>(handlerMap.values());
    }

    private InputMeta toJavaBean(Class<InputMeta> clazz, Map map) {
        try {
            return JSONObject.parseObject(JSONObject.toJSONString(map), clazz);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public AbstractCommandHandler getHandler(Command command) {
        return getHandler(command.getId());
    }

    @Override
    public AbstractCommandHandler getHandler(String commandId) {
        AbstractCommandHandler handler = handlerMap.get(commandId);
        AssertUtils.isTrue(handler != null, commandId + " not found handler");
        return handler;
    }

    private void registerBuiltInCommands() {
        registerCommandHandler(new InstallBizHandler());
        registerCommandHandler(new HelpHandler());
        registerCommandHandler(new QueryAllBizHandler());
        registerCommandHandler(new UninstallBizHandler());
        registerCommandHandler(new SwitchBizHandler());
    }
}
