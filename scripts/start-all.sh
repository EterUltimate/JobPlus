#!/bin/bash

# JobPlus 一键启动脚本 (Linux版本)

# Get project root directory (parent of scripts directory)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
LOG_DIR="$ROOT/logs"
BACKEND_DIR="$ROOT/backend"
FRONTEND_DIR="$ROOT/frontend"

mkdir -p "$LOG_DIR"

TS=$(date +"%Y%m%d-%H%M%S")
LOG_FILE="$LOG_DIR/start-all-$TS.log"
USER_SERVICE_PORT=8082

# Allow database credentials to be overridden via environment variables
JOBPLUS_DB_URL="${JOBPLUS_DB_URL:-jdbc:postgresql://localhost:5432/jobplus}"
JOBPLUS_DB_USERNAME="${JOBPLUS_DB_USERNAME:-postgres}"
JOBPLUS_DB_PASSWORD="${JOBPLUS_DB_PASSWORD:-postgres}"

log() {
    echo "$1"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" >> "$LOG_FILE"
}

log "=========================================="
log "JobPlus one-click startup"
log "Log file: $LOG_FILE"
log "=========================================="

cd "$ROOT"

HAS_MVN=0
HAS_NODE=0
HAS_NPM=0

command -v mvn >/dev/null 2>&1 && HAS_MVN=1
command -v node >/dev/null 2>&1 && HAS_NODE=1
command -v npm >/dev/null 2>&1 && HAS_NPM=1

if [ "$HAS_MVN" -eq 0 ]; then
    log "[FAIL] mvn not found in PATH"
    log "Please run setup-all.sh first."
    log "Press any key to exit..."
    read -n 1 -s
    exit 1
fi

if [ "$HAS_NODE" -eq 0 ]; then
    log "[FAIL] node not found in PATH"
    log "Please run setup-all.sh first."
    log "Press any key to exit..."
    read -n 1 -s
    exit 1
fi

if [ "$HAS_NPM" -eq 0 ]; then
    log "[FAIL] npm not found in PATH"
    log "Please run setup-all.sh first."
    log "Press any key to exit..."
    read -n 1 -s
    exit 1
fi

log "[1/4] Optional: start Nacos"
if [ -f "$ROOT/scripts/start-nacos.sh" ]; then
    # 在新终端窗口中启动Nacos（需要xterm或gnome-terminal等）
    if command -v xterm >/dev/null 2>&1; then
        xterm -title "JobPlus-Nacos" -e "cd '$ROOT/scripts' && ./start-nacos.sh" &
        log "Nacos window opened (xterm)"
    elif command -v gnome-terminal >/dev/null 2>&1; then
        gnome-terminal --title="JobPlus-Nacos" -- bash -c "cd '$ROOT/scripts' && ./start-nacos.sh; exec bash" &
        log "Nacos window opened (gnome-terminal)"
    else
        log "No terminal emulator found, skipping Nacos auto-start"
        log "Please manually run: cd scripts && ./start-nacos.sh"
    fi
else
    log "Nacos start script not found, skipped"
fi

log "[2/4] Start backend services in separate windows"

if command -v ss >/dev/null 2>&1 && ss -ltn "( sport = :8082 )" | tail -n +2 | grep -q .; then
    USER_SERVICE_PORT=18082
    log "Port 8082 is busy, user-service will start on $USER_SERVICE_PORT"
fi

# 启动各个后端服务（每个服务在单独的终端窗口中）
export JOBPLUS_DB_URL JOBPLUS_DB_USERNAME JOBPLUS_DB_PASSWORD

