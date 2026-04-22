#!/bin/bash

# Docker 镜像加速器配置脚本 (Linux版本)

echo "========================================"
echo "   Docker 镜像加速器配置脚本"
echo "========================================"
echo ""

# 检查Docker是否运行
if ! docker info &> /dev/null; then
    echo "[错误] Docker未运行，请先启动Docker服务"
    exit 1
fi

echo "[1/3] 正在配置Docker镜像加速器..."

# 创建daemon.json配置文件
DOCKER_CONFIG_DIR="$HOME/.docker"
mkdir -p "$DOCKER_CONFIG_DIR"

CONFIG_FILE="$DOCKER_CONFIG_DIR/daemon.json"

echo "[2/3] 写入镜像加速器配置..."
cat > "$CONFIG_FILE" << 'EOF'
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://huecker.io",
    "https://dockerhub.timeweb.cloud",
    "https://docker.1ms.run"
  ]
}
EOF

echo "[3/3] 重启Docker服务以应用配置..."
echo "[提示] 需要sudo权限来重启Docker服务"

# 尝试重启Docker服务
if command -v systemctl &> /dev/null; then
    echo "使用systemctl重启Docker..."
    sudo systemctl restart docker
elif command -v service &> /dev/null; then
    echo "使用service命令重启Docker..."
    sudo service docker restart
else
    echo "[警告] 无法自动重启Docker，请手动重启Docker服务"
    echo "命令: sudo systemctl restart docker 或 sudo service docker restart"
fi

echo ""
echo "========================================"
echo "   配置完成！"
echo "========================================"
echo ""
echo "已添加以下镜像加速器："
echo "  - docker.m.daocloud.io"
echo "  - huecker.io"
echo "  - dockerhub.timeweb.cloud"
echo "  - docker.1ms.run"
echo ""
echo "配置文件位置: $CONFIG_FILE"
echo ""
echo "请等待Docker完全启动后，再次运行 docker-deploy.sh"
echo ""