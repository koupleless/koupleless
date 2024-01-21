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
package com.alipay.sofa.koupleless.arklet.core.command;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.alipay.sofa.koupleless.arklet.core.command.builtin.BuiltinCommand;
import com.alipay.sofa.koupleless.arklet.core.command.coordinate.BizOpsCommandCoordinator;
import com.alipay.sofa.koupleless.arklet.core.command.meta.Command;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

public class BizOpsCommandCoordinatorTest {

    private final BuiltinCommand command = BuiltinCommand.INSTALL_BIZ;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        BizOpsCommandCoordinator.clear();
    }

    /**
     * 测试putBizExecution方法，验证当bizIdentityLockMap中已存在相同的identity时，会检查失败。
     */
    @Test
    public void testPutBizExecutionWithExistingIdentity() {
        String bizName = "biz";
        String bizVersion = "1.0";
        BizOpsCommandCoordinator.checkAndLock(bizName, bizVersion, command);
        Assert.assertFalse(BizOpsCommandCoordinator.checkAndLock(bizName, bizVersion, command));
    }

    /**
     * 测试putBizExecution方法，验证当bizIdentityLockMap中不存在相同的identity时，会成功将command放入bizIdentityLockMap中。
     */
    @Test
    public void testPutBizExecutionWithNonExistingIdentity() {
        String bizName = "biz";
        String bizVersionV1 = "1.0";
        String bizVersionV2 = "2.0";
        BizOpsCommandCoordinator.checkAndLock(bizName, bizVersionV1, command);
        BizOpsCommandCoordinator.checkAndLock(bizName, bizVersionV2, command);
        Assert.assertTrue(BizOpsCommandCoordinator.existBizProcessing(bizName, bizVersionV1));
        Assert.assertTrue(BizOpsCommandCoordinator.existBizProcessing(bizName, bizVersionV2));
    }

    /**
     * 测试popBizExecution方法，验证当bizIdentityLockMap中存在相同的identity时，会成功将该identity从bizIdentityLockMap中移除。
     */
    @Test
    public void testPopBizExecutionWithExistingIdentity() {
        String bizName = "biz";
        String bizVersion = "1.0";
        BizOpsCommandCoordinator.checkAndLock(bizName, bizVersion, command);
        BizOpsCommandCoordinator.unlock(bizName, bizVersion);
        Assert.assertFalse(BizOpsCommandCoordinator.existBizProcessing(bizName, bizVersion));
    }

    /**
     * 测试popBizExecution方法，验证当bizIdentityLockMap中不存在相同的identity时，不会有任何影响。
     */
    @Test
    public void testPopBizExecutionWithNonExistingIdentity() {
        String bizName = "biz";
        String bizVersion = "1.0";
        BizOpsCommandCoordinator.unlock(bizName, bizVersion);
    }

    /**
     * 测试getCurrentProcessingCommand方法，验证当bizIdentityLockMap中存在相同的identity时，返回对应的command。
     */
    @Test
    public void testGetCurrentProcessingCommandWithExistingIdentity() {
        String bizName = "biz";
        String bizVersion = "1.0";
        BizOpsCommandCoordinator.checkAndLock(bizName, bizVersion, command);
        Command result = BizOpsCommandCoordinator.getCurrentProcessingCommand(bizName, bizVersion);
        Assert.assertEquals(command, result);
    }

    /**
     * 验证在高并发场景下，`checkAndLock`方法是否能够正常执行，不会出现锁异常
     */
    @Test
    public void testCheckAndLock_Concurrency() throws InterruptedException {
        // 创建并发线程池
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // 模拟高并发场景，同时调用putBizExecution方法
        for (int i = 0; i < 1000; i++) {
            executor.execute(() -> {
                try {
                    BizOpsCommandCoordinator.checkAndLock("bizName", "bizVersion", command);
                } finally {
                    BizOpsCommandCoordinator.unlock("bizName", "bizVersion");
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        Assert.assertFalse("存在锁异常", BizOpsCommandCoordinator.existBizProcessing("bizName", "bizVersion"));
    }
}
