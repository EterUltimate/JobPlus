@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM Get project root directory (parent of scripts directory)
set "SCRIPT_DIR=%~dp0"
for %%i in ("%SCRIPT_DIR%..") do set "ROOT=%%~fi"

echo ========================================
echo    JobPlus Docker 部署脚本
echo ========================================
echo.

REM Change to project root directory
cd /d "%ROOT%"

REM 检查Docker是否安装
where docker >nul 2>nul
if %errorlevel% neq 0 (
    REM 尝试添加Docker Desktop默认路径
    if exist "C:\Program Files\Docker\Docker\resources\bin\docker.exe" (
        set PATH=C:\Program Files\Docker\Docker\resources\bin;%PATH%
    ) else (
        echo [错误] 未检测到Docker，请先安装 Docker Desktop
        echo [提示] 如果已安装，请运行 .\fix-docker-env.bat 修复环境
        pause
        exit /b 1
    )
)

REM 检查Docker Compose是否安装
where docker-compose >nul 2>nul
if %errorlevel% neq 0 (
    echo [提示] 使用新版 docker compose 命令
    set COMPOSE_CMD=docker compose
) else (
    echo [提示] 使用旧版 docker-compose 命令
    set COMPOSE_CMD=docker-compose
)

echo [1/4] 检查环境配置...
REM 确保在项目根目录
if not exist "%ROOT%\docker-compose.yml" (
    echo [错误] 未找到 docker-compose.yml，请确保在项目根目录运行此脚本
    pause
    exit /b 1
)

if not exist "%ROOT%\.env" (
    echo [提示] 未找到.env文件，从.env.example复制...
    copy .env.example .env >nul
    echo [提示] 请根据需要修改.env文件中的配置
    pause
)

echo [2/4] 构建Docker镜像...
%COMPOSE_CMD% build --no-cache
if %errorlevel% neq 0 (
    echo [错误] 镜像构建失败
    pause
    exit /b 1
)

echo [3/4] 启动服务...
%COMPOSE_CMD% up -d
if %errorlevel% neq 0 (
    echo [错误] 服务启动失败
    pause
    exit /b 1
)

echo [4/4] 等待服务就绪...
timeout /t 30 /nobreak >nul

echo.
echo ========================================
echo    部署完成！
echo ========================================
echo.
echo 访问地址：
echo   - 前端应用: http://localhost:3000
echo   - API网关:  http://localhost:8080
echo   - Auth服务: http://localhost:8081
echo   - User服务: http://localhost:8082
echo   - Job服务:  http://localhost:8083
echo   - Resume服务: http://localhost:8084
echo.
echo 查看服务状态: %COMPOSE_CMD% ps
echo 查看日志:     %COMPOSE_CMD% logs -f
echo 停止服务:     %COMPOSE_CMD% down
echo.

pause