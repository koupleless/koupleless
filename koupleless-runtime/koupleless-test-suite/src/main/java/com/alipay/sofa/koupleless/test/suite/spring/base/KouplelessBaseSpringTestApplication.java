package com.alipay.sofa.koupleless.test.suite.spring.base;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.api.ArkConfigs;
import com.alipay.sofa.ark.container.model.BizModel;
import com.alipay.sofa.koupleless.common.BizRuntimeContext;
import com.alipay.sofa.koupleless.common.BizRuntimeContextRegistry;
import com.alipay.sofa.koupleless.test.suite.biz.SOFAArkTestBootstrap;
import com.alipay.sofa.koupleless.test.suite.spring.model.KouplelessBaseSpringTestConfig;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Collection;
import java.util.stream.Collectors;

import static com.alipay.sofa.ark.spi.constant.Constants.MASTER_BIZ;

/**
 * @author CodeNoobKing
 * @date 2024/3/8
 */
@Getter
public class KouplelessBaseSpringTestApplication {

    private KouplelessBaseSpringTestConfig config;

    private ConfigurableApplicationContext applicationContext;

    public KouplelessBaseSpringTestApplication(
            KouplelessBaseSpringTestConfig config
    ) {
        config.init();
        this.config = config;
    }

    private boolean isNotArkApplicationStartListener(ApplicationListener<?> listener) {
        return !listener.getClass().getName().equals("com.alipay.sofa.ark.springboot.listener.ArkApplicationStartListener");
    }

    @SneakyThrows
    public void run() {
        // add necessary bizRuntimeContext

        SpringApplication springApplication = new SpringApplication(Class.forName(config.getMainClass())) {
            // the listener is not needed in the test workflow.
            // because it will automatically create another ArkServiceContainer with a unreachable Container ClassLoader
            @Override
            public void setListeners(Collection<? extends ApplicationListener<?>> listeners) {
                super.setListeners(listeners
                        .stream()
                        .filter(KouplelessBaseSpringTestApplication.this::isNotArkApplicationStartListener)
                        .collect(Collectors.toList()));
            }
        };

        springApplication.addListeners(
                new ApplicationListener<ApplicationEnvironmentPreparedEvent>() {
                    @Override
                    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
                        ArkClient.setMasterBiz(new BizModel()
                                .setBizName("master biz")
                                .setBizVersion("VERSION")
                                .setClassLoader(SOFAArkTestBootstrap.getBaseClassLoader()));
                        ArkConfigs.setSystemProperty(MASTER_BIZ, "master biz");
                        BizRuntimeContext bizRuntimeContext = new BizRuntimeContext(ArkClient.getMasterBiz());
                        BizRuntimeContextRegistry.registerBizRuntimeManager(bizRuntimeContext);
                    }
                });

        applicationContext = springApplication.run();

    }
}
