<template>
  <div class="layout">
    <Navbar />
    <div class="page">
      <h2>我的投递</h2>
      <el-tabs v-model="statusFilter">
        <el-tab-pane label="全部" name="all" />
        <el-tab-pane label="已投递" name="1" />
        <el-tab-pane label="待查看" name="2" />
        <el-tab-pane label="笔试中" name="3" />
        <el-tab-pane label="已录用" name="4" />
        <el-tab-pane label="未通过" name="5" />
      </el-tabs>

      <div v-loading="loading">
        <div v-for="item in deliveries" :key="item.delivery.id" class="delivery-row">
          <div class="info">
            <div class="title">{{ item.jobTitle }}</div>
            <div class="sub">{{ item.location }} · {{ item.salaryRange }}</div>
            <div class="time">投递时间: {{ dayjs(item.delivery.applyTime).format('YYYY-MM-DD HH:mm') }}</div>
            <div v-if="item.delivery.feedback" class="feedback">HR 反馈: {{ item.delivery.feedback }}</div>
          </div>
          <div class="status-area">
            <el-tag :type="statusType(item.delivery.status)" size="large">
              {{ statusMap[item.delivery.status] }}
            </el-tag>
          </div>
        </div>
        <el-empty v-if="!loading && deliveries.length === 0" description="暂无投递记录" />
      </div>

      <div class="pagination" v-if="total > 0">
        <el-pagination v-model:current-page="page" :page-size="10" :total="total"
          layout="prev, pager, next" @current-change="load" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useAuthStore, api } from '@/stores/auth'
import Navbar from '@/components/Navbar.vue'
import dayjs from 'dayjs'

const auth = useAuthStore()
const deliveries = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const statusFilter = ref('all')

const statusMap: any = { 1:'已投递', 2:'待查看', 3:'笔试中', 4:'已录用', 5:'已拒绝', 6:'已撤回' }
const statusType: any = { 1:'info', 2:'warning', 3:'primary', 4:'success', 5:'danger', 6:'info' }

async function load() {
  loading.value = true
  try {
    const { data } = await api.get(`/deliveries/me?page=${page.value}&size=10`)
    if (data.code === 200) {
      let records = data.data.records || []
      if (statusFilter.value !== 'all') records = records.filter((r: any) => r.delivery.status === Number(statusFilter.value))
      deliveries.value = records
      total.value = data.data.total || 0
    }
  } finally { loading.value = false }
}

watch(statusFilter, () => { page.value = 1; load() })
onMounted(() => load())
</script>

<style scoped>
.page { max-width: 800px; margin: 0 auto; padding: 24px; }
.page h2 { margin-bottom: 20px; }
.delivery-row { background: #fff; border-radius: 10px; padding: 20px; margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center; border: 1px solid #eee; }
.title { font-weight: 600; font-size: 15px; margin-bottom: 6px; }
.sub { color: #888; font-size: 13px; margin-bottom: 4px; }
.time { color: #aaa; font-size: 12px; }
.feedback { color: #f56c6c; font-size: 13px; margin-top: 4px; }
.pagination { display: flex; justify-content: center; margin-top: 20px; }
</style>
