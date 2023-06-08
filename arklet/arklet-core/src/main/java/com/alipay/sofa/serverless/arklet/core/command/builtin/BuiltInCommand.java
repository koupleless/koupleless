package com.alipay.sofa.serverless.arklet.core.command.builtin;

import com.alipay.sofa.serverless.arklet.core.command.meta.Command;
import com.alipay.sofa.serverless.arklet.core.command.meta.CommandType;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public enum BuiltInCommand implements Command {

    HELP("help",
        true,
        CommandType.READ,
        "list all supported commands",
        ""),

    INSTALL_BIZ("installBiz",
        false,
        CommandType.WRITE,
        "install one ark biz",
        "{\"arkBizFilePath\":\"file://xxx\"}"),

    UNINSTALL_BIZ("uninstallBiz",
        false,
        CommandType.WRITE,
        "uninstall one ark biz",
        "{\"bizName\":\"xxx\",\"bizVersion\":\"xxx\"}"),

    SWITCH_BIZ("switchBiz",
        false,
        CommandType.WRITE,
        "switch one ark biz",
        "{\"bizName\":\"xxx\",\"bizVersion\":\"xxx\"}"),

    QUERY_ALL_BIZ("queryAllBiz",
        true,
        CommandType.READ,
        "query all ark biz(including master biz)",
        "")


    ;

    private final String id;
    private final boolean concurrentEnabled;
    private final CommandType type;
    private final String desc;
    private final String sample;

    BuiltInCommand(String id, boolean concurrentEnabled, CommandType type, String desc, String sample) {
        this.id = id;
        this.concurrentEnabled = concurrentEnabled;
        this.type = type;
        this.desc = desc;
        this.sample = sample;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getDesc() {
        return this.desc;
    }

    @Override
    public String getSample() {
        return this.sample;
    }

    @Override
    public CommandType getType() {
        return this.type;
    }

    @Override
    public boolean concurrentEnabled() {
        return this.concurrentEnabled;
    }
}
