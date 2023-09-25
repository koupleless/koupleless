---
title: 模块发布与回滚
date: 2017-01-04
weight: 3
---

## 模块发布

修改 ModuleDeployment.spec.template.spec.module.version 字段和 ModuleDeployment.spec.template.spec.module.url 字段并重新 apply，即可实现新版本模块的分组发布，例如：

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
        version: '2.0.0'  # 注意：这里将 version 字段从 1.0.0 修改为了 2.0.0 即可实现模块新版本分组发布
        # 注意：url 字段可以修改为新的 jar 包地址，也可以不用修改
        url: http://serverless-opensource.oss-cn-shanghai.aliyuncs.com/module-packages/stable/dynamic-provider-1.0.0-ark-biz.jar
  replicas: 2
  strategy:
    upgradePolicy: installThenUninstall
    needConfirm: true
    useBeta: false
    batchCount: 2
```

如果要自定义模块发布运维策略可配置 deployType 和 strategy，具体可参考[模块发布运维策略](https://yuque.antfin.com/middleware/sofa-serverless/%E6%A8%A1%E5%9D%97%E5%8F%91%E5%B8%83%E8%BF%90%E7%BB%B4%E7%AD%96%E7%95%A5)。

样例演示的是使用 kubectl 方式，直接调用 K8S APIServer 修改 ModuleDeployment CR 一样能实现分组发布。



## 模块回滚

修改 ModuleDeployment CR 的 url 字段（可选）和 version 字段并重新 apply，即可实现模块新版本分组发布，例如：

如果要自定义模块发布运维策略（比如分组、Beta、暂停等）可配置 deployType 和 strategy，具体可参考模块发布运维策略。

样例演示的是使用 kubectl 方式，直接调用 K8S APIServer 修改 ModuleDeployment CR 一样能实现分组回滚。