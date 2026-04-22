@echo off
setlocal EnableDelayedExpansion
chcp 65001 >nul

REM Get project root directory (parent of scripts directory)
set "SCRIPT_DIR=%~dp0"
for %%i in ("%SCRIPT_DIR%..") do set "ROOT=%%~fi"
set "LOG_DIR=%ROOT%logs"
set "BACKEND_DIR=%ROOT%backend"
set "FRONTEND_DIR=%ROOT%frontend"

if not exist "%LOG_DIR%" mkdir "%LOG_DIR%"
for /f %%i in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd-HHmmss"') do set "TS=%%i"
set "LOG_FILE=%LOG_DIR%\start-all-%TS%.log"

call :log "=========================================="
call :log "JobPlus one-click startup"
call :log "Log file: %LOG_FILE%"
call :log "=========================================="

cd /d "%ROOT%"

set "HAS_MVN=0"
set "HAS_NODE=0"
set "HAS_NPM=0"
where mvn >nul 2>nul && set "HAS_MVN=1"
where node >nul 2>nul && set "HAS_NODE=1"
where npm >nul 2>nul && set "HAS_NPM=1"

if "%HAS_MVN%"=="0" (
  call :log "[FAIL] mvn not found in PATH"
  goto :end_fail
)
if "%HAS_NODE%"=="0" (
  call :log "[FAIL] node not found in PATH"
  goto :end_fail
)
if "%HAS_NPM%"=="0" (
  call :log "[FAIL] npm not found in PATH"
  goto :end_fail
)

call :log "[1/4] Optional: start Nacos"
if exist "%ROOT%scripts\start-nacos.bat" (
  start "JobPlus-Nacos" cmd /k "cd /d %ROOT%scripts && call start-nacos.bat"
  call :log "Nacos window opened"
) else (
  call :log "Nacos start script not found, skipped"
)

call :log "[2/4] Start backend services in separate windows"
start "JobPlus-Gateway" cmd /k "cd /d %BACKEND_DIR%\gateway && call mvn spring-boot:run 1>>""%LOG_DIR%\gateway-%TS%.log"" 2>&1"
start "JobPlus-Auth" cmd /k "cd /d %BACKEND_DIR%\auth-service && call mvn spring-boot:run 1>>""%LOG_DIR%\auth-%TS%.log"" 2>&1"
start "JobPlus-User" cmd /k "cd /d %BACKEND_DIR%\user-service && call mvn spring-boot:run 1>>""%LOG_DIR%\user-%TS%.log"" 2>&1"
start "JobPlus-Job" cmd /k "cd /d %BACKEND_DIR%\job-service && call mvn spring-boot:run 1>>""%LOG_DIR%\job-%TS%.log"" 2>&1"
start "JobPlus-Resume" cmd /k "cd /d %BACKEND_DIR%\resume-service && call mvn spring-boot:run 1>>""%LOG_DIR%\resume-%TS%.log"" 2>&1"

call :log "Waiting 8 seconds before starting frontend..."
timeout /t 8 /nobreak >nul

call :log "[3/4] Start frontend"
start "JobPlus-Frontend" cmd /k "cd /d %FRONTEND_DIR% && set npm_config_cache=%FRONTEND_DIR%\.npm-cache && set npm_config_offline=false && call npm run dev 1>>""%LOG_DIR%\frontend-%TS%.log"" 2>&1"

call :log "[4/4] Startup command finished"
call :log "Frontend:  http://localhost:5173"
call :log "Gateway:   http://localhost:8080"
call :log "Auth:      http://localhost:8081"
call :log "User:      http://localhost:8082"
call :log "Job:       http://localhost:8083"
call :log "Resume:    http://localhost:8084"
call :log "."
call :log "All processes run in dedicated cmd windows and must be stopped manually."
call :log "Press any key to exit this launcher window..."
pause >nul
exit /b 0

:end_fail
call :log "Please run setup-all.bat first."
call :log "Press any key to exit..."
pause >nul
exit /b 1

:log
echo %~1
echo [%date% %time%] %~1>> "%LOG_FILE%"
exit /b 0
