<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

# apollo support
## Problem Description
1. [Appollo初始化时，使用的引用项目的app.id · Issue #2659 · apolloconfig/apollo](https://github.com/apolloconfig/apollo/issues/2659)

2. [一个tomcat下部署多个war，导致app.id相同 · Issue #2921 · apolloconfig/apollo](https://github.com/apolloconfig/apollo/issues/2921)

current version apollo-java 1.6.0

## Problem effection
![image.png](https://cdn.nlark.com/yuque/0/2023/png/145710/1698390945071-ec3a6262-d337-4753-a467-aeb19547b142.png#averageHue=%230d0b0b&clientId=u5805e92a-6b59-4&from=paste&height=885&id=u7a23fc45&originHeight=885&originWidth=1393&originalType=binary&ratio=1&rotation=0&showTitle=false&size=232072&status=done&style=none&taskId=u1f644099-5b91-41a7-8393-421eab0da76&title=&width=1393)

can't get config for biz

# 问题探究
1. The problem is in `ApolloApplicationContextInitializer`
```

 private static final String[] APOLLO_SYSTEM_PROPERTIES = {"app.id", ConfigConsts.APOLLO_CLUSTER_KEY,
      "apollo.cacheDir", "apollo.accesskey.secret", ConfigConsts.APOLLO_META_KEY, PropertiesFactory.APOLLO_PROPERTY_ORDER_ENABLE};
/**
   * To fill system properties from environment config
   */
  void initializeSystemProperty(ConfigurableEnvironment environment) {
    for (String propertyName : APOLLO_SYSTEM_PROPERTIES) {
      fillSystemPropertyFromEnvironment(environment, propertyName);
    }
  }

  private void fillSystemPropertyFromEnvironment(ConfigurableEnvironment environment, String propertyName) {
    if (System.getProperty(propertyName) != null) {
      return;
    }

    String propertyValue = environment.getProperty(propertyName);

    if (Strings.isNullOrEmpty(propertyValue)) {
      return;
    }

    System.setProperty(propertyName, propertyValue);
  }

```
We can see that the `app.id` will be override by the first time setting `app.id`, then biz2 can't get it owner `app.id`. So we should not set `app.id` in the system properties.
![image.png](https://cdn.nlark.com/yuque/0/2023/png/145710/1698397563993-24dcd90c-0569-401b-9732-cfb62dd8f232.png#averageHue=%23516340&clientId=u5805e92a-6b59-4&from=paste&height=430&id=u8b49a995&originHeight=430&originWidth=1692&originalType=binary&ratio=1&rotation=0&showTitle=false&size=143102&status=done&style=none&taskId=ucf032262-798c-4124-b91f-e9a6b1dc3ea&title=&width=1692)

2. But how `app.id` should be inited now, then we check `DefaultApplicationProvider`
```
package com.ctrip.framework.foundation.internals.provider;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ctrip.framework.foundation.internals.Utils;
import com.ctrip.framework.foundation.internals.io.BOMInputStream;
import com.ctrip.framework.foundation.spi.provider.ApplicationProvider;
import com.ctrip.framework.foundation.spi.provider.Provider;

public class DefaultApplicationProvider implements ApplicationProvider {

  private static final Logger logger = LoggerFactory.getLogger(DefaultApplicationProvider.class);
  public static final String APP_PROPERTIES_CLASSPATH = "/META-INF/app.properties";
  private Properties m_appProperties = new Properties();

  private String m_appId;
  private String accessKeySecret;

  @Override
  public void initialize() {
    try {
      InputStream in = Thread.currentThread().getContextClassLoader()
          .getResourceAsStream(APP_PROPERTIES_CLASSPATH.substring(1));
      if (in == null) {
        in = DefaultApplicationProvider.class.getResourceAsStream(APP_PROPERTIES_CLASSPATH);
      }

      initialize(in);
    } catch (Throwable ex) {
      logger.error("Initialize DefaultApplicationProvider failed.", ex);
    }
  }

  @Override
  public void initialize(InputStream in) {
    try {
      if (in != null) {
        try {
          m_appProperties
              .load(new InputStreamReader(new BOMInputStream(in), StandardCharsets.UTF_8));
        } finally {
          in.close();
        }
      }

      initAppId();
      initAccessKey();
    } catch (Throwable ex) {
      logger.error("Initialize DefaultApplicationProvider failed.", ex);
    }
  }

  @Override
  public String getAppId() {
    return m_appId;
  }

  @Override
  public String getAccessKeySecret() {
    return accessKeySecret;
  }

  @Override
  public boolean isAppIdSet() {
    return !Utils.isBlank(m_appId);
  }

  @Override
  public String getProperty(String name, String defaultValue) {
    if ("app.id".equals(name)) {
      String val = getAppId();
      return val == null ? defaultValue : val;
    }

    if ("apollo.accesskey.secret".equals(name)) {
      String val = getAccessKeySecret();
      return val == null ? defaultValue : val;
    }

    String val = m_appProperties.getProperty(name, defaultValue);
    return val == null ? defaultValue : val;
  }

  @Override
  public Class<? extends Provider> getType() {
    return ApplicationProvider.class;
  }

  private void initAppId() {
    // 1. Get app.id from System Property
    m_appId = System.getProperty("app.id");
    if (!Utils.isBlank(m_appId)) {
      m_appId = m_appId.trim();
      logger.info("App ID is set to {} by app.id property from System Property", m_appId);
      return;
    }

    //2. Try to get app id from OS environment variable
    m_appId = System.getenv("APP_ID");
    if (!Utils.isBlank(m_appId)) {
      m_appId = m_appId.trim();
      logger.info("App ID is set to {} by APP_ID property from OS environment variable", m_appId);
      return;
    }

    // 3. Try to get app id from app.properties.
    m_appId = m_appProperties.getProperty("app.id");
    if (!Utils.isBlank(m_appId)) {
      m_appId = m_appId.trim();
      logger.info("App ID is set to {} by app.id property from {}", m_appId,
          APP_PROPERTIES_CLASSPATH);
      return;
    }

    m_appId = null;
    logger.warn("app.id is not available from System Property and {}. It is set to null",
        APP_PROPERTIES_CLASSPATH);
  }

  private void initAccessKey() {
    // 1. Get accesskey secret from System Property
    accessKeySecret = System.getProperty("apollo.accesskey.secret");
    if (!Utils.isBlank(accessKeySecret)) {
      accessKeySecret = accessKeySecret.trim();
      logger
          .info("ACCESSKEY SECRET is set by apollo.accesskey.secret property from System Property");
      return;
    }

    //2. Try to get accesskey secret from OS environment variable
    accessKeySecret = System.getenv("APOLLO_ACCESSKEY_SECRET");
    if (!Utils.isBlank(accessKeySecret)) {
      accessKeySecret = accessKeySecret.trim();
      logger.info(
          "ACCESSKEY SECRET is set by APOLLO_ACCESSKEY_SECRET property from OS environment variable");
      return;
    }

    // 3. Try to get accesskey secret from app.properties.
    accessKeySecret = m_appProperties.getProperty("apollo.accesskey.secret");
    if (!Utils.isBlank(accessKeySecret)) {
      accessKeySecret = accessKeySecret.trim();
      logger.info("ACCESSKEY SECRET is set by apollo.accesskey.secret property from {}",
          APP_PROPERTIES_CLASSPATH);
      return;
    }

    accessKeySecret = null;
  }

  @Override
  public String toString() {
    return "appId [" + getAppId() + "] properties: " + m_appProperties
        + " (DefaultApplicationProvider)";
  }
}
```

We found that it will try to load from `META-INF/app.properties`, so we can change to using config `META-INF/app.properties`.

![image.png](https://cdn.nlark.com/yuque/0/2023/png/145710/1698397772989-57c7bf84-1acc-4315-a979-57eb3e632a54.png#averageHue=%23547047&clientId=u5805e92a-6b59-4&from=paste&height=207&id=u5df59b62&originHeight=207&originWidth=977&originalType=binary&ratio=1&rotation=0&showTitle=false&size=30963&status=done&style=none&taskId=u72a7f7ca-c152-45e5-918c-8d70929e304&title=&width=977)

3. Further more, we found that this is a guice singleton instance, so we can't init different instance for different biz.
```
package com.ctrip.framework.foundation;

import com.ctrip.framework.foundation.internals.NullProviderManager;
import com.ctrip.framework.foundation.internals.ServiceBootstrap;
import com.ctrip.framework.foundation.spi.ProviderManager;
import com.ctrip.framework.foundation.spi.provider.ApplicationProvider;
import com.ctrip.framework.foundation.spi.provider.NetworkProvider;
import com.ctrip.framework.foundation.spi.provider.ServerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Foundation {
  private static final Logger logger = LoggerFactory.getLogger(Foundation.class);
  private static Object lock = new Object();

  private static volatile ProviderManager s_manager;

  // Encourage early initialization and fail early if it happens.
  static {
    getManager();
  }

  private static ProviderManager getManager() {
    try {
      if (s_manager == null) {
        // Double locking to make sure only one thread initializes ProviderManager.
        synchronized (lock) {
          if (s_manager == null) {
            s_manager = ServiceBootstrap.loadFirst(ProviderManager.class);
          }
        }
      }

      return s_manager;
    } catch (Throwable ex) {
      s_manager = new NullProviderManager();
      logger.error("Initialize ProviderManager failed.", ex);
      return s_manager;
    }
  }

  public static String getProperty(String name, String defaultValue) {
    try {
      return getManager().getProperty(name, defaultValue);
    } catch (Throwable ex) {
      logger.error("getProperty for {} failed.", name, ex);
      return defaultValue;
    }
  }

  public static NetworkProvider net() {
    try {
      return getManager().provider(NetworkProvider.class);
    } catch (Exception ex) {
      logger.error("Initialize NetworkProvider failed.", ex);
      return NullProviderManager.provider;
    }
  }

  public static ServerProvider server() {
    try {
      return getManager().provider(ServerProvider.class);
    } catch (Exception ex) {
      logger.error("Initialize ServerProvider failed.", ex);
      return NullProviderManager.provider;
    }
  }

  public static ApplicationProvider app() {
    try {
      return getManager().provider(ApplicationProvider.class);
    } catch (Exception ex) {
      logger.error("Initialize ApplicationProvider failed.", ex);
      return NullProviderManager.provider;
    }
  }
}

```
There are a lot of static instance in the code, so we using the same apollo instance for different biz. And also, The `ExecutorService` is also a singleton instance for long pulling config, so we can't use different `ExecutorService` for different biz to pulling cofig for multi biz. 

![image.png](https://cdn.nlark.com/yuque/0/2023/png/145710/1699414406192-03c5207c-912a-4137-b4b4-b27007c0d0f0.png#averageHue=%23232427&clientId=u1275a813-6484-4&from=paste&height=360&id=udcf16b77&originHeight=360&originWidth=1448&originalType=binary&ratio=1&rotation=0&showTitle=false&size=137407&status=done&style=none&taskId=uba1ce80d-0bf8-4a89-ad70-63721503ec8&title=&width=1448)

Finally, we found that we can't use one apollo instance in multi biz in the same jvm, we need to create each apollo instance for each biz. So we need to import apollo in each biz with compile scope which will create isolated apollo instance in each biz classLoader.

But there comes a new problem, we need to hack the apollo code to make it can be loaded by biz classLoader without System properties pollution. How to maintain this code? We don't want to change the code in each biz. So we need to patch the code in the base. We will not talk about this in this article.

## Problem solution
1. add this adapter in base.
2. import apollo with compile scope in each biz.
```xml
 <dependency>
    <groupId>com.ctrip.framework.apollo</groupId>
    <artifactId>apollo-client</artifactId>
    <version>1.8.0</version>
</dependency>
```
3. add config in `META-INF/app.properties` in each biz
