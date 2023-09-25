package com.alipay.sofa.serverless.arklet.core.health.custom;

import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.alipay.sofa.serverless.arklet.core.health.custom.model.CustomPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CustomPluginManagerService implements PluginManagerService {

    private final List<Plugin> pluginList = Arrays.asList(new Plugin[]{
            new CustomPlugin("testPlugin1", "testPluginVersion1"),
            new CustomPlugin("testPlugin2", "testPluginVersion2")
    });

    @Override
    public void registerPlugin(Plugin plugin) {

    }

    @Override
    public Plugin getPluginByName(String s) {
        Plugin plugin = null;
        for (Plugin item: pluginList) {
            if (s.equals(item.getPluginName())) {
                plugin = item;
                break;
            }
        }
        return plugin;
    }

    @Override
    public Set<String> getAllPluginNames() {
        return null;
    }

    @Override
    public List<Plugin> getPluginsInOrder() {
        return pluginList;
    }
}
