# CSOJ 后端 ARM64 镜像构建和打包脚本 (Windows PowerShell)
# 生成 archive-all-images.tar 用于 Docker Desktop 离线部署

param(
    [switch]$SkipPull,
    [switch]$SkipBuild,
    [string]$OutputFile = "archive-all-images.tar"
)

$ErrorActionPreference = "Stop"

# 颜色函数
function Write-Info { param($msg) Write-Host "[INFO] $msg" -ForegroundColor Blue }
function Write-Success { param($msg) Write-Host "[SUCCESS] $msg" -ForegroundColor Green }
function Write-Warning { param($msg) Write-Host "[WARNING] $msg" -ForegroundColor Yellow }
function Write-Error { param($msg) Write-Host "[ERROR] $msg" -ForegroundColor Red }

# 配置
$Platform = "linux/arm64"
$AppImage = "csoj-backend:arm64"

# 镜像列表
$Images = @(
    "mysql:8.0",
    "redis:7-alpine",
    "docker.elastic.co/elasticsearch/elasticsearch:8.12.0",
    "prom/prometheus:v2.48.0",
    "grafana/grafana:10.2.0"
)

# 检查 Docker
function Test-Docker {
    try {
        docker info | Out-Null
        Write-Success "Docker 已就绪"
        return $true
    }
    catch {
        Write-Error "Docker 未运行，请先启动 Docker Desktop"
        return $false
    }
}

# 检查 buildx
function Test-Buildx {
    $result = docker buildx version 2>$null
    if ($result) {
        Write-Success "Docker Buildx 已就绪"
        return $true
    }
    else {
        Write-Warning "Docker Buildx 不可用，将使用标准构建"
        return $false
    }
}

# 拉取镜像
function Pull-Images {
    Write-Info "开始拉取 ARM64 镜像..."

    foreach ($image in $Images) {
        Write-Info "拉取: $image"
        try {
            docker pull --platform $Platform $image
            Write-Success "完成: $image"
        }
        catch {
            Write-Warning "拉取失败，尝试不指定平台: $image"
            try {
                docker pull $image
                Write-Success "完成: $image"
            }
            catch {
                Write-Error "无法拉取: $image"
            }
        }
    }
}

# 构建应用镜像
function Build-AppImage {
    Write-Info "构建 CSOJ 后端 ARM64 镜像..."

    if (-not (Test-Path "Dockerfile.arm64")) {
        Write-Error "找不到 Dockerfile.arm64"
        exit 1
    }

    $useBuildx = Test-Buildx

    if ($useBuildx) {
        # 使用 buildx 构建并加载到本地
        docker buildx build `
            --platform $Platform `
            -t $AppImage `
            -f Dockerfile.arm64 `
            --load `
            .
    }
    else {
        # 标准构建
        docker build `
            -t $AppImage `
            -f Dockerfile.arm64 `
            .
    }

    Write-Success "应用镜像构建完成: $AppImage"
}

# 保存镜像
function Save-Images {
    Write-Info "打包所有镜像到 $OutputFile..."

    $allImages = $Images + @($AppImage)

    Write-Info "包含的镜像:"
    foreach ($img in $allImages) {
        Write-Host "  - $img"
    }

    docker save -o $OutputFile $allImages

    $size = (Get-Item $OutputFile).Length / 1GB
    Write-Success "镜像打包完成: $OutputFile (大小: $([math]::Round($size, 2)) GB)"
}

# 生成加载脚本 (Linux/macOS)
function New-LoadScriptUnix {
    Write-Info "生成加载脚本 (Unix)..."

    $script = @'
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
'@

    $script | Out-File -FilePath "load-images.sh" -Encoding utf8NoBOM
    Write-Success "已生成 load-images.sh"
}

# 生成加载脚本 (Windows)
function New-LoadScriptWindows {
    Write-Info "生成加载脚本 (Windows)..."

    $script = @'
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
'@

    $script | Out-File -FilePath "load-images.ps1" -Encoding utf8NoBOM
    Write-Success "已生成 load-images.ps1"
}

