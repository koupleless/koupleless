package com.alipay.sofa.serverless.arklet.core.command.builtin;

import com.alipay.sofa.serverless.arklet.core.command.meta.Command;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public enum BuiltinCommand implements Command {

    HELP("help",
        "list all supported commands"),

    INSTALL_BIZ("installBiz",
        "install one ark biz"),

    UNINSTALL_BIZ("uninstallBiz",
        "uninstall one ark biz"),

    SWITCH_BIZ("switchBiz",
        "switch one ark biz"),

    QUERY_ALL_BIZ("queryAllBiz",
        "query all ark biz(including master biz)"),

    HEALTH("health",
            "get master biz health info");

    private final String id;
    private final String desc;

    BuiltinCommand(String id, String desc) {
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
