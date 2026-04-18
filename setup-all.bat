@echo off
setlocal EnableDelayedExpansion
chcp 65001 >nul

set "ROOT=%~dp0"
set "LOG_DIR=%ROOT%logs"
if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
for /f %%i in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd-HHmmss"') do set "TS=%%i"
set "LOG_FILE=%LOG_DIR%\setup-all-%TS%.log"

call :log "=========================================="
call :log "JobPlus one-click dependency setup"
call :log "Log file: %LOG_FILE%"
call :log "=========================================="

cd /d "%ROOT%"

set "HAS_JAVA=0"
set "HAS_MVN=0"
set "HAS_NODE=0"
set "HAS_NPM=0"

where java >nul 2>nul && set "HAS_JAVA=1"
where mvn >nul 2>nul && set "HAS_MVN=1"
where node >nul 2>nul && set "HAS_NODE=1"
where npm >nul 2>nul && set "HAS_NPM=1"

if "%HAS_JAVA%"=="1" (
  for /f "tokens=2 delims==" %%v in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr /i "java.version"') do set "JAVA_VER=%%v"
  call :log "[OK] java detected: !JAVA_VER!"
) else (
  call :log "[MISS] java not found in PATH"
)

if "%HAS_MVN%"=="1" (
  for /f "tokens=3" %%v in ('mvn -v ^| findstr /b /c:"Apache Maven"') do set "MVN_VER=%%v"
  call :log "[OK] maven detected: !MVN_VER!"
) else (
  call :log "[MISS] maven not found in PATH"
)

if "%HAS_NODE%"=="1" (
  for /f %%v in ('node -v') do set "NODE_VER=%%v"
  call :log "[OK] node detected: !NODE_VER!"
) else (
  call :log "[MISS] node not found in PATH"
)

if "%HAS_NPM%"=="1" (
  for /f %%v in ('npm -v') do set "NPM_VER=%%v"
  call :log "[OK] npm detected: !NPM_VER!"
) else (
  call :log "[MISS] npm not found in PATH"
)

if not "%HAS_JAVA%"=="1" goto :deps_missing
if not "%HAS_MVN%"=="1" goto :deps_missing
if not "%HAS_NODE%"=="1" goto :deps_missing
if not "%HAS_NPM%"=="1" goto :deps_missing

goto :install

:deps_missing
call :log "."
call :log "Missing required tools. Please install: JDK 21, Maven 3.9+, Node.js 18+"
call :log "Then rerun setup-all.bat"
call :log "Press any key to exit..."
pause >nul
exit /b 1

:install
call :log "."
call :log "[1/3] Pre-fetch backend dependencies"
cd /d "%ROOT%backend"
call mvn -q -DskipTests dependency:go-offline >> "%LOG_FILE%" 2>&1
set "RC=!errorlevel!"
call :log "backend dependency:go-offline exit code: !RC!"
if not "!RC!"=="0" goto :fail

call :log "[2/3] Compile backend modules"
call mvn -q -DskipTests clean compile >> "%LOG_FILE%" 2>&1
set "RC=!errorlevel!"
call :log "backend compile exit code: !RC!"
if not "!RC!"=="0" goto :fail

call :log "[3/3] Install frontend dependencies"
cd /d "%ROOT%frontend"
if not exist "%ROOT%frontend\.npm-cache" mkdir "%ROOT%frontend\.npm-cache"
set "npm_config_cache=%ROOT%frontend\.npm-cache"
set "npm_config_offline=false"
call npm install >> "%LOG_FILE%" 2>&1
set "RC=!errorlevel!"
call :log "frontend npm install exit code: !RC!"
if not "!RC!"=="0" goto :fail

call :log "."
call :log "Setup completed successfully."
call :log "Next: run start-all.bat"
call :log "Press any key to exit..."
pause >nul
exit /b 0

:fail
call :log "."
call :log "Setup failed. Check log: %LOG_FILE%"
call :log "Press any key to exit..."
pause >nul
exit /b 1

:log
echo %~1
echo [%date% %time%] %~1>> "%LOG_FILE%"
exit /b 0

