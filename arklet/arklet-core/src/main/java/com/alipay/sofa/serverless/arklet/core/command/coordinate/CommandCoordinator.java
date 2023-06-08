package com.alipay.sofa.serverless.arklet.core.command.coordinate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.alipay.sofa.serverless.arklet.core.command.meta.Command;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public class CommandCoordinator {

    private static volatile CommandCoordinator instance;

    private Map<Command, List<Command>> mutexCmdMap;
    private Map<Command, CopyOnWriteArrayList<ExecutionLock>> executionLockMap = new ConcurrentHashMap<>(16);

    public static CommandCoordinator getInstance() {
        if (instance == null) {
            synchronized (CommandCoordinator.class) {
                if (instance == null) {
                    instance = new CommandCoordinator();
                }
            }
        }
        return instance;
    }

    public void registerConfig(CoordinatorConfig config) {
        //for (List<Command> mutexCmds : config.getMutexCmds()) {
        //    for (Command mutexCmd : mutexCmds) {
        //        Set<Command> commands = mutexCmdMap.get(mutexCmd);
        //        if (commands == null) {
        //            commands = new HashSet<>();
        //        }
        //    }
        //}
    }

    public void putExecution(ExecutionLock executionLock) {
        Command command = executionLock.getCommand();
        CopyOnWriteArrayList<ExecutionLock> commandExecutions = executionLockMap.get(command);
        if (commandExecutions == null || commandExecutions.size() == 0 ) {
            commandExecutions = new CopyOnWriteArrayList<>();
            commandExecutions.add(executionLock);
        } else {
            if (command.concurrentEnabled()) {
                commandExecutions.add(executionLock);
            } else {
                throw new CommandMutexException("command execution meet mutex lock, conflict command:%s is processing and not finish yet", command.getId());
            }
        }
        executionLockMap.put(command, commandExecutions);

    }

    public void popExecution(ExecutionLock executionLock) {
        Command command = executionLock.getCommand();
        CopyOnWriteArrayList<ExecutionLock> commandExecutions = executionLockMap.get(command);
        commandExecutions.remove(executionLock);
        executionLockMap.put(command, commandExecutions);
    }

}
