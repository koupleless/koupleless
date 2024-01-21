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
package com.alipay.sofa.koupleless.arklet.core.health;

import com.alipay.sofa.koupleless.arklet.core.ArkletComponent;
import com.alipay.sofa.koupleless.arklet.core.health.indicator.Indicator;
import com.alipay.sofa.koupleless.arklet.core.health.model.Health;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.model.BizInfo;
import com.alipay.sofa.koupleless.arklet.core.command.builtin.model.PluginInfo;

/**
 * @author Lunarscave
 */
public interface HealthService extends ArkletComponent {

    /**
     * get system health with all indicators
     * @return health with all details of indicators
     */
    Health getHealth();

    /**
     * get system health with indicator id
     * @param indicatorId indicator ids
     * @return health with indicator detail
     */
    Health getHealth(String indicatorId);

    /**
     * get system health with indicator ids
     * @param indicatorIds indicator ids
     * @return health with indicator detail(s)
     */
    Health getHealth(String[] indicatorIds);

    /**
     * query all module info
     * @return health with module infos
     */
    Health queryModuleInfo();

    /**
     * query module info with type, name and version
     * @param type module type, must in ("biz", "plugin")
     * @param name module name
     * @param version module version
     * @return health with module info(s)
     */
    Health queryModuleInfo(String type, String name, String version);

    /**
     * query biz info
     * @param bizInfo input plugin info
     * @return health with biz info(list)
     */
    Health queryModuleInfo(BizInfo bizInfo);

    /**
     * query plugin info
     * @param pluginInfo input plugin info
     * @return health with plugin info(list)
     */
    Health queryModuleInfo(PluginInfo pluginInfo);

    /**
     * query master biz info
     * @return health with master biz
     */
    Health queryMasterBiz();

    /**
     * get indicator by indicator id
     * @param indicatorId indicator id
     * @return indicator or null
     */
    Indicator getIndicator(String indicatorId);

    /**
     * register indicator
     * @param indicator input indicator
     */
    void registerIndicator(Indicator indicator);
}
