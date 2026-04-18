<template>
  <div class="layout">
    <Navbar />
    <div class="page" v-loading="loading">
      <template v-if="job">
        <div class="job-header">
          <div>
            <h1>{{ job.title }}</h1>
            <div class="meta">
              <span>📍 {{ job.location || '未标注' }}</span>
              <span>💰 {{ formatSalary(job) }}</span>
              <span>👁 {{ job.viewCount || 0 }}次浏览</span>
              <span>📮 {{ job.applyCount || 0 }}次投递</span>
            </div>
          </div>
          <div class="actions">
            <template v-if="auth.isLoggedIn && auth.role === 'SEEKER'">
              <el-button type="primary" size="large" :loading="applying" @click="applyJob">
                立即投递
              </el-button>
            </template>
            <template v-else-if="!auth.isLoggedIn">
              <el-button type="primary" size="large" @click="router.push('/login')">登录后投递</el-button>
            </template>
            <el-tag v-if="job.status === 1" type="success" size="large">招聘中</el-tag>
            <el-tag v-else type="info" size="large">已停止</el-tag>
          </div>
        </div>

        <el-card class="content-card">
          <h3>职位描述</h3>
          <p>{{ job.description || '暂无描述' }}</p>
          <el-divider />
          <h3>岗位要求</h3>
          <p>{{ job.requirements || '暂无要求' }}</p>
          <el-divider />
          <h3>职位信息</h3>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="薪资范围">{{ formatSalary(job) }}</el-descriptions-item>
            <el-descriptions-item label="工作方式">
              {{ workTypeMap[job.workType] || job.workType }}
            </el-descriptions-item>
            <el-descriptions-item label="发布时间">
              {{ dayjs(job.createTime).format('YYYY-MM-DD HH:mm') }}
            </el-descriptions-item>
            <el-descriptions-item label="薪资类型">
              {{ salaryTypeMap[job.salaryType] || job.salaryType }}
            </el-descriptions-item>
          </el-descriptions>
          <div class="tags" v-if="job.tags">
            <el-tag v-for="tag in job.tags.split(',')" :key="tag" style="margin:4px">{{ tag }}</el-tag>
          </div>
        </el-card>
      </template>
      <el-empty v-else-if="!loading" description="职位不存在" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore, api } from '@/stores/auth'
import Navbar from '@/components/Navbar.vue'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const job = ref<any>(null)
const loading = ref(true)
const applying = ref(false)

const workTypeMap: any = { remote: '远程办公', onsite: 'on-site', hybrid: '混合办公' }
const salaryTypeMap: any = { monthly: '月薪', daily: '日薪', hourly: '时薪' }

function formatSalary(j: any) {
  if (!j.salaryMin && !j.salaryMax) return '面议'
  const unit = salaryTypeMap[j.salaryType] || '元'
  if (j.salaryMax) return `${j.salaryMin}-${j.salaryMax}${unit}`
  return `${j.salaryMin}${unit}以下`
}

async function loadJob() {
  try {
    const { data } = await api.get(`/jobs/${route.params.id}`)
    if (data.code === 200) job.value = data.data
  } finally { loading.value = false }
}

async function applyJob() {
  if (!auth.isLoggedIn) { router.push('/login'); return }
  applying.value = true
  try {
    const { data } = await api.post('/deliveries', { jobId: job.value.id })
    if (data.code === 200) {
      ElMessage.success('投递成功！')
      await loadJob() // 刷新投递计数
    } else {
      ElMessage.error(data.message)
    }
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '投递失败')
  } finally { applying.value = false }
}

onMounted(() => loadJob())
</script>

<style scoped>
.page { max-width: 800px; margin: 0 auto; padding: 24px; }
.job-header { background: #fff; border-radius: 12px; padding: 24px; margin-bottom: 16px; display: flex; justify-content: space-between; align-items: flex-start; }
.job-header h1 { font-size: 24px; margin-bottom: 12px; }
.meta { display: flex; gap: 20px; font-size: 14px; color: #666; }
.actions { display: flex; flex-direction: column; align-items: flex-end; gap: 10px; }
.content-card { border-radius: 12px; }
.content-card h3 { color: #333; margin-bottom: 12px; }
.content-card p { color: #666; line-height: 1.8; white-space: pre-wrap; }
.tags { display: flex; flex-wrap: wrap; margin-top: 12px; }
</style>
