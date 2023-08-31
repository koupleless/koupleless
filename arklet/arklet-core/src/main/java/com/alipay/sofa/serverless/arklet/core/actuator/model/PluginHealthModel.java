package com.alipay.sofa.serverless.arklet.core.actuator.model;

import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.serverless.arklet.core.util.AssertUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lunarscave
 */
public class PluginHealthModel {

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

    public static PluginHealthModel createPluginModel(Plugin plugin) {
        AssertUtils.assertNotNull(plugin, "can not find plugin");
        PluginHealthModel pluginHealthModel = PluginHealthModel.createPluginModel(plugin.getPluginName(), plugin.getVersion());
        pluginHealthModel.setGroupId(plugin.getGroupId());
        pluginHealthModel.setArtifactId(plugin.getArtifactId());
        pluginHealthModel.setPluginActivator(plugin.getPluginActivator());
        pluginHealthModel.setPluginUrl(plugin.getPluginURL().getPath());
        return pluginHealthModel;
    }

    public static PluginHealthModel createPluginModel(String pluginName, String pluginVersion) {
        PluginHealthModel pluginHealthModel = new PluginHealthModel();
        pluginHealthModel.setPluginName(pluginName);
        pluginHealthModel.setPluginVersion(pluginVersion);
        return pluginHealthModel;
    }

    public static List<PluginHealthModel> createPluginModelList(List<Plugin> pluginList) {
        List<PluginHealthModel> pluginHealthModelList = new ArrayList<>();
        for (Plugin plugin : pluginList) {
            pluginHealthModelList.add(createPluginModel(plugin));
        }
        return pluginHealthModelList;
    }
}
