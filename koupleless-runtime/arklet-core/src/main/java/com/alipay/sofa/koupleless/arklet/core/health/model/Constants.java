/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.koupleless.arklet.core.health.model;

/**
 * @author Lunarscave
 */
public class Constants {

    public static final String SYSTEM                = "system";

    public static final String BIZ                   = "biz";

    public static final String PLUGIN                = "plugin";

    public static final String CPU                   = "cpu";

    public static final String JVM                   = "jvm";

    public static final String MASTER_BIZ_HEALTH     = "masterBizHealth";

    public static final String MASTER_BIZ_INFO       = "masterBizInfo";

    public static final String HEALTH_ERROR          = "error";

    public static final String HEALTH_ENDPOINT_ERROR = "endpointError";

    public static final String BIZ_INFO              = "bizInfo";

    public static final String BIZ_LIST_INFO         = "bizListInfo";

    public static final String PLUGIN_INFO           = "pluginInfo";

    public static final String PLUGIN_LIST_INFO      = "pluginListInfo";

    public static final String READINESS_HEALTHY     = "ACCEPTING_TRAFFIC";

    public static boolean typeOfQuery(String type) {
        return SYSTEM.equals(type) || BIZ.equals(type) || PLUGIN.equals(type);
    }

    public static boolean typeOfInfo(String type) {
        return Constants.BIZ.equals(type) || Constants.PLUGIN.equals(type);
    }
}
