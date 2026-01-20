#!/bin/bash

# CSOJ 状态检查脚本
# 用于检查所有服务的运行状态

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}  CSOJ 在线判题系统 - 状态检查${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 检查 Docker 是否运行
if ! docker info &> /dev/null; then
    echo -e "${RED}错误: Docker 未运行${NC}"
    exit 1
fi

# 显示容器状态
echo -e "${GREEN}容器状态:${NC}"
docker-compose ps
echo ""

# 检查各服务健康状态
echo -e "${GREEN}健康检查:${NC}"

check_service() {
    local service=$1
    local url=$2
    local status=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null || echo "000")

    if [ "$status" = "200" ]; then
        echo -e "  ${GREEN}✓${NC} $service - 正常"
    else
        echo -e "  ${RED}✗${NC} $service - 异常 (HTTP $status)"
    fi
}

check_service "后端API" "http://localhost:8121/api/system/monitor/health"
check_service "代码沙箱" "http://localhost:8090/health"
check_service "RabbitMQ管理" "http://localhost:15672"

echo ""

# 显示资源使用情况
echo -e "${GREEN}资源使用:${NC}"
docker stats --no-stream --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}" \
    $(docker-compose ps -q)

echo ""

# 显示最近的日志
echo -e "${GREEN}最近日志 (最后 20 行):${NC}"
echo -e "${YELLOW}选择要查看的服务:${NC}"
echo "  1) 后端"
echo "  2) 沙箱"
echo "  3) 数据库"
echo "  4) 取消"
read -p "请输入选项 (1-4): " choice

case $choice in
    1)
        echo -e "${BLUE}=== 后端日志 ===${NC}"
        docker-compose logs --tail=20 backend
        ;;
    2)
        echo -e "${BLUE}=== 沙箱日志 ===${NC}"
        docker-compose logs --tail=20 sandbox
        ;;
    3)
        echo -e "${BLUE}=== 数据库日志 ===${NC}"
        docker-compose logs --tail=20 mysql
        ;;
    4)
        echo "取消"
        ;;
    *)
        echo -e "${RED}无效选项${NC}"
        ;;
esac
