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
package com.alipay.sofa.koupleless.arklet.core.health.custom.model;

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
    public String getExportMode() {
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
