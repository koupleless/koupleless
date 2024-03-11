package com.alipay.sofa.web.intergration.test;

import com.alipay.sofa.koupleless.test.suite.spring.model.KouplelessBaseSpringTestConfig;
import com.alipay.sofa.koupleless.test.suite.spring.model.KouplelessBizSpringTestConfig;
import com.alipay.sofa.koupleless.test.suite.spring.model.KouplelessMultiSpringTestConfig;
import com.alipay.sofa.koupleless.test.suite.spring.multi.KouplelessTestMultiSpringApplication;
import lombok.SneakyThrows;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * @author CodeNoobKing
 * @date 2024/3/11
 */
public class WebSingleHostTest {

    private static KouplelessTestMultiSpringApplication multiApp;

    private static final OkHttpClient client = new OkHttpClient();

    @SneakyThrows
    @BeforeClass
    public static void setUpMultiApplication() {
        KouplelessBaseSpringTestConfig baseConfig = KouplelessBaseSpringTestConfig
                .builder()
                .packageName("com.alipay.sofa.web.base")
                .mainClass("com.alipay.sofa.web.base.BaseApplication")
                .build();

        List<KouplelessBizSpringTestConfig> bizConfigs = new ArrayList<>();
        bizConfigs.add(KouplelessBizSpringTestConfig
                .builder()
                .packageName("com.alipay.sofa.web.biz1")
                .bizName("biz1")
                .mainClass("com.alipay.sofa.web.biz1.Biz1Application")
                .build()
        );
        bizConfigs.add(KouplelessBizSpringTestConfig
                .builder()
                .packageName("com.alipay.sofa.web.biz2")
                .bizName("biz2")
                .mainClass("com.alipay.sofa.web.biz2.Biz2Application")
                .build()
        );

        multiApp = new KouplelessTestMultiSpringApplication(
                KouplelessMultiSpringTestConfig
                        .builder()
                        .baseConfig(baseConfig)
                        .bizConfigs(bizConfigs)
                        .build()
        );

        multiApp.run();
        Thread.sleep(1000);
    }

    @Test
    public void testContextWebhookPathPrefixIsAdded() throws Throwable {
        Response resp = client.newCall(new Request.Builder()
                .url("http://localhost:8080/")
                .get()
                .build()).execute();
        Assert.assertEquals("hello to base deploy", resp.body().string());

        resp = client.newCall(new Request.Builder()
                .url("http://localhost:8080/biz1/")
                .get()
                .build()).execute();
        Assert.assertEquals("hello to biz1 deploy", resp.body().string());

        resp = client.newCall(new Request.Builder()
                .url("http://localhost:8080/biz2/")
                .get()
                .build()).execute();
        Assert.assertEquals("hello to biz2 deploy", resp.body().string());

    }
}
