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
package com.alipay.sofa.koupleless.ext.web;

import com.alibaba.fastjson.JSONArray;
import com.alipay.sofa.koupleless.ext.web.gateway.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.server.ResponseStatusException;
import org.yaml.snakeyaml.Yaml;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;

@RunWith(MockitoJUnitRunner.class)
public class ForwardControllerTests {
    @InjectMocks
    private ForwardAutoConfiguration configuration;
    private String                   confPath   = "forwards.yaml";

    private ForwardController        controller = new ForwardController();

    @Before
    public void before() throws NoSuchFieldException, IllegalAccessException, IOException {
        Field field = ForwardAutoConfiguration.class.getDeclaredField("gatewayProperties");
        field.setAccessible(true);
        Yaml yaml = new Yaml();
        JSONArray array = yaml.loadAs(ForwardControllerTests.class.getClassLoader()
            .getResourceAsStream(confPath), JSONArray.class);
        GatewayProperties properties = new GatewayProperties();
        properties.setForwards(array.toJavaList(Forward.class));
        field.set(configuration, properties);

        Forwards forwards = configuration.forwards();
        field = ForwardController.class.getDeclaredField("forwards");
        field.setAccessible(true);
        field.set(controller, forwards);
    }

    @Test
    public void testRedirect() throws IOException, ServletException {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        ServletContext baseContext = Mockito.mock(ServletContext.class);
        Mockito.when(request.getServletContext()).thenReturn(baseContext);

        ServletContext context1 = Mockito.mock(ServletContext.class);
        RequestDispatcher dispatcher1 = Mockito.mock(RequestDispatcher.class);
        Mockito.when(context1.getRequestDispatcher((Mockito.anyString()))).thenReturn(dispatcher1);

        ServletContext context2 = Mockito.mock(ServletContext.class);
        RequestDispatcher dispatcher2 = Mockito.mock(RequestDispatcher.class);
        Mockito.when(context2.getRequestDispatcher((Mockito.anyString()))).thenReturn(dispatcher2);

        ServletContext context3 = Mockito.mock(ServletContext.class);
        RequestDispatcher dispatcher3 = Mockito.mock(RequestDispatcher.class);
        Mockito.when(context3.getRequestDispatcher((Mockito.anyString()))).thenReturn(dispatcher3);

        Mockito.when(baseContext.getContext(Mockito.anyString())).then((invocation) -> {
            String uri = invocation.getArgument(0);
            if (uri.startsWith("/test1/")) {
                return context1;
            }
            if (uri.startsWith("/test2/")) {
                return context2;
            }
            if (uri.startsWith("/test3/")) {
                return context3;
            }
            return baseContext;
        });


        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://test1.xxx.com/test2"));
        controller.redirect(request, response);
        Mockito.verify(dispatcher2, Mockito.times(1)).forward(request, response);


        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://test1.xxx.com/test1/xx"));
        controller.redirect(request, response);
        Mockito.verify(dispatcher1, Mockito.times(1)).forward(request, response);


        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://test3.xxx.com/test1/xx"));
        controller.redirect(request, response);
        Mockito.verify(dispatcher3, Mockito.times(1)).forward(request, response);


        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://test4.xxx.com/test1/xx"));
        Assert.assertThrows(ResponseStatusException.class, () -> controller.redirect(request, response));
    }
}