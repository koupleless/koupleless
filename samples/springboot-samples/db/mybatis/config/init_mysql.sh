#!/bin/bash

# 定义 MySQL 容器的配置
MYSQL_CONTAINER_NAME="mysql_container"
MYSQL_ROOT_PASSWORD="Zfj1995!"
MYSQL_DATABASE="test"
MYSQL_USER="myuser"
MYSQL_PASSWORD="mypassword"

## 启动 MySQL 容器
docker run --name $MYSQL_CONTAINER_NAME -e MYSQL_ROOT_PASSWORD=$MYSQL_ROOT_PASSWORD -e MYSQL_DATABASE=$MYSQL_DATABASE -e MYSQL_USER=$MYSQL_USER -e MYSQL_PASSWORD=$MYSQL_PASSWORD -p 3306:3306 -d mysql:8.2.0

# 等待 MySQL 服务启动
echo "Waiting for MySQL to start..."
sleep 5

# 在这里，我们可以编写一个循环来检测 MySQL 是否真的已经准备好了
for i in {1..12}; do
   if docker exec $MYSQL_CONTAINER_NAME mysql -u root -p$MYSQL_ROOT_PASSWORD -e "status" | grep 'root@localhost'; then
       echo "MySQL is ready."
       break
   else
       echo "Waiting for MySQL to be ready..."
       sleep 5
   fi
done

# 初始化数据库和表的 SQL 语句
# 执行 SQL 语句
echo "Initializing database and table..."
docker exec -i $MYSQL_CONTAINER_NAME mysql -u root -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE < ../base/create-table.sql
docker exec -i $MYSQL_CONTAINER_NAME mysql -u root -p$MYSQL_ROOT_PASSWORD $MYSQL_DATABASE < ../biz1/create-table.sql

echo "Fetching the list of tables in the database..."
TABLES=$(docker exec $MYSQL_CONTAINER_NAME mysql -u root -p$MYSQL_ROOT_PASSWORD -D $MYSQL_DATABASE -e "SHOW TABLES;" | grep -v "Tables_in")
echo "List of tables in the '$MYSQL_DATABASE' database:"
echo "$TABLES"

echo "Initialization done."