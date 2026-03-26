<template>
  <section class="application-page">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <div class="section-header">
          <h2>岗位投递记录</h2>
          <el-button @click="goBack">返回岗位管理</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="applications" border>
        <el-table-column prop="jobSeekerName" label="求职者" min-width="120" />
        <el-table-column prop="resumeName" label="投递简历" min-width="140" />
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" />
        <el-table-column prop="applyTime" label="投递时间" min-width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openStatusDialog(row)">更新状态</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="更新投递状态" width="520px">
      <el-form :model="statusForm" label-position="top">
        <el-form-item label="目标状态" for="status">
          <el-select id="status" v-model="statusForm.status" placeholder="请选择状态">
            <el-option label="已投递" value="SUBMITTED" />
            <el-option label="已查看" value="VIEWED" />
            <el-option label="沟通中" value="COMMUNICATING" />
            <el-option label="已录用" value="ACCEPTED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" for="remark">
          <el-input id="remark" v-model="statusForm.remark" type="textarea" :rows="3" placeholder="可填写处理备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleUpdateStatus">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getJobApplications, updateApplicationStatus } from '../../api/applications'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const applications = ref([])
const dialogVisible = ref(false)
const selectedApplicationId = ref()

const jobId = computed(() => Number(route.params.jobId))

const statusForm = reactive({
  status: 'VIEWED',
  remark: ''
})

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

function goBack() {
  router.push('/enterprise/jobs')
}

async function loadApplications() {
  loading.value = true
  try {
    const response = await getJobApplications(jobId.value)
    applications.value = response.data || []
  } finally {
    loading.value = false
  }
}

function openStatusDialog(row) {
  selectedApplicationId.value = row.id
  statusForm.status = row.status || 'VIEWED'
  statusForm.remark = row.remark || ''
  dialogVisible.value = true
}

async function handleUpdateStatus() {
  if (!selectedApplicationId.value) {
    return
  }
  saving.value = true
  try {
    await updateApplicationStatus(selectedApplicationId.value, {
      status: statusForm.status,
      remark: statusForm.remark
    })
    ElMessage.success('投递状态更新成功')
    dialogVisible.value = false
    await loadApplications()
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadApplications()
})
</script>
