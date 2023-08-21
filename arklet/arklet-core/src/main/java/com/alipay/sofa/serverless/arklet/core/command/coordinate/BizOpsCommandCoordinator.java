package com.alipay.sofa.serverless.arklet.core.command.coordinate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alipay.sofa.ark.common.util.BizIdentityUtils;
import com.alipay.sofa.serverless.arklet.core.command.meta.Command;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public class BizOpsCommandCoordinator {

    private static final Map<String, Command> bizIdentityLockMap = new ConcurrentHashMap<>(16);

    public static void putBizExecution(String bizName, String bizVersion, Command command) {
        String identity = BizIdentityUtils.generateBizIdentity(bizName, bizVersion);
        if (bizIdentityLockMap.containsKey(identity)) {
            throw new CommandMutexException("biz {} execution meet mutex lock, conflict command:%s is processing and not finish yet", identity, bizIdentityLockMap.get(identity));
        }
        bizIdentityLockMap.put(identity, command);
    }

    public static void popBizExecution(String bizName, String bizVersion) {
        String identity = BizIdentityUtils.generateBizIdentity(bizName, bizVersion);
        bizIdentityLockMap.remove(identity);
    }

    public static boolean existBizProcessing(String bizName, String bizVersion) {
        String identity = BizIdentityUtils.generateBizIdentity(bizName, bizVersion);
        return bizIdentityLockMap.containsKey(identity);
    }

    public static Command getCurrentProcessingCommand(String bizName, String bizVersion) {
        String identity = BizIdentityUtils.generateBizIdentity(bizName, bizVersion);
        return bizIdentityLockMap.get(identity);
    }

}
