package com.alipay.sofa.serverless.arklet.springboot.actuator.info.model;

import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.serverless.arklet.springboot.actuator.common.util.AssertUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lunarscave
 */
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
        AssertUtil.assertNotNull(plugin, "can not find plugin");
        PluginModel pluginModel = PluginModel.createPluginModel(plugin.getPluginName(), plugin.getVersion());
        pluginModel.setGroupId(plugin.getGroupId());
        pluginModel.setArtifactId(plugin.getArtifactId());
        pluginModel.setPluginActivator(plugin.getPluginActivator());
        pluginModel.setPluginUrl(plugin.getPluginURL().getPath());
        return pluginModel;
    }

    public static PluginModel createPluginModel(String pluginName, String pluginVersion) {
        PluginModel pluginModel = new PluginModel();
        pluginModel.setPluginName(pluginName);
        pluginModel.setPluginVersion(pluginVersion);
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
