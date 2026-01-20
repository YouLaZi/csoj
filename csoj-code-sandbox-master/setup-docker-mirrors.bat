@echo off
echo 正在配置 Docker 镜像源...

:: 检查 Docker Desktop 是否运行
tasklist /FI "IMAGENAME eq Docker Desktop.exe" 2>NUL | find /I /N "Docker Desktop.exe">NUL
if "%ERRORLEVEL%"=="0" (
    echo Docker Desktop 正在运行，请先关闭 Docker Desktop
    echo 按任意键继续...
    pause >nul
)

:: 创建 Docker 配置目录（如果不存在）
if not exist "%USERPROFILE%\.docker" mkdir "%USERPROFILE%\.docker"

:: 备份现有配置（如果存在）
if exist "%USERPROFILE%\.docker\daemon.json" (
    echo 备份现有配置文件...
    copy "%USERPROFILE%\.docker\daemon.json" "%USERPROFILE%\.docker\daemon.json.backup"
)

:: 复制新的配置文件
echo 复制新的镜像源配置...
copy "docker-daemon.json" "%USERPROFILE%\.docker\daemon.json"

echo.
echo 配置完成！请按以下步骤操作：
echo 1. 重启 Docker Desktop
echo 2. 等待 Docker 完全启动
echo 3. 运行以下命令验证配置：
echo    docker info ^| findstr "Registry Mirrors"
echo.
echo 如果配置成功，您将看到镜像源列表
echo.
pause