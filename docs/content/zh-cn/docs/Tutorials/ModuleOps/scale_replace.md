---
title: 模块扩缩容与替换
date: 2017-01-04
weight: 5
---

## 模块扩缩容
修改 ModuleDeployment CR 的 replicas 字段并重新 apply，即可实现模块扩缩容，例如：
```shell
kubectl apply -f sofa-serverless/module-controller/config/samples/module-deployment_v1alpha1_moduledeployment.yaml --namespace yournamespace
```
其中 deployment_v1alpha1_moduledeployment.yaml 替换成您的 ModuleDeployment 定义 yaml 文件，yournamespace 替换成您的 namespace。module-deployment_v1alpha1_moduledeployment.yaml 完整内容如下：

```yaml
apiVersion: serverless.alipay.com/v1alpha1
kind: ModuleDeployment
metadata:
  labels:
    app.kubernetes.io/name: moduledeployment
    app.kubernetes.io/instance: moduledeployment-sample
    app.kubernetes.io/part-of: module-controller
    app.kubernetes.io/managed-by: kustomize
    app.kubernetes.io/created-by: module-controller
  name: moduledeployment-sample
spec:
  baseDeploymentName: dynamic-stock-deployment
  template:
    spec:
      module:
        name: dynamic-provider
        version: '1.0.0'
        url: http://serverless-opensource.oss-cn-shanghai.aliyuncs.com/module-packages/stable/dynamic-provider-1.0.0-ark-biz.jar
  replicas: 2  # 注意：在此处修改模块实例 Module 副本数，实现扩缩容
  strategy:
    upgradePolicy: installThenUninstall
    needConfirm: true
    useBeta: false
    batchCount: 2
```

如果要自定义模块发布运维策略可配置 deployType 和 strategy，具体可参考[模块发布运维策略](https://yuque.antfin.com/middleware/sofa-serverless/%E6%A8%A1%E5%9D%97%E5%8F%91%E5%B8%83%E8%BF%90%E7%BB%B4%E7%AD%96%E7%95%A5)。

样例演示的是使用 kubectl 方式，直接调用 K8S APIServer 修改 ModuleDeployment CR 一样能实现扩缩容。

## 模块替换
修在 K8S 集群中删除一个 Module CR 资源即可完成模块替换，例如：
```shell
kubectl delete yourmodule --namespace yournamespace
```
其中 yourmodule 替换成您的 Module CR 实体名字（Module 的 metadata name），yournamespace 替换成您的 namespace。
样例演示的是使用 kubectl 方式，直接调用 K8S APIServer 删除 Module CR 一样能实现模块替换。