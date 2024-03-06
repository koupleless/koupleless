import com.alipay.sofa.ark.api.ArkClient;
import com.alipay.sofa.ark.container.registry.ContainerServiceProvider;
import com.alipay.sofa.ark.container.service.ArkServiceContainer;
import com.alipay.sofa.ark.container.service.ArkServiceContainerHolder;
import com.alipay.sofa.ark.loader.JarPluginArchive;
import com.alipay.sofa.ark.loader.archive.JarFileArchive;
import com.alipay.sofa.ark.spi.archive.PluginArchive;
import com.alipay.sofa.ark.spi.model.Biz;
import com.alipay.sofa.ark.spi.model.Plugin;
import com.alipay.sofa.ark.spi.service.PriorityOrdered;
import com.alipay.sofa.ark.spi.service.biz.BizFactoryService;
import com.alipay.sofa.ark.spi.service.biz.BizManagerService;
import com.alipay.sofa.ark.spi.service.classloader.ClassLoaderService;
import com.alipay.sofa.ark.spi.service.event.EventAdminService;
import com.alipay.sofa.ark.spi.service.injection.InjectionService;
import com.alipay.sofa.ark.spi.service.plugin.PluginDeployService;
import com.alipay.sofa.ark.spi.service.plugin.PluginFactoryService;
import com.alipay.sofa.ark.spi.service.plugin.PluginManagerService;
import com.alipay.sofa.ark.spi.service.registry.RegistryService;
import com.alipay.sofa.ark.support.startup.EmbedSofaArkBootstrap;
import com.alipay.sofa.koupleless.plugin.spring.ServerlessEnvironmentPostProcessor;
import com.alipay.sofa.koupleless.test.suite.biz.SOFAArkServiceContainerSingleton;
import com.alipay.sofa.koupleless.test.suite.biz.SOFAArkTestBiz;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import spring.base.Application;
import spring.biz0.SOFAArkTestSpringContext;
import spring.biz0.SampleService;

import javax.swing.*;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author CodeNoobKing
 * @date 2024/3/6
 */
public class BizSpringTest {

    public static void setUpSOFAArk() throws Throwable {
        SOFAArkServiceContainerSingleton.init(
                Thread.currentThread().getContextClassLoader()
        );

        RegistryService registryService = SOFAArkServiceContainerSingleton.instance()
                .getService(RegistryService.class);
        registryService.publishService(BizManagerService.class, SOFAArkServiceContainerSingleton
                        .instance()
                        .getService(BizManagerService.class),
                new ContainerServiceProvider(PriorityOrdered.HIGHEST_PRECEDENCE));

        registryService.publishService(BizFactoryService.class, SOFAArkServiceContainerSingleton
                .instance().getService(BizFactoryService.class), new ContainerServiceProvider(
                PriorityOrdered.HIGHEST_PRECEDENCE));
        registryService.publishService(PluginManagerService.class, SOFAArkServiceContainerSingleton
                .instance().getService(PluginManagerService.class), new ContainerServiceProvider(
                PriorityOrdered.HIGHEST_PRECEDENCE));
        registryService.publishService(PluginFactoryService.class, SOFAArkServiceContainerSingleton
                .instance().getService(PluginFactoryService.class), new ContainerServiceProvider(
                PriorityOrdered.HIGHEST_PRECEDENCE));
        registryService.publishService(EventAdminService.class, SOFAArkServiceContainerSingleton
                .instance().getService(EventAdminService.class), new ContainerServiceProvider(
                PriorityOrdered.HIGHEST_PRECEDENCE));
        registryService.publishService(RegistryService.class, SOFAArkServiceContainerSingleton
                .instance().getService(RegistryService.class), new ContainerServiceProvider(
                PriorityOrdered.HIGHEST_PRECEDENCE));

        URLClassLoader contextClassLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        for (URL url : contextClassLoader.getURLs()) {
            String path = url.getPath();
            if (StringUtils.contains(path, "web-ark-plugin") ||
                StringUtils.contains(path, "koupleless-base-plugin")) {
                JarFileArchive archive = new JarFileArchive(new File(url.getFile()));
                PluginArchive pluginArchive = new JarPluginArchive(archive);
                Plugin plugin = SOFAArkServiceContainerSingleton.instance()
                        .getService(PluginFactoryService.class)
                        .createEmbedPlugin(
                                pluginArchive,
                                Thread.currentThread().getContextClassLoader()
                        );
                ArkClient.getPluginManagerService().registerPlugin(plugin);
            }
        }

        SOFAArkServiceContainerSingleton.instance().getService(ClassLoaderService.class).prepareExportClassAndResourceCache();
        SOFAArkServiceContainerSingleton.instance().getService(PluginDeployService.class).deploy();
    }

    @Test
    public void testBiz() throws Throwable {
        setUpSOFAArk();
        ArkServiceContainer holder = ArkServiceContainerHolder.getContainer();
        SOFAArkTestSpringContext context = new SOFAArkTestSpringContext("spring.biz0");

        Biz memo = ArkClient.getMasterBiz();
        ArkClient.setMasterBiz(null);
        SpringApplication baseApplication = new SpringApplication(Application.class) {
            @Override
            public void setListeners(Collection<? extends ApplicationListener<?>> listeners) {
                List<? extends ApplicationListener<?>> filteredlisteners = listeners
                        .stream()
                        .filter((listener) -> !listener.getClass().getName().equals("com.alipay.sofa.ark.springboot.listener.ArkApplicationStartListener"))
                        .collect(Collectors.toList());

                super.setListeners(filteredlisteners);
            }
        };
        //baseApplication.addInitializers(ctx -> {
        //});
        //baseApplication.addInitializers(ctx -> {
        //    ConfigurableApplicationContext cctx = (ConfigurableApplicationContext) ctx;
        //    cctx.addApplicationListener(new ApplicationListener<ApplicationEvent>() {
        //        @Override
        //        public void onApplicationEvent(ApplicationEvent event) {
        //            if ("org.springframework.boot.context.event.ApplicationStartingEvent".equals(event.getClass().getCanonicalName())) {
        //                ArkServiceContainerHolder.setContainer(holder);
        //            }
        //        }
        //    });
        //});
        baseApplication.run();

        ArkClient.setMasterBiz(memo);

        context.run();
        context.getModuleBean(SampleService.class);
        Thread.sleep(36000000);
        //SampleService moduleBean = context.getModuleBean(SampleService.class);
        //System.out.println(moduleBean.hellyWorld());
    }
}
