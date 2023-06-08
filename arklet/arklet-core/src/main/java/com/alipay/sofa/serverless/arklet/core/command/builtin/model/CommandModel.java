package com.alipay.sofa.serverless.arklet.core.command.builtin.model;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public class CommandModel {
    private String id;
    private String desc;
    private String sample;

    public CommandModel(String id, String desc, String sample) {
        this.id = id;
        this.desc = desc;
        this.sample = sample;
    }

    public String getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }

    public String getSample() {
        return sample;
    }
}
