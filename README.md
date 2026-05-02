# JobPlus 兼职招聘平台

基于 Spring Cloud Alibaba 微服务架构的兼职招聘平台。

## 📋 目录

- [快速开始](#-快速开始)
  - [Docker 部署（推荐）](#选项1-docker-部署推荐--无需修改源码)
  - [本地开发环境搭建](#选项2-本地开发环境搭建)
- [Linux 支持](#-linux-支持)
- [项目结构](#️-项目结构)
- [开发指南](#️-开发指南)
- [软件工程实验报告](#-软件工程实验报告)
- [常见问题排查](#-常见问题排查)
- [许可证](#许可证)

## 🚀 快速开始

### 选项1：Docker 部署（推荐 - 无需修改源码）

**前置条件：**
- Docker Desktop 20.10+
- Docker Compose V2

**三步部署：**

```batch
REM 步骤1：检查环境
cd scripts && .\docker-check.bat

REM 步骤2：一键部署
cd scripts && .\docker-deploy.bat

REM 步骤3：访问应用
REM 打开浏览器：http://localhost:3000
```

**常用命令：**
- 停止服务：`cd scripts && .\docker-stop.bat`
- 查看日志：`cd scripts && .\docker-logs.bat`

📖 **完整 Docker 指南**：详见下方 [Docker 部署章节](#选项1-docker-部署推荐--无需修改源码)

---

### 选项2：本地开发环境搭建

#### 自动安装（推荐新手使用）

```batch
REM 以管理员身份运行
cd scripts && .\setup-all.bat
```

#### 手动安装

安装以下软件：
- JDK 21
- Maven 3.9+
- PostgreSQL 15+
- Redis 6+
- Nacos 2.2.3
- Node.js 18+

**Windows 快速安装：**
```powershell
choco install temurin21 maven postgresql18 memurai-developer nodejs -y
```

#### 配置并启动

```batch
REM 一键安装依赖
cd scripts && .\setup-all.bat

REM 一键启动（会打开各个服务的窗口）
cd scripts && .\start-all.bat
```

#### 访问应用

- **前端页面**：http://localhost:5173
- **API 网关**：http://localhost:8080
- **Nacos 控制台**：http://localhost:8848/nacos（账号密码：nacos/nacos）

**测试账号：**
- 求职者：seeker001 / 123456
- HR：hr001 / 123456
- 管理员：admin / 123456

---

## 🐧 Linux 支持

所有 Windows 批处理脚本都有对应的 Linux Shell 脚本版本，支持跨平台使用。

### 脚本对照表

| Windows 脚本 | Linux 脚本 | 说明 |
|-------------|-----------|------|
| `setup-all.bat` | `setup-all.sh` | 一键安装依赖 |
| `start-all.bat` | `start-all.sh` | 一键启动服务 |
| `docker-deploy.bat` | `docker-deploy.sh` | Docker 部署脚本 |
| `setup-docker-mirror.bat` | `setup-docker-mirror.sh` | Docker 镜像加速器配置 |
| `scripts\init-database.ps1` | `scripts\init-database.sh` | 数据库初始化脚本 |
| `scripts\start-nacos.bat` | `scripts\start-nacos.sh` | Nacos 启动脚本 |

### Linux 快速开始

#### 1. 设置执行权限

```bash
cd /path/to/JobPlus/scripts
chmod +x *.sh
```

#### 2. 安装依赖

```bash
cd scripts
./setup-all.sh
```

该脚本会：
- 检查 Java、Maven、Node.js、npm 是否已安装
- 预下载后端 Maven 依赖
- 编译后端模块
- 安装前端 npm 依赖

#### 3. 启动所有服务

```bash
cd scripts
./start-all.sh
```

该脚本会：
- 可选启动 Nacos 配置中心
- 在独立终端窗口中启动 5 个微服务（网关、认证、用户、职位、简历）
- 等待 8 秒后启动前端开发服务器
- 显示所有服务的访问地址

**注意**：脚本默认使用 xterm 或 gnome-terminal。如果你的系统使用其他终端模拟器，可能需要调整脚本。

#### 4. Linux Docker 部署

```bash
cd scripts
./docker-deploy.sh
```

#### 5. 配置 Docker 镜像加速器

```bash
cd scripts
sudo ./setup-docker-mirror.sh
```

**注意**：该脚本需要 sudo 权限来重启 Docker 服务。

### Linux 常见问题

#### 权限不足

如果遇到 "Permission denied" 错误，请确保设置了执行权限：

```bash
cd scripts
chmod +x *.sh
```

#### 命令未找到

如果 java、mvn、node 命令找不到，请添加到 PATH：

```bash
export PATH=/path/to/java/bin:$PATH
export PATH=/path/to/maven/bin:$PATH
export PATH=/path/to/node/bin:$PATH
```

#### 终端模拟器问题

`start-all.sh` 默认使用 xterm 或 gnome-terminal。其他桌面环境：

- 安装 xterm：`sudo apt install xterm`（Debian/Ubuntu）
- 或修改脚本以使用你系统的终端模拟器

#### Docker 权限问题

如果 Docker 命令需要 sudo，将你的用户加入 docker 组：

```bash
sudo usermod -aG docker $USER
# 重新登录生效
```

### Linux 停止服务

#### 非 Docker 部署

```bash
# 停止所有 Java 服务
pkill -f "mvn spring-boot:run"

# 停止前端服务
pkill -f "npm run dev"

# 停止 Nacos
pkill -f "startup.sh"
```

**或使用停止脚本：**
```bash
cd scripts
./stop-all.sh  # 如果存在
```

#### Docker 部署

```bash
cd scripts
./docker-deploy.sh down
# 或
docker-compose down
```

### Linux 查看日志

所有服务日志保存在 `logs/` 目录：

```bash
# 查看最新日志
tail -f logs/gateway-*.log
tail -f logs/frontend-*.log

# 列出所有日志文件
ls -la logs/
```

### Linux 自定义配置

要修改脚本行为，请编辑对应的 `.sh` 文件。主要可配置项：

- **Nacos 路径**：修改 `scripts/start-nacos.sh` 中的 `NACOS_HOME`
- **服务端口**：修改各服务的 `application.yml`
- **数据库连接**：修改 `.env` 文件或 `application.yml`

## 🏗️ 项目结构

```text
JobPlus/
+-- backend/            # 后端微服务
|   +-- common/         # 公共模块
|   +-- gateway/        # API 网关（端口：8080）
|   +-- auth-service/   # 认证服务（端口：8081）
|   +-- user-service/   # 用户服务（端口：8082）
|   +-- job-service/    # 职位服务（端口：8083）
|   +-- resume-service/ # 简历服务（端口：8084）
+-- frontend/           # 前端应用（端口：3000/5173）
+-- scripts/            # 部署与工具脚本
|   +-- setup-all.*     # 依赖安装脚本
|   +-- start-all.*     # 服务启动脚本
|   +-- docker-deploy.* # Docker 部署脚本
|   +-- setup-docker-mirror.* # Docker 镜像加速器配置
|   +-- init-database.* # 数据库初始化脚本
|   +-- start-nacos.*   # Nacos 启动脚本
|   +-- init-postgresql.sql # 数据库表结构
+-- project-report/     # 软件工程实验报告
+-- logs/               # 应用日志
+-- docker-compose.yml  # Docker 编排配置
+-- .env                # 环境变量配置
+-- README.md           # 项目说明文档
+-- LICENSE             # 许可证文件
```

## 🛠️ 开发指南

### 修改代码后重启

**Docker 模式：**
```batch
REM Windows
cd scripts
docker compose up -d --build
```

```bash
# Linux
cd scripts
docker compose up -d --build
```

**本地模式：**
1. 用 Ctrl+C（Windows）或 pkill 命令（Linux）停止服务窗口。
2. 重新运行 `cd scripts && start-all.bat`（Windows）或 `cd scripts && ./start-all.sh`（Linux）。

### 查看日志

**Docker 模式：**
```batch
REM Windows
cd scripts
.\docker-logs.bat
# 或
docker compose logs -f [服务名]
```

```bash
# Linux
cd scripts
docker compose logs -f [服务名]
```

**本地模式：**
```powershell
# Windows PowerShell
Get-Content "logs\start-all-*.log" -Tail 50
```

```bash
# Linux
tail -f logs/start-all-*.log
```

---

## ❓ 常见问题排查

### Windows 特有问题

#### JDK 25 兼容性问题

如果使用 JDK 25，可能会遇到 Lombok 编译错误。解决方法：
- 降级到 JDK 21（推荐）
- 或更新 Lombok 到支持 JDK 25 的最新版本

#### Docker Desktop 未检测到

如果 Docker 已安装但未检测到：
1. 确保 Docker Desktop 正在运行
2. 运行 `cd scripts && .\fix-docker-env.bat` 修复环境变量
3. 重启终端

### Linux 特有问题

详见上方 [Linux 支持章节](#-linux-支持)。

### 通用问题

#### 端口冲突

如果端口已被占用：
- 修改 `application.yml` 文件中的端口配置
- 或停止占用该端口的进程

#### 数据库连接失败

确保 PostgreSQL 正在运行且可访问：
```bash
# 检查 PostgreSQL 状态
pg_isready

# 初始化数据库
./scripts/init-database.sh
```

#### Nacos 连接失败

确保 Nacos 正在运行：
```bash
# 检查 Nacos 状态
curl http://localhost:8848/nacos

# 启动 Nacos
./scripts/start-nacos.sh
```

---

## 📊 软件工程实验报告

`project-report/` 目录包含软件工程课程所需的 PlantUML 图表和报告模板。

### 可用图表

| 实验 | 文件 | 说明 |
|-----|------|------|
| **数据流图** | `实验一_数据流图.puml` | 上下文图 + 0 层分解 |
| **用例图** | `实验二_用例图.puml` | 3 个参与者，28 个用例 |
| **类图** | `实验三_类图.puml` | 核心领域模型 + 服务层 |
| **序列图** | `实验四_序列图.puml` | 完整简历投递流程 |
| **活动图** | `实验五_活动图.puml` | 带判断节点的业务流程 |
| **状态图** | `实验六_状态图.puml` | 投递记录 6 状态转换 |

### 渲染 PlantUML 图表

#### 方法1：在线工具（✅ 推荐，无需安装）

**PlantText** - https://www.planttext.com/

1. 打开网站
2. 复制 `.puml` 文件内容
3. 粘贴到编辑器
4. 点击 "Export" 下载图片
5. **选择 SVG 格式**（矢量图，插入 Word 不会失真）

#### 方法2：VS Code 插件（✅ 最方便）

1. 安装插件：**PlantUML**（作者 jebbs）
2. 打开 `.puml` 文件
3. 按 `Alt + D` 预览
4. 右键 → "Export Current Diagram"
5. 选择 SVG 或 PNG 格式

#### 方法3：命令行工具

```bash
# 安装 PlantUML（需要 Java）
choco install plantuml    # Windows
brew install plantuml     # macOS

# 渲染所有文件为 SVG
plantuml -tsvg *.puml
```

#### 方法4：IntelliJ IDEA / WebStorm

1. 安装插件：**PlantUML Integration**
2. 打开 `.puml` 文件
3. 自动出现预览窗口
4. 点击导出按钮

### 完成实验报告步骤

1. **渲染图表**：使用上述任意方法将所有 6 个 `.puml` 文件渲染为 **SVG 格式**

2. **插入 Word 报告**：
   - 打开 `软件工程基础实验报告.docx`
   - 找到每个实验的占位符
   - 删除占位文字
   - Word 菜单：**插入 → 图片 → 此设备**
   - 选择对应的 SVG 文件
   - 调整图片大小（建议宽度：14-16 厘米）

3. **填写个人信息**：
   - 学号
   - 姓名
   - 班级
   - 指导教师

4. **完善实验内容**：
   - 添加 **实验步骤**：详细描述你的操作过程
   - 添加 **实验总结**：收获与遇到的问题

5. **检查并提交**：
   - 检查所有图表是否清晰可读
   - 确认格式统一（字体、大小）
   - 检查拼写和语法
   - 保存最终版本
   - 提交实验报告

### 重要提示

- **Word 插入请使用 SVG 格式**（矢量图，不会失真）
- **PNG 需要高分辨率**（建议 300 DPI）
- **保持宽高比**，不要拉伸
- **中文字符显示正常**，所有图表均使用 UTF-8 编码测试
- **定期保存 Word 文档**，避免数据丢失

### 资源链接

- **PlantUML 官网**：https://plantuml.com/zh/
- **在线编辑器**：https://www.planttext.com/
- **VS Code 插件**：PlantUML by jebbs

## 许可证

本项目仅供学习交流使用。
