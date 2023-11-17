---
title: ehcache 的多模块化最佳实践
date: 2023-10-10T20:32:35+08:00
weight: 2
---

## 为什么需要最佳实践
1. 多应用使用相同的 ehcache name，导致缓存互相覆盖

在多模块应用合并部署情况下，如果不同模块应用在合并前指定的 ehcache 名字相同，会导致多个模块应用使用同一个 ehcache，这样会导致缓存数据不一致，为避免这种情况发生，可以使用这里提供的最佳实践方式。

例如
ehcache 如果放在基座里，多个模块会复用这个类的 static 变量，且由于这个 map 的key为 cacheManagerName, 会出现 cacheName 冲突，然后在一个 JVM 里是不允许的。
![static_cache_manager_map.png](static_cache_manager_map.png)

![same_cache_manager_check.png](same_cache_manager_check.png)

![already_exists_exception.png](already_exists_exception.png)

所以需要放在模块里，并且每次卸载时，需要做清理。

或者做治理，
如果要解决这样的问题，需要每个应用修改代码更改 cache 名