package com.alipay.sofa.koupleless.ext.web.gateway;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@ConditionalOnClass(ConfigChangeListener.class)
public class ForwardsConfChangeListener implements ConfigChangeListener {
    @Autowired
    private Forwards forwards;
    @Autowired
    private GatewayProperties gatewayProperties;
    private static final String WATCH_KEY_PREFIX = "koupleless.web.gateway";

    @Override
    public void onChange(ConfigChangeEvent event) {
        Set<String> keys = event.changedKeys();
        boolean watchKeyChanged = false;
        for (String key : keys) {
            if (key.contains(WATCH_KEY_PREFIX)) {
                watchKeyChanged = true;
                break;
            }
        }
        if (watchKeyChanged) {
            ForwardItems.init(forwards, gatewayProperties);
        }
    }
}
