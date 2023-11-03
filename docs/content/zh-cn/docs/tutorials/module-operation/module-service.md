---
title: 模块Service
weight: 800
---

## ModuleService 简介
K8S 通过 [Service](https://kubernetes.io/docs/concepts/services-networking/service/) ，将运行在一个或一组 Pod 上的网络应用程序公开为网络服务。
模块也支持 Module 相关的 Service ，在模块发布时自动创建一个 service 来服务模块，将安装在一个或一组 Pod 的模块公开为网络服务。
具体见：OperationStrategy.ServiceStrategy
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
  name: moduledeployment-sample-provider
spec:
  baseDeploymentName: dynamic-stock-deployment
  template:
    spec:
      module:
        name: dynamic-provider
        version: '1.0.0'
        url: http://serverless-opensource.oss-cn-shanghai.aliyuncs.com/module-packages/stable/dynamic-provider-1.0.0-ark-biz.jar
  replicas: 1
  operationStrategy:
    needConfirm: false
    grayTimeBetweenBatchSeconds: 120
    useBeta: false
    batchCount: 1
    upgradePolicy: install_then_uninstall
    serviceStrategy:
      enableModuleService: true
      port: 8080
      targetPort: 8080
  schedulingStrategy:
    schedulingPolicy: scatter
```
## 字段解释
OperationStrategy.ServiceStrategy 字段解释如下：

|  | 字段解释 | 取值范围 |
| --- | --- | --- |
| EnableModuleService | 开启模块service | true or false |
| Port | 公开的端口 | 1 到 65535 |
| TargetPort | pod上要访问的端口 | 1 到 65535 |

## 示例
```bash
kubectl apply -f sofa-serverless/module-controller/config/samples/module-deployment_v1alpha1_moduledeployment_provider.yaml --namespace yournamespace
```
自动创建的模块的 service
```yaml
apiVersion: v1
kind: Service
metadata:
  creationTimestamp: "2023-11-03T09:52:22Z"
  name: dynamic-provider-service
  namespace: default
  resourceVersion: "28170024"
  uid: 1f85e468-65e3-4181-b40e-48959a069df5
spec:
  clusterIP: 10.0.147.22
  clusterIPs:
  - 10.0.147.22
  externalTrafficPolicy: Cluster
  internalTrafficPolicy: Cluster
  ipFamilies:
  - IPv4
  ipFamilyPolicy: SingleStack
  ports:
  - name: http
    nodePort: 32232
    port: 8080
    protocol: TCP
    targetPort: 8080
  selector:
    module.serverless.alipay.com/dynamic-provider: "true"
  sessionAffinity: None
  type: NodePort
status:
  loadBalancer: {}
```
