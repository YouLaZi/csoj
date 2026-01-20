#!/bin/bash
# MySQL 主从复制初始化脚本
# 用于在 Docker 容器中设置主从复制关系

set -e

echo "==================================="
echo "MySQL 主从复制初始化脚本"
echo "==================================="

# 配置变量
MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-123456}
REPLICATION_USER=${REPLICATION_USER:-replicator}
REPLICATION_PASSWORD=${REPLICATION_PASSWORD:-replicator_password}
MASTER_CONTAINER=${MASTER_CONTAINER:-csoj-mysql-master}
SLAVE_CONTAINER=${SLAVE_CONTAINER:-csoj-mysql-slave}

echo "等待主库启动..."
while ! docker exec $MASTER_CONTAINER mysqladmin ping -h localhost --silent; do
    echo "  主库尚未就绪，等待中..."
    sleep 2
done
echo "  主库已就绪！"

echo "等待从库启动..."
while ! docker exec $SLAVE_CONTAINER mysqladmin ping -h localhost --silent; do
    echo "  从库尚未就绪，等待中..."
    sleep 2
done
echo "  从库已就绪！"

# 在主库创建复制用户
echo ""
echo "在主库创建复制用户..."
docker exec $MASTER_CONTAINER mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "
CREATE USER IF NOT EXISTS '${REPLICATION_USER}'@'%' IDENTIFIED BY '${REPLICATION_PASSWORD}';
GRANT REPLICATION SLAVE ON *.* TO '${REPLICATION_USER}'@'%';
FLUSH PRIVILEGES;
"
echo "  复制用户创建完成！"

# 获取主库的 binlog 位置
echo ""
echo "获取主库 binlog 位置..."
MASTER_STATUS=$(docker exec $MASTER_CONTAINER mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "SHOW MASTER STATUS\G")
echo "$MASTER_STATUS"

# 提取 binlog 文件和位置
BINLOG_FILE=$(echo "$MASTER_STATUS" | grep "File:" | awk '{print $2}')
BINLOG_POSITION=$(echo "$MASTER_STATUS" | grep "Position:" | awk '{print $2}')
MASTER_HOST=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $MASTER_CONTAINER)

echo "  Binlog 文件: $BINLOG_FILE"
echo "  Binlog 位置: $BINLOG_POSITION"
echo "  主库 IP: $MASTER_HOST"

# 配置从库
echo ""
echo "配置从库复制..."
docker exec $SLAVE_CONTAINER mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "
STOP SLAVE;
CHANGE MASTER TO
    MASTER_HOST='${MASTER_HOST}',
    MASTER_USER='${REPLICATION_USER}',
    MASTER_PASSWORD='${REPLICATION_PASSWORD}',
    MASTER_LOG_FILE='${BINLOG_FILE}',
    MASTER_LOG_POSITION=${BINLOG_POSITION};
START SLAVE;
"
echo "  从库配置完成！"

# 检查从库状态
echo ""
echo "检查从库复制状态..."
SLAVE_STATUS=$(docker exec $SLAVE_CONTAINER mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "SHOW SLAVE STATUS\G")

# 检查 IO 和 SQL 线程状态
IO_RUNNING=$(echo "$SLAVE_STATUS" | grep "Slave_IO_Running:" | awk '{print $2}')
SQL_RUNNING=$(echo "$SLAVE_STATUS" | grep "Slave_SQL_Running:" | awk '{print $2}')

echo "  IO 线程状态: $IO_RUNNING"
echo "  SQL 线程状态: $SQL_RUNNING"

if [ "$IO_RUNNING" = "Yes" ] && [ "$SQL_RUNNING" = "Yes" ]; then
    echo ""
    echo "==================================="
    echo "主从复制设置成功！"
    echo "==================================="
else
    echo ""
    echo "==================================="
    echo "警告：主从复制可能存在问题！"
    echo "==================================="
    echo "请检查从库错误日志："
    echo "$SLAVE_STATUS" | grep -E "Last_Error|LastError"
fi

# 显示主库状态
echo ""
echo "主库状态："
docker exec $MASTER_CONTAINER mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "SHOW PROCESSLIST\G" | grep -A 5 "system user"

echo ""
echo "从库状态："
docker exec $SLAVE_CONTAINER mysql -uroot -p${MYSQL_ROOT_PASSWORD} -e "SHOW SLAVE STATUS\G" | grep -E "Slave_IO_Running|Slave_SQL_Running|Seconds_Behind_Master|Last_Error"
