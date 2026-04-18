<template>
  <div class="layout">
    <Navbar />
    <div class="page">
      <h2>HR 工作台</h2>

      <!-- 发布职位 -->
      <el-card class="publish-card">
        <template #header><span>发布新职位</span></template>
        <el-form :model="jobForm" label-position="top">
          <el-row :gutter="12">
            <el-col :span="12"><el-form-item label="职位名称" required><el-input v-model="jobForm.title" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="工作地点"><el-input v-model="jobForm.location" /></el-form-item></el-col>
          </el-row>
          <el-row :gutter="12">
            <el-col :span="8"><el-form-item label="最低薪资"><el-input-number v-model="jobForm.salaryMin" :min="0" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="最高薪资"><el-input-number v-model="jobForm.salaryMax" :min="0" /></el-form-item></el-col>
            <el-col :span="8"><el-form-item label="薪资类型">
              <el-select v-model="jobForm.salaryType" style="width:100%">
                <el-option value="monthly" label="月薪" />
                <el-option value="daily" label="日薪" />
                <el-option value="hourly" label="时薪" />
              </el-select>
            </el-form-item></el-col>
          </el-row>
          <el-row :gutter="12">
            <el-col :span="8"><el-form-item label="工作方式">
              <el-select v-model="jobForm.workType" style="width:100%">
                <el-option value="onsite" label="on-site" />
                <el-option value="remote" label="remote" />
                <el-option value="hybrid" label="hybrid" />
              </el-select>
            </el-form-item></el-col>
            <el-col :span="16"><el-form-item label="标签（逗号分隔）"><el-input v-model="jobForm.tags" placeholder="如：Java,实习,远程" /></el-form-item></el-col>
          </el-row>
          <el-form-item label="职位描述"><el-input v-model="jobForm.description" type="textarea" :rows="3" /></el-form-item>
          <el-form-item label="岗位要求"><el-input v-model="jobForm.requirements" type="textarea" :rows="3" /></el-form-item>
          <el-button type="primary" size="large" :loading="publishing" @click="publishJob">发布职位</el-button>
        </el-form>
      </el-card>

      <!-- 收到的投递 -->
      <el-card style="margin-top: 20px">
        <template #header><span>收到的投递 ({{ total }}份)</span></template>
        <div v-loading="loadingDeliveries">
          <el-table :data="deliveries" stripe>
            <el-table-column prop="jobTitle" label="职位" />
            <el-table-column prop="delivery.userId" label="求职者ID" width="100" />
            <el-table-column prop="delivery.applyTime" label="投递时间" width="160" :formatter="(row: any) => dayjs(row.delivery.applyTime).format('YYYY-MM-DD HH:mm')" />
            <el-table-column prop="delivery.status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="statusType[row.delivery.status]">{{ statusMap[row.delivery.status] }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="delivery.feedback" label="HR反馈" />
            <el-table-column label="操作" width="200">
              <template #default="{ row }">
                <el-button size="small" @click="openProcess(row)" v-if="row.delivery.status !== 4 && row.delivery.status !== 5">
                  处理
                </el-button>
                <el-tag v-else type="info" size="small">已处理</el-tag>
              </template>
            </el-table-column>
          </el-table>
          <div class="pagination" v-if="total > 0">
            <el-pagination v-model:current-page="page" :page-size="10" :total="total" layout="prev, pager, next" @current-change="loadDeliveries" />
          </div>
        </div>
      </el-card>
    </div>

    <!-- 处理投递对话框 -->
    <el-dialog v-model="dialogVisible" title="处理投递" width="500px">
      <el-form :model="processForm" label-position="top">
        <el-form-item label="新状态">
          <el-select v-model="processForm.status" style="width:100%">
            <el-option :value="2" label="待查看" />
            <el-option :value="3" label="笔试中" />
            <el-option :value="4" label="录用" />
            <el-option :value="5" label="拒绝" />
          </el-select>
        </el-form-item>
        <el-form-item label="反馈信息">
          <el-input v-model="processForm.feedback" type="textarea" :rows="3" placeholder="选填，可填写拒绝理由或面试安排等" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="processing" @click="confirmProcess">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore, api } from '@/stores/auth'
import Navbar from '@/components/Navbar.vue'
import dayjs from 'dayjs'

const auth = useAuthStore()
const publishing = ref(false)
const loadingDeliveries = ref(false)
const processing = ref(false)
const dialogVisible = ref(false)
const deliveries = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const currentDeliveryId = ref<number | null>(null)

const jobForm = reactive({ title: '', salaryMin: 0, salaryMax: 0, salaryType: 'monthly',
  location: '', workType: 'onsite', requirements: '', description: '', tags: '', companyId: 1 })
const processForm = reactive({ status: 2, feedback: '' })
const statusMap: any = { 1:'已投递', 2:'待查看', 3:'笔试中', 4:'已录用', 5:'已拒绝' }
const statusType: any = { 1:'info', 2:'warning', 3:'primary', 4:'success', 5:'danger' }

async function publishJob() {
  if (!jobForm.title) { ElMessage.warning('请填写职位名称'); return }
  publishing.value = true
  try {
    const { data } = await api.post('/jobs', { ...jobForm, companyId: 1 })
    if (data.code === 200) { ElMessage.success('发布成功'); Object.assign(jobForm, { title:'', salaryMin:0, salaryMax:0, requirements:'', description:'', tags:'', location:'', workType:'onsite' }) }
    else ElMessage.error(data.message)
  } catch (e: any) { ElMessage.error(e.message) }
  finally { publishing.value = false }
}

async function loadDeliveries() {
  loadingDeliveries.value = true
  try {
    const { data } = await api.get('/jobs/hr/deliveries')
    if (data.code === 200) { deliveries.value = data.data; total.value = data.data.length }
  } finally { loadingDeliveries.value = false }
}

function openProcess(row: any) {
  currentDeliveryId.value = row.delivery.id
  processForm.status = 2; processForm.feedback = ''
  dialogVisible.value = true
}

async function confirmProcess() {
  processing.value = true
  try {
    const { data } = await api.put(`/deliveries/${currentDeliveryId.value}`, processForm)
    if (data.code === 200) { ElMessage.success('处理成功'); dialogVisible.value = false; await loadDeliveries() }
    else ElMessage.error(data.message)
  } catch (e: any) { ElMessage.error(e.message) }
  finally { processing.value = false }
}

onMounted(() => loadDeliveries())
</script>

<style scoped>
.page { max-width: 960px; margin: 0 auto; padding: 24px; }
.page h2 { margin-bottom: 20px; }
.pagination { display: flex; justify-content: center; margin-top: 16px; }
</style>
