#!/bin/bash

# JobPlus 一键依赖安装脚本 (Linux版本)

# Get project root directory (parent of scripts directory)
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
LOG_DIR="$ROOT/logs"
mkdir -p "$LOG_DIR"

TS=$(date +"%Y%m%d-%H%M%S")
LOG_FILE="$LOG_DIR/setup-all-$TS.log"

log() {
    echo "$1"
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" >> "$LOG_FILE"
}

log "=========================================="
log "JobPlus one-click dependency setup"
log "Log file: $LOG_FILE"
log "=========================================="

cd "$ROOT"

HAS_JAVA=0
HAS_MVN=0
HAS_NODE=0
HAS_NPM=0

command -v java >/dev/null 2>&1 && HAS_JAVA=1
command -v mvn >/dev/null 2>&1 && HAS_MVN=1
command -v node >/dev/null 2>&1 && HAS_NODE=1
command -v npm >/dev/null 2>&1 && HAS_NPM=1

if [ "$HAS_JAVA" -eq 1 ]; then
    JAVA_VER=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    log "[OK] java detected: $JAVA_VER"
else
    log "[MISS] java not found in PATH"
fi

if [ "$HAS_MVN" -eq 1 ]; then
    MVN_VER=$(mvn -v | grep "Apache Maven" | awk '{print $3}')
    log "[OK] maven detected: $MVN_VER"
else
    log "[MISS] maven not found in PATH"
fi

if [ "$HAS_NODE" -eq 1 ]; then
    NODE_VER=$(node -v)
    log "[OK] node detected: $NODE_VER"
else
    log "[MISS] node not found in PATH"
fi

if [ "$HAS_NPM" -eq 1 ]; then
    NPM_VER=$(npm -v)
    log "[OK] npm detected: $NPM_VER"
else
    log "[MISS] npm not found in PATH"
fi

if [ "$HAS_JAVA" -ne 1 ] || [ "$HAS_MVN" -ne 1 ] || [ "$HAS_NODE" -ne 1 ] || [ "$HAS_NPM" -ne 1 ]; then
    log "."
    log "Missing required tools. Please install: JDK 21, Maven 3.9+, Node.js 18+"
    log "Then rerun setup-all.sh"
    log "Press any key to exit..."
    read -n 1 -s
    exit 1
fi

log "."
log "[1/3] Pre-fetch backend dependencies"
cd "$ROOT/backend"
mvn -q -DskipTests dependency:go-offline >> "$LOG_FILE" 2>&1
RC=$?
log "backend dependency:go-offline exit code: $RC"
if [ "$RC" -ne 0 ]; then
    log "."
    log "Setup failed. Check log: $LOG_FILE"
    log "Press any key to exit..."
    read -n 1 -s
    exit 1
fi

log "[2/3] Compile backend modules"
mvn -q -DskipTests clean compile >> "$LOG_FILE" 2>&1
RC=$?
log "backend compile exit code: $RC"
if [ "$RC" -ne 0 ]; then
    log "."
    log "Setup failed. Check log: $LOG_FILE"
    log "Press any key to exit..."
    read -n 1 -s
    exit 1
fi

log "[3/3] Install frontend dependencies"
cd "$ROOT/frontend"
mkdir -p "$ROOT/frontend/.npm-cache"
export npm_config_cache="$ROOT/frontend/.npm-cache"
export npm_config_offline=false
npm install >> "$LOG_FILE" 2>&1
RC=$?
log "frontend npm install exit code: $RC"
if [ "$RC" -ne 0 ]; then
    log "."
    log "Setup failed. Check log: $LOG_FILE"
    log "Press any key to exit..."
    read -n 1 -s
    exit 1
fi

log "."
log "Setup completed successfully."
log "Next: run start-all.sh"
log "Press any key to exit..."
read -n 1 -s
exit 0