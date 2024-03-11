package spring.biz0;

import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.container.service.classloader.BizClassLoader;
import com.alipay.sofa.ark.spi.event.biz.AfterBizStartupEvent;
import com.alipay.sofa.ark.spi.event.biz.BeforeBizStartupEvent;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.koupleless.test.suite.biz.SOFAArkTestBiz;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * @author CodeNoobKing
 * @date 2024/3/6
 */
public class SOFAArkTestSpringApplication {

    @Getter
    private SOFAArkTestBiz testBiz;

    @Getter
    private ConfigurableApplicationContext applicationContext;

    private List<String> excludePackages = new ArrayList<>();

    @Getter
    private SOFAArkTestSpringContextConfig config;

    @SneakyThrows
    public SOFAArkTestSpringApplication(SOFAArkTestSpringContextConfig config) {
        config.init();
        this.config = config;

    }

    public void initBiz() {
        ArrayList<String> includeClassPatterns = new ArrayList<>();
        includeClassPatterns.add(config.getPackageName() + ".*");
        URLClassLoader tccl = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        List<URL> excludedUrls = new ArrayList<>();
        for (URL url : tccl.getURLs()) {
            if (!url.toString().contains("koupleless-base-starter") &&
                !url.toString().contains("sofa-ark-springboot-starter") &&
                !url.toString().contains("arklet-springboot-starter") &&
                !url.toString().contains("koupleless-adapter")
            ) {
                excludedUrls.add(url);
            } else {
                System.out.println(url.toString());
            }
        }

        testBiz = new SOFAArkTestBiz(
                "",
                config.getBizName(),
                "TEST",
                new ArrayList<>(),
                includeClassPatterns,
                new URLClassLoader(
                        excludedUrls.toArray(new URL[0]),
                        tccl.getParent()
                ));
        testBiz.setWebContextPath("biz");
    }

    @SneakyThrows
    public <T> T getModuleBean(Class<T> beanClass) {
        Preconditions.checkState(
                beanClass.isInterface() &&
                !(beanClass.getClassLoader() instanceof BizClassLoader),
                "the bean class must be an interface and not loaded by BizClassLoader"
                + "or the return value would throw unwanted exceptions"

        );
        return (T) applicationContext.getBean(beanClass);
    }

    @SneakyThrows
    public void run() {
        CompletableFuture.runAsync(() -> {
            Thread.currentThread().setContextClassLoader(testBiz.getBizClassLoader());
            EventAdminService eventAdminService = ArkClient.getEventAdminService();
            eventAdminService.sendEvent(new BeforeBizStartupEvent(testBiz));
            SpringApplication springApplication = new SpringApplication(
                    Application.class
            );
            applicationContext = springApplication.run();
            eventAdminService.sendEvent(new AfterBizStartupEvent(testBiz));

        }, new Executor() {
            @Override
            public void execute(Runnable command) {
                new Thread(command).start();
            }
        }).get();
    }

}
