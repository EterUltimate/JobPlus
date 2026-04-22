# JobPlus Part-time Recruitment Platform

A part-time recruitment platform based on Spring Cloud Alibaba microservices architecture.

## 📋 Table of Contents

- [Quick Start](#-quick-start)
  - [Docker Deployment (Recommended)](#option-1-docker-deployment-recommended---no-source-code-changes)
  - [Local Development Setup](#option-2-local-development-setup)
- [Linux Support](#-linux-support)
- [Project Structure](#️-project-structure)
- [Development Guide](#️-development)
- [Software Engineering Lab Reports](#-software-engineering-lab-reports)
- [Troubleshooting](#-troubleshooting)
- [License](#license)

## 🚀 Quick Start

### Option 1: Docker Deployment (Recommended - No Source Code Changes)

**Prerequisites:**
- Docker Desktop 20.10+ 
- Docker Compose V2

**Deploy in 3 steps:**

```batch
REM Step 1: Check environment
cd scripts && .\docker-check.bat

REM Step 2: One-click deployment
cd scripts && .\docker-deploy.bat

REM Step 3: Access application
REM Open browser: http://localhost:3000
```

**Quick commands:**
- Stop services: `cd scripts && .\docker-stop.bat`
- View logs: `cd scripts && .\docker-logs.bat`

📖 **Full Docker guide**: See [Docker Deployment section](#option-1-docker-deployment-recommended---no-source-code-changes) below

---

### Option 2: Local Development Setup

#### Automatic Installation (Recommended for Beginners)

```batch
REM Run as administrator
cd scripts && .\setup-all.bat
```

#### Manual Installation

Install the following software:
- JDK 21
- Maven 3.9+
- PostgreSQL 15+
- Redis 6+
- Nacos 2.2.3
- Node.js 18+

**Quick install for Windows:**
```powershell
choco install temurin21 maven postgresql18 memurai-developer nodejs -y
```

#### Configure and Start

```batch
REM One-click dependency setup
cd scripts && .\setup-all.bat

REM One-click startup (opens service windows)
cd scripts && .\start-all.bat
```

#### Access Application

- **Frontend**: http://localhost:5173
- **API Gateway**: http://localhost:8080
- **Nacos**: http://localhost:8848/nacos (nacos/nacos)

**Test Accounts:**
- Job Seeker: seeker001 / 123456
- HR: hr001 / 123456
- Admin: admin / 123456

---

## 🐧 Linux Support

All Windows batch scripts have corresponding Linux shell script versions for cross-platform support.

### Script Mapping

| Windows Script | Linux Script | Description |
|---------------|-------------|-------------|
| `setup-all.bat` | `setup-all.sh` | One-click dependency installation |
| `start-all.bat` | `start-all.sh` | One-click service startup |
| `docker-deploy.bat` | `docker-deploy.sh` | Docker deployment script |
| `setup-docker-mirror.bat` | `setup-docker-mirror.sh` | Docker mirror accelerator configuration |
| `scripts\init-database.ps1` | `scripts\init-database.sh` | Database initialization script |
| `scripts\start-nacos.bat` | `scripts\start-nacos.sh` | Nacos startup script |

### Quick Start on Linux

#### 1. Set Execute Permissions

```bash
cd /path/to/JobPlus/scripts
chmod +x *.sh
```

#### 2. Install Dependencies

```bash
cd scripts
./setup-all.sh
```

This script will:
- Check if Java, Maven, Node.js, npm are installed
- Pre-fetch backend Maven dependencies
- Compile backend modules
- Install frontend npm dependencies

#### 3. Start All Services

```bash
cd scripts
./start-all.sh
```

This script will:
- Optionally start Nacos configuration center
- Launch 5 microservices in separate terminal windows (Gateway, Auth, User, Job, Resume)
- Wait 8 seconds then start frontend development server
- Display access URLs for all services

**Note**: The script tries to use xterm or gnome-terminal. If your system uses a different terminal emulator, you may need to adjust the script.

#### 4. Docker Deployment on Linux

```bash
cd scripts
./docker-deploy.sh
```

#### 5. Configure Docker Mirror Accelerator

```bash
cd scripts
sudo ./setup-docker-mirror.sh
```

**Note**: This script requires sudo privileges to restart Docker service.

### Common Issues on Linux

#### Permission Denied

If you encounter "Permission denied" errors, ensure execute permissions are set:

```bash
cd scripts
chmod +x *.sh
```

#### Command Not Found

If java, mvn, node commands are not found, add them to PATH:

```bash
export PATH=/path/to/java/bin:$PATH
export PATH=/path/to/maven/bin:$PATH
export PATH=/path/to/node/bin:$PATH
```

#### Terminal Emulator Issues

`start-all.sh` defaults to xterm or gnome-terminal. For other desktop environments:

- Install xterm: `sudo apt install xterm` (Debian/Ubuntu)
- Or modify the script to use your system's terminal emulator

#### Docker Permission Issues

If Docker commands require sudo, add your user to the docker group:

```bash
sudo usermod -aG docker $USER
# Re-login to take effect
```

### Stopping Services on Linux

#### Non-Docker Deployment

```bash
# Stop all Java services
pkill -f "mvn spring-boot:run"

# Stop frontend service
pkill -f "npm run dev"

# Stop Nacos
pkill -f "startup.sh"
```

**Or use the stop script:**
```bash
cd scripts
./stop-all.sh  # If available
```

#### Docker Deployment

```bash
cd scripts
./docker-deploy.sh down
# or
docker-compose down
```

### Viewing Logs on Linux

All service logs are saved in the `logs/` directory:

```bash
# View latest logs
tail -f logs/gateway-*.log
tail -f logs/frontend-*.log

# List all log files
ls -la logs/
```

### Custom Configuration on Linux

To modify script behavior, edit the corresponding `.sh` files. Key configurable items:

- **Nacos Path**: Modify `NACOS_HOME` in `scripts/start-nacos.sh`
- **Service Ports**: Modify in each service's `application.yml`
- **Database Connection**: Modify in `.env` file or `application.yml`

## 🏗️ Project Structure

```text
JobPlus/
+-- backend/            # Backend microservices
|   +-- common/         # Common module
|   +-- gateway/        # API Gateway (Port: 8080)
|   +-- auth-service/   # Authentication service (Port: 8081)
|   +-- user-service/   # User service (Port: 8082)
|   +-- job-service/    # Job service (Port: 8083)
|   +-- resume-service/ # Resume service (Port: 8084)
+-- frontend/           # Frontend application (Port: 3000/5173)
+-- scripts/            # All deployment & utility scripts
|   +-- setup-all.*     # Dependency installation scripts
|   +-- start-all.*     # Service startup scripts
|   +-- docker-deploy.* # Docker deployment scripts
|   +-- setup-docker-mirror.* # Docker mirror configuration
|   +-- init-database.* # Database initialization
|   +-- start-nacos.*   # Nacos startup scripts
|   +-- init-postgresql.sql # Database schema
+-- project-report/     # Software engineering lab reports
+-- logs/               # Application logs
+-- docker-compose.yml  # Docker orchestration
+-- .env                # Environment configuration
+-- README.md           # Project documentation
+-- LICENSE             # License file
```

## 🛠️ Development

### Restart After Code Changes

**Docker mode:**
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

**Local mode:**
1. Stop service windows with Ctrl+C (Windows) or pkill command (Linux).
2. Re-run `cd scripts && start-all.bat` (Windows) or `cd scripts && ./start-all.sh` (Linux).

### View Logs

**Docker mode:**
```batch
REM Windows
cd scripts
.\docker-logs.bat
# or
docker compose logs -f [service-name]
```

```bash
# Linux
cd scripts
docker compose logs -f [service-name]
```

**Local mode:**
```powershell
# Windows PowerShell
Get-Content "logs\start-all-*.log" -Tail 50
```

```bash
# Linux
tail -f logs/start-all-*.log
```

---

## ❓ Troubleshooting

### Windows-Specific Issues

#### JDK 25 Compatibility Issue

If using JDK 25, you may encounter Lombok compilation errors. Solution:
- Downgrade to JDK 21 (recommended)
- Or update Lombok to the latest version that supports JDK 25

#### Docker Desktop Not Detected

If Docker is installed but not detected:
1. Ensure Docker Desktop is running
2. Run `cd scripts && .\fix-docker-env.bat` to fix environment variables
3. Restart your terminal

### Linux-Specific Issues

See the [Linux Support section](#-linux-support) above for detailed troubleshooting.

### General Issues

#### Port Conflicts

If ports are already in use:
- Modify port configurations in `application.yml` files
- Or stop processes using those ports

#### Database Connection Failed

Ensure PostgreSQL is running and accessible:
```bash
# Check PostgreSQL status
pg_isready

# Initialize database
./scripts/init-database.sh
```

#### Nacos Connection Failed

Ensure Nacos is running:
```bash
# Check Nacos status
curl http://localhost:8848/nacos

# Start Nacos
./scripts/start-nacos.sh
```

---

## 📊 Software Engineering Lab Reports

The `project-report/` directory contains PlantUML diagrams and report templates for software engineering coursework.

### Available Diagrams

| Experiment | File | Description |
|-----------|------|-------------|
| **Data Flow Diagram** | `实验一_数据流图.puml` | Context diagram + Level-0 decomposition |
| **Use Case Diagram** | `实验二_用例图.puml` | 3 actors, 28 use cases |
| **Class Diagram** | `实验三_类图.puml` | Core domain model + service layer |
| **Sequence Diagram** | `实验四_序列图.puml` | Complete resume delivery flow |
| **Activity Diagram** | `实验五_活动图.puml` | Business process with decision nodes |
| **State Diagram** | `实验六_状态图.puml` | 6-state delivery record transitions |

### Rendering PlantUML Diagrams

#### Method 1: Online Tool (✅ Recommended, No Installation)

**PlantText** - https://www.planttext.com/

1. Open the website
2. Copy `.puml` file content
3. Paste into editor
4. Click "Export" to download image
5. **Choose SVG format** (vector graphics, no quality loss in Word)

#### Method 2: VS Code Extension (✅ Most Convenient)

1. Install extension: **PlantUML** (by jebbs)
2. Open `.puml` file
3. Press `Alt + D` to preview
4. Right-click → "Export Current Diagram"
5. Choose SVG or PNG format

#### Method 3: Command Line Tool

```bash
# Install PlantUML (requires Java)
choco install plantuml    # Windows
brew install plantuml     # macOS

# Render all files to SVG
plantuml -tsvg *.puml
```

#### Method 4: IntelliJ IDEA / WebStorm

1. Install plugin: **PlantUML Integration**
2. Open `.puml` file
3. Preview window appears automatically
4. Click export button

### Steps to Complete Lab Reports

1. **Render Diagrams**: Use any method above to render all 6 `.puml` files to **SVG format**

2. **Insert into Word Report**:
   - Open `软件工程基础实验报告.docx`
   - Find each experiment's placeholder
   - Delete placeholder text
   - Word menu: **Insert → Picture → This Device**
   - Select corresponding SVG file
   - Adjust image size (recommended width: 14-16 cm)

3. **Fill Personal Information**:
   - Student ID
   - Name
   - Class
   - Instructor

4. **Complete Experiment Content**:
   - Add **Experiment Steps**: Detailed description of your process
   - Add **Experiment Summary**: Lessons learned and issues encountered

5. **Review and Submit**:
   - Check all diagrams are clear and readable
   - Verify consistent formatting (font, size)
   - Check spelling and grammar
   - Save final version
   - Submit lab report

### Important Notes

- **Use SVG format** for Word insertion (vector graphics, no quality loss)
- **PNG requires high resolution** (recommend 300 DPI)
- **Maintain aspect ratio**, don't stretch
- **Chinese characters display correctly**, all diagrams tested with UTF-8 encoding
- **Save Word document regularly** to avoid data loss

### Resources

- **PlantUML Official**: https://plantuml.com/zh/
- **Online Editor**: https://www.planttext.com/
- **VS Code Extension**: PlantUML by jebbs

## License

This project is for learning and educational purposes only.
