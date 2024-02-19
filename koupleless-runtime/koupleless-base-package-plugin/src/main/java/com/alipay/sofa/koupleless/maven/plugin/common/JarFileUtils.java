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
package com.alipay.sofa.koupleless.maven.plugin.common;

import lombok.SneakyThrows;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Pattern;

/**
 * jar file utils
 *
 * @author CodeNoobKing
 */
public class JarFileUtils {

    /**
     * get content as file lines inside a bundled jar file.
     *
     * @param file         jarfile
     * @param entryPattern the target file.
     * @return the content as list of string.
     */
    @SneakyThrows
    public static Map<String, Byte[]> getFileContentAsLines(File file, Pattern entryPattern) {
        Map<String, Byte[]> result = new HashMap<>();
        try (JarInputStream jin = new JarInputStream(new FileInputStream(file))) {

            JarEntry entry = null;
            while ((entry = jin.getNextJarEntry()) != null) {
                if (!entryPattern.matcher(entry.getName()).matches() || entry.isDirectory()) {
                    continue;
                }

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead = -1;
                byte[] data = new byte[1024];
                while ((nRead = jin.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }

                byte[] byteArray = buffer.toByteArray();
                Byte[] boxing = new Byte[byteArray.length];
                for (int i = 0; i < byteArray.length; i++) {
                    boxing[i] = byteArray[i];
                }
                result.put(entry.getName(), boxing);
            }
        }
        return result;
    }

}
