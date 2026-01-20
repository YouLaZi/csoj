#!/bin/bash
# CSOJ 高可用集群启动脚本

set -e

echo "==================================="
echo "  CSOJ 高可用集群启动脚本"
echo "==================================="

# 配置
COMPOSE_FILE="docker-compose-ha.yml"
PROFILES="default,monitoring"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查 Docker
echo -e "\n${YELLOW}[1/5]${NC} 检查 Docker 环境..."
if ! command -v docker &> /dev/null; then
    echo -e "${RED}错误: Docker 未安装${NC}"
    exit 1
fi
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo -e "${RED}错误: Docker Compose 未安装${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Docker 环境正常${NC}"

# 创建必要的目录
echo -e "\n${YELLOW}[2/5]${NC} 创建数据目录..."
mkdir -p docker/prometheus
mkdir -p docker/grafana/dashboard
mkdir -p docker/grafana/datasource
mkdir -p docker/mysql
mkdir -p docker/redis
mkdir -p docker/nginx
mkdir -p uploads
mkdir -p logs
echo -e "${GREEN}✓ 目录创建完成${NC}"

# 启动服务
echo -e "\n${YELLOW}[3/5]${NC} 启动 Docker 服务..."
if [ -f "$COMPOSE_FILE" ]; then
    docker compose -f "$COMPOSE_FILE" --profile monitoring up -d
    echo -e "${GREEN}✓ 服务启动完成${NC}"
else
    echo -e "${RED}错误: 找不到 $COMPOSE_FILE${NC}"
    exit 1
fi

# 等待服务就绪
echo -e "\n${YELLOW}[4/5]${NC} 等待服务就绪..."
echo "等待 MySQL 主库启动..."
sleep 5
echo "等待 MySQL 从库启动..."
sleep 5
echo "等待 Redis 启动..."
sleep 3
echo "等待后端服务启动..."
sleep 5

# 初始化 MySQL 主从复制
echo -e "\n${YELLOW}[5/5]${NC} 初始化 MySQL 主从复制..."
if [ -f "scripts/setup-mysql-replication.sh" ]; then
    chmod +x scripts/setup-mysql-replication.sh
    ./scripts/setup-mysql-replication.sh
else
    echo -e "${YELLOW}警告: 找不到主从复制初始化脚本${NC}"
fi

# 显示服务状态
echo -e "\n${GREEN}==================================="
echo "  服务启动完成！"
echo "===================================${NC}"
echo ""
echo "访问地址："
echo "  - 前端: http://localhost"
echo "  - 后端API: http://localhost/api"
echo "  - Prometheus: http://localhost:9090"
echo "  - Grafana: http://localhost:3000 (admin/admin)"
echo ""
echo "查看日志："
echo "  docker compose -f $COMPOSE_FILE logs -f"
echo ""
echo "停止服务："
echo "  docker compose -f $COMPOSE_FILE down"
echo ""
