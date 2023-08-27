package com.alipay.sofa.serverless.arklet.springboot.actuator.common.util;

import com.alipay.sofa.serverless.arklet.springboot.actuator.common.log.ActuatorLogger;
import com.alipay.sofa.serverless.arklet.springboot.actuator.common.log.ActuatorLoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;


/**
 * @author Lunarscave
 */
public class SpringBootUtil implements ApplicationContextAware {

    private static final ActuatorLogger LOGGER = ActuatorLoggerFactory.getDefaultLogger();
    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringBootUtil.applicationContext = applicationContext;
        LOGGER.info("finish configure application context");
    }

    public static ApplicationContext getApplicationContext() {
        return SpringBootUtil.applicationContext;
    }

    public static <T> T getBean(Class<T> clazz){
        return getApplicationContext().getBean(clazz);
    }
}
