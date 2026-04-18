# JobPlus PostgreSQL 数据库初始化脚本
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "JobPlus PostgreSQL 数据库初始化" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$psqlPath = "C:\Program Files\PostgreSQL\18\bin\psql.exe"
$sqlFile = "C:\Users\zacza\Desktop\x\JobPlus\scripts\init-postgresql.sql"

# 检查 psql 是否存在
if (-Not (Test-Path $psqlPath)) {
    Write-Host "错误: 找不到 psql.exe，请确认 PostgreSQL 已安装" -ForegroundColor Red
    exit 1
}

# 检查 SQL 文件是否存在
if (-Not (Test-Path $sqlFile)) {
    Write-Host "错误: 找不到初始化 SQL 文件: $sqlFile" -ForegroundColor Red
    exit 1
}

Write-Host "`n请输入 PostgreSQL postgres 用户的密码:" -ForegroundColor Yellow
Write-Host "(如果密码是 'postgres'，直接按回车即可)" -ForegroundColor Gray

# 尝试连接并执行 SQL
Write-Host "`n正在连接到 PostgreSQL..." -ForegroundColor Green

# 使用交互式密码输入
$securePassword = Read-Host "密码" -AsSecureString
$password = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto([System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword))

$env:PGPASSWORD = $password

& $psqlPath -U postgres -f $sqlFile

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✓ 数据库初始化成功！" -ForegroundColor Green
    Write-Host "数据库: jobplus" -ForegroundColor Green
    Write-Host "用户: postgres" -ForegroundColor Green
    Write-Host "端口: 5432" -ForegroundColor Green
} else {
    Write-Host "`n✗ 数据库初始化失败，退出码: $LASTEXITCODE" -ForegroundColor Red
}

# 清除环境变量
Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue

Write-Host "`n按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
