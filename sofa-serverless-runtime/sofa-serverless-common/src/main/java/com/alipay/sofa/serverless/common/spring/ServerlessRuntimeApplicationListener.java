package com.alipay.sofa.serverless.common.spring;

import com.alipay.sofa.ark.api.ArkConfigs;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.event.SpringApplicationEvent;
import org.springframework.context.ApplicationListener;

import static com.alipay.sofa.ark.spi.constant.Constants.PLUGIN_EXPORT_CLASS_ENABLE;

/**
 * @author: yuanyuan
 * @date: 2023/10/30 9:38 下午
 */
public class ServerlessRuntimeApplicationListener implements ApplicationListener<SpringApplicationEvent> {

    @Override
    public void onApplicationEvent(SpringApplicationEvent event) {
        if (this.getClass().getClassLoader() == Thread.currentThread().getContextClassLoader()) {
            if (event instanceof ApplicationStartingEvent) {
                // 开启ark2.0 embed
                ArkConfigs.setEmbedEnable(true);
                // 基础设施类强制委托
                System.setProperty(PLUGIN_EXPORT_CLASS_ENABLE, "true");
            }
        }
    }
}
