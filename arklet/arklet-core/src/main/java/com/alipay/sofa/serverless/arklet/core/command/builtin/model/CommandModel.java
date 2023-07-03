package com.alipay.sofa.serverless.arklet.core.command.builtin.model;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public class CommandModel {
    private String id;
    private String desc;

    public CommandModel(String id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public String getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }

}
