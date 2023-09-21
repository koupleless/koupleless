package com.alipay.sofa.serverless.arklet.core.health.model;

import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.serverless.arklet.core.util.AssertUtils;

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
        PluginHealthMeta pluginHealthMeta = PluginHealthMeta.createPluginMeta(plugin.getPluginName(), plugin.getVersion());
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
