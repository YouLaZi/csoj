# CSOJ 后端 ARM64 Docker 快速部署脚本 (Windows PowerShell)
# 适用于 Windows ARM64 设备

param(
    [Parameter(Position=0)]
    [ValidateSet("deploy", "build", "start", "stop", "restart", "logs", "status", "monitoring", "clean", "help")]
    [string]$Command = "help"
)

# 颜色函数
function Write-Info { param($msg) Write-Host "[INFO] $msg" -ForegroundColor Blue }
function Write-Success { param($msg) Write-Host "[SUCCESS] $msg" -ForegroundColor Green }
function Write-Warning { param($msg) Write-Host "[WARNING] $msg" -ForegroundColor Yellow }
function Write-Error { param($msg) Write-Host "[ERROR] $msg" -ForegroundColor Red }

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

# 检查 Docker Compose
function Get-ComposeCommand {
    if (docker compose version 2>$null) {
        return "docker compose"
    }
    elseif (Get-Command docker-compose -ErrorAction SilentlyContinue) {
        return "docker-compose"
    }
    else {
        Write-Error "Docker Compose 未安装"
        exit 1
    }
}

# 创建环境配置
function Setup-Env {
    if (-not (Test-Path .env)) {
        Write-Info "创建 .env 配置文件..."
        Copy-Item .env.example .env
        Write-Success ".env 文件已创建"
    }
    else {
        Write-Info ".env 文件已存在"
    }
}

# 构建镜像
function Build-Image {
    Write-Info "构建 ARM64 Docker 镜像..."
    $cmd = Get-ComposeCommand
    Invoke-Expression "$cmd build --no-cache csoj-backend"
    Write-Success "镜像构建完成"
}

# 启动服务
function Start-Services {
    Write-Info "启动 CSOJ 后端服务..."
    $cmd = Get-ComposeCommand
    Invoke-Expression "$cmd up -d"

    Write-Info "等待服务启动..."
    Start-Sleep -Seconds 10

    Write-Info "服务状态:"
    Invoke-Expression "$cmd ps"

    Write-Success "服务启动完成!"
    Write-Host ""
    Write-Host "=========================================="
    Write-Host "  访问地址:"
    Write-Host "  - API 文档: http://localhost:8121/api/doc.html"
    Write-Host "  - 健康检查: http://localhost:8121/api/system/monitor/health"
    Write-Host "=========================================="
}

# 停止服务
function Stop-Services {
    Write-Info "停止 CSOJ 后端服务..."
    $cmd = Get-ComposeCommand
    Invoke-Expression "$cmd down"
    Write-Success "服务已停止"
}

# 查看日志
function Show-Logs {
    $cmd = Get-ComposeCommand
    Invoke-Expression "$cmd logs -f csoj-backend"
}

# 清理数据
function Clean-Data {
    Write-Warning "这将删除所有数据（数据库、缓存、日志等）"
    $confirm = Read-Host "确认继续? (y/N)"
    if ($confirm -eq "y" -or $confirm -eq "Y") {
        $cmd = Get-ComposeCommand
        Invoke-Expression "$cmd down -v"
        Write-Success "数据已清理"
    }
    else {
        Write-Info "已取消"
    }
}

# 启动监控
function Start-Monitoring {
    Write-Info "启动监控服务..."
    $cmd = Get-ComposeCommand
    Invoke-Expression "$cmd --profile monitoring up -d prometheus grafana"
    Write-Success "监控服务已启动"
    Write-Host ""
    Write-Host "=========================================="
    Write-Host "  监控地址:"
    Write-Host "  - Prometheus: http://localhost:9090"
    Write-Host "  - Grafana: http://localhost:3000 (admin/admin)"
    Write-Host "=========================================="
}

# 完整部署
function Deploy-Full {
    if (-not (Test-Docker)) { exit 1 }
    $script:ComposeCmd = Get-ComposeCommand
    Setup-Env
    Build-Image
    Start-Services
}

# 帮助信息
function Show-Help {
    Write-Host "CSOJ 后端 ARM64 Docker 部署脚本 (Windows)"
    Write-Host ""
    Write-Host "用法: .\deploy.ps1 [命令]"
    Write-Host ""
    Write-Host "命令:"
    Write-Host "  deploy      完整部署（检查环境、构建、启动）"
    Write-Host "  build       构建 Docker 镜像"
    Write-Host "  start       启动所有服务"
    Write-Host "  stop        停止所有服务"
    Write-Host "  restart     重启所有服务"
    Write-Host "  logs        查看应用日志"
    Write-Host "  status      查看服务状态"
    Write-Host "  monitoring  启动监控服务"
    Write-Host "  clean       清理所有数据"
    Write-Host "  help        显示帮助信息"
}

# 主入口
switch ($Command) {
    "deploy" { Deploy-Full }
    "build" {
        if (-not (Test-Docker)) { exit 1 }
        $script:ComposeCmd = Get-ComposeCommand
        Build-Image
    }
    "start" {
        if (-not (Test-Docker)) { exit 1 }
        $script:ComposeCmd = Get-ComposeCommand
        Start-Services
    }
    "stop" {
        $script:ComposeCmd = Get-ComposeCommand
        Stop-Services
    }
    "restart" {
        if (-not (Test-Docker)) { exit 1 }
        $script:ComposeCmd = Get-ComposeCommand
        Stop-Services
        Start-Services
    }
    "logs" {
        $script:ComposeCmd = Get-ComposeCommand
        Show-Logs
    }
    "status" {
        $script:ComposeCmd = Get-ComposeCommand
        Invoke-Expression "$script:ComposeCmd ps"
    }
    "monitoring" {
        if (-not (Test-Docker)) { exit 1 }
        $script:ComposeCmd = Get-ComposeCommand
        Start-Monitoring
    }
    "clean" {
        $script:ComposeCmd = Get-ComposeCommand
        Clean-Data
    }
    default { Show-Help }
}
