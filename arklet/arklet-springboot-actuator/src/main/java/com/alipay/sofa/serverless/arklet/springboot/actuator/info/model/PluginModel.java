package com.alipay.sofa.serverless.arklet.springboot.actuator.info.model;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.Plugin;

import java.util.ArrayList;
import java.util.List;

public class PluginModel {

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

    public static PluginModel createPluginModel(Plugin plugin) {
        PluginModel pluginModel = new PluginModel();
        pluginModel.setPluginName(plugin.getPluginName());
        pluginModel.setGroupId(plugin.getGroupId());
        pluginModel.setArtifactId(plugin.getArtifactId());
        pluginModel.setPluginVersion(plugin.getVersion());
        pluginModel.setPluginActivator(plugin.getPluginActivator());
        pluginModel.setPluginUrl(plugin.getPluginURL().getPath());
        return pluginModel;
    }

    public static List<PluginModel> createPluginModelList(List<Plugin> pluginList) {
        List<PluginModel> pluginModelList = new ArrayList<>();
        for (Plugin plugin : pluginList) {
            pluginModelList.add(createPluginModel(plugin));
        }
        return pluginModelList;
    }
}
