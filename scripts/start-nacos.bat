@echo off
chcp 65001 >nul
echo ========================================
echo 启动 Nacos Server (单机模式)
echo ========================================
echo.

cd /d "C:\nacos\nacos\bin"

echo 正在启动 Nacos...
echo 访问地址: http://localhost:8848/nacos
echo 默认账号: nacos / nacos
echo.
echo 按 Ctrl+C 停止服务
echo.

startup.cmd -m standalone
