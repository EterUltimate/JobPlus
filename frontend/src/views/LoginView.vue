<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1 class="logo">JobPlus</h1>
      <p class="subtitle">兼职 · 实习 · 全职</p>
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名 / 学号" size="large" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" prefix-icon="Lock"
            show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" class="submit-btn" @click="handleLogin">
          登 录
        </el-button>
      </el-form>
      <div class="extra-links">
        <router-link to="/register">还没有账号？去注册</router-link>
      </div>
      <el-divider />
      <div class="demo-hint">
        <p>演示账号：</p>
        <el-tag v-for="u in demoUsers" :key="u.username" class="demo-tag" @click="fillDemo(u)">
          {{ u.label }}
        </el-tag>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const demoUsers = [
  { label: '求职者 张三', username: 'seeker001', password: '123456' },
  { label: 'HR 王HR', username: 'hr001', password: '123456' },
]

function fillDemo(u: any) {
  form.username = u.username
  form.password = u.password
}

async function handleLogin() {
  try {
    await formRef.value.validate()
    loading.value = true
    await auth.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push(auth.role === 'HR' ? '/hr/dashboard' : '/jobs')
  } catch (e: any) {
    ElMessage.error(e.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.auth-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px 36px;
  width: 400px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.logo { font-size: 32px; font-weight: 800; text-align: center; color: #667eea; margin-bottom: 4px; }
.subtitle { text-align: center; color: #999; margin-bottom: 28px; font-size: 14px; }
.submit-btn { width: 100%; margin-top: 8px; height: 44px; font-size: 16px; }
.extra-links { text-align: center; margin-top: 12px; font-size: 14px; }
.extra-links a { color: #667eea; }
.demo-hint { font-size: 13px; color: #888; text-align: center; }
.demo-tag { margin: 4px; cursor: pointer; }
</style>
