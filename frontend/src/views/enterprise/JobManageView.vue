<template>
  <section class="enterprise-job-page">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <div class="section-header">
          <h2>岗位管理</h2>
          <el-button type="primary" :disabled="authStore.isBlacklisted" @click="goPublish">发布岗位</el-button>
        </div>
      </template>

      <el-alert
        v-if="authStore.isBlacklisted"
        type="warning"
        show-icon
        :closable="false"
        class="blacklist-page-alert"
        title="您的企业账号已被列入黑名单，无法发布新岗位或上架岗位。请联系管理员解除。"
        role="status"
      />

      <el-table
        v-loading="loading"
        :data="jobList"
        border
        empty-text="暂无岗位，点击“发布岗位”开始创建"
        style="width: 100%"
      >
        <el-table-column prop="title" label="岗位名称" min-width="180" />
        <el-table-column prop="city" label="城市" width="110" />
        <el-table-column label="薪资" min-width="130">
          <template #default="{ row }">{{ row.salaryMin }} - {{ row.salaryMax }}</template>
        </el-table-column>
        <el-table-column label="工作方式" width="110">
          <template #default="{ row }">{{ workModeText(row.workMode) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goEdit(row.id)">编辑</el-button>
            <el-button link type="info" @click="goApplications(row.id)">投递记录</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
            <el-button
              v-if="row.status !== 'OPEN'"
              link
              type="success"
              :disabled="authStore.isBlacklisted"
              @click="handleChangeStatus(row, 'OPEN')"
            >
              上架
            </el-button>
            <el-button v-else link type="warning" @click="handleChangeStatus(row, 'CLOSED')">下架</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteJob, getMyJobs, updateJobStatus } from '../../api/jobs'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const jobList = ref([])

function goPublish() {
  if (authStore.isBlacklisted) {
    ElMessage.warning('您的企业账号已被列入黑名单，无法发布新岗位')
    return
  }
  router.push('/enterprise/jobs/publish')
}

function goEdit(id) {
  router.push(`/enterprise/jobs/${id}/edit`)
}

function goApplications(id) {
  router.push(`/enterprise/jobs/${id}/applications`)
}

function statusText(status) {
  return {
    DRAFT: '草稿',
    OPEN: '已上架',
    CLOSED: '已下架'
  }[status] || status
}

function statusTagType(status) {
  return {
    DRAFT: 'info',
    OPEN: 'success',
    CLOSED: 'warning'
  }[status] || 'info'
}

function workModeText(workMode) {
  return {
    OFFLINE: '线下',
    REMOTE: '远程',
    HYBRID: '混合'
  }[workMode] || workMode
}

async function loadMyJobs() {
  loading.value = true
  try {
    const response = await getMyJobs()
    jobList.value = response.data || []
  } catch (error) {
    ElMessage.error('加载岗位列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除岗位《${row.title}》吗？`, '提示', {
    type: 'warning'
  })
  await deleteJob(row.id)
  ElMessage.success('岗位删除成功')
  await loadMyJobs()
}

async function handleChangeStatus(row, targetStatus) {
  if (targetStatus === 'OPEN' && authStore.isBlacklisted) {
    ElMessage.warning('您的企业账号已被列入黑名单，无法上架岗位')
    return
  }
  const actionText = targetStatus === 'OPEN' ? '上架' : '下架'
  await updateJobStatus(row.id, targetStatus)
  ElMessage.success(`岗位${actionText}成功`)
  await loadMyJobs()
}

onMounted(() => {
  if (authStore.token) {
    authStore.fetchCurrentUser().catch(() => {})
  }
  loadMyJobs()
})
</script>

<style scoped>
.blacklist-page-alert {
  margin-bottom: 16px;
}
</style>
