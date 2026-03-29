#!/bin/bash
# CSOJ 后端 ARM64 镜像构建和打包脚本
# 生成 archive-all-images.tar 用于 Docker Desktop 离线部署

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
print_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
print_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 配置
OUTPUT_FILE="archive-all-images.tar"
PLATFORM="linux/arm64"

# 镜像列表 (ARM64 版本)
declare -a IMAGES=(
    "mysql:8.0"
    "redis:7-alpine"
    "docker.elastic.co/elasticsearch/elasticsearch:8.12.0"
    "prom/prometheus:v2.48.0"
    "grafana/grafana:10.2.0"
)

# 应用镜像名称
APP_IMAGE="csoj-backend:arm64"

# 检查 Docker
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker 未安装"
        exit 1
    fi
    if ! docker info &> /dev/null; then
        print_error "Docker 未运行"
        exit 1
    fi
    print_success "Docker 已就绪"
}

# 拉取基础镜像
pull_images() {
    print_info "开始拉取 ARM64 镜像..."

    for image in "${IMAGES[@]}"; do
        print_info "拉取: $image"
        if docker pull --platform $PLATFORM "$image"; then
            print_success "完成: $image"
        else
            print_warning "拉取失败，尝试不指定平台: $image"
            docker pull "$image" || print_error "无法拉取: $image"
        fi
    done
}

# 构建应用镜像
build_app_image() {
    print_info "构建 CSOJ 后端 ARM64 镜像..."

    if [ -f "Dockerfile.arm64" ]; then
        docker buildx build \
            --platform $PLATFORM \
            -t "$APP_IMAGE" \
            -f Dockerfile.arm64 \
            --load \
            .
    else
        print_error "找不到 Dockerfile.arm64"
        exit 1
    fi

    print_success "应用镜像构建完成: $APP_IMAGE"
}

# 保存所有镜像到 tar 文件
save_images() {
    print_info "打包所有镜像到 $OUTPUT_FILE..."

    local all_images=("${IMAGES[@]}")
    all_images+=("$APP_IMAGE")

    print_info "包含的镜像:"
    for img in "${all_images[@]}"; do
        echo "  - $img"
    done

    docker save -o "$OUTPUT_FILE" "${all_images[@]}"

    local size=$(du -h "$OUTPUT_FILE" | cut -f1)
    print_success "镜像打包完成: $OUTPUT_FILE (大小: $size)"
}

# 生成加载脚本
generate_load_script() {
    print_info "生成加载脚本..."

    cat > load-images.sh << 'EOF'
#!/bin/bash
# CSOJ 后端镜像加载脚本
# 在目标机器上运行此脚本加载所有镜像

set -e

ARCHIVE_FILE="archive-all-images.tar"

if [ ! -f "$ARCHIVE_FILE" ]; then
    echo "错误: 找不到 $ARCHIVE_FILE"
    exit 1
fi

echo "[INFO] 加载 Docker 镜像..."
docker load -i "$ARCHIVE_FILE"

echo "[SUCCESS] 所有镜像加载完成!"
echo ""
echo "已加载的镜像:"
docker images | grep -E "mysql|redis|elasticsearch|prometheus|grafana|csoj-backend"
echo ""
echo "下一步: 运行 docker compose up -d 启动服务"
EOF

    chmod +x load-images.sh
    print_success "已生成 load-images.sh"
}

# 生成 Windows 加载脚本
generate_load_script_windows() {
    cat > load-images.ps1 << 'EOF'
# CSOJ 后端镜像加载脚本 (Windows)
# 在目标机器上运行此脚本加载所有镜像

$ArchiveFile = "archive-all-images.tar"

if (-not (Test-Path $ArchiveFile)) {
    Write-Error "找不到 $ArchiveFile"
    exit 1
}

Write-Host "[INFO] 加载 Docker 镜像..." -ForegroundColor Blue
docker load -i $ArchiveFile

Write-Host "[SUCCESS] 所有镜像加载完成!" -ForegroundColor Green
Write-Host ""
Write-Host "已加载的镜像:"
docker images | Select-String -Pattern "mysql|redis|elasticsearch|prometheus|grafana|csoj-backend"
Write-Host ""
Write-Host "下一步: 运行 docker compose up -d 启动服务"
EOF

    print_success "已生成 load-images.ps1"
}

