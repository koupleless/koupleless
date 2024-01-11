# 实验内容


1. 不同模块注册并读取不同的 dataId
2. 不同模块使用相同 dataId

## 实验任务

### 前置准备
nacos采用标准的 CS 分布式架构, 在实验前需要先将 server 端启动。
启动步骤参考: [Nacos 快速开始](https://nacos.io/zh-cn/docs/quick-start.html)

下面我们将启动一个基座 base 和两个模块 biz1 和 biz2, 模块与其对应的 dataId 映射如下
- base模块: base
- biz1模块: biz
- biz2模块: biz

> 由于 nacos 依赖 prometheus 做指标采集, 由于其内部机制, io.prometheus:simpleclient 该 GA 不能做包与类下沉基座, 否则模块会启动不了


### 不同模块注册并读取不同的 dataId

启动 base、biz1、biz2 模块后, 验证 dataId的读取逻辑
- base: 调用 *curl http://localhost:8090/config/get* 返回false
- biz1: 调用 *curl http://localhost:8090/biz1/config/get* 返回false
- biz2: 调用 *curl http://localhost:8090/biz2/config/get* 返回false

执行 *curl -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs?dataId=base&group=DEFAULT_GROUP&content=useLocalCache=true"* 将 dataId base 的值修改为 true

再次调用 *curl http://localhost:8090/config/get* 发现值已经范围 true, 说明基座的 dataId已经修改成功且生效。而调用 biz 查询依旧是 false

### 不同模块使用相同 dataId

在上述基础上, 由于模块 biz1 和 biz2 都使用 "biz" 作为 dataId, 期望修改该 dataId 的值后, biz1 和 biz2 读取的值同时发生变化。

执行 *curl -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs?dataId=biz&group=DEFAULT_GROUP&content=useLocalCache=true"* 将 dataId biz 的值修改为 true

- biz1: 调用 *curl http://localhost:8090/biz1/config/get* 返回 true
- biz2: 调用 *curl http://localhost:8090/biz2/config/get* 返回 true

发现, 共享同 dataId 的两个模块都读取到了修改的值。