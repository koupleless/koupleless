<div align="center">

English | [简体中文](./README-zh_CN.md)

</div>

## Source code of this experiment
Backend： https://github.com/lvjing2/eladmin

Frontend： https://github.com/lvjing2/eladmin-web

> This experiment project is derived from https://eladmin.vip/, and has been modified to verify the modular function.


### Backend modification content
1. We split the code in eladmin-system into three modules: eladmin-biz-mng, eladmin-biz-quartz, and eladmin-biz-system.
2. Modify the packaging method, change the original packaging method to modular packaging method
3. The internal logic has not been modified, just split the original code into three modules

### Frontend modification content
Cause the module used http service and add web context path, so need to modify the front-end routing to the back-end url address, add web context path.

## Experiment steps
### Start the backend base service and initialize
1. Download the backend code
```shell
git clone https://github.com/lvjing2/eladmin
```

2. Start mysql
```shell
docker run -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 --privileged=true --restart=always --name mariadb -d mariadb:10.6.16 --lower_case_table_names=1
```
3. Start redis
```shell
docker run -itd --name redis --restart=always -p 6379:6379 redis
```
4. Initialize the sql script
https://github.com/lvjing2/eladmin/blob/master/sql/eladmin.sql

5. Start the base
add startup parameter `-Dspring.jmx.default-domain=${spring.application.name}`, click me.zhengjie.BaseAppRun#main to start the base

6. Deploy 3 modules
```shell
arkctl deploy --sub modules/eladmin-biz-mng
arkctl deploy --sub modules/eladmin-biz-quartz
arkctl deploy --sub modules/eladmin-biz-system
```

### Start the frontend service
1. Download the frontend code
```shell
git clone https://github.com/lvjing2/eladmin-web
```

2. Start the frontend code
```shell
npm run dev
```

### Access verification
Open localhost:8013, all page functions are normal


## Experiment benchmark
||||
|-|-|-|



