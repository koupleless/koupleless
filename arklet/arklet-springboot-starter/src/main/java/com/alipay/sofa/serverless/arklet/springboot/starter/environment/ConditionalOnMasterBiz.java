package com.alipay.sofa.serverless.arklet.springboot.starter.environment;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

/**
 * @author mingmen
 * @date 2023/6/14
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnMasterBiz.class)
public @interface ConditionalOnMasterBiz {
}
