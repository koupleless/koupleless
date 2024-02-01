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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 *
 * @author CodeNoobKingKc2
 * @version $Id: BatchInstallHelper, v 0.1 2023-11-22 10:59 CodeNoobKingKc2 Exp $
 */
@RunWith(MockitoJUnitRunner.class)
public class BatchInstallHelperTest {
    @InjectMocks
    private BatchInstallHelper batchInstallHelper;

    @Test
    public void testGetBizUrlsFromLocalFileSystem() {
        try (MockedStatic<Files> mockedStatic = Mockito.mockStatic(Files.class)) {
            try (MockedConstruction<JarFile> mocked = Mockito.mockConstruction(JarFile.class, (mock, context) -> {
                // You can check the arguments passed to the constructor
                if (context.arguments().size() == 1 && context.arguments().get(0).equals("/path/a-biz.jar")) {
                    Attributes mattr = mock(Attributes.class);
                    doAnswer(new Answer() {
                        @Override
                        public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                            BiConsumer biConsumer = invocationOnMock.getArgument(0, BiConsumer.class);
                            biConsumer.accept("Ark-Biz-Name", "true");
                            return null;
                        }
                    }).when(mattr).forEach(any());
                    Manifest mockedManifest = mock(Manifest.class);
                    doReturn(mattr).when(mockedManifest).getMainAttributes();
                    doReturn(mockedManifest).when(mock).getManifest();
                }

                if (context.arguments().size() == 1 && context.arguments().get(0).equals("/path/b-biz.jar")) {
                    Attributes mattr = mock(Attributes.class);
                    doAnswer(new Answer() {
                        @Override
                        public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                            BiConsumer biConsumer = invocationOnMock.getArgument(0, BiConsumer.class);
                            biConsumer.accept("Ark-Biz-Name", "true");
                            return null;
                        }
                    }).when(mattr).forEach(any());
                    Manifest mockedManifest = mock(Manifest.class);
                    doReturn(mattr).when(mockedManifest).getMainAttributes();
                    doReturn(mockedManifest).when(mock).getManifest();
                }

                if (context.arguments().size() == 1 && context.arguments().get(0).equals("/path/noop.jar")) {
                    Attributes mattr = mock(Attributes.class);
                    Manifest mockedManifest = mock(Manifest.class);
                    doReturn(mattr).when(mockedManifest).getMainAttributes();
                    doReturn(mockedManifest).when(mock).getManifest();
                }
            })) {
                mockedStatic.when(() -> Files.walkFileTree(any(), any())).thenAnswer(new Answer<Object>() {
                    @Override
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {

                        Path path0 = mock(Path.class);
                        doReturn("/path/a-biz.jar").when(path0).toString();
                        doReturn(path0).when(path0).toAbsolutePath();
                        Path path1 = mock(Path.class);
                        doReturn("/path/b-biz.jar").when(path1).toString();
                        doReturn(path1).when(path1).toAbsolutePath();
                        Path path2 = mock(Path.class);
                        doReturn("/path/noop.jar").when(path2).toString();
                        doReturn(path2).when(path2).toAbsolutePath();

                        Path path3 = mock(Path.class);
                        doReturn("/path/noopjar").when(path3).toString();
                        doReturn(path3).when(path3).toAbsolutePath();

                        FileVisitor<Path> visitor = invocationOnMock.getArgument(1, FileVisitor.class);
                        visitor.visitFile(path0, null);
                        visitor.visitFile(path1, null);
                        visitor.visitFile(path2, null);
                        visitor.visitFile(path3, null);
                        return null;
                    }
                });

                List<String> bizUrls = batchInstallHelper.getBizUrlsFromLocalFileSystem("/path/to/jar/file");
                List<String> expected = new ArrayList<>();
                expected.add("/path/a-biz.jar");
                expected.add("/path/b-biz.jar");
                Assert.assertEquals(expected, bizUrls);
            }

        }
    }

    @Test
    public void testGetManAttributes() {
        try (MockedConstruction<JarFile> mocked = Mockito.mockConstruction(JarFile.class, (mock, context) -> {
            // You can check the arguments passed to the constructor
            if (context.arguments().size() == 1 && context.arguments().get(0).equals("/path/to/jar/file")) {
                Attributes mattr = mock(Attributes.class);
                doAnswer(new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                        BiConsumer biConsumer = invocationOnMock.getArgument(0, BiConsumer.class);
                        biConsumer.accept("foo", "bar");
                        return null;
                    }
                }).when(mattr).forEach(any());

                Manifest mockedManifest = mock(Manifest.class);

                doReturn(mattr).when(mockedManifest).getMainAttributes();
                doReturn(mockedManifest).when(mock).getManifest();
            }
        })) {
            Map<String, Object> mainAttributes = batchInstallHelper.getMainAttributes("/path/to/jar/file");
            Assert.assertEquals("bar", mainAttributes.get("foo"));
        }
    }
}
