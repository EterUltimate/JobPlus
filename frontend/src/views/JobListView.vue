<template>
  <div class="layout">
    <Navbar />
    <div class="page">
      <!-- 筛选栏 -->
      <div class="filter-bar">
        <el-input v-model="params.keyword" placeholder="搜索职位名称..." clearable @clear="search" style="width:240px">
          <template #append><el-button :icon="Search" @click="search" /></template>
        </el-input>
        <el-select v-model="params.workType" placeholder="工作方式" clearable @change="search" style="width:140px">
          <el-option value="onsite" label="on-site" />
          <el-option value="remote" label="remote" />
          <el-option value="hybrid" label="hybrid" />
        </el-select>
        <el-input v-model="params.location" placeholder="工作地点" clearable @change="search" style="width:160px" />
        <el-button @click="reset">重置</el-button>
      </div>

      <!-- 职位列表 -->
      <div class="job-list" v-loading="loading">
        <div v-for="job in jobs" :key="job.id" class="job-row" @click="goDetail(job.id)">
          <div class="job-main">
            <div class="job-title">{{ job.title }}</div>
            <div class="job-info">
              <span class="tag remote" v-if="job.workType === 'remote'">远程</span>
              <span class="tag onsite" v-else-if="job.workType === 'onsite'">on-site</span>
              <span class="tag hybrid" v-else>混合</span>
              <span>📍 {{ job.location || '未标注' }}</span>
              <span>👁 {{ job.viewCount || 0 }}次浏览</span>
            </div>
          </div>
          <div class="job-right">
            <div class="salary">{{ formatSalary(job) }}</div>
            <div class="time">{{ dayjs(job.createTime).format('MM-DD HH:mm') }}</div>
            <el-button type="primary" size="small" @click.stop="goDetail(job.id)">查看详情</el-button>
          </div>
        </div>
        <el-empty v-if="!loading && jobs.length === 0" description="没有找到符合条件的职位" />
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
          v-model:current-page="params.page"
          :page-size="params.size"
          :total="total"
          layout="prev, pager, next"
          @current-change="loadJobs"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { useAuthStore, api } from '@/stores/auth'
import Navbar from '@/components/Navbar.vue'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const loading = ref(false)
const jobs = ref<any[]>([])
const total = ref(0)
const params = reactive({
  keyword: (route.query.keyword as string) || '',
  location: '',
  workType: '',
  salaryMin: undefined as number | undefined,
  page: 1,
  size: 10,
})

function formatSalary(job: any) {
  if (!job.salaryMin && !job.salaryMax) return '面议'
  const unit = job.salaryType === 'daily' ? '元/天' : job.salaryType === 'hourly' ? '元/时' : '元/月'
  if (job.salaryMax) return `${job.salaryMin}-${job.salaryMax}${unit}`
  return `${job.salaryMin}${unit}以下`
}

async function loadJobs() {
  loading.value = true
  try {
    const q = new URLSearchParams()
    if (params.keyword) q.set('keyword', params.keyword)
    if (params.location) q.set('location', params.location)
    if (params.workType) q.set('workType', params.workType)
    q.set('page', String(params.page))
    q.set('size', String(params.size))
    const { data } = await api.get(`/jobs?${q}`)
    if (data.code === 200) {
      jobs.value = data.data.records || []
      total.value = data.data.total || 0
    }
  } finally { loading.value = false }
}

function search() { params.page = 1; loadJobs() }
function reset() { Object.assign(params, { keyword: '', location: '', workType: '', page: 1 }); loadJobs() }
function goDetail(id: number) { router.push(`/jobs/${id}`) }

onMounted(() => loadJobs())
</script>

<style scoped>
.page { max-width: 960px; margin: 0 auto; padding: 24px; }
.filter-bar { display: flex; gap: 12px; align-items: center; margin-bottom: 20px; background: #fff; padding: 16px; border-radius: 10px; }
.job-list { display: flex; flex-direction: column; gap: 12px; }
.job-row { background: #fff; border-radius: 10px; padding: 20px; display: flex; justify-content: space-between; align-items: center; cursor: pointer; border: 1px solid #eee; transition: all 0.2s; }
.job-row:hover { box-shadow: 0 4px 16px rgba(102,126,234,0.12); border-color: #667eea; }
.job-title { font-size: 16px; font-weight: 600; margin-bottom: 8px; }
.job-info { display: flex; gap: 16px; font-size: 13px; color: #888; }
.tag { padding: 2px 8px; border-radius: 4px; font-size: 12px; }
.tag.remote { background: #e6f7ff; color: #1890ff; }
.tag.onsite { background: #f6ffed; color: #52c41a; }
.tag.hybrid { background: #fff7e6; color: #fa8c16; }
.job-right { text-align: right; display: flex; flex-direction: column; align-items: flex-end; gap: 6px; }
.salary { color: #f56c6c; font-weight: 700; font-size: 16px; }
.time { font-size: 12px; color: #aaa; }
.pagination { display: flex; justify-content: center; margin-top: 24px; }
</style>
