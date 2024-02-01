<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

# Experiment

1. Read different dataId in different modules
2. Read the same dataId in different modules

## Experiment task

### Preparations
nacos is based on the standard CS distributed architecture, and the server side needs to be started before the experiment.
how to start nacos server: [Nacos Quick Start](https://nacos.io/en-us/docs/quick-start.html)

now we will start a base and two modules biz1 and biz2, the mapping between module and dataId is as follows
- base: base
- biz1 module: biz
- biz2 module: biz

> Nacos using prometheus for metrics collection, due to its internal mechanism, io.prometheus:simpleclient cannot be delegated to base, otherwise the module will not start

### Writing and reading different dataId in different modules

start base, biz1 and biz2 modules, verify the logic of reading dataId
- base: `curl http://localhost:8090/config/get` return false
- biz1: `curl http://localhost:8090/biz1/config/get` return false
- biz2: `curl http://localhost:8090/biz2/config/get` return false

run *curl -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs?dataId=base&group=DEFAULT_GROUP&content=useLocalCache=true"* to set the value of dataId base to true

run `curl http://localhost:8090/config/get` again, we can see the value is true, which means the dataId of base has been modified successfully and take effect. But the value of biz is still false

### using the same dataId in different modules

based on the above, because the modules biz1 and biz2 both use "biz" as dataId, it is expected that after modifying the value of dataId, the values read by biz1 and biz2 will change at the same time.

run `curl -X POST "http://127.0.0.1:8848/nacos/v1/cs/configs?dataId=biz&group=DEFAULT_GROUP&content=useLocalCache=true"` to set the value of dataId biz to true

- biz1: `curl http://localhost:8090/biz1/config/get` return true
- biz2: `curl http://localhost:8090/biz2/config/get` return true

we can found that the two modules sharing the same dataId have read the modified value.
