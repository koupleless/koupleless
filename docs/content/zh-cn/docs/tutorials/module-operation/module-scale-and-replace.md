---
title: 模块扩缩容与替换
weight: 400
---

<a name="uHuOJ"></a>
## 模块扩缩容
修改 ModuleDeployment CR 的 replicas 字段并重新 apply，即可实现模块扩缩容，例如：
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
        name: dynamic-provider
        version: '1.0.0'
        url: http://serverless-opensource.oss-cn-shanghai.aliyuncs.com/module-packages/stable/dynamic-provider-1.0.0-ark-biz.jar
  replicas: 2  # 注意：在此处修改模块实例 Module 副本数，实现扩缩容
  operationStrategy:
    upgradePolicy: installThenUninstall
    needConfirm: true
    useBeta: false
    batchCount: 2
  schedulingStrategy: # 此处可自定义调度策略
    schedulingType: Scatter  
```

如果要自定义模块发布运维策略可配置 operationStrategy 和 schedulingStrategy，具体可参考[模块发布运维策略](../operation-and-scheduling-strategy)。<br />样例演示的是使用 kubectl 方式，直接调用 K8S APIServer 修改 ModuleDeployment CR 一样能实现扩缩容。

<a name="T0hpZ"></a>
## 模块替换
在 K8S 集群中删除一个 Module CR 资源即可完成模块替换，例如：
```bash
kubectl delete yourmodule --namespace yournamespace
```
其中 _yourmodule_ 替换成您的 Module CR 实体名字（Module 的 metadata name），_yournamespace_ 替换成您的 namespace。<br />样例演示的是使用 kubectl 方式，直接调用 K8S APIServer 删除 Module CR 一样能实现模块替换。


<br/>
<br/>
