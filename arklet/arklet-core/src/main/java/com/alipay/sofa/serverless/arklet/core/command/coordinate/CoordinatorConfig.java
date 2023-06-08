package com.alipay.sofa.serverless.arklet.core.command.coordinate;

import java.util.ArrayList;
import java.util.List;

import com.alipay.sofa.serverless.arklet.core.command.meta.Command;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public class CoordinatorConfig {
    private List<List<Command>> mutexCmds = new ArrayList<>();

    public List<List<Command>> getMutexCmds() {
        return mutexCmds;
    }

    public CoordinatorConfig addMutex(List<Command> commands) {
        mutexCmds.add(commands);
        return this;
    }
}
