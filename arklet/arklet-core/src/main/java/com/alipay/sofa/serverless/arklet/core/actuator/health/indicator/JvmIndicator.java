package com.alipay.sofa.serverless.arklet.core.actuator.health.indicator;

import com.alipay.sofa.serverless.arklet.core.actuator.health.handler.JvmHandler;
import com.alipay.sofa.serverless.arklet.core.util.ConvertUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lunarscave
 */
public class JvmIndicator extends ArkletBaseIndicator {

    private final JvmHandler jvmHandler;

    public JvmIndicator() {
        super("jvm");
        jvmHandler = new JvmHandler();
    }

    @Override
    protected Map<String, Object> getHealthDetails() {
        Map<String, Object> jvmHealthDetails = new HashMap<>(6);

        jvmHealthDetails.put("java version", jvmHandler.getJvmVersion());
        jvmHealthDetails.put("java home", jvmHandler.getJavaHome());
        jvmHealthDetails.put("total memory(M)", ConvertUtils.convertBytes2Megabyte(jvmHandler.getTotalMemory()));
        jvmHealthDetails.put("max memory(M)", ConvertUtils.convertBytes2Megabyte(jvmHandler.getMaxMemory()));
        jvmHealthDetails.put("free memory(M)", ConvertUtils.convertBytes2Megabyte(jvmHandler.getFreeMemory()));
        jvmHealthDetails.put("run time(s)", jvmHandler.getDuration());
        return jvmHealthDetails;
    }

}
