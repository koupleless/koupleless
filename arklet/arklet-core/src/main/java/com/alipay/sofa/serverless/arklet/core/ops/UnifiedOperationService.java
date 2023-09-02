package com.alipay.sofa.serverless.arklet.core.ops;

import java.util.List;

import com.alipay.sofa.ark.api.ClientResponse;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.serverless.arklet.core.ArkletComponent;

/**
 * 统一操作服务接口
 * @author mingmen
 * @date 2023/6/14
 */
public interface UnifiedOperationService extends ArkletComponent {


    /**
     * install biz
     * @param bizUrl biz URL
     * @return response
     * @throws Throwable error
     */
    ClientResponse install(String bizUrl) throws Throwable;

    /**
     * uninstall biz
     * @param bizName bizName
     * @param bizVersion bizVersion
     * @return response
     * @throws Throwable error
     */
    ClientResponse uninstall(String bizName, String bizVersion) throws Throwable;

    /**
     * query biz list
     * @return biz list
     */
    List<Biz> queryBizList();

    /**
     * switch biz
     * @param bizName bizName
     * @param bizVersion bizVersion
     * @return response
     * @throws Throwable error
     */
    ClientResponse switchBiz(String bizName, String bizVersion) throws Throwable;

}
