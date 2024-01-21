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

import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.koupleless.arklet.core.util.AssertUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lunarscave
 */
public class PluginHealthMeta {

    private String pluginName;

    private String groupId;

    private String artifactId;

    private String pluginVersion;

    private String pluginUrl;

    private String pluginActivator;

    public String getPluginName() {
        return pluginName;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }

    public String getPluginActivator() {
        return pluginActivator;
    }

    public String getPluginUrl() {
        return pluginUrl;
    }

    public void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public void setPluginVersion(String pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    public void setPluginUrl(String pluginUrl) {
        this.pluginUrl = pluginUrl;
    }

    public void setPluginActivator(String pluginActivator) {
        this.pluginActivator = pluginActivator;
    }

    public static PluginHealthMeta createPluginMeta(Plugin plugin) {
        AssertUtils.assertNotNull(plugin, "can not find plugin");
        PluginHealthMeta pluginHealthMeta = PluginHealthMeta.createPluginMeta(
            plugin.getPluginName(), plugin.getVersion());
        pluginHealthMeta.setGroupId(plugin.getGroupId());
        pluginHealthMeta.setArtifactId(plugin.getArtifactId());
        pluginHealthMeta.setPluginActivator(plugin.getPluginActivator());
        pluginHealthMeta.setPluginUrl(plugin.getPluginURL().getPath());
        return pluginHealthMeta;
    }

    public static PluginHealthMeta createPluginMeta(String pluginName, String pluginVersion) {
        PluginHealthMeta pluginHealthMeta = new PluginHealthMeta();
        pluginHealthMeta.setPluginName(pluginName);
        pluginHealthMeta.setPluginVersion(pluginVersion);
        return pluginHealthMeta;
    }

    public static List<PluginHealthMeta> createPluginMetaList(List<Plugin> pluginList) {
        List<PluginHealthMeta> pluginHealthMetaList = new ArrayList<>();
        for (Plugin plugin : pluginList) {
            pluginHealthMetaList.add(createPluginMeta(plugin));
        }
        return pluginHealthMetaList;
    }
}
