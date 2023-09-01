package com.alipay.sofa.serverless.arklet.core.api.tunnel;

import com.alipay.sofa.serverless.arklet.core.command.CommandService;

/**
 * @author mingmen
 * @date 2023/6/8
 */
public interface Tunnel {

    void init(CommandService commandService);

    void run();

    void shutdown();
}
