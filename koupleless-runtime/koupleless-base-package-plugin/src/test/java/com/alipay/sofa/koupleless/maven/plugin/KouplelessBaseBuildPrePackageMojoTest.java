package com.alipay.sofa.koupleless.maven.plugin;

import com.alipay.sofa.koupleless.maven.plugin.model.KouplelessAdapterConfig;
import com.alipay.sofa.koupleless.maven.plugin.model.MavenDependencyAdapterMapping;
import com.alipay.sofa.koupleless.maven.plugin.model.MavenDependencyMatcher;
import lombok.SneakyThrows;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * @author CodeNoobKing
 * @date 2024/2/19
 */
@RunWith(MockitoJUnitRunner.class)
public class KouplelessBaseBuildPrePackageMojoTest {
    @InjectMocks
    private KouplelessBaseBuildPrePackageMojo mojo;

    // create a tmp directory using os tmp
    private File outputDirectory = null;

    @Mock
    MavenProject project;

    @Mock
    MavenSession session;

    @Mock
    RepositorySystem repositorySystem;

    @Test
    public void testLazyInitKouplelessAdapterConfig() throws Exception {
        Dependency mockDependency = new Dependency();
        mockDependency.setGroupId("XXX");
        mockDependency.setArtifactId("YYY");
        mockDependency.setVersion("ZZZ");

        List<Dependency> commonDependencies = new ArrayList<>();
        commonDependencies.add(mockDependency);

        List<MavenDependencyAdapterMapping> mappings = new ArrayList<>();
        mappings.add(MavenDependencyAdapterMapping
                .builder()
                .adapter(mockDependency)
                .matcher(MavenDependencyMatcher
                        .builder()
                        .regexp(".*")
                        .build())
                .build());

        mojo.initKouplelessAdapterConfig();
        KouplelessAdapterConfig expected = KouplelessAdapterConfig
                .builder()
                .commonDependencies(commonDependencies)
                .adapterMappings(mappings)
                .build();

        Assert.assertEquals(
                expected.toString(),
                mojo.kouplelessAdapterConfig.toString()
        );
    }

    @Test
    public void testAddDependencyDynamically() throws Exception {
        {
            // init the adapter config
            Dependency mockDependency = new Dependency();
            mockDependency.setGroupId("XXX");
            mockDependency.setArtifactId("YYY");
            mockDependency.setVersion("ZZZ");

            List<Dependency> commonDependencies = new ArrayList<>();
            commonDependencies.add(mockDependency);

            List<MavenDependencyAdapterMapping> mappings = new ArrayList<>();
            mappings.add(MavenDependencyAdapterMapping
                    .builder()
                    .adapter(mockDependency)
                    .matcher(MavenDependencyMatcher
                            .builder()
                            .regexp(".*A:B:C.*")
                            .build())
                    .build());

            mojo.kouplelessAdapterConfig = KouplelessAdapterConfig
                    .builder()
                    .commonDependencies(commonDependencies)
                    .adapterMappings(mappings)
                    .build();
        }

        {
            // init maven project
            List<Dependency> dependencies = new ArrayList<>();
            Dependency mockDependency = new Dependency();
            mockDependency.setGroupId("A");
            mockDependency.setArtifactId("B");
            mockDependency.setVersion("C");
            dependencies.add(mockDependency);

            doReturn(dependencies)
                    .when(project)
                    .getDependencies();
        }

        {
            // mock the repository system
            ArtifactResult mockArtifactResult = new ArtifactResult(new ArtifactRequest());
            Artifact mockArtifact = mock(Artifact.class);
            mockArtifactResult.setArtifact(mockArtifact);

            URL demoJarUrl = getClass()
                    .getClassLoader()
                    .getResource("demo.jar");
            doReturn(new File(demoJarUrl.toURI()))
                    .when(mockArtifact)
                    .getFile();
            doReturn(mockArtifactResult)
                    .when(repositorySystem)
                    .resolveArtifact(any(), any());
        }

        {
            // init output directory
            mojo.outputDirectory = Files.createTempDirectory("mojotest").toFile();
        }

        mojo.execute();

        {
            Assert.assertTrue(
                    Paths.get(mojo.outputDirectory.getAbsolutePath(),
                                    "classes",
                                    "com",
                                    "example",
                                    "demo")
                            .toFile()
                            .exists()
            );
        }
    }

}
