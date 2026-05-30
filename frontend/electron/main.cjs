const { app, BrowserWindow, shell } = require('electron')
const fs = require('fs')
const http = require('http')
const path = require('path')
const { spawn } = require('child_process')

const backendProcesses = []

const backendServices = [
  { name: 'auth-service', jar: 'auth-service.jar', port: 18081 },
  { name: 'user-service', jar: 'user-service.jar', port: 18082 },
  { name: 'job-service', jar: 'job-service.jar', port: 18083 },
  { name: 'resume-service', jar: 'resume-service.jar', port: 18084 },
  {
    name: 'gateway',
    jar: 'gateway.jar',
    port: 18080,
    args: [
      '--jobplus.gateway.auth-uri=http://127.0.0.1:18081',
      '--jobplus.gateway.user-uri=http://127.0.0.1:18082',
      '--jobplus.gateway.job-uri=http://127.0.0.1:18083',
      '--jobplus.gateway.resume-uri=http://127.0.0.1:18083',
      '--jobplus.gateway.circuit-breaker.enabled=false'
    ]
  }
]

function resourcePath(name) {
  return app.isPackaged
    ? path.join(process.resourcesPath, name)
    : path.join(__dirname, '..', name)
}

function getJavaCommand() {
  const bundledJava = path.join(resourcePath('java-runtime'), 'bin', process.platform === 'win32' ? 'java.exe' : 'java')
  if (fs.existsSync(bundledJava)) return bundledJava
  return process.env.JOBPLUS_JAVA_PATH || 'java'
}

function toSqliteUrl(filePath) {
  return `jdbc:sqlite:${filePath.replace(/\\/g, '/')}`
}

function createBackendRuntime() {
  const userDataDir = app.getPath('userData')
  const dataDir = path.join(userDataDir, 'data')
  const logDir = path.join(userDataDir, 'logs')
  fs.mkdirSync(dataDir, { recursive: true })
  fs.mkdirSync(logDir, { recursive: true })

  const sqliteFile = path.join(dataDir, 'jobplus.sqlite')
  return {
    logDir,
    env: {
      ...process.env,
      JOBPLUS_DB_FALLBACK_ENABLED: 'true',
      JOBPLUS_DB_PROBE_TIMEOUT_MS: '800',
      JOBPLUS_SQLITE_URL: toSqliteUrl(sqliteFile),
      JOBPLUS_DB_URL: process.env.JOBPLUS_DB_URL || 'jdbc:postgresql://127.0.0.1:5432/jobplus',
      JOBPLUS_DB_USERNAME: process.env.JOBPLUS_DB_USERNAME || 'postgres',
      JOBPLUS_DB_PASSWORD: process.env.JOBPLUS_DB_PASSWORD || 'postgres',
      JOBPLUS_AUTH_REDIS_REQUIRED: 'false',
      NACOS_ADDR: '127.0.0.1:8848',
      REDIS_HOST: '127.0.0.1',
      CORS_ALLOWED_ORIGINS: '*'
    }
  }
}

function commonBackendArgs(port) {
  return [
    '-Dfile.encoding=UTF-8',
    '-jar',
    '',
    `--server.port=${port}`,
    '--spring.cloud.discovery.enabled=false',
    '--spring.cloud.nacos.discovery.enabled=false',
    '--spring.cloud.nacos.config.enabled=false',
    '--management.health.redis.enabled=false',
    '--management.health.diskspace.enabled=false',
    '--jobplus.auth.redis-required=false'
  ]
}

function startService(service, backendDir, javaCommand, runtime) {
  const jarPath = path.join(backendDir, service.jar)
  if (!fs.existsSync(jarPath)) {
    console.warn(`[backend] ${service.jar} not found, skipped`)
    return
  }

  const args = commonBackendArgs(service.port)
  args[2] = jarPath
  args.push(...(service.args || []))

  const logFd = fs.openSync(path.join(runtime.logDir, `${service.name}.log`), 'a')
  fs.writeSync(logFd, `\n[${new Date().toISOString()}] starting ${service.name} on ${service.port}\n`)

  let child
  try {
    child = spawn(javaCommand, args, {
      cwd: backendDir,
      env: runtime.env,
      windowsHide: true,
      stdio: ['ignore', logFd, logFd]
    })
  } catch (err) {
    fs.writeSync(logFd, `[${new Date().toISOString()}] failed to spawn: ${err.message}\n`)
    fs.closeSync(logFd)
    return
  }

  child.on('error', err => {
    fs.writeSync(logFd, `[${new Date().toISOString()}] failed to start: ${err.message}\n`)
  })
  child.on('exit', (code, signal) => {
    fs.writeSync(logFd, `[${new Date().toISOString()}] exited code=${code} signal=${signal}\n`)
    fs.closeSync(logFd)
  })

  backendProcesses.push({ child, logFd })
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

function probeHttp(url) {
  return new Promise(resolve => {
    let settled = false
    const done = value => {
      if (!settled) {
        settled = true
        resolve(value)
      }
    }
    const req = http.get(url, res => {
      res.resume()
      done(true)
    })
    req.on('error', () => done(false))
    req.setTimeout(1000, () => {
      req.destroy()
      done(false)
    })
  })
}

async function waitForGateway() {
  const deadline = Date.now() + 45000
  while (Date.now() < deadline) {
    if (await probeHttp('http://127.0.0.1:18080/actuator/health')) return true
    await sleep(1000)
  }
  return false
}

async function startBackendServices() {
  const backendDir = resourcePath('backend')
  if (!fs.existsSync(backendDir)) {
    console.warn('[backend] packaged backend directory not found, frontend will still open')
    return
  }

  const javaCommand = getJavaCommand()
  const runtime = createBackendRuntime()
  for (const service of backendServices) {
    startService(service, backendDir, javaCommand, runtime)
    await sleep(500)
  }
  await waitForGateway()
}

function stopBackendServices() {
  for (const { child } of backendProcesses) {
    if (!child.pid || child.killed) continue
    if (process.platform === 'win32') {
      spawn('taskkill', ['/pid', String(child.pid), '/T', '/F'], { windowsHide: true, stdio: 'ignore' })
    } else {
      child.kill('SIGTERM')
    }
  }
}

function createWindow() {
  const win = new BrowserWindow({
    width: 1280,
    height: 820,
    minWidth: 960,
    minHeight: 640,
    title: 'JobPlus',
    webPreferences: {
      contextIsolation: true,
      nodeIntegration: false,
      sandbox: true
    }
  })

  win.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url)
    return { action: 'deny' }
  })

  win.loadFile(path.join(__dirname, '..', 'dist', 'index.html'))
}

app.whenReady().then(async () => {
  await startBackendServices()
  createWindow()
  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})

app.on('before-quit', () => {
  stopBackendServices()
})

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') app.quit()
})
