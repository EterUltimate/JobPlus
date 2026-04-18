<template>
  <div class="layout">
    <Navbar />
    <div class="page">
      <h2>我的简历</h2>
      <el-card v-loading="loading">
        <el-form :model="form" label-position="top">
          <el-row :gutter="16">
            <el-col :span="12"><el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="性别"><el-select v-model="form.gender" style="width:100%"><el-option value="male" label="男" /><el-option value="female" label="女" /><el-option value="other" label="其他" /></el-select></el-form-item></el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12"><el-form-item label="学历"><el-input v-model="form.education" placeholder="如：本科在读" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="专业"><el-input v-model="form.major" /></el-form-item></el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12"><el-form-item label="手机"><el-input v-model="form.phone" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item></el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12"><el-form-item label="工作年限"><el-input-number v-model="form.workExp" :min="0" :max="50" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="简历可见性"><el-radio-group v-model="form.visibility"><el-radio :value="1">公开</el-radio><el-radio :value="0">私密</el-radio></el-radio-group></el-form-item></el-col>
          </el-row>
          <el-form-item label="技能标签（逗号分隔）"><el-input v-model="form.skills" placeholder="如：Java,Spring Boot,MySQL" /></el-form-item>
          <el-form-item label="简历正文（JSON）">
            <el-input v-model="form.contentJson" type="textarea" :rows="4" placeholder='{"summary":"...","projects":[]}' />
          </el-form-item>
          <el-button type="primary" size="large" :loading="saving" @click="save">保存简历</el-button>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore, api } from '@/stores/auth'
import Navbar from '@/components/Navbar.vue'

const auth = useAuthStore()
const loading = ref(false)
const saving = ref(false)
const form = reactive({
  realName: '', gender: '', age: undefined as number | undefined,
  education: '', major: '', phone: '', email: '',
  workExp: 0, skills: '', contentJson: '', fileUrl: '', visibility: 1,
})

async function load() {
  loading.value = true
  try {
    const { data } = await api.get('/resumes/me')
    if (data.code === 200 && data.data.id) {
      Object.assign(form, data.data)
      if (typeof form.contentJson === 'string') { /* already string */ }
    }
  } finally { loading.value = false }
}

async function save() {
  saving.value = true
  try {
    const { data } = await api.put('/resumes/me', form)
    if (data.code === 200) { ElMessage.success('简历已保存'); await load() }
    else ElMessage.error(data.message)
  } catch (e: any) { ElMessage.error(e.message) }
  finally { saving.value = false }
}

onMounted(() => load())
</script>

<style scoped>
.page { max-width: 800px; margin: 0 auto; padding: 24px; }
.page h2 { margin-bottom: 20px; }
</style>
