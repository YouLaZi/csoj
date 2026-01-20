@echo off
REM CSOJ 停止脚本 (Windows)
REM 用于快速停止所有服务

setlocal enabledelayedexpansion

set "GREEN=[92m"
set "NC=[0m"

echo %GREEN%========================================%NC%
echo %GREEN%  CSOJ 在线判题系统 - 停止脚本%NC%
echo %GREEN%========================================%NC%
echo.

REM 检查 Docker 是否运行
docker info >nul 2>&1
if errorlevel 1 (
    echo %RED%错误: Docker 未运行%NC%
    pause
    exit /b 1
)

REM 停止服务
echo %GREEN%正在停止所有服务...%NC%
docker-compose stop

REM 显示状态
echo.
echo %GREEN%服务已停止%NC%
docker-compose ps

pause
