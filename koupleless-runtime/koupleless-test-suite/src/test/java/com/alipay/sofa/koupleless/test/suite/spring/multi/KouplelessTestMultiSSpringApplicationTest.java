package com.alipay.sofa.koupleless.test.suite.spring.multi;

import com.alipay.sofa.koupleless.test.suite.spring.mock.common.HelloService;
import com.alipay.sofa.koupleless.test.suite.spring.model.KouplelessBaseSpringTestConfig;
import com.alipay.sofa.koupleless.test.suite.spring.model.KouplelessBizSpringTestConfig;
import com.alipay.sofa.koupleless.test.suite.spring.model.KouplelessMultiSpringTestConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author CodeNoobKing
 * @date 2024/3/11
 */
public class KouplelessTestMultiSSpringApplicationTest {

    @Test
    public void testMultiApplicationLaunched() throws Throwable {
        KouplelessBaseSpringTestConfig baseConfig = KouplelessBaseSpringTestConfig
                .builder()
                .packageName("com.alipay.sofa.koupleless.test.suite.spring.mock.base")
                .build();

        List<KouplelessBizSpringTestConfig> bizConfigs = new ArrayList<>();
        bizConfigs.add(KouplelessBizSpringTestConfig
                .builder()
                .packageName("com.alipay.sofa.koupleless.test.suite.spring.mock.biz")
                .bizName("biz0")
                .build()
        );

        KouplelessTestMultiSpringApplication application = new KouplelessTestMultiSpringApplication(
                KouplelessMultiSpringTestConfig
                        .builder()
                        .baseConfig(baseConfig)
                        .bizConfigs(bizConfigs)
                        .build()
        );

        application.run();
        Thread.sleep(1_000);

        HelloService sampleBaseService = application
                .getBaseApplication()
                .getApplicationContext()
                .getBean(HelloService.class);

        Assert.assertEquals(
                Thread.currentThread().getContextClassLoader().getClass().getName(),
                sampleBaseService.helloWorld()
        );

        HelloService sampleBizService = application
                .getBizApplication("biz0")
                .getApplicationContext()
                .getBean(HelloService.class);

        Assert.assertEquals(
                "com.alipay.sofa.koupleless.test.suite.biz.SOFAArkTestBizClassLoader",
                sampleBizService.helloWorld()
        );
    }
}
