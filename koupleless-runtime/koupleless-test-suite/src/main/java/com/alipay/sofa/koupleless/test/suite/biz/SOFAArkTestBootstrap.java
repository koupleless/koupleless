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
package com.alipay.sofa.koupleless.test.suite.biz;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.ark.container.registry.ContainerServiceProvider;
import com.alipay.sofa.ark.container.service.ArkServiceContainer;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.loader.JarPluginArchive;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.spi.archive.PluginArchive;
import com.alipay.sofa.ark.spi.model.BizState;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.PriorityOrdered;
import com.alipay.sofa.ark.spi.service.biz.BizFactoryService;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderService;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.injection.InjectionService;
import com.alipay.sofa.ark.spi.service.plugin.PluginDeployService;
import com.alipay.sofa.ark.spi.service.plugin.PluginFactoryService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.alipay.sofa.ark.spi.service.registry.RegistryService;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.alipay.sofa.ark.spi.constant.Constants.MASTER_BIZ;

public class SOFAArkTestBootstrap {

    @Getter
    private static URLClassLoader      baseClassLoader;

    private static ArkServiceContainer INSTANCE            = new ArkServiceContainer(new String[0]);
    private static AtomicBoolean       started             = new AtomicBoolean();
    private static AtomicBoolean       masterBizRegistered = new AtomicBoolean();

    private static List<String>        pluginDependencies  = new ArrayList<>();

    private static boolean isPluginDependency(String path) {
        return pluginDependencies.stream().anyMatch(
                plugin -> StringUtils.contains(path, plugin)
        );
    }

    @SneakyThrows
    public static void setUpPlugins() {
        pluginDependencies.add("web-ark-plugin");
        pluginDependencies.add("koupleless-base-plugin");

        ArkServiceContainer container = ArkServiceContainerHolder.getContainer();
        for (URL url : SOFAArkTestBootstrap.getBaseClassLoader().getURLs()) {
            String path = url.getPath();
            if (isPluginDependency(path)) {
                JarFileArchive archive = new JarFileArchive(new File(url.getFile()));
                PluginArchive pluginArchive = new JarPluginArchive(archive);
                Plugin plugin = container.getService(PluginFactoryService.class).createEmbedPlugin(
                    pluginArchive, Thread.currentThread().getContextClassLoader());

                ArkClient.getPluginManagerService().registerPlugin(plugin);
            }
        }
        container.getService(ClassLoaderService.class).prepareExportClassAndResourceCache();
        container.getService(PluginDeployService.class).deploy();
    }

    public static void publicService() {
        ArkServiceContainer container = ArkServiceContainerHolder.getContainer();
        RegistryService registryService = container.getService(RegistryService.class);
        registryService.publishService(BizManagerService.class, container
            .getService(BizManagerService.class), new ContainerServiceProvider(
            PriorityOrdered.HIGHEST_PRECEDENCE));
        registryService.publishService(ClassLoaderService.class, container
            .getService(ClassLoaderService.class), new ContainerServiceProvider(
            PriorityOrdered.HIGHEST_PRECEDENCE));
        registryService.publishService(InjectionService.class, container
            .getService(InjectionService.class), new ContainerServiceProvider(
            PriorityOrdered.HIGHEST_PRECEDENCE));
        registryService.publishService(BizFactoryService.class, container
            .getService(BizFactoryService.class), new ContainerServiceProvider(
            PriorityOrdered.HIGHEST_PRECEDENCE));
        registryService.publishService(PluginManagerService.class, container
            .getService(PluginManagerService.class), new ContainerServiceProvider(
            PriorityOrdered.HIGHEST_PRECEDENCE));
        registryService.publishService(PluginFactoryService.class, container
            .getService(PluginFactoryService.class), new ContainerServiceProvider(
            PriorityOrdered.HIGHEST_PRECEDENCE));
        registryService.publishService(EventAdminService.class, container
            .getService(EventAdminService.class), new ContainerServiceProvider(
            PriorityOrdered.HIGHEST_PRECEDENCE));
        registryService.publishService(RegistryService.class, container
            .getService(RegistryService.class), new ContainerServiceProvider(
            PriorityOrdered.HIGHEST_PRECEDENCE));
    }

    public static void init(ClassLoader baseClassLoader) {
        if (started.compareAndSet(false, true)) {
            SOFAArkTestBootstrap.baseClassLoader = (URLClassLoader) baseClassLoader;
            INSTANCE.start();
            publicService();
            setUpPlugins();

            ArkServiceContainerHolder.setContainer(INSTANCE);
        }
    }

    public static void registerMasterBiz() {
        if (masterBizRegistered.compareAndSet(false, true)) {
            BizModel bizModel = new BizModel();
            bizModel.setBizName("master biz");
            bizModel.setBizVersion("TEST");
            bizModel.setClassLoader(baseClassLoader);
            bizModel.setBizState(BizState.RESOLVED);
            ArkClient.setMasterBiz(bizModel);
        }
    }
}
