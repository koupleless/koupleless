package com.alipay.sofa.serverless.arklet.core.netty;

import com.alipay.sofa.serverless.arklet.core.BaseTest;
import com.alipay.sofa.serverless.arklet.core.api.tunnel.http.netty.NettyHttpServer;
import com.alipay.sofa.serverless.arklet.core.api.tunnel.http.netty.NettyHttpServer.NettyHttpHandler;
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
        DefaultFullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/installBiz", Unpooled.copiedBuffer(content.getBytes()));
        handler.channelRead(mockCtx, fullHttpRequest);
    }

    @Test
    public void open() throws InterruptedException {
        NettyHttpServer server = new NettyHttpServer(1239, commandService);
        Channel channel = server.getChannel();
        server.open();
    }

}
