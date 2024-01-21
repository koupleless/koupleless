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
package com.alipay.sofa.koupleless.spring.loader.tools;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

import com.alipay.sofa.koupleless.spring.loader.tools.Layouts.Jar;
import junit.framework.TestCase;
import org.junit.Assert;

import org.springframework.boot.loader.tools.JarWriter;
import org.springframework.boot.loader.tools.Layout;

public class LayoutsTest extends TestCase {

    public void testForNullFile() throws IOException {
        IllegalArgumentException exception = null;
        try {
            Layouts.forFile(null);
        } catch (IllegalArgumentException e) {
            exception = e;
        }
        assertNotNull(exception);
    }

    public void testForFile() throws IOException {
        CustomLayoutFactory customLayoutFactory = new CustomLayoutFactory();

        File appJar = new File(getClass().getClassLoader().getResource("demo-executable.jar")
            .getFile());
        Layout layout = customLayoutFactory.getLayout(appJar);
        assertTrue(layout instanceof Jar);
        Jar jar = (Jar) layout;
        assertEquals("com.alipay.sofa.koupleless.spring.loader.JarLauncher",
            jar.getLauncherClassName());
        File rewrite = new File(appJar.getParent() + "/demo-executable-rewrite.jar");
        JarWriter writer = new JarWriter(rewrite);
        jar.writeLoadedClasses(writer);
        writer.close();
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[] { rewrite.toURI().toURL() },
            null);
        try {
            urlClassLoader.loadClass(jar.getLauncherClassName());
        } catch (ClassNotFoundException e) {
            Assert.fail(e.getMessage());
        }
    }
}
