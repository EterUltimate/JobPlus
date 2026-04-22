@echo off
chcp 65001 >nul
echo ========================================
echo    Docker 镜像加速器配置脚本
echo ========================================
echo.

REM 检查Docker是否运行
docker info >nul 2>nul
if %errorlevel% neq 0 (
    echo [错误] Docker未运行，请先启动Docker Desktop
    pause
    exit /b 1
)

echo [1/3] 正在配置Docker镜像加速器...

REM 创建daemon.json配置文件
set DOCKER_CONFIG_DIR=%USERPROFILE%\.docker
if not exist "%DOCKER_CONFIG_DIR%" mkdir "%DOCKER_CONFIG_DIR%"

set CONFIG_FILE=%DOCKER_CONFIG_DIR%\daemon.json

echo [2/3] 写入镜像加速器配置...
(
echo {
echo   "registry-mirrors": [
echo     "https://docker.m.daocloud.io",
echo     "https://huecker.io",
echo     "https://dockerhub.timeweb.cloud",
echo     "https://docker.1ms.run"
echo   ]
echo }
) > "%CONFIG_FILE%"

echo [3/3] 重启Docker Desktop以应用配置...
echo [提示] 请手动重启Docker Desktop，或者等待自动重启...
echo.

REM 尝试重启Docker
taskkill /f /im "Docker Desktop.exe" >nul 2>nul
timeout /t 3 /nobreak >nul
start "" "C:\Program Files\Docker\Docker\Docker Desktop.exe"

echo ========================================
echo    配置完成！
echo ========================================
echo.
echo 已添加以下镜像加速器：
echo   - docker.m.daocloud.io
echo   - huecker.io
echo   - dockerhub.timeweb.cloud
echo   - docker.1ms.run
echo.
echo 请等待Docker Desktop完全启动后，再次运行 docker-deploy.bat
echo.

pause