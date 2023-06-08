package com.alipay.sofa.serverless.arklet.core.api.tunnel.http;

import java.util.concurrent.atomic.AtomicBoolean;

import com.alipay.sofa.ark.common.log.ArkLogger;
import com.alipay.sofa.ark.common.log.ArkLoggerFactory;
import com.alipay.sofa.ark.common.util.AssertUtils;
import com.alipay.sofa.ark.common.util.EnvironmentUtils;
import com.alipay.sofa.ark.common.util.PortSelectUtils;
import com.alipay.sofa.ark.common.util.StringUtils;
import com.alipay.sofa.serverless.arklet.core.api.tunnel.Tunnel;
import com.alipay.sofa.serverless.arklet.core.api.tunnel.http.netty.NettyHttpServer;
import com.alipay.sofa.serverless.arklet.core.command.CommandService;
import com.alipay.sofa.serverless.arklet.core.common.ArkletInitException;
import com.alipay.sofa.serverless.arklet.core.common.ArkletRuntimeException;
import com.google.inject.Singleton;

/**
 * @author mingmen
 * @date 2023/6/8
 */

@Singleton
public class HttpTunnel implements Tunnel {

    private static final ArkLogger LOGGER = ArkLoggerFactory.getDefaultLogger();
    private final static String HTTP_PORT_ATTRIBUTE = "sofa.serverless.arklet.http.port";
    private int port = -1;
    private final static int DEFAULT_HTTP_PORT = 1238;
    private final static int DEFAULT_SELECT_PORT_SIZE = 100;
    private NettyHttpServer nettyHttpServer;
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final AtomicBoolean init = new AtomicBoolean(false);
    private final AtomicBoolean run = new AtomicBoolean(false);
    private CommandService commandService;

    @Override
    public void init(CommandService commandService) {
        if (init.compareAndSet(false, true)) {
            this.commandService = commandService;
            String httpPort = EnvironmentUtils.getProperty(HTTP_PORT_ATTRIBUTE);
            try {
                if (!StringUtils.isEmpty(httpPort)) {
                    port = Integer.parseInt(httpPort);
                } else {
                    port = PortSelectUtils.selectAvailablePort(DEFAULT_HTTP_PORT,
                        DEFAULT_SELECT_PORT_SIZE);
                }
            } catch (NumberFormatException e) {
                LOGGER.error(String.format("Invalid arklet http port in %s", httpPort), e);
                throw new ArkletInitException(e);
            }
        }
    }

    @Override
    public void run() {
        if (run.compareAndSet(false, true)) {
            AssertUtils.isTrue(port > 0, "Http port should be positive integer.");
            try {
                LOGGER.info("arklet listening on port: " + port);
                nettyHttpServer = new NettyHttpServer(port, commandService);
                nettyHttpServer.open();
            } catch (InterruptedException e) {
                LOGGER.error("Unable to open netty schedule http server.", e);
                throw new ArkletRuntimeException(e);
            }
        }
    }

    @Override
    public void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            try {
                if (nettyHttpServer != null) {
                    nettyHttpServer.close();
                    nettyHttpServer = null;
                }
            } catch (Throwable t) {
                LOGGER.error("An error occurs when shutdown arklet http server.", t);
                throw new ArkletRuntimeException(t);
            }
        }
    }

}
