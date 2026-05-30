#!/bin/bash

# JobPlus Docker 部署脚本 (Linux版本)

# Get project root directory (parent of scripts directory)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Change to project root directory
cd "$ROOT" || exit 1

echo "========================================"
echo "   JobPlus Docker 部署脚本"
echo "========================================"
echo ""

# 检查Docker是否安装
if ! command -v docker &> /dev/null; then
    echo "[错误] 未检测到Docker，请先安装 Docker"
    exit 1
fi

# 检查Docker Compose是否安装
if command -v docker-compose &> /dev/null; then
    echo "[提示] 使用旧版 docker-compose 命令"
    COMPOSE_CMD="docker-compose"
elif docker compose version &> /dev/null; then
    echo "[提示] 使用新版 docker compose 命令"
    COMPOSE_CMD="docker compose"
else
    echo "[错误] 未检测到 Docker Compose，请先安装"
    exit 1
fi

echo "[1/3] 检查环境配置..."

# 确保在项目根目录
if [ ! -f "docker-compose.yml" ]; then
    echo "[错误] 未找到 docker-compose.yml，请确保在项目根目录运行此脚本"
    exit 1
fi

if [ ! -f ".env" ]; then
    echo "[提示] 未找到.env文件，从.env.example复制..."
    cp .env.example .env
    echo "[提示] 请根据需要修改.env文件中的配置"
    if [ -z "${CI:-}" ]; then
        read -p "按回车继续..."
    fi
fi

echo "[2/3] 构建并启动服务..."
if ! $COMPOSE_CMD up -d --build; then
    echo "[错误] 服务构建或启动失败"
    exit 1
fi

echo "[3/3] 当前服务状态..."
$COMPOSE_CMD ps

echo ""
echo "========================================"
echo "   部署命令已完成！"
echo "========================================"
echo ""
echo "访问地址："
echo "  - 前端应用: http://localhost:3000"
echo "  - API网关:  http://localhost:8080"
echo "  - Auth服务: http://localhost:8081"
echo "  - User服务: http://localhost:8082"
echo "  - Job服务:  http://localhost:8083"
echo "  - Resume服务: http://localhost:8084"
echo ""
echo "查看服务状态: $COMPOSE_CMD ps"
echo "查看日志:     $COMPOSE_CMD logs -f"
echo "停止服务:     $COMPOSE_CMD down"
echo "提示: 健康检查会在后台继续运行，首次启动可能仍需等待镜像和服务初始化。"
echo ""
