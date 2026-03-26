<template>
  <section class="application-page">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <div class="section-header">
          <h2>我的投递</h2>
          <el-button @click="goJobs">继续投递</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="applications"
        border
        empty-text="暂无投递记录，去岗位列表投递试试吧"
      >
        <el-table-column prop="jobTitle" label="岗位名称" min-width="180" />
        <el-table-column prop="enterpriseName" label="企业名称" min-width="170" />
        <el-table-column prop="resumeName" label="投递简历" min-width="140" />
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" />
        <el-table-column prop="applyTime" label="投递时间" min-width="180" />
      </el-table>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyApplications } from '../../api/applications'

const router = useRouter()
const loading = ref(false)
const applications = ref([])

function statusText(status) {
  return {
    SUBMITTED: '已投递',
    VIEWED: '已查看',
    COMMUNICATING: '沟通中',
    ACCEPTED: '已录用',
    REJECTED: '已拒绝'
  }[status] || status
}

function statusTagType(status) {
  return {
    SUBMITTED: 'info',
    VIEWED: 'warning',
    COMMUNICATING: 'primary',
    ACCEPTED: 'success',
    REJECTED: 'danger'
  }[status] || 'info'
}

function goJobs() {
  router.push('/jobs')
}

async function loadMyApplications() {
  loading.value = true
  try {
    const response = await getMyApplications()
    applications.value = response.data || []
  } catch (error) {
    ElMessage.error('加载投递记录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadMyApplications()
})
</script>
