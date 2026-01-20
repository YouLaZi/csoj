#!/bin/bash

# CSOJ 停止脚本
# 用于快速停止所有服务

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  CSOJ 在线判题系统 - 停止脚本${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# 检查 Docker 是否运行
if ! docker info &> /dev/null; then
    echo -e "${RED}错误: Docker 未运行${NC}"
    exit 1
fi

# 停止服务
echo -e "${GREEN}正在停止所有服务...${NC}"
docker-compose stop

# 显示状态
echo ""
echo -e "${GREEN}服务已停止${NC}"
docker-compose ps
