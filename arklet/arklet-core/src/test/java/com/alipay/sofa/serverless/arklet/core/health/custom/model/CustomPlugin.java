package com.alipay.sofa.serverless.arklet.core.health.custom.model;

import com.alipay.sofa.ark.exception.ArkRuntimeException;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.model.PluginContext;

import java.net.URL;
import java.util.Set;

public class CustomPlugin implements Plugin {

    private final String pluginName;
    private final String pluginVersion;

    public CustomPlugin(String pluginName, String pluginVersion) {
        this.pluginName = pluginName;
        this.pluginVersion = pluginVersion;
    }

    @Override
    public String getPluginName() {
        return pluginName;
    }

    @Override
    public String getGroupId() {
        return null;
    }

    @Override
    public String getArtifactId() {
        return null;
    }

    @Override
    public String getVersion() {
        return pluginVersion;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public String getPluginActivator() {
        return null;
    }

    @Override
    public URL[] getClassPath() {
        return new URL[0];
    }

    @Override
    public ClassLoader getPluginClassLoader() {
        return null;
    }

    @Override
    public PluginContext getPluginContext() {
        return null;
    }

    @Override
    public Set<String> getExportPackages() {
        return null;
    }

    @Override
    public Set<String> getExportPackageNodes() {
        return null;
    }

    @Override
    public Set<String> getExportPackageStems() {
        return null;
    }

    @Override
    public Set<String> getExportClasses() {
        return null;
    }

    @Override
    public Set<String> getImportPackages() {
        return null;
    }

    @Override
    public Set<String> getImportPackageNodes() {
        return null;
    }

    @Override
    public Set<String> getImportPackageStems() {
        return null;
    }

    @Override
    public Set<String> getImportClasses() {
        return null;
    }

    @Override
    public Set<String> getImportResources() {
        return null;
    }

    @Override
    public Set<String> getImportPrefixResourceStems() {
        return null;
    }

    @Override
    public Set<String> getImportSuffixResourceStems() {
        return null;
    }

    @Override
    public Set<String> getExportResources() {
        return null;
    }

    @Override
    public Set<String> getExportPrefixResourceStems() {
        return null;
    }

    @Override
    public Set<String> getExportSuffixResourceStems() {
        return null;
    }

    @Override
    public URL getPluginURL() {
        try {
            return new URL("https://test.path");
        } catch (Throwable throwable) {
            return null;
        }
    }

    @Override
    public void start() throws ArkRuntimeException {

    }

    @Override
    public void stop() throws ArkRuntimeException {

    }
}
