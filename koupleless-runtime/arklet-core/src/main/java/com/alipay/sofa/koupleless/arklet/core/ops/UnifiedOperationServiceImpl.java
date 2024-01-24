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
package com.alipay.sofa.koupleless.arklet.core.ops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.api.ResponseCode;
import com.alipay.sofa.ark.spi.constant.Constants;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.BizOperation;
import com.alipay.sofa.koupleless.arklet.core.command.executor.ExecutorServiceManager;
import com.alipay.sofa.koupleless.arklet.core.common.log.ArkletLoggerFactory;
import com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallRequest;
import com.alipay.sofa.koupleless.arklet.core.common.model.BatchInstallResponse;
import com.alipay.sofa.koupleless.common.util.OSUtils;
import com.google.inject.Singleton;

/**
 * @author mingmen
 * @date 2023/6/14
 */
@Singleton
public class UnifiedOperationServiceImpl implements UnifiedOperationService {

    private BatchInstallHelper batchInstallHelper = new BatchInstallHelper();

    @Override
    public void init() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public ClientResponse install(String bizUrl) throws Throwable {
        BizOperation bizOperation = new BizOperation()
            .setOperationType(BizOperation.OperationType.INSTALL);
        bizOperation.putParameter(Constants.CONFIG_BIZ_URL, bizUrl);
        return ArkClient.installOperation(bizOperation);
    }

    public ClientResponse safeBatchInstall(String bizUrl) {
        try {
            BizOperation bizOperation = new BizOperation()
                .setOperationType(BizOperation.OperationType.INSTALL);

            bizOperation.putParameter(Constants.CONFIG_BIZ_URL,
                OSUtils.getLocalFileProtocolPrefix() + bizUrl);
            Map<String, Object> mainAttributes = batchInstallHelper.getMainAttributes(bizUrl);
            bizOperation.setBizName((String) mainAttributes.get(Constants.ARK_BIZ_NAME));
            bizOperation.setBizVersion((String) mainAttributes.get(Constants.ARK_BIZ_VERSION));
            return ArkClient.installOperation(bizOperation);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return new ClientResponse().setCode(ResponseCode.FAILED).setMessage(
                String.format("internal exception: %s", throwable.getMessage()));
        }
    }

    @Override
    public ClientResponse uninstall(String bizName, String bizVersion) throws Throwable {
        return ArkClient.uninstallBiz(bizName, bizVersion);
    }

    @Override
    public BatchInstallResponse batchInstall(BatchInstallRequest request) throws Throwable {
        List<String> bizUrls = batchInstallHelper.getBizUrlsFromLocalFileSystem(request.getBizDirAbsolutePath());
        ThreadPoolExecutor executorService = ExecutorServiceManager.getArkBizOpsExecutor();
        List<CompletableFuture<ClientResponse>> futures = new ArrayList<>();

        long startTimestamp = System.currentTimeMillis();
        for (String bizUrl : bizUrls) {
            futures.add(CompletableFuture.supplyAsync(() -> safeBatchInstall(bizUrl), executorService));
        }

        // wait for all install futures done
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        long endTimestamp = System.currentTimeMillis();
        ArkletLoggerFactory.getDefaultLogger().info("batch install cost {} ms", endTimestamp - startTimestamp);

        // analyze install result per bizUrl
        int counter = 0;
        boolean hasFailed = false;
        Map<String, ClientResponse> bizUrlToInstallResult = new HashMap<>();
        for (CompletableFuture<ClientResponse> future : futures) {
            ClientResponse clientResponse = future.get();
            String bizUrl = bizUrls.get(counter);
            bizUrlToInstallResult.put(bizUrl, clientResponse);
            hasFailed = hasFailed || clientResponse.getCode() != ResponseCode.SUCCESS;
            counter++;
        }

        return BatchInstallResponse.builder().
                code(hasFailed ? ResponseCode.FAILED : ResponseCode.SUCCESS).
                message(hasFailed ? "batch install failed" : "batch install success").
                bizUrlToResponse(bizUrlToInstallResult).
                build();
    }

    @Override
    public List<Biz> queryBizList() {
        return ArkClient.getBizManagerService().getBizInOrder();
    }

    @Override
    public ClientResponse switchBiz(String bizName, String bizVersion) throws Throwable {
        return ArkClient.switchBiz(bizName, bizVersion);
    }
}
