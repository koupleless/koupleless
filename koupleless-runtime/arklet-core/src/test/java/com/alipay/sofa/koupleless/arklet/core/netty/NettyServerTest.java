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
package com.alipay.sofa.koupleless.arklet.core.netty;

import com.alipay.sofa.koupleless.arklet.core.BaseTest;
import com.alipay.sofa.koupleless.arklet.core.api.tunnel.http.netty.NettyHttpServer;
import com.alipay.sofa.koupleless.arklet.core.api.tunnel.http.netty.NettyHttpServer.NettyHttpHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Test;
import org.mockito.BDDMockito;
import org.mockito.Mock;

/**
 * @author mingmen
 * @date 2023/11/6
 */
public class NettyServerTest extends BaseTest {

    @Mock
    ChannelHandlerContext mockCtx = BDDMockito.mock(ChannelHandlerContext.class);

    @Test
    public void command() throws Exception {
        NettyHttpHandler handler = new NettyHttpHandler(commandService);
        String content = "{\"bizName\":\"test\",\"bizVersion\":\"1.0.0\",\"bizUrl\":\"http://serverless-opensource"
                         + ".oss-cn-shanghai.aliyuncs.com/module-packages/stable/dynamic-provider-1.0.0-ark-biz.jar\"}";
        DefaultFullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1,
            HttpMethod.POST, "/installBiz", Unpooled.copiedBuffer(content.getBytes()));
        handler.channelRead(mockCtx, fullHttpRequest);
    }

    @Test
    public void open() throws InterruptedException {
        NettyHttpServer server = new NettyHttpServer(1239, commandService);
        Channel channel = server.getChannel();
        server.open();
    }

}
