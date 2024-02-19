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
package com.alipay.sofa.koupleless.maven.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.alipay.sofa.koupleless.maven.plugin.common.JarFileUtils;
import com.alipay.sofa.koupleless.maven.plugin.model.KouplelessAdapterConfig;
import com.alipay.sofa.koupleless.maven.plugin.model.MavenDependencyAdapterMapping;
import com.alipay.sofa.koupleless.maven.plugin.model.MavenDependencyMatcher;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 * @phase process-sources
 */
@Mojo(name = "add-patch", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class KouplelessBaseBuildPrePackageMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}", readonly = true)
    File                    outputDirectory;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject            project;

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    MavenSession            session;

    @Component
    RepositorySystem        repositorySystem;

    private ObjectMapper    yamlMapper = new ObjectMapper(new YAMLFactory());

    KouplelessAdapterConfig kouplelessAdapterConfig;

    void initKouplelessAdapterConfig() throws Exception {
        if (kouplelessAdapterConfig == null) {
            InputStream mappingConfigIS = this.getClass().getClassLoader()
                .getResourceAsStream("adapter-mapping.yaml");

            kouplelessAdapterConfig = yamlMapper.readValue(mappingConfigIS,
                KouplelessAdapterConfig.class);
        }
    }

    String getDependencyId(Dependency dependency) {
        return dependency.getGroupId() + ":" + dependency.getArtifactId() + ":"
               + dependency.getVersion() + ":" + dependency.getType()
               + (dependency.getClassifier() != null ? ":" + dependency.getClassifier() : "");
    }

    // visible for testing
    List<Dependency> getDependenciesToAdd() {
        List<Dependency> adapterDependencies = new ArrayList<>();
        if (kouplelessAdapterConfig == null) {
            getLog().info("kouplelessAdapterConfig is null, skip adding dependencies.");
            return adapterDependencies;
        }

        if (kouplelessAdapterConfig.getCommonDependencies() != null) {
            adapterDependencies.addAll(kouplelessAdapterConfig.getCommonDependencies());
        }

        Collection<MavenDependencyAdapterMapping> adapterMappings = CollectionUtils
            .emptyIfNull(kouplelessAdapterConfig.getAdapterMappings());
        for (MavenDependencyAdapterMapping adapterMapping : adapterMappings) {
            MavenDependencyMatcher matcher = adapterMapping.getMatcher();

            if (matcher != null && matcher.getRegexp() != null) {
                String regexp = matcher.getRegexp();
                for (Dependency dependency : project.getDependencies()) {
                    String dependencyId = getDependencyId(dependency);
                    if (Pattern.compile(regexp).matcher(dependencyId).matches()) {
                        adapterDependencies.add(adapterMapping.getAdapter());
                    }
                }
            }

        }
        return adapterDependencies;
    }

    void addDependenciesDynamically() {
        if (kouplelessAdapterConfig == null) {
            getLog().info("kouplelessAdapterConfig is null, skip adding dependencies.");
            return;
        }

        Collection<Dependency> commonDependencies = getDependenciesToAdd();
        for (Dependency dependency : commonDependencies) {
            try {
                getLog().debug("start downloading dependency: " + dependency.toString());
                Artifact artifact = downloadAdapterDependency(dependency);
                getLog().debug("start add dependency to project root: " + dependency.toString());
                addArtifactToProjectRoot(artifact);
                getLog().info("success add dependency: " + dependency.toString());
            } catch (Throwable t) {
                getLog().error("error add dependency: " + dependency.toString(), t);
            }
        }
    }

    Artifact downloadAdapterDependency(Dependency dependency) {
        DefaultArtifact patchArtifact = new DefaultArtifact(dependency.getGroupId() + ":"
                                                            + dependency.getArtifactId() + ":"
                                                            + dependency.getVersion());

        try {
            ArtifactRequest artifactRequest = new ArtifactRequest().setArtifact(patchArtifact)
                .setRepositories(project.getRemoteProjectRepositories());

            ArtifactResult artifactResult = repositorySystem.resolveArtifact(
                session.getRepositorySession(), artifactRequest);

            Preconditions.checkState(artifactResult.isResolved(), "artifact not resolved.");
            return artifactResult.getArtifact();
        } catch (Throwable t) {
            getLog().error(t);
            throw new RuntimeException(t);
        }
    }

    void addArtifactToProjectRoot(Artifact artifact) {
        File file = artifact.getFile();
        Map<String, Byte[]> entryToContent = JarFileUtils.getFileContentAsLines(file,
            Pattern.compile(".*\\.class$"));
        for (Map.Entry<String, Byte[]> entry : entryToContent.entrySet()) {
            addPatchToProjectRoot(entry.getKey(), entry.getValue());
        }
    }

    @SneakyThrows
    void addPatchToProjectRoot(String entryName, Byte[] bytes) {
        Path outputfile = Paths.get(outputDirectory.getAbsolutePath(), "classes", entryName);
        Path parentDir = outputfile.getParent();
        byte[] primitiveByte = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            primitiveByte[i] = bytes[i];
        }
        Files.createDirectories(parentDir);
        Files.write(outputfile, primitiveByte);
    }

    @Override
    public void execute() throws MojoExecutionException {
        try {
            initKouplelessAdapterConfig();
            addDependenciesDynamically();
        } catch (Throwable t) {
            getLog().error(t);
            throw new RuntimeException(t);
        }
    }
}
