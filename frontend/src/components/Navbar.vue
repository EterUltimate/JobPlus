<template>
  <header class="navbar">
    <div class="nav-inner">
      <router-link to="/" class="logo">JobPlus</router-link>
      <nav>
        <router-link to="/home">首页</router-link>
        <router-link to="/jobs">职位</router-link>
        <router-link to="/resume" v-if="auth.isLoggedIn && auth.role === 'SEEKER'">我的简历</router-link>
        <router-link to="/my-deliveries" v-if="auth.isLoggedIn && auth.role === 'SEEKER'">我的投递</router-link>
        <router-link to="/hr/dashboard" v-if="auth.isLoggedIn && auth.role === 'HR'">HR 工作台</router-link>
      </nav>
      <div class="nav-right">
        <template v-if="!auth.isLoggedIn">
          <router-link to="/login"><el-button size="small">登录</el-button></router-link>
          <router-link to="/register"><el-button type="primary" size="small">注册</el-button></router-link>
        </template>
        <template v-else>
          <el-dropdown>
            <span class="user-chip">
              <el-avatar :size="28" :src="`https://api.dicebear.com/7.x/initials/svg?seed=${auth.userInfo?.username}`" />
              <span>{{ auth.userInfo?.username || '用户' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-item @click="router.push('/home')">首页</el-dropdown-item>
              <el-dropdown-item divided @click="logout">退出登录</el-dropdown-item>
            </template>
          </el-dropdown>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
function logout() { auth.logout(); router.push('/login') }
</script>

<style scoped>
.navbar { background: #fff; border-bottom: 1px solid #eee; position: sticky; top: 0; z-index: 100; }
.nav-inner { max-width: 1200px; margin: 0 auto; padding: 0 24px; height: 56px; display: flex; align-items: center; gap: 28px; }
.logo { font-weight: 800; font-size: 20px; color: #667eea; }
nav { display: flex; gap: 18px; flex: 1; }
nav a { color: #555; font-size: 14px; transition: color 0.2s; }
nav a:hover, nav a.router-link-active { color: #667eea; font-weight: 600; }
.nav-right { display: flex; gap: 8px; align-items: center; }
.user-chip { display: flex; align-items: center; gap: 6px; cursor: pointer; font-size: 14px; }
</style>
