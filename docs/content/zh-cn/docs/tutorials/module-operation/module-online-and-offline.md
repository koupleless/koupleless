---
title: 模块上线与下线
weight: 100
---


## 模块上线
在 K8S 集群中创建一个 ModuleDeployment CR 资源即可完成模块上线，例如：
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
        version: '1.0.2'
        url: http://serverless-opensource.oss-cn-shanghai.aliyuncs.com/module-packages/stable/dynamic-provider-1.0.2-ark-biz.jar
  replicas: 2
  operationStrategy:  # 此处可自定义发布运维策略
    upgradePolicy: installThenUninstall
    needConfirm: true
    useBeta: false
    batchCount: 2
  schedulingStrategy: # 此处可自定义调度策略
    schedulingPolicy: Scatter
```

ModuleDeployment 所有字段可参考 [ModuleDeployment CRD 字段解释](/docs/contribution-guidelines/module-controller/crd-definition)。<br />如果要自定义模块发布运维策略（比如分组、Beta、暂停等）可配置 operationStrategy 和 schedulingStrategy，具体可参考[模块发布运维策略](../operation-and-scheduling-strategy)。<br />样例演示的是使用 kubectl 方式，直接调用 K8S APIServer 创建 ModuleDeployment CR 一样能实现模块分组上线。


## 模块下线
在 K8S 集群中删除一个 ModuleDeployment CR 资源即可完成模块下线，例如：
```bash
kubectl delete yourmoduledeployment --namespace yournamespace
```
其中 _yourmoduledeployment_ 替换成您的 ModuleDeployment 名字（ModuleDeployment 的 metadata name），_yournamespace_ 替换成您的 namespace。<br />如果要自定义模块发布运维策略（比如分组、Beta、暂停等）可配置 operationStrategy 和 schedulingStrategy，具体可参考[模块发布运维策略](../operation-and-scheduling-strategy)。<br />样例演示的是使用 kubectl 方式，直接调用 K8S APIServer 删除 ModuleDeployment CR 一样能实现模块分组下线。

<br/>
<br/>