if command -v xterm >/dev/null 2>&1; then
    xterm -title "JobPlus-Gateway" -e "export JOBPLUS_DB_URL='$JOBPLUS_DB_URL' JOBPLUS_DB_USERNAME='$JOBPLUS_DB_USERNAME' JOBPLUS_DB_PASSWORD='$JOBPLUS_DB_PASSWORD'; cd '$BACKEND_DIR/gateway' && mvn spring-boot:run 1>>'$LOG_DIR/gateway-$TS.log' 2>&1" &
    xterm -title "JobPlus-Auth" -e "export JOBPLUS_DB_URL='$JOBPLUS_DB_URL' JOBPLUS_DB_USERNAME='$JOBPLUS_DB_USERNAME' JOBPLUS_DB_PASSWORD='$JOBPLUS_DB_PASSWORD'; cd '$BACKEND_DIR/auth-service' && mvn spring-boot:run 1>>'$LOG_DIR/auth-$TS.log' 2>&1" &
    xterm -title "JobPlus-User" -e "export JOBPLUS_DB_URL='$JOBPLUS_DB_URL' JOBPLUS_DB_USERNAME='$JOBPLUS_DB_USERNAME' JOBPLUS_DB_PASSWORD='$JOBPLUS_DB_PASSWORD'; cd '$BACKEND_DIR/user-service' && mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=$USER_SERVICE_PORT 1>>'$LOG_DIR/user-$TS.log' 2>&1" &
    xterm -title "JobPlus-Job" -e "export JOBPLUS_DB_URL='$JOBPLUS_DB_URL' JOBPLUS_DB_USERNAME='$JOBPLUS_DB_USERNAME' JOBPLUS_DB_PASSWORD='$JOBPLUS_DB_PASSWORD'; cd '$BACKEND_DIR/job-service' && mvn spring-boot:run 1>>'$LOG_DIR/job-$TS.log' 2>&1" &
    xterm -title "JobPlus-Resume" -e "export JOBPLUS_DB_URL='$JOBPLUS_DB_URL' JOBPLUS_DB_USERNAME='$JOBPLUS_DB_USERNAME' JOBPLUS_DB_PASSWORD='$JOBPLUS_DB_PASSWORD'; cd '$BACKEND_DIR/resume-service' && mvn spring-boot:run 1>>'$LOG_DIR/resume-$TS.log' 2>&1" &
elif command -v gnome-terminal >/dev/null 2>&1; then
    gnome-terminal --title="JobPlus-Gateway" -- bash -c "export JOBPLUS_DB_URL='$JOBPLUS_DB_URL' JOBPLUS_DB_USERNAME='$JOBPLUS_DB_USERNAME' JOBPLUS_DB_PASSWORD='$JOBPLUS_DB_PASSWORD'; cd '$BACKEND_DIR/gateway' && mvn spring-boot:run 1>>'$LOG_DIR/gateway-$TS.log' 2>&1; exec bash" &
    gnome-terminal --title="JobPlus-Auth" -- bash -c "export JOBPLUS_DB_URL='$JOBPLUS_DB_URL' JOBPLUS_DB_USERNAME='$JOBPLUS_DB_USERNAME' JOBPLUS_DB_PASSWORD='$JOBPLUS_DB_PASSWORD'; cd '$BACKEND_DIR/auth-service' && mvn spring-boot:run 1>>'$LOG_DIR/auth-$TS.log' 2>&1; exec bash" &
    gnome-terminal --title="JobPlus-User" -- bash -c "export JOBPLUS_DB_URL='$JOBPLUS_DB_URL' JOBPLUS_DB_USERNAME='$JOBPLUS_DB_USERNAME' JOBPLUS_DB_PASSWORD='$JOBPLUS_DB_PASSWORD'; cd '$BACKEND_DIR/user-service' && mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=$USER_SERVICE_PORT 1>>'$LOG_DIR/user-$TS.log' 2>&1; exec bash" &
    gnome-terminal --title="JobPlus-Job" -- bash -c "export JOBPLUS_DB_URL='$JOBPLUS_DB_URL' JOBPLUS_DB_USERNAME='$JOBPLUS_DB_USERNAME' JOBPLUS_DB_PASSWORD='$JOBPLUS_DB_PASSWORD'; cd '$BACKEND_DIR/job-service' && mvn spring-boot:run 1>>'$LOG_DIR/job-$TS.log' 2>&1; exec bash" &
    gnome-terminal --title="JobPlus-Resume" -- bash -c "export JOBPLUS_DB_URL='$JOBPLUS_DB_URL' JOBPLUS_DB_USERNAME='$JOBPLUS_DB_USERNAME' JOBPLUS_DB_PASSWORD='$JOBPLUS_DB_PASSWORD'; cd '$BACKEND_DIR/resume-service' && mvn spring-boot:run 1>>'$LOG_DIR/resume-$TS.log' 2>&1; exec bash" &
