package com.alipay.sofa.serverless.arklet.core.health.model;

/**
 * @author Lunarscave
 */
public class Constants {

    public static final String SYSTEM = "system";

    public static final String BIZ = "biz";

    public static final String PLUGIN = "plugin";

    public static final String CPU = "cpu";

    public static final String JVM = "jvm";

    public static final String MASTER_BIZ_HEALTH = "masterBizHealth";

    public static final String HEALTH_ERROR = "error";

    public static final String HEALTH_ENDPOINT_ERROR = "endpointError";

    public static final String READINESS_HEALTHY = "ACCEPTING_TRAFFIC";

    public static boolean typeOfQuery(String type) {
        return SYSTEM.equals(type) || BIZ.equals(type) || PLUGIN.equals(type);
    }

    public static boolean typeOfInfo(String type) {
        return Constants.BIZ.equals(type) || Constants.PLUGIN.equals(type);
    }
}