# 生成部署包
function New-DeployPackage {
    Write-Info "生成完整部署包..."

    $packageDir = "csoj-arm64-deploy"

    # 创建目录
    New-Item -ItemType Directory -Force -Path $packageDir | Out-Null

    # 复制文件
    Copy-Item $OutputFile $packageDir/
    Copy-Item "load-images.sh" $packageDir/ -ErrorAction SilentlyContinue
    Copy-Item "load-images.ps1" $packageDir/
    Copy-Item "docker-compose.yml" $packageDir/
    Copy-Item ".env.example" $packageDir/
    Copy-Item "deploy.sh" $packageDir/ -ErrorAction SilentlyContinue
    Copy-Item "deploy.ps1" $packageDir/ -ErrorAction SilentlyContinue
    Copy-Item "Dockerfile.arm64" $packageDir/Dockerfile -ErrorAction SilentlyContinue

    # 复制 sql 目录
    if (Test-Path "sql") {
        Copy-Item -Recurse "sql" $packageDir/
    }

    # 复制 docker 目录
    if (Test-Path "docker") {
        Copy-Item -Recurse "docker" $packageDir/
    }

    # 创建 README
    $readme = @'
CSOJ 后端 ARM64 离线部署包
==========================

适用于: Apple Silicon Mac (M1/M2/M3), Windows ARM64, Linux ARM64

快速部署步骤:

1. 加载镜像:
   macOS/Linux: ./load-images.sh
   Windows:     .\load-images.ps1

2. 配置环境:
   复制 .env.example 为 .env
   根据需要编辑 .env 文件

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
'@

    $readme | Out-File -FilePath "$packageDir/README.txt" -Encoding utf8

    # 打包 (使用 7-Zip 或 PowerShell 压缩)
    $archivePath = "$packageDir.zip"
    if (Get-Command 7z -ErrorAction SilentlyContinue) {
        7z a -tzip $archivePath $packageDir
    }
    else {
        Compress-Archive -Path $packageDir -DestinationPath $archivePath -Force
    }

    $size = (Get-Item $archivePath).Length / 1GB
    Write-Success "部署包已生成: $archivePath (大小: $([math]::Round($size, 2)) GB)"
}

# 主流程
function Main {
    Write-Host "=========================================="
    Write-Host "  CSOJ 后端 ARM64 镜像构建工具"
    Write-Host "=========================================="
    Write-Host ""

    if (-not (Test-Docker)) { exit 1 }

    $step = 1

    # 拉取镜像
    if (-not $SkipPull) {
        Write-Info "步骤 $step/6: 拉取基础镜像"
        Pull-Images
        $step++
    }

    # 构建应用镜像
    if (-not $SkipBuild) {
        Write-Info "步骤 $step/6: 构建应用镜像"
        Build-AppImage
        $step++
    }

    Write-Info "步骤 $step/6: 保存镜像到 tar 文件"
    Save-Images
    $step++

    Write-Info "步骤 $step/6: 生成加载脚本"
    New-LoadScriptUnix
    New-LoadScriptWindows
    $step++

    Write-Info "步骤 $step/6: 生成部署包"
    New-DeployPackage
    $step++

    Write-Host ""
    Write-Host "=========================================="
    Write-Success "构建完成!"
    Write-Host "=========================================="
    Write-Host ""
    Write-Host "生成的文件:"
    Write-Host "  - $OutputFile          (Docker 镜像归档)"
    Write-Host "  - csoj-arm64-deploy.zip (完整部署包)"
    Write-Host ""
    Write-Host "在目标机器上:"
    Write-Host "  1. 解压 csoj-arm64-deploy.zip"
    Write-Host "  2. 运行 .\load-images.ps1"
    Write-Host "  3. 运行 docker compose up -d"
}

# 运行
Main
