package spring.biz0;

import com.alipay.sofa.ark.spi.event.biz.BeforeBizStartupEvent;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.koupleless.test.suite.biz.SOFAArkServiceContainerSingleton;
import com.alipay.sofa.koupleless.test.suite.biz.SOFAArkTestBiz;
import io.netty.util.concurrent.ThreadPerTaskExecutor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

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
public class SOFAArkTestSpringContext {

    private SOFAArkTestBiz testBiz;

    public GenericApplicationContext applicationContext;

    @SneakyThrows
    public SOFAArkTestSpringContext(String packageName) {
        ArrayList<String> includeClassPatterns = new ArrayList<>();
        includeClassPatterns.add(packageName + ".*");
        URLClassLoader original = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        List<URL> excludedUrls = new ArrayList<>();
        for (URL url : original.getURLs()) {
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
        SOFAArkTestBiz sofaArkTestBiz = new SOFAArkTestBiz(
                "",
                "biz",
                "1.0.0",
                new ArrayList<>(),
                includeClassPatterns,
                new URLClassLoader(
                        excludedUrls.toArray(new URL[0]),
                        original.getParent()
                ));
        this.testBiz = sofaArkTestBiz;
        this.testBiz.setWebContextPath("biz");
        //applicationContext = new AnnotationConfigApplicationContext();
        //applicationContext.setClassLoader(sofaArkTestBiz.getBizClassLoader());
        //((AnnotationConfigApplicationContext) applicationContext)
        //        .scan(packageName);
        //applicationContext.refresh();
    }

    @SneakyThrows
    public <T> T getModuleBean(Class<T> beanClass) {
        return (T) applicationContext.getBean("sampleService");
    }

    @SneakyThrows
    public void run() {
        CompletableFuture.runAsync(() -> {
            Thread.currentThread().setContextClassLoader(testBiz.getBizClassLoader());
            SOFAArkServiceContainerSingleton.instance()
                    .getService(EventAdminService.class)
                    .sendEvent(new BeforeBizStartupEvent(testBiz));

            SpringApplication springApplication = new SpringApplication(
                    Application.class
            );
            ConfigurableApplicationContext ctx = springApplication.run();
            applicationContext = (GenericApplicationContext) ctx;
        }, new Executor() {
            @Override
            public void execute(Runnable command) {
                new Thread(command).start();
            }
        }).get();
    }

}
