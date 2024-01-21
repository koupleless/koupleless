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
package com.alipay.sofa.koupleless.arklet.core.command.coordinate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alipay.sofa.ark.common.util.BizIdentityUtils;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Command;

/**
 * @author mingmen
 * @date 2023/6/14
 */
public class BizOpsCommandCoordinator {

    private static final Map<String, Command> bizIdentityLockMap = new ConcurrentHashMap<>(16);

    public synchronized static boolean checkAndLock(String bizName, String bizVersion,
                                                    Command command) {
        String identity = BizIdentityUtils.generateBizIdentity(bizName, bizVersion);
        if (existBizProcessing(identity)) {
            return false;
        }
        bizIdentityLockMap.put(identity, command);
        return true;
    }

    public static void unlock(String bizName, String bizVersion) {
        String identity = BizIdentityUtils.generateBizIdentity(bizName, bizVersion);
        bizIdentityLockMap.remove(identity);
    }

    public static boolean existBizProcessing(String bizName, String bizVersion) {
        String identity = BizIdentityUtils.generateBizIdentity(bizName, bizVersion);
        return bizIdentityLockMap.containsKey(identity);
    }

    public static boolean existBizProcessing(String identity) {
        return bizIdentityLockMap.containsKey(identity);
    }

    public static Command getCurrentProcessingCommand(String bizName, String bizVersion) {
        String identity = BizIdentityUtils.generateBizIdentity(bizName, bizVersion);
        return bizIdentityLockMap.get(identity);
    }

    public static void clear() {
        bizIdentityLockMap.clear();
    }

}
