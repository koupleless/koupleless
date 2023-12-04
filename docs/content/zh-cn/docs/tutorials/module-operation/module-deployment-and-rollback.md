---
title: 模块发布
weight: 200
---


## 模块发布
修改 ModuleDeployment.spec.template.spec.module.version 字段和 ModuleDeployment.spec.template.spec.module.url（可选）字段并重新 apply，即可实现新版本模块的分组发布，例如：
```bash
kubectl apply -f sofa-serverless/module-controller/config/samples/module-deployment_v1alpha1_moduledeployment.yaml --namespace yournamespace
```
其中 _deployment_v1alpha1_moduledeployment.yaml_ 替换成您的 ModuleDeployment 定义 yaml 文件，_yournamespace_ 替换成您的 namespace。module-deployment_v1alpha1_moduledeployment.yaml 完整内容如下：
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
        name: provider
        version: '2.0.0'  # 注意：这里将 version 字段从 1.0.2 修改为了 2.0.0 即可实现模块新版本分组发布
        # 注意：url 字段可以修改为新的 jar 包地址，也可以不用修改
        url: http://serverless-opensource.oss-cn-shanghai.aliyuncs.com/module-packages/stable/dynamic-provider-1.0.2-ark-biz.jar
  replicas: 2
  operationStrategy:
    upgradePolicy: install_then_uninstall
    needConfirm: true
    grayTimeBetweenBatchSeconds: 0
    useBeta: false
    batchCount: 2
  schedulingStrategy:
    schedulingPolicy: scatter
```

如果要自定义模块发布运维策略可配置 operationStrategy，具体可参考[模块发布运维策略](/docs/contribution-guidelines/module-controller/crd-definition)。<br />样例演示的是使用 kubectl 方式，直接调用 K8S APIServer 修改 ModuleDeployment CR 一样能实现分组发布。


## 模块回滚
重新修改 ModuleDeployment.spec.template.spec.module.version 字段和 ModuleDeployment.spec.template.spec.module.url（可选）字段并重新 apply，即可实现模块的分组回滚发布。

<br/>
<br/>
