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
package com.alipay.sofa.koupleless.ext.web.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

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
    private Forwards            forwards;

    private static final String SEPARATOR         = "/";
    private static final String DOUBLE_SEPARATORS = SEPARATOR + SEPARATOR;

    @RequestMapping("/**")
    public void redirect(HttpServletRequest request, HttpServletResponse response)
                                                                                  throws ServletException,
                                                                                  IOException {

        //定位forward信息
        URI uri = URI.create(request.getRequestURL().toString());
        String host = uri.getHost();
        String sourcePath = uri.getPath();
        if (!StringUtils.hasLength(sourcePath)) {
            sourcePath = Forwards.ROOT_PATH;
        }
        ForwardItem forwardItem = forwards.getForwardItem(host, sourcePath);
        if (forwardItem == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        //计算要跳转的路径
        String contextPath = forwardItem.getContextPath();
        String targetPath = forwardItem.getTo()
                            + sourcePath.substring(forwardItem.getFrom().length());
        if (targetPath.startsWith(DOUBLE_SEPARATORS)) {
            targetPath = targetPath.substring(1);
        }
        ServletContext currentContext = request.getServletContext();
        ServletContext nextContext = currentContext.getContext(contextPath + targetPath);
        if (currentContext == nextContext) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        RequestDispatcher dispatcher = nextContext.getRequestDispatcher(targetPath);
        dispatcher.forward(request, response);
    }
}
