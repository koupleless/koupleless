/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2023 All Rights Reserved.
 */
package com.alipay.sofa.serverless.arklet.core.ops;

import com.alipay.sofa.ark.api.ArkClient;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 *
 * @author gouzhendong.gzd
 * @version $Id: CombineInstallHelper, v 0.1 2023-11-22 10:59 gouzhendong.gzd Exp $
 */
@RunWith(MockitoJUnitRunner.class)
public class CombineInstallHelperTest {
    @InjectMocks
    private CombineInstallHelper combineInstallHelper;

    @Test
    public void testGetBizUrlsFromLocalFileSystem() {
        try (MockedStatic<Files> mockedStatic = Mockito.mockStatic(Files.class)) {
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

                    FileVisitor<Path> visitor = invocationOnMock.getArgument(1, FileVisitor.class);
                    visitor.visitFile(path0, null);
                    visitor.visitFile(path1, null);
                    visitor.visitFile(path2, null);
                    return null;
                }
            });

            List<String> bizUrls = combineInstallHelper.getBizUrlsFromLocalFileSystem("/path/to/jar/file");
            List<String> expected = new ArrayList<>();
            expected.add("/path/a-biz.jar");
            expected.add("/path/b-biz.jar");
            Assert.assertEquals(expected, bizUrls);
        }
    }

    @Test
    public void testGetManAttributes() {
        try (MockedConstruction<JarFile> mocked = Mockito.mockConstruction(JarFile.class, (mock, context) -> {
            // You can check the arguments passed to the constructor
            if (context.arguments().size() == 1 && context.arguments().get(0).equals("/path/to/jar/file")) {
                Attributes mattr = mock(Attributes.class);
                doReturn("bar").when(mattr).get("foo");

                Manifest mockedManifest = mock(Manifest.class);

                doReturn(mattr).when(mockedManifest).getMainAttributes();
                doReturn(mockedManifest).when(mock).getManifest();
            }
        })) {
            Map<Object, Object> mainAttributes = combineInstallHelper.getMainAttributes("/path/to/jar/file");
            Assert.assertEquals("bar", mainAttributes.get("foo"));
        }
    }

}
