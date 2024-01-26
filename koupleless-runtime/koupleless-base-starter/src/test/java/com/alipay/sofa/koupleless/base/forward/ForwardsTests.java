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
package com.alipay.sofa.koupleless.base.forward;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Comparator;

@RunWith(MockitoJUnitRunner.class)
public class ForwardsTests {
    @InjectMocks
    private ForwardAutoConfiguration configuration;

    @Spy
    private Comparator<ForwardItem>  forwardItemComparator = new DefaultForwardItemComparator();
    @Mock
    private ApplicationContext       applicationContext;
    private String                   confPath              = "classpath:forwards.yaml";

    @Before
    public void before() throws NoSuchFieldException, IllegalAccessException, IOException {
        Field field = ForwardAutoConfiguration.class.getDeclaredField("confPath");
        field.setAccessible(true);
        field.set(configuration, confPath);

        Mockito.when(applicationContext.getResources(Mockito.anyString())).thenReturn(
            new Resource[] { new ClassPathResource("forwards.yaml") });
    }

    @Test
    public void testForwards() throws IOException {
        Forwards forwards = configuration.forwards();
        Assert.assertNotNull(forwards);
        URI uri = URI.create("http://test1.xxx.com/test2");
        String contextPath = forwards.getContextPath(uri);
        Assert.assertEquals(contextPath, "/test2");


        uri = URI.create("http://test1.xxx.com/test1");
        contextPath = forwards.getContextPath(uri);
        Assert.assertEquals(contextPath, "/test1");

        uri = URI.create("http://test3.xxx.com/test1");
        contextPath = forwards.getContextPath(uri);
        Assert.assertEquals(contextPath, "/test3");

        uri = URI.create("http://test4.xxx.com/test1");

        URI errorUri = uri;
        Assert.assertThrows(IllegalArgumentException.class, () -> forwards.getContextPath(errorUri));
    }
}