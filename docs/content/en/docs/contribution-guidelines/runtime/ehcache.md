---
title: Best Practices for Multi-Module with ehcache
date: 2024-01-25T10:28:32+08:00
description: Best practices for implementing multi-module architecture with ehcache in Koupleless.
weight: 2
---

## Why Best Practices are Needed
During CacheManager initialization, there are shared static variables causing issues when multiple applications use the same Ehcache name, resulting in cache overlap.

## Requirements for Best Practices
1. Base module must include Ehcache, and modules should reuse the base.

In Spring Boot, Ehcache initialization requires creating it through the EhCacheCacheConfiguration defined in Spring, which belongs to Spring and is usually placed in the base module.
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1700202934067-7a0d74b7-b765-4c96-ab95-6189602235b8.png#clientId=u4cdbd480-e8bb-4&from=paste&height=679&id=u3a86e2ae&originHeight=1358&originWidth=2284&originalType=binary&ratio=2&rotation=0&showTitle=false&size=801737&status=done&style=none&taskId=ub2119003-e3dd-4276-83a3-bc0a8598185&title=&width=1142)

During bean initialization, the condition check will lead to class verification,
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1700203147758-c2f4f211-27b1-408a-8a59-04b54a0602f3.png#clientId=u4cdbd480-e8bb-4&from=paste&height=532&id=ea4Xj&originHeight=1064&originWidth=1052&originalType=binary&ratio=2&rotation=0&showTitle=false&size=607056&status=done&style=none&taskId=u59dc4240-37cd-4a97-8b57-0e71250149b&title=&width=526)
if net.sf.ehcache.CacheManager is found, it will use a Java native method to search for the net.sf.ehcache.CacheManager class in the ClassLoader where the class belongs. Therefore, the base module must include this dependency; otherwise, it will result in ClassNotFound errors.
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1700203220867-62f2b7be-e853-488c-a6bc-a95c874793f1.png#clientId=u4cdbd480-e8bb-4&from=paste&height=97&id=u3ca967f5&originHeight=194&originWidth=1798&originalType=binary&ratio=2&rotation=0&showTitle=false&size=104469&status=done&style=none&taskId=u4957f800-31ee-40b3-bb09-487b9ab16ba&title=&width=899)

2. Modules should exclude the included Ehcache (set scope to provided or utilize automatic slimming capabilities).

When a module uses its own imported Ehcache, theoretically, it should avoid sharing static variables in the base CacheManager class, thus preventing potential errors. However, in our actual testing, during the module installation process, when initializing the EhCacheCacheManager, we encountered an issue where, during the creation of a new object, it required obtaining the CacheManager belonging to the class of the object, which in turn should be the base CacheManager. Importantly, we cannot include the CacheManager dependency in the module's compilation, as it would lead to conflicts caused by a single class being imported by multiple different ClassLoaders.

When a module uses its own imported Ehcache, theoretically, it should avoid sharing static variables in the base CacheManager class, thus preventing potential errors. However, in our actual testing, during the module installation process, when initializing the EhCacheCacheManager, we encountered an issue where,
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1700203897715-c9f97922-b466-4e73-8319-1a0f5ec3cc73.png#clientId=u4cdbd480-e8bb-4&from=paste&height=211&id=uaa50406f&originHeight=422&originWidth=2048&originalType=binary&ratio=2&rotation=0&showTitle=false&size=235120&status=done&style=none&taskId=ub3d92b21-fec0-4462-92ad-91449dcea2d&title=&width=1024)
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1700203915265-f42253e4-1ff4-4088-a87e-8b6e063540ba.png#clientId=u4cdbd480-e8bb-4&from=paste&height=107&id=uedd0a010&originHeight=214&originWidth=1258&originalType=binary&ratio=2&rotation=0&showTitle=false&size=101140&status=done&style=none&taskId=u044240e0-fe55-4f77-b63e-41ebf9eca47&title=&width=629)
during the creation of a new object, it required obtaining the CacheManager belonging to the class of the object, which in turn should be the base CacheManager. Importantly, we cannot include the CacheManager dependency in the module's compilation, as it would lead to conflicts caused by a single class being imported by multiple different ClassLoaders.
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1700212320690-8112f0f7-7ab7-48a7-8d9d-95aa3d49492a.png#clientId=u4cdbd480-e8bb-4&from=paste&height=145&id=ud90248f9&originHeight=290&originWidth=2736&originalType=binary&ratio=2&rotation=0&showTitle=false&size=294518&status=done&style=none&taskId=ue9c723ea-0a3b-4854-b069-402238e5fcd&title=&width=1368)

Therefore, all loading should be delegated to the base module.

### Best Practice Approach
1. Delegate module Ehcache slimming to the base.
2. If multiple modules have the same cacheName, modify cacheName to be different.
3. If you don't want to change the code to modify cache name, you can dynamically replace cacheName through packaging plugins.
```xml
 <plugin>
    <groupId>com.google.code.maven-replacer-plugin</groupId>
    <artifactId>replacer</artifactId>
    <version>1.5.3</version>
    <executions>
        <!-- Perform replacement before packaging -->
        <execution>
            <phase>prepare-package</phase>
            <goals>
                <goal>replace</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <!-- Automatically recognize the project's target folder -->
        <basedir>${build.directory}</basedir>
        <!-- Directory rules for replacement files -->
        <includes>
            <include>classes/j2cache/*.properties</include>
        </includes>
        <replacements>
            <replacement>
                <token>ehcache.ehcache.name=f6-cache</token>
                <value>ehcache.ehcache.name=f6-${parent.artifactId}-cache</value>
            </replacement>

        </replacements>
    </configuration>
</plugin>
```

4. Set the shared property of the FactoryBean to false.
```java
@Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
        EhCacheManagerFactoryBean factoryBean = new EhCacheManagerFactoryBean();

        // Set the factoryBean's shared property to false
        factoryBean.setShared(true);
//        factoryBean.setShared(false);
        factoryBean.setCacheManagerName("biz1EhcacheCacheManager");
        factoryBean.setConfigLocation(new ClassPathResource("ehcache.xml"));
        return factoryBean;
    }
```
Otherwise, it will enter this logic, initializing the static variable instance of CacheManager. If this variable has a value, and if shared is true in the module, it will reuse the CacheManager's instance, leading to errors.
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1700360794825-3f7f4a63-22bc-49ea-81d1-83bd94804087.png#clientId=u2481e0c2-f328-4&from=paste&height=399&id=u7432be71&originHeight=798&originWidth=1596&originalType=binary&ratio=2&rotation=0&showTitle=false&size=422965&status=done&style=none&taskId=u1e450639-4846-4b6a-9862-bac787ae8e5&title=&width=798)
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1700359643422-7b252689-7e0c-41f3-995e-cbc40726136e.png#clientId=u2481e0c2-f328-4&from=paste&height=161&id=u80efa85e&originHeight=322&originWidth=2426&originalType=binary&ratio=2&rotation=0&showTitle=false&size=339519&status=done&style=none&taskId=u15aeda8f-e089-4bf0-8bc7-e47eff9d2f0&title=&width=1213)


## Example of Best Practices
For an example project, please[refer to here](https://github.com/koupleless/koupleless/tree/master/samples/springboot-samples/cache/ehcache)
