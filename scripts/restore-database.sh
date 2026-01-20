#!/bin/bash

# CSOJ 数据库恢复脚本
# 用于从备份文件恢复 MySQL 数据库

set -e

# 配置
BACKUP_DIR="${BACKUP_DIR:-./backups}"
MYSQL_HOST="${MYSQL_HOST:-localhost}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_DATABASE="${MYSQL_DATABASE:-oj_db}"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  CSOJ 数据库恢复脚本${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 检查是否提供了备份文件参数
if [ -z "$1" ]; then
    echo -e "${YELLOW}用法: $0 <备份文件>${NC}"
    echo ""
    echo "可用的备份文件:"
    ls -lh "$BACKUP_DIR"/${MYSQL_DATABASE}_*.sql.gz 2>/dev/null || echo "暂无备份文件"
    echo ""
    echo "或者使用最新备份:"
    echo "  $0 latest"
    exit 1
fi

# 确定备份文件路径
if [ "$1" = "latest" ]; then
    BACKUP_FILE="${BACKUP_DIR}/${MYSQL_DATABASE}_latest.sql.gz"
else
    if [ -f "$1" ]; then
        BACKUP_FILE="$1"
    else
        BACKUP_FILE="${BACKUP_DIR}/$1"
        if [ ! -f "$BACKUP_FILE" ]; then
            BACKUP_FILE="${BACKUP_FILE}.gz"
            if [ ! -f "$BACKUP_FILE" ]; then
                echo -e "${RED}错误: 找不到备份文件 $1${NC}"
                exit 1
            fi
        fi
    fi
fi

echo "备份文件: $BACKUP_FILE"

# 获取 MySQL 密码
if [ -z "$MYSQL_ROOT_PASSWORD" ]; then
    if [ -f ".env" ]; then
        MYSQL_ROOT_PASSWORD=$(grep MYSQL_ROOT_PASSWORD .env | cut -d '=' -f2)
    else
        echo -e "${RED}错误: 未找到 MySQL 密码${NC}"
        exit 1
    fi
fi

# 警告确认
echo ""
echo -e "${RED}========================================${NC}"
echo -e "${RED}  警告：此操作将覆盖当前数据库！${NC}"
echo -e "${RED}========================================${NC}"
echo ""
read -p "确定要恢复数据库吗？(yes/no): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    echo "操作已取消"
    exit 0
fi

# 解压并恢复
echo ""
echo -e "${YELLOW}正在恢复数据库...${NC}"

# 临时解压文件
TEMP_FILE="${BACKUP_FILE%.gz}.tmp.sql"
gunzip -c "$BACKUP_FILE" > "$TEMP_FILE"

# 检查是 Docker 环境还是本地 MySQL
if command -v docker &> /dev/null && docker ps | grep -q "csoj-mysql"; then
    # Docker 环境
    echo "检测到 Docker 环境，使用 docker exec 恢复..."
    docker exec -i csoj-mysql mysql -u root -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE" < "$TEMP_FILE"
else
    # 本地 MySQL 环境
    echo "检测到本地 MySQL 环境..."
    mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE" < "$TEMP_FILE"
fi

# 清理临时文件
rm -f "$TEMP_FILE"

# 检查恢复是否成功
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}✓ 数据库恢复成功！${NC}"
    echo "恢复的数据库: $MYSQL_DATABASE"
    echo "使用的备份: $BACKUP_FILE"
else
    echo ""
    echo -e "${RED}✗ 数据库恢复失败！${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  恢复完成！${NC}"
echo -e "${GREEN}========================================${NC}"
