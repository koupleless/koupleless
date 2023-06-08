package com.alipay.sofa.serverless.arklet.springboot.starter.environment;

import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public class OnMasterBiz extends SpringBootCondition {

    private static final String BIZ_CLASSLOADER = "com.alipay.sofa.ark.container.service.classloader.BizClassLoader";

    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader == null || BIZ_CLASSLOADER.equals(contextClassLoader.getClass().getName())) {
            return new ConditionOutcome(false, "Current context classloader is biz classloader.");
        }
        return new ConditionOutcome(true, "Current context classloader is not biz classloader.");
    }
}
