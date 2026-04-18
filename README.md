# JobPlus Part-time Recruitment Platform

A part-time recruitment platform based on Spring Cloud Alibaba microservices architecture.

## 🚀 Quick Start

### Option 1: Docker Deployment (Recommended - No Source Code Changes)

**Prerequisites:**
- Docker Desktop 20.10+ 
- Docker Compose V2

**Deploy in 3 steps:**

```batch
REM Step 1: Check environment
.\docker-check.bat

REM Step 2: One-click deployment
.\docker-deploy.bat

REM Step 3: Access application
REM Open browser: http://localhost:3000
```

**Quick commands:**
- Stop services: `.\docker-stop.bat`
- View logs: `.\docker-logs.bat`

📖 **Full Docker guide**: [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md) | [Quick Start](DOCKER_QUICKSTART.md)

---

### Option 2: Local Development Setup

#### Automatic Installation (Recommended for Beginners)

```batch
REM Run as administrator
.\setup-all.bat
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
.\setup-all.bat

REM One-click startup (opens service windows)
.\start-all.bat
```

#### Access Application

- **Frontend**: http://localhost:5173
- **API Gateway**: http://localhost:8080
- **Nacos**: http://localhost:8848/nacos (nacos/nacos)

**Test Accounts:**
- Job Seeker: seeker001 / 123456
- HR: hr001 / 123456
- Admin: admin / 123456

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
+-- scripts/            # Utility scripts
+-- docker-compose.yml  # Docker orchestration
+-- *.bat               # Deployment scripts
+-- DOCKER_*.md         # Docker documentation
```

## 🛠️ Development

### Restart After Code Changes

**Docker mode:**
```batch
docker compose up -d --build
```

**Local mode:**
1. Stop service windows with Ctrl+C.
2. Re-run `.\start-all.bat`.

### View Logs

**Docker mode:**
```batch
.\docker-logs.bat
# or
docker compose logs -f [service-name]
```

**Local mode:**
```powershell
Get-Content "logs\start-all-*.log" -Tail 50
```

## License

This project is for learning and educational purposes only.
