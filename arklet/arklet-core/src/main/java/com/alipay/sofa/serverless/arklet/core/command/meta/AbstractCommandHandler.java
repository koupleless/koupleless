package com.alipay.sofa.serverless.arklet.core.command.meta;

import java.lang.reflect.ParameterizedType;

import com.alipay.sofa.common.utils.StringUtil;
import com.alipay.sofa.serverless.arklet.core.actuator.ActuatorService;
import com.alipay.sofa.serverless.arklet.core.command.CommandService;
import com.alipay.sofa.serverless.arklet.core.ArkletComponentRegistry;
import com.alipay.sofa.serverless.arklet.core.common.exception.CommandValidationException;
import com.alipay.sofa.serverless.arklet.core.ops.UnifiedOperationService;

/**
 * @author mingmen
 * @date 2023/6/8
 */

@SuppressWarnings("unchecked")
public abstract class AbstractCommandHandler<P extends InputMeta, Q> {

    private final UnifiedOperationService unifiedOperationService = ArkletComponentRegistry.getOperationServiceInstance();
    private final CommandService commandService = ArkletComponentRegistry.getCommandServiceInstance();
    private final ActuatorService actuatorService = ArkletComponentRegistry.getActuatorServiceInstance();


    public abstract void validate(P p) throws CommandValidationException;
    public abstract Output<Q> handle(P p);
    public abstract Command command();

    public UnifiedOperationService getOperationService() {
        return unifiedOperationService;
    }
    public CommandService getCommandService() {
        return commandService;
    }
    public ActuatorService getActuatorService() {return actuatorService; }

    public Class<P> getInputClass() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<P>) parameterizedType.getActualTypeArguments()[0];
    }

    public static void isTrue(final boolean expression, final String message,
        final Object... values) {
        if (!expression) {
            throw new CommandValidationException(String.format(message, values));
        }
    }

    public static void notBlank(final String check, final String message,
        final Object... values) {
        if (StringUtil.isBlank(check)) {
            throw new CommandValidationException(String.format(message, values));
        }
    }

    public static void notNull(final Object check, final String message,
        final Object... values) {
        if (null == check) {
            throw new CommandValidationException(String.format(message, values));
        }
    }

}
