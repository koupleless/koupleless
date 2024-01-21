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
package com.alipay.sofa.koupleless.arklet.core.api.tunnel.http.netty;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;

import com.alipay.sofa.koupleless.arklet.core.api.model.Response;
import com.alipay.sofa.koupleless.arklet.core.command.CommandService;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Output;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLogger;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.core.util.ExceptionUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

/**
 * @author mingmen
 * @date 2023/6/8
 */

@SuppressWarnings("unchecked")
public class NettyHttpServer {

    private final int                 port;
    private final EventLoopGroup      bossGroup;
    private final EventLoopGroup      workerGroup;
    private Channel                   channel;

    private final CommandService      commandService;
    private static final ArkletLogger LOGGER = ArkletLoggerFactory.getDefaultLogger();

    public NettyHttpServer(int port, CommandService commandService) {
        this.port = port;
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        this.commandService = commandService;
    }

    public Channel getChannel() {
        return channel;
    }

    public void open() throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
            .handler(new LoggingHandler(LogLevel.INFO))
            .childHandler(new NettyHttpInitializer(commandService));
        channel = serverBootstrap.bind(port).sync().channel();
    }

    public void close() {
        channel.close();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    static class NettyHttpInitializer extends ChannelInitializer<SocketChannel> {

        private final CommandService commandService;

        public NettyHttpInitializer(CommandService commandService) {
            this.commandService = commandService;
        }

        @Override
        protected void initChannel(SocketChannel channel) throws Exception {
            ChannelPipeline pipeline = channel.pipeline();
            pipeline.addLast(new HttpResponseEncoder());
            pipeline.addLast(new HttpRequestDecoder());
            pipeline.addLast(new HttpObjectAggregator(1024 * 1024));
            pipeline.addLast(new HttpServerExpectContinueHandler());
            pipeline.addLast(new NettyHttpHandler(commandService));
        }

    }

    public static class NettyHttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

        private final CommandService commandService;

        public NettyHttpHandler(CommandService commandService) {
            this.commandService = commandService;
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request)
                                                                                       throws Exception {
            RequestValidation validation = validate(request);
            try {
                if (!validation.isPass()) {
                    ret500(ctx, validation.getMessage());
                } else {
                    if (!validation.isCmdSupported()) {
                        returnResponse(ctx, Response.notFound());
                    }
                    Output<?> output = commandService.process(validation.getCmd(),
                        validation.getCmdContent());
                    Response response = Response.fromCommandOutput(output);
                    returnResponse(ctx, response);
                }
            } catch (Throwable e) {
                returnResponse(
                    ctx,
                    Response.internalError("Internal Error: " + e.getMessage(),
                        ExceptionUtils.getStackTraceAsString(e)));
                LOGGER.error("arklet process exception, cmd: {}", validation.getCmd(), e);
            }
        }

        private RequestValidation validate(FullHttpRequest request) throws IOException {
            HttpMethod method = request.method();
            if (HttpMethod.POST != method) {
                return RequestValidation.notPass("POST http request only supported by arklet");
            }
            String cmd = new QueryStringDecoder(request.uri()).path().substring(1);
            boolean supported = commandService.supported(cmd);

            ByteBuf jsonBuf = request.content();
            String jsonStr = jsonBuf.toString(CharsetUtil.UTF_8);
            Map<String, Object> paramMap = JSONObject.parseObject(jsonStr, HashMap.class);
            return RequestValidation.passed(supported, cmd, paramMap);
        }

        private void returnResponse(ChannelHandlerContext ctx, Response response) {
            DefaultFullHttpResponse httpResponse = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.copiedBuffer(
                    JSONObject.toJSONString(response), CharsetUtil.UTF_8));
            ChannelFuture future = ctx.writeAndFlush(httpResponse);
            if (future != null) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }

        private void ret404(ChannelHandlerContext ctx) {
            ChannelFuture future = ctx.writeAndFlush(new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
            if (future != null) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }

        private void ret500(ChannelHandlerContext ctx, String message) {
            FullHttpResponse httpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR, Unpooled.copiedBuffer(message,
                    CharsetUtil.UTF_8));
            httpResponse.headers().set("Content_Length", httpResponse.content().readableBytes());
            ChannelFuture future = ctx.writeAndFlush(httpResponse);
            if (future != null) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }

    }
}
