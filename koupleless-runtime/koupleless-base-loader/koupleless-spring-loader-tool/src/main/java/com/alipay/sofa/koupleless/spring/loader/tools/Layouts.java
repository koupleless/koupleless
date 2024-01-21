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
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.boot.loader.tools.CustomLoaderLayout;
import org.springframework.boot.loader.tools.Layout;
import org.springframework.boot.loader.tools.LibraryScope;
import org.springframework.boot.loader.tools.LoaderClassesWriter;

/**
 * Custom Layouts
 * @author zjulbj
 * @daye 2023/12/26
 * @version Layouts.java, v 0.1 2023年12月26日 14:45 syd
 */
public class Layouts {
    private Layouts() {
    }

    /**
     * Return a layout for the given source file.
     *
     * @param file the source file
     * @return a {@link Layout}
     */
    public static Layout forFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null");
        }
        String lowerCaseFileName = file.getName().toLowerCase(Locale.ENGLISH);
        if (lowerCaseFileName.endsWith(".jar")) {
            return new Jar();
        }
        return org.springframework.boot.loader.tools.Layouts.forFile(file);
    }

    /**
     * Executable JAR layout.
     */
    public static class Jar extends org.springframework.boot.loader.tools.Layouts.Jar implements
                                                                                     CustomLoaderLayout {
        @Override
        public String getLauncherClassName() {
            return "com.alipay.sofa.koupleless.spring.loader.JarLauncher";
        }

        @Override
        public void writeLoadedClasses(LoaderClassesWriter writer) throws IOException {
            writer.writeLoaderClasses("META-INF/loader/spring-boot-loader.jar");
            writer.writeLoaderClasses("META-INF/loader/koupleless-spring-loader.jar");
        }
    }
}
