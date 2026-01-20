#!/bin/bash

# CSOJ 启动脚本
# 用于快速启动所有服务

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  CSOJ 在线判题系统 - 启动脚本${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo -e "${RED}错误: Docker 未安装${NC}"
    echo "请先安装 Docker: https://docs.docker.com/get-docker/"
    exit 1
fi

# 检查 Docker Compose 是否安装
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}错误: Docker Compose 未安装${NC}"
    echo "请先安装 Docker Compose: https://docs.docker.com/compose/install/"
    exit 1
fi

# 检查 .env 文件是否存在
if [ ! -f ".env" ]; then
    echo -e "${YELLOW}警告: .env 文件不存在，从 .env.example 复制...${NC}"
    if [ -f ".env.example" ]; then
        cp .env.example .env
        echo -e "${YELLOW}已创建 .env 文件，请编辑配置后再运行启动脚本${NC}"
        echo -e "${YELLOW}编辑命令: vim .env${NC}"
        exit 0
    else
        echo -e "${RED}错误: .env.example 文件不存在${NC}"
        exit 1
    fi
fi

# 检查 sql 目录是否存在
if [ ! -d "sql" ]; then
    echo -e "${YELLOW}警告: sql 目录不存在，创建中...${NC}"
    mkdir -p sql
    if [ -f "csoj-backend-master/src/main/resources/sql/create_table.sql" ]; then
        cp csoj-backend-master/src/main/resources/sql/create_table.sql sql/
    fi
    if [ -f "csoj-backend-master/src/main/resources/sql/init_data.sql" ]; then
        cp csoj-backend-master/src/main/resources/sql/init_data.sql sql/
    fi
fi

# 拉取最新镜像
echo -e "${GREEN}正在拉取最新的 Docker 镜像...${NC}"
docker-compose pull

# 构建并启动服务
echo -e "${GREEN}正在构建并启动服务...${NC}"
docker-compose up -d --build

# 等待服务启动
echo -e "${GREEN}等待服务启动...${NC}"
sleep 10

# 检查服务状态
echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  服务状态${NC}"
echo -e "${GREEN}========================================${NC}"
docker-compose ps

# 等待健康检查
echo ""
echo -e "${YELLOW}等待健康检查完成（最多 30 秒）...${NC}"
for i in {1..6}; do
    sleep 5
    HEALTHY=$(docker-compose ps | grep -c "healthy" || true)
    if [ "$HEALTHY" -ge 3 ]; then
        echo -e "${GREEN}✓ 服务健康检查通过！${NC}"
        break
    fi
    echo -ne "${YELLOW}等待中... ($i/6)${NC}\r"
done
echo ""

# 显示访问信息
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  启动完成！${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "后端 API:        ${GREEN}http://localhost:8121/api${NC}"
echo -e "系统监控:        ${GREEN}http://localhost:8121/api/system/monitor/health${NC}"
echo -e "沙箱健康检查:    ${GREEN}http://localhost:8090/health${NC}"
echo -e "RabbitMQ 管理:   ${GREEN}http://localhost:15672${NC} (guest/guest)"
echo ""
echo -e "查看日志: ${YELLOW}docker-compose logs -f${NC}"
echo -e "停止服务: ${YELLOW}docker-compose stop${NC}"
echo -e "重启服务: ${YELLOW}docker-compose restart${NC}"
echo ""
