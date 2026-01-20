@echo off
REM CSOJ 数据库自动备份脚本 (Windows)
REM 用于自动备份 MySQL 数据库

setlocal enabledelayedexpansion

REM 配置
set BACKUP_DIR=.\backups
set MYSQL_DATABASE=oj_db
set BACKUP_RETENTION_DAYS=7

set "GREEN=[92m"
set "YELLOW=[93m"
set "RED=[91m"
set "NC=[0m"

echo %GREEN%========================================%NC%
echo %GREEN%  CSOJ 数据库备份脚本%NC%
echo %GREEN%========================================%NC%
echo.

REM 创建备份目录
if not exist "%BACKUP_DIR%" mkdir "%BACKUP_DIR%"

REM 生成备份文件名（带时间戳）
set TIMESTAMP=%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%
set BACKUP_FILE=%BACKUP_DIR%\%MYSQL_DATABASE%_%TIMESTAMP%.sql
set LATEST_LINK=%BACKUP_DIR%\%MYSQL_DATABASE%_latest.sql

echo %YELLOW%开始备份数据库...%NC%
echo 数据库: %MYSQL_DATABASE%
echo 备份文件: %BACKUP_FILE%

REM 获取 MySQL 密码
if "%MYSQL_ROOT_PASSWORD%"=="" (
    findstr /B "MYSQL_ROOT_PASSWORD" .env > temp_env.txt 2>nul
    if exist temp_env.txt (
        set /p MYSQL_ROOT_PASSWORD=<temp_env.txt
        set MYSQL_ROOT_PASSWORD=%MYSQL_ROOT_PASSWORD:~19%
        del temp_env.txt
    ) else (
        echo %RED%错误: 未找到 MySQL 密码%NC%
        echo 请设置 MYSQL_ROOT_PASSWORD 环境变量或在 .env 文件中配置
        pause
        exit /b 1
    )
)

REM 检查 Docker 环境
docker ps | findstr "csoj-mysql" >nul 2>&1
if %errorlevel%==0 (
    REM Docker 环境
    echo 检测到 Docker 环境，使用 docker exec 备份...
    docker exec csoj-mysql mysqldump -u root -p%MYSQL_ROOT_PASSWORD% --single-transaction --routines --triggers --events %MYSQL_DATABASE% > %BACKUP_FILE%
) else (
    echo %RED%错误: 未找到运行中的 MySQL 容器%NC%
    pause
    exit /b 1
)

REM 检查备份是否成功
if %errorlevel%==0 (
    REM 压缩备份文件 (使用 PowerShell)
    powershell -Command "Compress-Archive -Path '%BACKUP_FILE%' -DestinationPath '%BACKUP_FILE%.zip' -Force"
    del "%BACKUP_FILE%"
    set BACKUP_FILE=%BACKUP_FILE%.zip%

    REM 删除旧的最新链接
    if exist "%LATEST_LINK%.zip" del "%LATEST_LINK%.zip%

    echo.
    echo %GREEN%✓ 备份成功！%NC%
    echo 备份文件: %BACKUP_FILE%
    echo 最新备份: %LATEST_LINK%.zip
) else (
    echo.
    echo %RED%✗ 备份失败！%NC%
    pause
    exit /b 1
)

echo.
echo %GREEN%========================================%NC%
echo %GREEN%  备份完成！%NC%
echo %GREEN%========================================%NC%

pause
