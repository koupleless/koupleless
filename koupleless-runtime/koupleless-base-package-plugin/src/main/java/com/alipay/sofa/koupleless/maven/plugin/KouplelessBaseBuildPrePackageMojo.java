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
import com.google.common.base.Preconditions;
import lombok.SneakyThrows;
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
    private File outputDirectory;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(defaultValue = "${session}", required = true, readonly = true)
    MavenSession session;

    @Component
    RepositorySystem repositorySystem;

    private ObjectMapper

    KouplelessAdapterConfig kouplelessAdapterConfig;

    private void initKouplelessAdapterConfig() {
        InputStream mappingConfig = this.getClass().getClassLoader().getResourceAsStream("adapter-mapping.yaml");
        try {

        } catch (Throwable t) {
            getLog().error(t);
            throw new RuntimeException(t);
        }
    }

    private void lazyInit() {
        InputStream mappingConfig = this.getClass().getClassLoader().getResourceAsStream("adapter-mapping.yaml");
    }

    private Artifact downloadAdapterDependency(
            String groupId,
            String artifactId,
            String version) {
        DefaultArtifact patchArtifact = new DefaultArtifact(groupId + ":" + artifactId + ":" + version);
        try {
            ArtifactRequest artifactRequest = new ArtifactRequest()
                    .setArtifact(patchArtifact)
                    .setRepositories(project.getRemoteProjectRepositories());

            ArtifactResult artifactResult = repositorySystem.resolveArtifact(session.getRepositorySession(), artifactRequest);
            Preconditions.checkState(artifactResult.isResolved(), "artifact not resolved.");
            return artifactResult.getArtifact();
        } catch (Throwable t) {
            getLog().error(t);
            throw new RuntimeException(t);
        }
    }

    @SneakyThrows
    private void addPatchToProjectRoot(String entryName, Byte[] bytes) {
        Path outputfile = Paths.get(outputDirectory.getAbsolutePath(), "classes", entryName);
        Path parentDir = outputfile.getParent();
        byte[] primitiveByte = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            primitiveByte[i] = bytes[i];
        }
        Files.createDirectories(parentDir);
        Files.write(outputfile, primitiveByte);
        getLog().info("patched added " + outputfile.toAbsolutePath().toString());
    }

    @Override
    public void execute() throws MojoExecutionException {
        try {
            File file = downloadAdapterDependency(
                    "com.alipay.sofa.koupleless",
                    "koupleless-adapter-dubbo-2.6",
                    "0.5.7-SNAPSHOT"
            ).getFile();
            Map<String, Byte[]> entryToContent = JarFileUtils.getFileContentAsLines(file, Pattern.compile(".*\\.class$"));
            for (Map.Entry<String, Byte[]> entry : entryToContent.entrySet()) {
                addPatchToProjectRoot(entry.getKey(), entry.getValue());
            }
            getLog().info("downloaded adapter dependency to " + file.getAbsolutePath().toString());
            //addDependencyToFirst();
        } catch (Throwable t) {
            getLog().error(t);
        }
    }
}
