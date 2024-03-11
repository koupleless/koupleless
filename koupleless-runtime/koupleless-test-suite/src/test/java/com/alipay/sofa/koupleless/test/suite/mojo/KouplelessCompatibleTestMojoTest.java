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

import com.alipay.sofa.koupleless.test.suite.mock.TestJunit4Class;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URL;

/**
 * @author CodeNoobKing
 * @date 2024/2/20
 */
@RunWith(MockitoJUnitRunner.class)
public class KouplelessCompatibleTestMojoTest {
    @InjectMocks
    private KouplelessCompatibleTestMojo mojo;

    MavenProject                         project = new MavenProject();

    @Before
    public void setUpProject() {
        project.setBuild(new Build());
        URL testDir = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        project.getBuild().setTestOutputDirectory(testDir.getPath());
        URL buildDir = mojo.getClass().getProtectionDomain().getCodeSource().getLocation();
        project.getBuild().setOutputDirectory(buildDir.getPath());

        mojo.project = project;

    }

    @Test
    public void testJunit4() throws Throwable {
        mojo.execute();
    }
}
