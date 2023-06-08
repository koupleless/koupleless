package com.alipay.sofa.serverless.arklet.core.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.fastjson.JSONObject;

import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.common.utils.StringUtil;
import com.alipay.sofa.serverless.arklet.core.command.builtin.BuiltInCommand;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.HelpHandler;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.InstallBizHandler;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.QueryAllBizHandler;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.SwitchBizHandler;
import com.alipay.sofa.serverless.arklet.core.command.builtin.handler.UninstallBizHandler;
import com.alipay.sofa.serverless.arklet.core.command.coordinate.CommandCoordinator;
import com.alipay.sofa.serverless.arklet.core.command.coordinate.CommandMutexException;
import com.alipay.sofa.serverless.arklet.core.command.coordinate.CoordinatorConfig;
import com.alipay.sofa.serverless.arklet.core.command.coordinate.ExecutionLock;
import com.alipay.sofa.serverless.arklet.core.command.meta.InputMeta;
import com.alipay.sofa.serverless.arklet.core.command.meta.AbstractCommandHandler;
import com.alipay.sofa.serverless.arklet.core.command.meta.OutputMeta;
import com.alipay.sofa.serverless.arklet.core.common.CommandValidationException;
import com.alipay.sofa.serverless.arklet.core.common.ArkletInitException;
import com.alipay.sofa.serverless.arklet.core.util.AssertUtils;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;

/**
 * @author mingmen
 * @date 2023/6/8
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Singleton
public class CommandServiceImpl implements CommandService {

    private static final ArkLogger LOGGER = ArkLoggerFactory.getDefaultLogger();

    private final CommandCoordinator coordinator = CommandCoordinator.getInstance();

    private final Map<String, AbstractCommandHandler> handlerMap = new ConcurrentHashMap<>(16);

    @Override
    public void registerCommandHandler(AbstractCommandHandler handler) {
        AssertUtils.isTrue(StringUtil.isNotBlank(handler.command().getId()), "command handler id should not be blank");
        AssertUtils.isTrue(StringUtil.isNotBlank(handler.command().getDesc()), "command handler desc should not be blank");
        AssertUtils.isTrue(null != handler.command().getSample(), "command handler sample should not be null");
        if (handlerMap.containsKey(handler.command().getId())) {
            throw new ArkletInitException("handler id (" + handler.command().getId() + ") duplicated");
        }
        handlerMap.put(handler.command().getId(), handler);
        LOGGER.info("arklet registered command:{}", handler.command().getId());
    }

    @Override
    public void init() {
        registerBuiltInCommands();
        registerBuiltInCoordinateConfigs();
    }

    @Override
    public void destroy() {

    }

    @Override
    public OutputMeta process(String cmd, Map content) throws CommandValidationException, CommandMutexException {
        AbstractCommandHandler handler = getHandler(cmd);
        InputMeta input = toJavaBean(handler.getInputClass(), content);
        handler.validate(input);
        ExecutionLock executionLock = ExecutionLock.newInstance(handler.command());
        coordinator.putExecution(executionLock);
        try {
            return handler.handle(input);
        } finally {
            coordinator.popExecution(executionLock);
        }
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

    private AbstractCommandHandler getHandler(String cmdId) {
        AbstractCommandHandler handler = handlerMap.get(cmdId);
        AssertUtils.isTrue(handler != null, cmdId + " not found handler");
        return handler;
    }

    private void registerBuiltInCommands() {
        registerCommandHandler(new InstallBizHandler());
        registerCommandHandler(new HelpHandler());
        registerCommandHandler(new QueryAllBizHandler());
        registerCommandHandler(new UninstallBizHandler());
        registerCommandHandler(new SwitchBizHandler());
    }

    private void registerBuiltInCoordinateConfigs() {
        CoordinatorConfig config = new CoordinatorConfig();
        config.addMutex(Lists.newArrayList(BuiltInCommand.INSTALL_BIZ, BuiltInCommand.UNINSTALL_BIZ));
        coordinator.registerConfig(config);
    }
}
