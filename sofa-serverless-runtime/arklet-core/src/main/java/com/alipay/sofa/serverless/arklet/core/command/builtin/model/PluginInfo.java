package com.alipay.sofa.serverless.arklet.core.command.builtin.model;

/**
 * @author Lunarscave
 */
public class PluginInfo {

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

}
