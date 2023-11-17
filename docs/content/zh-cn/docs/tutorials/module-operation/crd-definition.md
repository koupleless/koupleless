---
title: 所有 K8S 资源定义及部署方式
weight: 900
---

### 资源文件位置

1. [ModuleDeployment CRD 定义](https://github.com/sofastack/sofa-serverless/blob/master/module-controller/config/crd/bases/serverless.alipay.com_moduledeployments.yaml)
2. [ModuleReplicaset CRD 定义](https://github.com/sofastack/sofa-serverless/blob/master/module-controller/config/crd/bases/serverless.alipay.com_modulereplicasets.yaml) 
3. [ModuleTemplate CRD 定义](https://github.com/sofastack/sofa-serverless/blob/master/module-controller/config/crd/bases/serverless.alipay.com_moduletemplates.yaml)
4. [Module CRD 定义](https://github.com/sofastack/sofa-serverless/blob/master/module-controller/config/crd/bases/serverless.alipay.com_modules.yaml)
5. [Role 定义](https://github.com/sofastack/sofa-serverless/blob/master/module-controller/config/rbac/role.yaml)
6. [RBAC 定义](https://github.com/sofastack/sofa-serverless/blob/master/module-controller/config/rbac/role_binding.yaml)
7. [ServiceAccount 定义](https://github.com/sofastack/sofa-serverless/blob/master/module-controller/config/rbac/service_account.yaml)
8. [ModuleController 部署定义](https://github.com/sofastack/sofa-serverless/blob/master/module-controller/config/samples/module-deployment-controller.yaml)

### 部署方式

使用 kubectl apply 命令，依次 apply 上述 8 个资源文件，即可完成 ModuleController 部署。

<br/>
<br/>