# 生成部署包
generate_deploy_package() {
    print_info "生成完整部署包..."

    local package_dir="csoj-arm64-deploy"

    mkdir -p "$package_dir"

    # 复制必要文件
    cp "$OUTPUT_FILE" "$package_dir/"
    cp load-images.sh "$package_dir/"
    cp load-images.ps1 "$package_dir/"
    cp docker-compose.yml "$package_dir/"
    cp .env.example "$package_dir/"
    cp deploy.sh "$package_dir/" 2>/dev/null || true
    cp deploy.ps1 "$package_dir/" 2>/dev/null || true
    cp -r sql "$package_dir/" 2>/dev/null || true
    cp -r docker "$package_dir/" 2>/dev/null || true

    # 复制 Dockerfile（可选，用于重新构建）
    cp Dockerfile.arm64 "$package_dir/Dockerfile" 2>/dev/null || true

    # 创建说明文件
    cat > "$package_dir/README.txt" << 'README'
CSOJ 后端 ARM64 离线部署包
==========================

适用于: Apple Silicon Mac (M1/M2/M3), Windows ARM64, Linux ARM64

快速部署步骤:

1. 加载镜像:
   macOS/Linux: ./load-images.sh
   Windows:     .\load-images.ps1

2. 配置环境:
   cp .env.example .env
   # 根据需要编辑 .env 文件

3. 启动服务:
   docker compose up -d

4. 访问应用:
   API 文档: http://localhost:8121/api/doc.html
   健康检查: http://localhost:8121/api/system/monitor/health

注意事项:
- 确保 Docker Desktop 已安装并运行
- 首次启动可能需要等待 2-3 分钟初始化数据库
- 默认数据库密码: csoj123456 (生产环境请修改)

包含的服务:
- csoj-backend (主应用)
- MySQL 8.0 (数据库)
- Redis 7 (缓存)
- Elasticsearch 8.12 (搜索引擎)
- Prometheus (可选监控)
- Grafana (可选监控)
README

    # 打包
    tar -czvf "${package_dir}.tar.gz" "$package_dir"

    local size=$(du -h "${package_dir}.tar.gz" | cut -f1)
    print_success "部署包已生成: ${package_dir}.tar.gz (大小: $size)"
}

# 清理
clean() {
    print_info "清理临时文件..."
    rm -rf csoj-arm64-deploy
    print_success "清理完成"
}

# 主流程
main() {
    echo "=========================================="
    echo "  CSOJ 后端 ARM64 镜像构建工具"
    echo "=========================================="
    echo ""

    check_docker

    print_info "步骤 1/6: 拉取基础镜像"
    pull_images

    print_info "步骤 2/6: 构建应用镜像"
    build_app_image

    print_info "步骤 3/6: 保存镜像到 tar 文件"
    save_images

    print_info "步骤 4/6: 生成加载脚本"
    generate_load_script
    generate_load_script_windows

    print_info "步骤 5/6: 生成部署包"
    generate_deploy_package

    print_info "步骤 6/6: 清理临时文件"
    clean

    echo ""
    echo "=========================================="
    print_success "构建完成!"
    echo "=========================================="
    echo ""
    echo "生成的文件:"
    echo "  - archive-all-images.tar      (Docker 镜像归档)"
    echo "  - csoj-arm64-deploy.tar.gz    (完整部署包)"
    echo ""
    echo "在目标机器上:"
    echo "  1. 解压 csoj-arm64-deploy.tar.gz"
    echo "  2. 运行 load-images.sh (或 load-images.ps1)"
    echo "  3. 运行 docker compose up -d"
}

# 运行
main "$@"
