package com.alipay.sofa.koupleless.test.suite.mojo;

import com.alipay.sofa.koupleless.test.suite.mojo.KouplelessCompatibleTestMojo;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;

import static org.mockito.Mockito.doReturn;

/**
 * @author CodeNoobKing
 * @date 2024/2/20
 */
@ExtendWith(MockitoExtension.class)
public class KouplelessCompatibleTestMojoTest {
    @InjectMocks
    private KouplelessCompatibleTestMojo mojo;

    MavenProject project = new MavenProject();

    @BeforeEach
    public void setUpProject() {
        project.setBuild(new Build());
        URL testDir = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        project.getBuild().setTestOutputDirectory(testDir.getPath());
        URL buildDir = mojo.getClass().getProtectionDomain().getCodeSource().getLocation();
        project.getBuild().setOutputDirectory(buildDir.getPath());

        mojo.project = project;

    }

    @Test
    public void testJunit5() {
        mojo.execute();
    }
}
