package com.alipay.sofa.serverless.arklet.springboot.actuator.health.indicator;

import com.alipay.sofa.serverless.arklet.springboot.actuator.common.util.ConvertUtil;
import com.alipay.sofa.serverless.arklet.springboot.actuator.health.model.HealthDetailsModel;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Date;
import java.util.Properties;

/**
 * @author Lunarscave
 */
@Component
public class JvmIndicator extends ArkletIndicator {

    public JvmIndicator() {
        super("jvm");
    }

    @Override
    protected HealthDetailsModel getHealthInfo() {
        HealthDetailsModel jvmHealthModel = new HealthDetailsModel(getIndicatorId());
        Properties properties = System.getProperties();
        Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();

        jvmHealthModel.putHealthData("java version", properties.getProperty("java.version"));
        jvmHealthModel.putHealthData("java home", properties.getProperty("java.home"));
        jvmHealthModel.putHealthData("total memory(M)", ConvertUtil.convertBytes2Megabyte(runtime.totalMemory()));
        jvmHealthModel.putHealthData("max memory(M)", ConvertUtil.convertBytes2Megabyte(runtime.maxMemory()));
        jvmHealthModel.putHealthData("free memory(M)", ConvertUtil.convertBytes2Megabyte(runtime.freeMemory()));
        jvmHealthModel.putHealthData("run time", ConvertUtil.convertEndDate2Duration(new Date(runtimeMxBean.getStartTime())));
        return jvmHealthModel;
    }

    @Override
    public Health health() {
        try {
            HealthDetailsModel jvmHealthModel = getHealthInfo();
            return Health.up().withDetail(getIndicatorId(), jvmHealthModel).build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
