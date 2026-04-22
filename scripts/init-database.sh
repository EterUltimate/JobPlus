#!/bin/bash

# JobPlus PostgreSQL 数据库初始化脚本 (Linux版本)

echo "========================================"
echo "JobPlus PostgreSQL 数据库初始化"
echo "========================================"

PSQL_PATH="psql"
SQL_FILE="$(dirname "$0")/init-postgresql.sql"

# 检查 psql 是否存在
if ! command -v $PSQL_PATH &> /dev/null; then
    echo "错误: 找不到 psql，请确认 PostgreSQL 已安装"
    exit 1
fi

# 检查 SQL 文件是否存在
if [ ! -f "$SQL_FILE" ]; then
    echo "错误: 找不到初始化 SQL 文件: $SQL_FILE"
    exit 1
fi

echo ""
echo "请输入 PostgreSQL postgres 用户的密码:"
echo "(如果密码是 'postgres'，直接按回车即可)"

# 尝试连接并执行 SQL
echo ""
echo "正在连接到 PostgreSQL..."

# 使用交互式密码输入
read -s -p "密码: " PGPASSWORD
echo ""

export PGPASSWORD

$PSQL_PATH -U postgres -f "$SQL_FILE"

if [ $? -eq 0 ]; then
    echo ""
    echo "✓ 数据库初始化成功！"
    echo "数据库: jobplus"
    echo "用户: postgres"
    echo "端口: 5432"
else
    echo ""
    echo "✗ 数据库初始化失败，退出码: $?"
fi

# 清除环境变量
unset PGPASSWORD

echo ""
echo "按任意键退出..."
read -n 1 -s