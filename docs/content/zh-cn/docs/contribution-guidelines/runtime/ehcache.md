---
title: ehcache 的多模块化最佳实践
date: 2023-10-10T20:32:35+08:00
weight: 2
---

## 为什么需要最佳实践
CacheManager 初始化的时候存在共用 static 变量，多应用使用相同的 ehcache name，导致缓存互相覆盖。

## 最佳实践的几个要求
1. 基座里必须引入 ehcache，模块里复用基座

在 springboot 里 ehcache 的初始化需要通过 Spring 里定义的 EhCacheCacheConfiguration 来创建，由于 EhCacheCacheConfiguration 是属于 Spring, Spring 统一放在基座里。
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1700202934067-7a0d74b7-b765-4c96-ab95-6189602235b8.png#clientId=u4cdbd480-e8bb-4&from=paste&height=679&id=u3a86e2ae&originHeight=1358&originWidth=2284&originalType=binary&ratio=2&rotation=0&showTitle=false&size=801737&status=done&style=none&taskId=ub2119003-e3dd-4276-83a3-bc0a8598185&title=&width=1142)

这里在初始化的时候，在做 Bean 初始化的条件判断时会走到类的检验，
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1700203147758-c2f4f211-27b1-408a-8a59-04b54a0602f3.png#clientId=u4cdbd480-e8bb-4&from=paste&height=532&id=ea4Xj&originHeight=1064&originWidth=1052&originalType=binary&ratio=2&rotation=0&showTitle=false&size=607056&status=done&style=none&taskId=u59dc4240-37cd-4a97-8b57-0e71250149b&title=&width=526)
如果 net.sf.ehcache.CacheManager 是。这里会走到 java native 方法上做判断，从当前类所在的 ClassLoader 里查找 net.sf.ehcache.CacheManager 类，所以基座里必须引入这个依赖，否则会报 ClassNotFound 的错误。
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1700203220867-62f2b7be-e853-488c-a6bc-a95c874793f1.png#clientId=u4cdbd480-e8bb-4&from=paste&height=97&id=u3ca967f5&originHeight=194&originWidth=1798&originalType=binary&ratio=2&rotation=0&showTitle=false&size=104469&status=done&style=none&taskId=u4957f800-31ee-40b3-bb09-487b9ab16ba&title=&width=899)

2. 模块里将引入的 ehcache 排包掉（scope设置成 provide，或者使用自动瘦身能力）

模块使用自己 引入的 ehcache，照理可以避免共用基座 CacheManager 类里的 static 变量，而导致报错的问题。但是实际测试发现，模块安装的时候，在初始化 enCacheCacheManager 时，
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1700203897715-c9f97922-b466-4e73-8319-1a0f5ec3cc73.png#clientId=u4cdbd480-e8bb-4&from=paste&height=211&id=uaa50406f&originHeight=422&originWidth=2048&originalType=binary&ratio=2&rotation=0&showTitle=false&size=235120&status=done&style=none&taskId=ub3d92b21-fec0-4462-92ad-91449dcea2d&title=&width=1024)
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1700203915265-f42253e4-1ff4-4088-a87e-8b6e063540ba.png#clientId=u4cdbd480-e8bb-4&from=paste&height=107&id=uedd0a010&originHeight=214&originWidth=1258&originalType=binary&ratio=2&rotation=0&showTitle=false&size=101140&status=done&style=none&taskId=u044240e0-fe55-4f77-b63e-41ebf9eca47&title=&width=629)
这里在 new 对象时，需要先获得对象所属类的 CacheManager 是基座的 CacheManager。这里也不能讲 CacheManager 由模块 compile 引入，否则会出现一个类由多个不同 ClassLoader 引入导致的问题。
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1700212320690-8112f0f7-7ab7-48a7-8d9d-95aa3d49492a.png#clientId=u4cdbd480-e8bb-4&from=paste&height=145&id=ud90248f9&originHeight=290&originWidth=2736&originalType=binary&ratio=2&rotation=0&showTitle=false&size=294518&status=done&style=none&taskId=ue9c723ea-0a3b-4854-b069-402238e5fcd&title=&width=1368)

所以结论是，这里需要全部委托给基座加载。

## 最佳实践的方式
1. 模块 ehcache 排包瘦身委托给基座加载
2. 如果多个模块里有多个相同的 cacheName，需要修改 cacheName 为不同值。
3. 如果不想改代码的方式修改 cache name，可以通过打包插件的方式动态替换 cacheName
```xml
 <plugin>
    <groupId>com.google.code.maven-replacer-plugin</groupId>
    <artifactId>replacer</artifactId>
    <version>1.5.3</version>
    <executions>
        <!-- 打包前进行替换 -->
        <execution>
            <phase>prepare-package</phase>
            <goals>
                <goal>replace</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <!-- 自动识别到项目target文件夹 -->
        <basedir>${build.directory}</basedir>
        <!-- 替换的文件所在目录规则 -->
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

4. 需要把 FactoryBean 的 shared 设置成 false
```java
@Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
        EhCacheManagerFactoryBean factoryBean = new EhCacheManagerFactoryBean();

        // 需要把 factoryBean 的 share 属性设置成 false
        factoryBean.setShared(true);
//        factoryBean.setShared(false);
        factoryBean.setCacheManagerName("biz1EhcacheCacheManager");
        factoryBean.setConfigLocation(new ClassPathResource("ehcache.xml"));
        return factoryBean;
    }
```
否则会进入这段逻辑，初始化 CacheManager 的static 变量 instance. 该变量如果有值，且如果模块里 shared 也是ture 的化，就会重新复用 CacheManager 的 instance，从而拿到基座的 CacheManager, 从而报错。
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1700360794825-3f7f4a63-22bc-49ea-81d1-83bd94804087.png#clientId=u2481e0c2-f328-4&from=paste&height=399&id=u7432be71&originHeight=798&originWidth=1596&originalType=binary&ratio=2&rotation=0&showTitle=false&size=422965&status=done&style=none&taskId=u1e450639-4846-4b6a-9862-bac787ae8e5&title=&width=798)
![image.png](https://intranetproxy.alipay.com/skylark/lark/0/2023/png/149473/1700359643422-7b252689-7e0c-41f3-995e-cbc40726136e.png#clientId=u2481e0c2-f328-4&from=paste&height=161&id=u80efa85e&originHeight=322&originWidth=2426&originalType=binary&ratio=2&rotation=0&showTitle=false&size=339519&status=done&style=none&taskId=u15aeda8f-e089-4bf0-8bc7-e47eff9d2f0&title=&width=1213)


## 最佳实践的样例
样例工程请[参考这里](https://github.com/sofastack/sofa-serverless/tree/master/samples/springboot-samples/cache/ehcache)
