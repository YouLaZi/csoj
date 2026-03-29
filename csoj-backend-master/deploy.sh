#!/bin/bash
# CSOJ 后端 ARM64 Docker 快速部署脚本
# 适用于 macOS (Apple Silicon) 和 Linux ARM64

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
print_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
print_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 检查 Docker 是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker 未安装，请先安装 Docker Desktop"
        exit 1
    fi

    if ! docker info &> /dev/null; then
        print_error "Docker 未运行，请先启动 Docker Desktop"
        exit 1
    fi

    print_success "Docker 已就绪"
}

# 检查 Docker Compose 是否可用
check_docker_compose() {
    if docker compose version &> /dev/null; then
        COMPOSE_CMD="docker compose"
    elif command -v docker-compose &> /dev/null; then
        COMPOSE_CMD="docker-compose"
    else
        print_error "Docker Compose 未安装"
        exit 1
    fi
    print_success "Docker Compose 已就绪: $COMPOSE_CMD"
}

# 创建环境配置文件
setup_env() {
    if [ ! -f .env ]; then
        print_info "创建 .env 配置文件..."
        cp .env.example .env
        print_success ".env 文件已创建，可根据需要修改配置"
    else
        print_info ".env 文件已存在"
    fi
}

# 构建镜像
build() {
    print_info "构建 ARM64 Docker 镜像..."
    $COMPOSE_CMD build --no-cache csoj-backend
    print_success "镜像构建完成"
}

# 启动服务
start() {
    print_info "启动 CSOJ 后端服务..."
    $COMPOSE_CMD up -d

    print_info "等待服务启动..."
    sleep 10

    print_info "服务状态:"
    $COMPOSE_CMD ps

    print_success "服务启动完成!"
    echo ""
    echo "=========================================="
    echo "  访问地址:"
    echo "  - API 文档: http://localhost:8121/api/doc.html"
    echo "  - 健康检查: http://localhost:8121/api/system/monitor/health"
    echo "=========================================="
}

# 停止服务
stop() {
    print_info "停止 CSOJ 后端服务..."
    $COMPOSE_CMD down
    print_success "服务已停止"
}

# 查看日志
logs() {
    $COMPOSE_CMD logs -f csoj-backend
}

# 清理数据
clean() {
    print_warning "这将删除所有数据（数据库、缓存、日志等）"
    read -p "确认继续? (y/N): " confirm
    if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
        $COMPOSE_CMD down -v
        print_success "数据已清理"
    else
        print_info "已取消"
    fi
}

# 启动监控服务
monitoring() {
    print_info "启动监控服务 (Prometheus + Grafana)..."
    $COMPOSE_CMD --profile monitoring up -d prometheus grafana
    print_success "监控服务已启动"
    echo ""
    echo "=========================================="
    echo "  监控地址:"
    echo "  - Prometheus: http://localhost:9090"
    echo "  - Grafana: http://localhost:3000 (admin/admin)"
    echo "=========================================="
}

# 完整部署
deploy() {
    check_docker
    check_docker_compose
    setup_env
    build
    start
}

# 帮助信息
help() {
    echo "CSOJ 后端 ARM64 Docker 部署脚本"
    echo ""
    echo "用法: ./deploy.sh [命令]"
    echo ""
    echo "命令:"
    echo "  deploy      完整部署（检查环境、构建、启动）"
    echo "  build       构建 Docker 镜像"
    echo "  start       启动所有服务"
    echo "  stop        停止所有服务"
    echo "  restart     重启所有服务"
    echo "  logs        查看应用日志"
    echo "  status      查看服务状态"
    echo "  monitoring  启动监控服务"
    echo "  clean       清理所有数据"
    echo "  help        显示帮助信息"
}

# 主入口
case "$1" in
    deploy)
        deploy
        ;;
    build)
        check_docker
        check_docker_compose
        build
        ;;
    start)
        check_docker
        check_docker_compose
        start
        ;;
    stop)
        check_docker_compose
        stop
        ;;
    restart)
        check_docker
        check_docker_compose
        stop
        start
        ;;
    logs)
        check_docker_compose
        logs
        ;;
    status)
        check_docker_compose
        $COMPOSE_CMD ps
        ;;
    monitoring)
        check_docker
        check_docker_compose
        monitoring
        ;;
    clean)
        check_docker_compose
        clean
        ;;
    help|--help|-h|"")
        help
        ;;
    *)
        print_error "未知命令: $1"
        help
        exit 1
        ;;
esac
