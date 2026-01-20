# Docker 镜像拉取测试脚本
Write-Host "=== Docker 镜像拉取测试 ===" -ForegroundColor Green

# 检查 Docker 是否运行
Write-Host "检查 Docker 状态..." -ForegroundColor Yellow
try {
    docker version | Out-Null
    Write-Host "✓ Docker 运行正常" -ForegroundColor Green
} catch {
    Write-Host "✗ Docker 未运行或未安装" -ForegroundColor Red
    exit 1
}

# 检查镜像源配置
Write-Host "`n检查镜像源配置..." -ForegroundColor Yellow
$registryMirrors = docker info 2>$null | Select-String "Registry Mirrors"
if ($registryMirrors) {
    Write-Host "✓ 镜像源配置已生效:" -ForegroundColor Green
    docker info 2>$null | Select-String -A 10 "Registry Mirrors"
} else {
    Write-Host "⚠ 未检测到镜像源配置" -ForegroundColor Yellow
}

# 测试拉取 Java 镜像
Write-Host "`n测试拉取 openjdk:8-jdk-alpine 镜像..." -ForegroundColor Yellow
try {
    $pullResult = docker pull openjdk:8-jdk-alpine 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✓ Java 镜像拉取成功" -ForegroundColor Green
    } else {
        Write-Host "✗ Java 镜像拉取失败:" -ForegroundColor Red
        Write-Host $pullResult -ForegroundColor Red
    }
} catch {
    Write-Host "✗ 拉取过程中发生错误: $($_.Exception.Message)" -ForegroundColor Red
}

# 测试其他常用镜像
$testImages = @(
    "mcr.microsoft.com/dotnet/sdk:latest",
    "python:3.9-alpine",
    "node:16-alpine"
)

Write-Host "`n测试其他镜像拉取..." -ForegroundColor Yellow
foreach ($image in $testImages) {
    Write-Host "测试 $image..." -ForegroundColor Cyan
    try {
        $result = docker pull $image 2>&1
        if ($LASTEXITCODE -eq 0) {
            Write-Host "  ✓ 成功" -ForegroundColor Green
        } else {
            Write-Host "  ✗ 失败" -ForegroundColor Red
        }
    } catch {
        Write-Host "  ✗ 错误: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 显示本地镜像列表
Write-Host "`n本地镜像列表:" -ForegroundColor Yellow
docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green