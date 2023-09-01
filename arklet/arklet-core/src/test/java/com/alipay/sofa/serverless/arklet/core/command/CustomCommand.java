package com.alipay.sofa.serverless.arklet.core.command;

import com.alipay.sofa.serverless.arklet.core.command.meta.Command;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public enum CustomCommand implements Command {

    HELLO("hello", "say hello");

    private final String id;
    private final String desc;

    CustomCommand(String id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }
}
