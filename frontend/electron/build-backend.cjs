const fs = require('fs')
const path = require('path')
const { spawnSync } = require('child_process')

const frontendDir = path.resolve(__dirname, '..')
const rootDir = path.resolve(frontendDir, '..')
const backendDir = path.join(rootDir, 'backend')
const backendOutDir = path.join(frontendDir, 'backend-services')
const javaRuntimeDir = path.join(frontendDir, 'java-runtime')

const services = ['auth-service', 'user-service', 'job-service', 'resume-service', 'gateway']

function run(command, args, options = {}) {
  const result = spawnSync(command, args, {
    cwd: options.cwd || rootDir,
    stdio: 'inherit',
    shell: process.platform === 'win32',
    windowsHide: true
  })
  if (result.error) throw result.error
  if (result.status !== 0) {
    throw new Error(`${command} ${args.join(' ')} failed with exit code ${result.status}`)
  }
}

function findExecutable(name) {
  if (process.platform !== 'win32') return name
  const result = spawnSync('cmd.exe', ['/c', `where ${name}`], {
    cwd: rootDir,
    encoding: 'utf8',
    windowsHide: true
  })
  if (result.status !== 0) return null
  return result.stdout.split(/\r?\n/).map(line => line.trim()).find(Boolean) || null
}

function findJavaHome() {
  if (process.env.JAVA_HOME) return process.env.JAVA_HOME
  const javaPath = findExecutable(process.platform === 'win32' ? 'java.exe' : 'java')
  if (!javaPath) return null
  return path.dirname(path.dirname(javaPath))
}

function copyServiceJars() {
  fs.rmSync(backendOutDir, { recursive: true, force: true })
  fs.mkdirSync(backendOutDir, { recursive: true })

  for (const service of services) {
    const targetDir = path.join(backendDir, service, 'target')
    const jar = fs.readdirSync(targetDir)
      .filter(file => file.endsWith('.jar') && !file.endsWith('.jar.original') && !file.includes('sources') && !file.includes('javadoc'))
      .sort((a, b) => b.length - a.length)[0]
    if (!jar) throw new Error(`No packaged jar found for ${service}`)

    const source = path.join(targetDir, jar)
    const destination = path.join(backendOutDir, `${service}.jar`)
    fs.copyFileSync(source, destination)
    console.log(`copied ${source} -> ${destination}`)
  }
}

function buildJavaRuntime() {
  const javaHome = findJavaHome()
  if (!javaHome) {
    console.warn('JAVA_HOME/java not found; packaged app will use java from PATH')
    return
  }

  const jlink = path.join(javaHome, 'bin', process.platform === 'win32' ? 'jlink.exe' : 'jlink')
  if (!fs.existsSync(jlink)) {
    console.warn(`jlink not found at ${jlink}; packaged app will use java from PATH`)
    return
  }

  fs.rmSync(javaRuntimeDir, { recursive: true, force: true })

  const modules = [
    'java.base',
    'java.compiler',
    'java.datatransfer',
    'java.desktop',
    'java.instrument',
    'java.logging',
    'java.management',
    'java.naming',
    'java.net.http',
    'java.prefs',
    'java.rmi',
    'java.scripting',
    'java.security.jgss',
    'java.security.sasl',
    'java.sql',
    'java.transaction.xa',
    'java.xml',
    'java.xml.crypto',
    'jdk.charsets',
    'jdk.crypto.ec',
    'jdk.crypto.cryptoki',
    'jdk.httpserver',
    'jdk.jfr',
    'jdk.localedata',
    'jdk.management',
    'jdk.management.agent',
    'jdk.naming.dns',
    'jdk.unsupported',
    'jdk.zipfs'
  ]

  run(jlink, [
    '--add-modules', modules.join(','),
    '--output', javaRuntimeDir,
    '--strip-debug',
    '--no-header-files',
    '--no-man-pages'
  ])
  console.log(`created Java runtime at ${javaRuntimeDir}`)
}

const mvn = process.platform === 'win32' ? 'mvn' : 'mvn'
run(mvn, ['-B', '-ntp', 'package', '-DskipTests'], { cwd: backendDir })
copyServiceJars()
buildJavaRuntime()