else
    log "Warning: No terminal emulator found. Starting services in background..."
    cd "$BACKEND_DIR/gateway" && nohup env JOBPLUS_DB_URL="$JOBPLUS_DB_URL" JOBPLUS_DB_USERNAME="$JOBPLUS_DB_USERNAME" JOBPLUS_DB_PASSWORD="$JOBPLUS_DB_PASSWORD" mvn spring-boot:run > "$LOG_DIR/gateway-$TS.log" 2>&1 &
    cd "$BACKEND_DIR/auth-service" && nohup env JOBPLUS_DB_URL="$JOBPLUS_DB_URL" JOBPLUS_DB_USERNAME="$JOBPLUS_DB_USERNAME" JOBPLUS_DB_PASSWORD="$JOBPLUS_DB_PASSWORD" mvn spring-boot:run > "$LOG_DIR/auth-$TS.log" 2>&1 &
    cd "$BACKEND_DIR/user-service" && nohup env JOBPLUS_DB_URL="$JOBPLUS_DB_URL" JOBPLUS_DB_USERNAME="$JOBPLUS_DB_USERNAME" JOBPLUS_DB_PASSWORD="$JOBPLUS_DB_PASSWORD" mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=$USER_SERVICE_PORT > "$LOG_DIR/user-$TS.log" 2>&1 &
    cd "$BACKEND_DIR/job-service" && nohup env JOBPLUS_DB_URL="$JOBPLUS_DB_URL" JOBPLUS_DB_USERNAME="$JOBPLUS_DB_USERNAME" JOBPLUS_DB_PASSWORD="$JOBPLUS_DB_PASSWORD" mvn spring-boot:run > "$LOG_DIR/job-$TS.log" 2>&1 &
    cd "$BACKEND_DIR/resume-service" && nohup env JOBPLUS_DB_URL="$JOBPLUS_DB_URL" JOBPLUS_DB_USERNAME="$JOBPLUS_DB_USERNAME" JOBPLUS_DB_PASSWORD="$JOBPLUS_DB_PASSWORD" mvn spring-boot:run > "$LOG_DIR/resume-$TS.log" 2>&1 &
fi

log "Waiting 8 seconds before starting frontend..."
sleep 8

log "[3/4] Start frontend"
if command -v xterm >/dev/null 2>&1; then
    xterm -title "JobPlus-Frontend" -e "cd '$FRONTEND_DIR' && export npm_config_cache='$FRONTEND_DIR/.npm-cache' && export npm_config_offline=false && npm run dev 1>>'$LOG_DIR/frontend-$TS.log' 2>&1" &
elif command -v gnome-terminal >/dev/null 2>&1; then
    gnome-terminal --title="JobPlus-Frontend" -- bash -c "cd '$FRONTEND_DIR' && export npm_config_cache='$FRONTEND_DIR/.npm-cache' && export npm_config_offline=false && npm run dev 1>>'$LOG_DIR/frontend-$TS.log' 2>&1; exec bash" &
else
    log "Starting frontend in background..."
    cd "$FRONTEND_DIR" && export npm_config_cache="$FRONTEND_DIR/.npm-cache" && export npm_config_offline=false && nohup npm run dev > "$LOG_DIR/frontend-$TS.log" 2>&1 &
fi

log "[4/4] Startup command finished"
log "Frontend:  http://localhost:5173"
log "Gateway:   http://localhost:8080"
log "Auth:      http://localhost:8081"
log "User:      http://localhost:$USER_SERVICE_PORT"
log "Job:       http://localhost:8083"
log "Resume:    http://localhost:8084"
log "."
log "All processes are running. Check logs in $LOG_DIR"
log "To stop services, use: pkill -f 'mvn spring-boot:run' and pkill -f 'npm run dev'"
log "Press any key to exit this launcher window..."
read -n 1 -s
exit 0
