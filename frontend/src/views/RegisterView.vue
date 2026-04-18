<template>
  <div class="auth-page">
    <div class="auth-card">
      <h1 class="logo">JobPlus</h1>
      <p class="subtitle">创建你的账号</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleRegister">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="用于登录" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-radio-group v-model="form.role">
            <el-radio value="SEEKER">我是求职者</el-radio>
            <el-radio value="HR">我是 HR</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="至少6位" show-password />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="选填" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="选填" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" class="submit-btn" @click="handleRegister">
          注 册
        </el-button>
      </el-form>
      <div class="extra-links">
        <router-link to="/login">已有账号？去登录</router-link>
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

const form = reactive({ username: '', realName: '', role: 'SEEKER', password: '', phone: '', email: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true }],
  password: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }],
}

async function handleRegister() {
  try {
    await formRef.value.validate()
    loading.value = true
    await auth.register(form)
    ElMessage.success('注册成功')
    router.push('/jobs')
  } catch (e: any) {
    ElMessage.error(e.message || '注册失败')
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
  padding: 36px;
  width: 420px;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}
.logo { font-size: 28px; font-weight: 800; text-align: center; color: #667eea; margin-bottom: 4px; }
.subtitle { text-align: center; color: #999; margin-bottom: 24px; }
.submit-btn { width: 100%; height: 44px; font-size: 16px; margin-top: 8px; }
.extra-links { text-align: center; margin-top: 12px; font-size: 14px; }
.extra-links a { color: #667eea; }
</style>
