<template>
  <article class="job-detail-page" aria-label="岗位详情页面">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <div class="section-header">
          <h2>岗位详情</h2>
          <el-button aria-label="返回岗位列表页" @click="goBack">返回岗位列表</el-button>
        </div>
      </template>

      <el-descriptions v-if="job" :column="1" border aria-label="岗位详细信息">
        <el-descriptions-item label="岗位名称">{{ job.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="企业名称">{{ job.enterpriseName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="岗位分类">{{ job.category || '-' }}</el-descriptions-item>
        <el-descriptions-item label="工作城市">{{ job.city || '-' }}</el-descriptions-item>
        <el-descriptions-item label="薪资范围">{{ job.salaryMin }} - {{ job.salaryMax }} 元</el-descriptions-item>
        <el-descriptions-item label="工作方式">{{ workModeText(job.workMode) }}</el-descriptions-item>
        <el-descriptions-item label="学历要求">{{ job.educationRequirement || '-' }}</el-descriptions-item>
        <el-descriptions-item label="经验要求">{{ job.experienceRequirement || '-' }}</el-descriptions-item>
        <el-descriptions-item label="适配残疾类型">{{ job.disabilitySupportType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="技能要求">{{ job.skillRequirements || '-' }}</el-descriptions-item>
        <el-descriptions-item label="福利待遇">{{ job.welfare || '-' }}</el-descriptions-item>
        <el-descriptions-item label="岗位描述">{{ job.jobDescription || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />
      <el-form label-position="top" aria-label="岗位投递表单">
        <el-form-item label="选择投递简历" for="resumeId">
          <el-select id="resumeId" v-model="resumeId" placeholder="请选择简历后投递">
            <el-option
              v-for="item in resumes"
              :key="item.id"
              :label="`${item.title}${item.isDefault ? '（默认）' : ''}`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" aria-label="提交岗位投递" @click="handleApply">投递岗位</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </article>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getJobDetail } from '../../api/jobs'
import { getResumeList } from '../../api/resume'
import { createApplication } from '../../api/applications'

const route = useRoute()
const router = useRouter()
const job = ref(null)
const resumes = ref([])
const resumeId = ref()
const submitting = ref(false)

function workModeText(mode) {
  return {
    OFFLINE: '线下办公',
    REMOTE: '远程办公',
    HYBRID: '混合办公'
  }[mode] || mode
}

async function loadDetail() {
  const id = Number(route.params.id)
  if (!Number.isFinite(id) || id <= 0) {
    return
  }
  const response = await getJobDetail(id)
  job.value = response.data || null
}

async function loadResumes() {
  const response = await getResumeList()
  resumes.value = response.data || []
  const defaultResume = resumes.value.find((item) => item.isDefault)
  resumeId.value = defaultResume?.id || resumes.value[0]?.id
}

async function handleApply() {
  if (!resumeId.value) {
    ElMessage.warning('请先选择简历')
    return
  }
  const id = Number(route.params.id)
  submitting.value = true
  try {
    await createApplication({
      jobId: id,
      resumeId: resumeId.value
    })
    ElMessage.success('投递成功')
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.push('/jobs')
}

onMounted(() => {
  loadDetail()
  loadResumes()
})
</script>
