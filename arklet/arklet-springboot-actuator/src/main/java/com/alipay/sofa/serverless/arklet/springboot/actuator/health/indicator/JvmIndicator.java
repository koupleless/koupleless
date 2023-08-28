package com.alipay.sofa.serverless.arklet.springboot.actuator.health.indicator;

import com.alipay.sofa.serverless.arklet.springboot.actuator.common.util.ConvertUtil;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.handler.JvmHandler;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Lunarscave
 */
@Component
public class JvmIndicator extends ArkletIndicator {

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
        jvmHealthDetails.put("total memory(M)", ConvertUtil.convertBytes2Megabyte(jvmHandler.getTotalMemory()));
        jvmHealthDetails.put("max memory(M)", ConvertUtil.convertBytes2Megabyte(jvmHandler.getMaxMemory()));
        jvmHealthDetails.put("free memory(M)", ConvertUtil.convertBytes2Megabyte(jvmHandler.getFreeMemory()));
        jvmHealthDetails.put("run time", ConvertUtil.convertEndDate2Duration(jvmHandler.getduration()));
        return jvmHealthDetails;
    }

    @Override
    public Health health() {
        try {
            Map<String, Object> jvmHealthDetails = getHealthDetails();
            return Health.up().withDetails(jvmHealthDetails).build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
