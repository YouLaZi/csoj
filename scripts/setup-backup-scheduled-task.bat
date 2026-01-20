@echo off
REM CSOJ 定时备份配置脚本 (Windows)
REM 用于设置 Windows 计划任务

setlocal enabledelayedexpansion

set "GREEN=[92m"
set "YELLOW=[93m"
set "NC=[0m"

echo %GREEN%========================================%NC%
echo %GREEN%  配置数据库自动备份计划任务%NC%
echo %GREEN%========================================%NC%
echo.

set SCRIPT_DIR=%~dp0
set BACKUP_SCRIPT=%SCRIPT_DIR%backup-database.bat

REM 检查备份脚本是否存在
if not exist "%BACKUP_SCRIPT%" (
    echo 错误: 找不到备份脚本 %BACKUP_SCRIPT%
    pause
    exit /b 1
)

echo %YELLOW%请选择备份频率:%NC%
echo   1) 每天凌晨 2 点
echo   2) 每 6 小时
echo   3) 每小时
echo   4) 自定义
set /p choice="请输入选项 (1-4): "

if "%choice%"=="1" (
    set SCHEDULE=02:00
    set SC=DAILY
    set DESCRIPTION="每天凌晨 2 点"
) else if "%choice%"=="2" (
    set SCHEDULE=02:00
    set SC=HOURLY
    set MODIFIER=/MO 6
    set DESCRIPTION="每 6 小时"
) else if "%choice%"=="3" (
    set SCHEDULE=
    set SC=HOURLY
    set DESCRIPTION="每小时"
) else if "%choice%"=="4" (
    set /p SCHEDULE="请输入时间 (HH:MM) 或间隔 (分钟): "
    set /p SC="请输入类型 (HOURLY/DAILY): "
    set DESCRIPTION="自定义"
) else (
    echo 无效选项
    pause
    exit /b 1
)

echo.
echo %YELLOW%配置信息:%NC%
echo 备份频率: %DESCRIPTION%
echo 备份脚本: %BACKUP_SCRIPT%

echo.
set /p CONFIRM="确定要添加计划任务吗？(yes/no): "

if not "%CONFIRM%"=="yes" (
    echo 操作已取消
    pause
    exit /b 0
)

echo.
echo %GREEN%正在配置计划任务...%NC%

REM 删除旧任务（如果存在）
schtasks /Delete /TN "CSOJ-Database-Backup" /F >nul 2>&1

REM 创建新任务
schtasks /Create /TN "CSOJ-Database-Backup" /TR "\"%BACKUP_SCRIPT%\"" /SC %SC% %SCHEDULE% /RU SYSTEM /F

if %errorlevel%==0 (
    echo.
    echo %GREEN%✓ 计划任务配置成功！%NC%
    echo.
    echo 任务名称: CSOJ-Database-Backup
    echo.
    echo 查看任务: schtasks /Query /TN "CSOJ-Database-Backup"
    echo 运行任务: schtasks /Run /TN "CSOJ-Database-Backup"
    echo 删除任务: schtasks /Delete /TN "CSOJ-Database-Backup"
) else (
    echo.
    echo %RED%✗ 计划任务配置失败！%NC%
    echo 请以管理员身份运行此脚本
)

echo.
echo %GREEN%========================================%NC%
echo %GREEN%  配置完成！%NC%
echo %GREEN%========================================%NC%

pause
