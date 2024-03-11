package com.alipay.sofa.koupleless.test.suite.spring.multi;

/**
 * @author CodeNoobKing
 * @date 2024/3/11
 */

import com.alipay.sofa.koupleless.test.suite.biz.SOFAArkTestBootstrap;
import com.alipay.sofa.koupleless.test.suite.spring.base.KouplelessBaseSpringTestApplication;
import com.alipay.sofa.koupleless.test.suite.spring.biz.KouplelessBizSpringTestApplication;
import com.alipay.sofa.koupleless.test.suite.spring.model.KouplelessBizSpringTestConfig;
import com.alipay.sofa.koupleless.test.suite.spring.model.KouplelessMultiSpringTestConfig;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author CodeNoobKing
 * @date 2024/3/7
 */
public class KouplelessTestMultiSpringApplication {

    @Getter
    private KouplelessBaseSpringTestApplication baseApplication;

    private Map<String, KouplelessBizSpringTestApplication> bizApplications = new HashMap<>();

    public KouplelessBizSpringTestApplication getBizApplication(String bizName) {
        return bizApplications.get(bizName);
    }

    public KouplelessTestMultiSpringApplication(
            KouplelessMultiSpringTestConfig config
    ) {
        this.baseApplication = new KouplelessBaseSpringTestApplication(config.getBaseConfig());
        for (KouplelessBizSpringTestConfig bizConfig : config.getBizConfigs()) {
            this.bizApplications.put(
                    bizConfig.getBizName(),
                    new KouplelessBizSpringTestApplication(bizConfig)
            );
        }
    }

    public void runBase() {
        baseApplication.run();
    }

    public void runBiz(String bizName) {
        bizApplications.get(bizName).initBiz(); // register biz to ark container
        bizApplications.get(bizName).run(); // run biz
    }

    public void run() {
        SOFAArkTestBootstrap.init(Thread.currentThread().getContextClassLoader());
        runBase();
        bizApplications.keySet().forEach(this::runBiz);
    }
}
