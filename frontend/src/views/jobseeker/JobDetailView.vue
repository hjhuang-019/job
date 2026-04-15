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
        <el-descriptions-item label="适合招收的残疾类型">
          <DisabilityTypeTags :value="job.disabilitySupportType || ''" />
        </el-descriptions-item>
        <el-descriptions-item label="技能要求">{{ job.skillRequirements || '-' }}</el-descriptions-item>
        <el-descriptions-item label="福利待遇">{{ job.welfare || '-' }}</el-descriptions-item>
        <el-descriptions-item label="岗位描述">{{ job.jobDescription || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider />
      <el-alert
        v-if="authStore.isBlacklisted"
        type="warning"
        show-icon
        :closable="false"
        class="blacklist-page-alert"
        title="您的账号已被列入黑名单，无法投递简历。请联系管理员解除。"
        role="status"
      />
      <el-form label-position="top" aria-label="岗位投递表单" :disabled="authStore.isBlacklisted">
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
          <el-button
            type="primary"
            :loading="submitting"
            :disabled="authStore.isBlacklisted"
            :aria-label="hasAppliedBefore ? '再次投递该岗位' : '提交岗位投递'"
            @click="handleApply"
          >
            {{ hasAppliedBefore ? '再次投递' : '投递岗位' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </article>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getJobDetail } from '../../api/jobs'
import { getResumeList } from '../../api/resume'
import { createApplication, getMyApplications } from '../../api/applications'
import { registerJobSeekerVoicePage } from '../../voice/jobSeekerVoiceRegistry'
import DisabilityTypeTags from '../../components/DisabilityTypeTags.vue'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const job = ref(null)
const resumes = ref([])
const resumeId = ref()
const submitting = ref(false)
const hasAppliedBefore = ref(false)

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

async function refreshApplyState() {
  const id = Number(route.params.id)
  if (!Number.isFinite(id) || id <= 0) {
    hasAppliedBefore.value = false
    return
  }
  try {
    const response = await getMyApplications()
    const list = response.data || []
    hasAppliedBefore.value = list.some((item) => Number(item.jobId) === id)
  } catch {
    hasAppliedBefore.value = false
  }
}

async function onVoiceInterpret(vo) {
  const intent = String(vo.intent || '')
  const apply = vo.apply || {}
  const wantApply =
    intent === 'APPLY_JOB' || apply.useDefaultResume || apply.resumeId != null || apply.resumeIndex != null
  if (!wantApply) {
    return
  }
  const jobId = apply.jobId != null ? Number(apply.jobId) : Number(route.params.id)
  if (!Number.isFinite(jobId) || jobId <= 0) {
    return
  }
  let rid = apply.resumeId != null ? Number(apply.resumeId) : null
  if (apply.useDefaultResume) {
    const def = resumes.value.find((item) => item.isDefault)
    rid = def?.id || resumes.value[0]?.id
  }
  if (apply.resumeIndex != null) {
    const idx = Number(apply.resumeIndex) - 1
    if (idx >= 0 && idx < resumes.value.length) {
      rid = resumes.value[idx].id
    }
  }
  if (!rid) {
    ElMessage.warning('请先创建简历后再投递')
    return
  }
  if (authStore.isBlacklisted) {
    ElMessage.warning('您的账号已被列入黑名单，无法投递简历')
    return
  }
  try {
    await createApplication({
      jobId,
      resumeId: rid
    })
    ElMessage.success('已通过语音助手完成投递')
    hasAppliedBefore.value = true
  } catch {
    /* 全局拦截已提示 */
  }
}

let unregisterVoicePage = () => {}

async function handleApply() {
  if (authStore.isBlacklisted) {
    ElMessage.warning('您的账号已被列入黑名单，无法投递简历')
    return
  }
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
    hasAppliedBefore.value = true
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.push('/jobs')
}

onMounted(async () => {
  if (authStore.token) {
    await authStore.fetchCurrentUser().catch(() => {})
  }
  unregisterVoicePage = registerJobSeekerVoicePage(route.name, {
    getPageContext: () => ({
      jobId: Number(route.params.id),
      jobTitle: job.value?.title || '',
      resumeOptions: resumes.value.map((item, index) => ({
        index: index + 1,
        id: item.id,
        title: item.title,
        isDefault: item.isDefault
      }))
    }),
    onInterpret: onVoiceInterpret
  })
  await Promise.all([loadDetail(), loadResumes()])
  await refreshApplyState()
})

onUnmounted(() => {
  unregisterVoicePage()
})
</script>

<style scoped>
.blacklist-page-alert {
  margin-bottom: 16px;
}
</style>
