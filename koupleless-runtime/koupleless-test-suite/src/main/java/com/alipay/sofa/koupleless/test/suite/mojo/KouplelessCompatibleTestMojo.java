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
package com.alipay.sofa.koupleless.test.suite.mojo;

import com.alipay.sofa.koupleless.test.suite.biz.SOFAArkTestBootstrap;
import com.alipay.sofa.koupleless.test.suite.biz.SOFAArkTestBiz;
import com.alipay.sofa.koupleless.test.suite.model.CompatibleTestBizConfig;
import com.alipay.sofa.koupleless.test.suite.model.CompatibleTestConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

/**
 * @author CodeNoobKing
 * @date 2024/1/15
 */
@Mojo(name = "compatible-test", defaultPhase = LifecyclePhase.INTEGRATION_TEST, requiresDependencyResolution = ResolutionScope.COMPILE)
public class KouplelessCompatibleTestMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    MavenProject         project;

    @Parameter(property = "compatibleTestConfigFile", defaultValue = "sofa-ark-compatible-test-config.yaml")
    String               compatibleTestConfigFile = "sofa-ark-compatible-test-config.yaml";

    private ObjectMapper yamlObjectMapper         = new ObjectMapper(new YAMLFactory())
                                                      .configure(
                                                          DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                                                          false);

    @SneakyThrows
    public URLClassLoader buildURLClassLoader() {
        List<URL> urls = new ArrayList<>();
        urls.add(new File(project.getBuild().getTestOutputDirectory()).toURI().toURL());
        urls.add(new File(project.getBuild().getOutputDirectory()).toURI().toURL());
        for (Artifact artifact : project.getArtifacts()) {
            urls.add(artifact.getFile().toURI().toURL());
        }

        for (URL url : urls) {
            getLog().debug(String.format("%s, BaseClassLoaderUrl", url));
        }

        return new URLClassLoader(urls.toArray(new URL[0]),
        // this is necessary because we are calling test engine programmatically.
        // some classes are required to be loaded by TCCL even when we are setting TCCL to biz classLoader.
            Thread.currentThread().getContextClassLoader());
    }

    @SneakyThrows
    private CompatibleTestConfig loadConfigs() {
        return yamlObjectMapper.readValue(
            Paths.get(project.getBuild().getTestOutputDirectory(), compatibleTestConfigFile)
                .toUri().toURL(), new TypeReference<CompatibleTestConfig>() {
            });
    }

    private List<SOFAArkTestBiz> buildTestBiz(URLClassLoader baseClassLoader) {
        CompatibleTestConfig configs = loadConfigs();

        // if root project classes is not configured to include by class loader
        // then it mused be loaded by base classloader
        List<String> rootProjectClasses = new ArrayList<>();
        rootProjectClasses.add(Paths.get(project.getBuild().getOutputDirectory()).toAbsolutePath()
            .toString());

        rootProjectClasses.add(Paths.get(project.getBuild().getTestOutputDirectory())
            .toAbsolutePath().toString());

        List<SOFAArkTestBiz> result = new ArrayList<>();
        for (CompatibleTestBizConfig config : CollectionUtils.emptyIfNull(configs
            .getTestBizConfigs())) {

            SOFAArkTestBiz testBiz = new SOFAArkTestBiz(config.getBootstrapClass(),
                config.getName(), project.getVersion(), ListUtils.emptyIfNull(config
                    .getTestClasses()), ListUtils.union(
                    ListUtils.emptyIfNull(config.getLoadByBizClassLoaderPatterns()),
                    rootProjectClasses), baseClassLoader);

            result.add(testBiz);
        }
        return result;
    }

    @SneakyThrows
    public void executeJunit4() {
        URLClassLoader baseClassLoader = buildURLClassLoader();
        SOFAArkTestBootstrap.init(baseClassLoader);
        SOFAArkTestBootstrap.registerMasterBiz();
        List<SOFAArkTestBiz> sofaArkTestBizs = buildTestBiz(baseClassLoader);
        for (SOFAArkTestBiz sofaArkTestBiz : sofaArkTestBizs) {
            getLog().info(String.format("%s, CompatibleTestStarted", sofaArkTestBiz.getIdentity()));

            sofaArkTestBiz.executeTest(new Runnable() {
                @Override
                @SneakyThrows
                public void run() {
                    List<Class<?>> testClasses = sofaArkTestBiz.getTestClasses();
                    Result result = JUnitCore.runClasses(testClasses.toArray(new Class[0]));
                    getLog().info(
                        String.format("%s, CompatibleTestFinished", sofaArkTestBiz.getIdentity()));

                    Preconditions.checkState(result.wasSuccessful(),
                        "Test failed: " + result.getFailures());
                }
            }).get();
        }
    }

    public void execute() {
        executeJunit4();
    }
}
