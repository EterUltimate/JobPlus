<template>
  <div class="layout">
    <!-- 顶部导航 -->
    <header class="navbar">
      <div class="nav-inner">
        <div class="logo">JobPlus</div>
        <nav>
          <router-link to="/home">首页</router-link>
          <router-link to="/jobs">职位</router-link>
          <template v-if="auth.isLoggedIn">
            <router-link to="/resume" v-if="auth.role === 'SEEKER'">我的简历</router-link>
            <router-link to="/my-deliveries" v-if="auth.role === 'SEEKER'">我的投递</router-link>
            <router-link to="/hr/dashboard" v-if="auth.role === 'HR'">HR 工作台</router-link>
          </template>
        </nav>
        <div class="nav-right">
          <template v-if="!auth.isLoggedIn">
            <router-link to="/login"><el-button>登录</el-button></router-link>
            <router-link to="/register"><el-button type="primary">注册</el-button></router-link>
          </template>
          <template v-else>
            <el-dropdown>
              <span class="user-chip">
                <el-avatar :size="32" :src="`https://api.dicebear.com/7.x/initials/svg?seed=${auth.userInfo?.username}`" />
                <span>{{ auth.userInfo?.username }}</span>
              </span>
              <template #dropdown>
                <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
              </template>
            </el-dropdown>
          </template>
        </div>
      </div>
    </header>

    <!-- Hero 区域 -->
    <section class="hero">
      <h1>找一份满意的兼职 / 实习</h1>
      <p>覆盖全城实时更新的岗位，支持一键投递</p>
      <div class="hero-search">
        <el-input v-model="keyword" placeholder="搜索职位、公司、地点..." size="large" @keyup.enter="goSearch">
          <template #append>
            <el-button :icon="Search" @click="goSearch">搜索</el-button>
          </template>
        </el-input>
      </div>
    </section>

    <!-- 热门岗位 -->
    <section class="section">
      <div class="section-header">
        <h2>🔥 正在招聘</h2>
        <router-link to="/jobs"><el-button text>查看全部 →</el-button></router-link>
      </div>
      <div class="job-grid" v-loading="loading">
        <div v-for="job in jobs" :key="job.id" class="job-card" @click="goDetail(job.id)">
          <div class="job-title">{{ job.title }}</div>
          <div class="job-company">{{ job.location }}</div>
          <div class="job-salary">{{ formatSalary(job) }}</div>
          <div class="job-tags">
            <el-tag v-for="t in (job.tags || '').split(',')" :key="t" size="small" v-if="t">{{ t }}</el-tag>
          </div>
          <div class="job-meta">
            <span>{{ job.workTypeText }}</span>
            <span>{{ dayjs(job.createTime).format('MM-DD') }}发布</span>
          </div>
        </div>
        <el-empty v-if="!loading && jobs.length === 0" description="暂无职位" />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { useAuthStore, api } from '@/stores/auth'
import dayjs from 'dayjs'

const router = useRouter()
const auth = useAuthStore()
const keyword = ref('')
const jobs = ref<any[]>([])
const loading = ref(true)

function formatSalary(job: any) {
  if (!job.salaryMin && !job.salaryMax) return '面议'
  const unit = job.salaryType === 'daily' ? '元/天' : job.salaryType === 'hourly' ? '元/时' : '元/月'
  if (job.salaryMax) return `${job.salaryMin}-${job.salaryMax}${unit}`
  return `${job.salaryMin}${unit}以下`
}

async function loadJobs() {
  try {
    const { data } = await api.get('/jobs?page=1&size=6')
    if (data.code === 200) jobs.value = data.data.records || []
  } catch { jobs.value = [] }
  finally { loading.value = false }
}

function goSearch() { router.push(`/jobs?keyword=${keyword.value}`) }
function goDetail(id: number) { router.push(`/jobs/${id}`) }
function logout() { auth.logout(); router.push('/login') }

onMounted(() => { loadJobs() })
</script>

<style scoped>
.navbar { background: #fff; border-bottom: 1px solid #eee; position: sticky; top: 0; z-index: 100; }
.nav-inner { max-width: 1200px; margin: 0 auto; padding: 0 24px; height: 60px; display: flex; align-items: center; gap: 32px; }
.logo { font-weight: 800; font-size: 20px; color: #667eea; }
nav { display: flex; gap: 20px; flex: 1; }
nav a { color: #666; font-size: 15px; }
nav a.router-link-active { color: #667eea; font-weight: 600; }
.nav-right { display: flex; gap: 8px; align-items: center; }
.user-chip { display: flex; align-items: center; gap: 8px; cursor: pointer; }

.hero { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; padding: 80px 24px; text-align: center; }
.hero h1 { font-size: 42px; margin-bottom: 12px; }
.hero p { font-size: 18px; opacity: 0.9; margin-bottom: 32px; }
.hero-search { max-width: 600px; margin: 0 auto; }

.section { max-width: 1200px; margin: 48px auto; padding: 0 24px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.section-header h2 { font-size: 22px; }
.job-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); gap: 20px; }
.job-card { background: #fff; border-radius: 12px; padding: 20px; cursor: pointer; border: 1px solid #eee; transition: all 0.2s; }
.job-card:hover { box-shadow: 0 8px 24px rgba(102,126,234,0.15); border-color: #667eea; transform: translateY(-2px); }
.job-title { font-size: 16px; font-weight: 600; color: #333; margin-bottom: 6px; }
.job-company { color: #666; font-size: 14px; margin-bottom: 8px; }
.job-salary { color: #f56c6c; font-size: 16px; font-weight: 600; margin-bottom: 10px; }
.job-tags { display: flex; flex-wrap: wrap; gap: 4px; margin-bottom: 10px; }
.job-meta { display: flex; justify-content: space-between; font-size: 12px; color: #999; }
</style>
