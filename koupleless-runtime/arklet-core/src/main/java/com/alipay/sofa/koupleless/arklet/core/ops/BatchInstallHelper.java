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
package com.alipay.sofa.koupleless.arklet.core.ops;

import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.core.util.ManifestUtil;
import com.alipay.sofa.koupleless.common.util.OSUtils;
import com.google.common.base.Preconditions;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * 合并部署帮助类。
 * @author CodeNoobKingKc2
 * @version $Id: BatchInstallService, v 0.1 2023-11-20 15:35 CodeNoobKingKc2 Exp $
 */
public class BatchInstallHelper {

    // mark标记文件
    private static final String MarkFilePath = "/com/alipay/sofa/ark/biz/mark";

    /**
     * 判断是否是 biz jar 文件
     * 目前简单的以后缀 '-biz.jar' 为约束。
     * @param path 文件路径。
     * @return 是否是 biz jar 文件。
     */
    public boolean isBizJarFile(Path path) {
        if (path.toString().endsWith(".jar")) {
            Map<String, Object> attributes = getMainAttributes(path.toString());
            return attributes.containsKey("Ark-Biz-Name");
        } else {
            return false;
        }
    }

    private String manifestFilePath(String folder) {
        return folder + File.separator + "META-INF" + File.separator + "MANIFEST.MF";
    }

    /**
     * 判断是否是 biz 文件夹
     * 目前简单的以后缀 '-biz.jar' 为约束。
     * @param path 文件路径。
     * @return 是否是 biz jar 文件。
     */
    public boolean isBizFolderFile(String path) {
        String manifestFilePath = manifestFilePath(path);
        Map<String, Object> manifestProperties = ManifestUtil.readProperties(manifestFilePath);
        return manifestProperties.containsKey("Ark-Biz-Name");
    }

    private static String pathToPackageName(String path) {
        return path.replaceAll("\\\\", "/").replaceAll("/", ".");
    }

    private static String convert2linuxPath(String path) {
        return path.replaceAll("\\\\", "/");
    }

    @SneakyThrows
    public List<String> getBizUrlsFromLocalFileSystem(String absoluteBizDirPaths) {
        List<String> bizUrls = new ArrayList<>();
        // 支持 ; 分割配置多个文件路径
        String[] absoluteBizDirPathList = absoluteBizDirPaths.split(";");
        for (String absoluteBizDirPath : absoluteBizDirPathList) {
            Files.walkFileTree(Paths.get(absoluteBizDirPath), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    Path absolutePath = file.toAbsolutePath();

                    if (isBizJarFile(absolutePath)) {
                        ArkletLoggerFactory.getDefaultLogger().info("Found biz jar file: {}",
                            absolutePath);
                        bizUrls.add(absolutePath.toString());
                    } else {
                        String pathStr = convert2linuxPath(absolutePath.toString());
                        if (pathStr.endsWith(MarkFilePath)) {
                            String bizFolder = pathStr.replace(MarkFilePath, "");
                            if (isBizFolderFile(bizFolder)) {
                                ArkletLoggerFactory.getDefaultLogger().info(
                                    "Found biz folder file: {}", bizFolder);
                                bizUrls.add(bizFolder);
                            }
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        }

        return bizUrls;
    }

    /**
     * 获取 biz jar 文件的主属性。
     * @param bizUrl biz jar 文件路径。
     * @return 主属性。
     */
    @SneakyThrows
    public Map<String, Object> getMainAttributes(String bizUrl) {
        if (bizUrl.endsWith(".jar")) {
            bizUrl = bizUrl.replace(OSUtils.getLocalFileProtocolPrefix(),"");
            try (JarFile jarFile = new JarFile(bizUrl)) {
                Manifest manifest = jarFile.getManifest();
                Preconditions.checkState(manifest != null, "Manifest file not found in the JAR.");
                Map<String, Object> result = new HashMap<>();
                manifest.getMainAttributes().forEach((k, v) -> result.put(k.toString(), v));
                return result;
            }
        } else {
           return ManifestUtil.readProperties(manifestFilePath(bizUrl));
        }
    }
}
