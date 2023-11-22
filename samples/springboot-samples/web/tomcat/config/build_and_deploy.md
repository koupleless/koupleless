## 如何构建基座部署到k8s集群，然后部署模块到集群

> 前提需要安装好 minikube

### 基座部署
1. 执行 build.sh 构建基座镜像
2. 推送基座镜像到镜像中心
3. 执行 base-deployment.yaml (修改镜像地址与版本)
4. 执行 `minikube service base-web-single-host-service`
5. 访问 curl http://NodeIP:NodePort/order1 查看结果

### 模块构建与部署
1. 根据实际运行操作系统，[下载 arkctl ](https://github.com/sofastack/sofa-serverless/tree/master/arkctl/bin) , 并放入 `/usr/local/bin` 目录中
2. 在对应项目里执行模块构建与部署，例如对于 web/tomcat 需要在 web/tomcat 目录里，执行
```shell
arkctl deploy biz1/target/biz1-web-single-host-0.0.1-SNAPSHOT-ark-biz.jar --pod ${namespace}/${podName}
```
3. 访问 curl http://NodeIP:NodePort/order1 查看结果
