@echo off
REM CSOJ 启动脚本 (Windows)
REM 用于快速启动所有服务

setlocal enabledelayedexpansion

REM 颜色设置
set "GREEN=[92m"
set "YELLOW=[93m"
set "RED=[91m"
set "NC=[0m"

echo %GREEN%========================================%NC%
echo %GREEN%  CSOJ 在线判题系统 - 启动脚本%NC%
echo %GREEN%========================================%NC%
echo.

REM 检查 Docker 是否运行
docker info >nul 2>&1
if errorlevel 1 (
    echo %RED%错误: Docker 未运行%NC%
    echo 请先启动 Docker Desktop
    pause
    exit /b 1
)

REM 检查 .env 文件是否存在
if not exist ".env" (
    echo %YELLOW%警告: .env 文件不存在，从 .env.example 复制...%NC%
    if exist ".env.example" (
        copy .env.example .env >nul
        echo %YELLOW%已创建 .env 文件，请编辑配置后再运行启动脚本%NC%
        echo %YELLOW%编辑命令: notepad .env%NC%
        pause
        exit /b 0
    ) else (
        echo %RED%错误: .env.example 文件不存在%NC%
        pause
        exit /b 1
    )
)

REM 检查 sql 目录是否存在
if not exist "sql" (
    echo %YELLOW%警告: sql 目录不存在，创建中...%NC%
    mkdir sql
    if exist "csoj-backend-master\src\main\resources\sql\create_table.sql" (
        copy "csoj-backend-master\src\main\resources\sql\create_table.sql" "sql\" >nul
    )
    if exist "csoj-backend-master\src\main\resources\sql\init_data.sql" (
        copy "csoj-backend-master\src\main\resources\sql\init_data.sql" "sql\" >nul
    )
)

REM 拉取最新镜像
echo %GREEN%正在拉取最新的 Docker 镜像...%NC%
docker-compose pull

REM 构建并启动服务
echo %GREEN%正在构建并启动服务...%NC%
docker-compose up -d --build

REM 等待服务启动
echo.
echo %GREEN%等待服务启动...%NC%
timeout /t 10 /nobreak >nul

REM 检查服务状态
echo.
echo %GREEN%========================================%NC%
echo %GREEN%  服务状态%NC%
echo %GREEN%========================================%NC%
docker-compose ps

REM 显示访问信息
echo.
echo %GREEN%========================================%NC%
echo %GREEN%  启动完成！%NC%
echo %GREEN%========================================%NC%
echo.
echo 后端 API:        %GREEN%http://localhost:8121/api%NC%
echo 系统监控:        %GREEN%http://localhost:8121/api/system/monitor/health%NC%
echo 沙箱健康检查:    %GREEN%http://localhost:8090/health%NC%
echo RabbitMQ 管理:   %GREEN%http://localhost:15672%NC% (guest/guest)
echo.
echo 查看日志: %YELLOW%docker-compose logs -f%NC%
echo 停止服务: %YELLOW%docker-compose stop%NC%
echo 重启服务: %YELLOW%docker-compose restart%NC%
echo.

pause
