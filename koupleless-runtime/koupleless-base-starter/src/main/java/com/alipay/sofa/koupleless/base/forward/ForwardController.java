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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

@Controller
@RequestMapping
public class ForwardController {
    @Autowired
    private Forwards forwards;

    @RequestMapping("/**")
    public void redirect(HttpServletRequest request, HttpServletResponse response)
                                                                                  throws ServletException,
                                                                                  IOException {
        String path = request.getServletPath();
        URI uri = URI.create(request.getRequestURL().toString());
        String contextPath = forwards.getContextPath(uri);
        ServletContext currentContext = request.getServletContext();
        ServletContext nextContext = currentContext.getContext(contextPath + path);
        if (currentContext == nextContext) {
            throw new IllegalArgumentException("No match biz for path:" + path);
        }
        RequestDispatcher dispatcher = nextContext.getRequestDispatcher(path);
        dispatcher.forward(request, response);
    }
}
