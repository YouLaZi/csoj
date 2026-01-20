#!/bin/bash

# CSOJ 数据库自动备份脚本
# 用于自动备份 MySQL 数据库

set -e

# 配置
BACKUP_DIR="${BACKUP_DIR:-./backups}"
MYSQL_HOST="${MYSQL_HOST:-localhost}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_DATABASE="${MYSQL_DATABASE:-oj_db}"
MYSQL_USER="${MYSQL_USER:-root}"
BACKUP_RETENTION_DAYS="${BACKUP_RETENTION_DAYS:-7}"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  CSOJ 数据库备份脚本${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 创建备份目录
mkdir -p "$BACKUP_DIR"

# 生成备份文件名（带时间戳）
BACKUP_FILE="$BACKUP_DIR/${MYSQL_DATABASE}_$(date +%Y%m%d_%H%M%S).sql"
LATEST_LINK="$BACKUP_DIR/${MYSQL_DATABASE}_latest.sql"

echo -e "${YELLOW}开始备份数据库...${NC}"
echo "数据库: $MYSQL_DATABASE"
echo "备份文件: $BACKUP_FILE"

# 获取 MySQL 密码
if [ -z "$MYSQL_ROOT_PASSWORD" ]; then
    # 从环境变量或 docker-compose 配置中读取
    if [ -f ".env" ]; then
        MYSQL_ROOT_PASSWORD=$(grep MYSQL_ROOT_PASSWORD .env | cut -d '=' -f2)
    else
        echo -e "${RED}错误: 未找到 MySQL 密码${NC}"
        echo "请设置 MYSQL_ROOT_PASSWORD 环境变量或在 .env 文件中配置"
        exit 1
    fi
fi

# 检查是 Docker 环境还是本地 MySQL
if command -v docker &> /dev/null && docker ps | grep -q "csoj-mysql"; then
    # Docker 环境
    echo "检测到 Docker 环境，使用 docker exec 备份..."
    docker exec csoj-mysql mysqldump -u root -p"$MYSQL_ROOT_PASSWORD" \
        --single-transaction \
        --routines \
        --triggers \
        --events \
        "$MYSQL_DATABASE" > "$BACKUP_FILE"
else
    # 本地 MySQL 环境
    echo "检测到本地 MySQL 环境..."
    mysqldump -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -p"$MYSQL_ROOT_PASSWORD" \
        --single-transaction \
        --routines \
        --triggers \
        --events \
        "$MYSQL_DATABASE" > "$BACKUP_FILE"
fi

# 检查备份是否成功
if [ $? -eq 0 ]; then
    # 压缩备份文件
    gzip "$BACKUP_FILE"
    BACKUP_FILE="${BACKUP_FILE}.gz"

    # 更新最新备份链接
    rm -f "$LATEST_LINK" "${LATEST_LINK}.gz"
    ln -sf "$(basename "$BACKUP_FILE")" "${LATEST_LINK}.gz"

    # 获取文件大小
    FILE_SIZE=$(du -h "$BACKUP_FILE" | cut -f1)

    echo -e "${GREEN}✓ 备份成功！${NC}"
    echo "备份文件: $BACKUP_FILE"
    echo "文件大小: $FILE_SIZE"
    echo "最新备份: ${LATEST_LINK}.gz"
else
    echo -e "${RED}✗ 备份失败！${NC}"
    exit 1
fi

# 清理过期备份
echo ""
echo -e "${YELLOW}清理 $BACKUP_RETENTION_DAYS 天前的备份...${NC}"
DELETED_FILES=$(find "$BACKUP_DIR" -name "${MYSQL_DATABASE}_*.sql.gz" -type f -mtime +$BACKUP_RETENTION_DAYS -delete -print | wc -l)
if [ "$DELETED_FILES" -gt 0 ]; then
    echo -e "${GREEN}✓ 已删除 $DELETED_FILES 个过期备份文件${NC}"
else
    echo "没有需要清理的过期文件"
fi

# 显示备份目录中的所有备份
echo ""
echo -e "${GREEN}当前备份文件列表:${NC}"
ls -lh "$BACKUP_DIR"/${MYSQL_DATABASE}_*.sql.gz 2>/dev/null || echo "暂无备份文件"

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  备份完成！${NC}"
echo -e "${GREEN}========================================${NC}"
