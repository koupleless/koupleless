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
package com.alipay.sofa.koupleless.arklet.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Manifest;

/**
 * @author Darkness
 * @version 1.0
 * @date 2024/3/5 20:42
 */
public class ManifestUtil {

    public static Map<String, Object> readProperties(String path) {
        Map<String, Object> result = new HashMap<>();
        try(FileInputStream fileInputStream = new FileInputStream(path)) {
            Manifest manifest = new Manifest(fileInputStream);
            manifest.getMainAttributes().forEach((k, v) -> result.put(k.toString(), v));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
