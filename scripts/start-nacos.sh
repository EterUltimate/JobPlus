#!/bin/bash

# 启动 Nacos Server (单机模式) - Linux版本

echo "========================================"
echo "启动 Nacos Server (单机模式)"
echo "========================================"
echo ""

# 设置Nacos路径（根据实际安装位置修改）
NACOS_HOME="${NACOS_HOME:-/opt/nacos}"
cd "$NACOS_HOME/bin" || exit 1

echo "正在启动 Nacos..."
echo "访问地址: http://localhost:8848/nacos"
echo "默认账号: nacos / nacos"
echo ""
echo "按 Ctrl+C 停止服务"
echo ""

./startup.sh -m standalone