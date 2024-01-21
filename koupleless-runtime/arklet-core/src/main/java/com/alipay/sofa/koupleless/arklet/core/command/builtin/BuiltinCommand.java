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
package com.alipay.sofa.koupleless.arklet.core.command.builtin;

import com.alipay.sofa.koupleless.arklet.core.command.meta.Command;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public enum BuiltinCommand implements Command {

    HELP("help", "list all supported commands"),

    INSTALL_BIZ("installBiz", "install one ark biz"),

    UNINSTALL_BIZ("uninstallBiz", "uninstall one ark biz"),

    SWITCH_BIZ("switchBiz", "switch one ark biz"),

    QUERY_ALL_BIZ("queryAllBiz", "query all ark biz(including master biz)"),

    HEALTH("health", "get all health info"),

    QUERY_BIZ_OPS("queryBizOps", "query ark biz ops");

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
