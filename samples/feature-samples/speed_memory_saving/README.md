
## 实验源码地址
后端地址： https://github.com/lvjing2/eladmin

前端地址： https://github.com/lvjing2/eladmin-web

注：该实验项目源自于 https://eladmin.vip/ ，本项为了验证模块化功能，对其进行了一些修改。

### 后端修改内容
1. 将原来 eladmin-system 里的代码拆分到了 eladmin-biz-mng、eladmin-biz-quartz、eladmin-biz-system 三个模块中
2. 修改打包方式，将原来的打包方式改为了模块化打包方式
3. 内部逻辑没有修改任何地方，只是将原来的代码拆分到了三个模块中

### 前端修改内容
由于模块 http 服务，统一增加了 web context path，所以需要修改前端的路由到后端的 url 地址，增加 web context path。

## 实验步骤
### 启动后端基础服务和初始化
1. 下载后端代码
```shell
git clone https://github.com/lvjing2/eladmin
```

2. 启动 mysql
```shell
docker run -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 --privileged=true --restart=always --name mariadb -d mariadb:10.6.16 --lower_case_table_names=1
```
3. 启动 redis
```shell
docker run -itd --name redis --restart=always -p 6379:6379 redis
```
4. 初始化 sql 脚本
https://github.com/lvjing2/eladmin/blob/master/sql/eladmin.sql

5. 启动基座
带上启动参数 `-Dspring.jmx.default-domain=${spring.application.name}`，点击 me.zhengjie.BaseAppRun#main 方法启动基座即可

6. 部署3个模块
```shell
arkctl deploy --sub modules/eladmin-biz-mng
arkctl deploy --sub modules/eladmin-biz-quartz
arkctl deploy --sub modules/eladmin-biz-system
```

### 启动前端服务
1. 下载前端代码
```shell
git clone https://github.com/lvjing2/eladmin-web
```

2. 启动前端代码
```shell
npm run dev
```

### 访问验证
打开 localhost:8013，访问页面功能都正常


## 实验 benchmark 
||||
|-|-|-|



