<template>
  <section class="application-page">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <div class="section-header">
          <h2>岗位投递记录</h2>
          <el-button @click="goBack">返回岗位管理</el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="applications" border row-key="id">
        <el-table-column type="expand" width="48">
          <template #default="{ row }">
            <div class="application-expand" role="region" :aria-label="`求职者 ${row.jobSeekerName} 投递详情`">
              <p><span class="k">企业回复</span>{{ row.remark || '—' }}</p>
              <p><span class="k">面试时间</span>{{ formatDateTime(row.interviewTime) || '—' }}</p>
              <p><span class="k">面试地址</span>{{ row.interviewAddress || '—' }}</p>
              <p><span class="k">HR 联系方式</span>{{ row.hrContact || '—' }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="jobSeekerName" label="求职者" min-width="120" />
        <el-table-column prop="resumeName" label="投递简历" min-width="140" />
        <el-table-column label="状态" width="130">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ enterpriseStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="applyTime" label="投递时间" min-width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.applyTime) || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openStatusDialog(row)">更新状态</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="更新投递状态与回复" width="560px" destroy-on-close>
      <el-form :model="statusForm" label-position="top">
        <el-form-item label="目标状态" for="status">
          <el-select id="status" v-model="statusForm.status" placeholder="请选择状态">
            <el-option label="已投递" value="SUBMITTED" />
            <el-option label="已查阅" value="VIEWED" />
            <el-option label="沟通/待面试" value="COMMUNICATING" />
            <el-option label="已录用" value="ACCEPTED" />
            <el-option label="已拒绝" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="企业回复（通过说明、拒绝原因等，选填）" for="remark">
          <el-input
            id="remark"
            v-model="statusForm.remark"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            :placeholder="
              statusForm.status === 'REJECTED'
                ? '可填写拒绝原因等，将一并通知求职者'
                : '可与面试时间、地址、HR 联系方式一并填写，一次性发给求职者'
            "
          />
        </el-form-item>
        <template v-if="statusForm.status !== 'REJECTED'">
          <el-form-item label="面试时间（选填）" for="interviewTime">
            <el-date-picker
              id="interviewTime"
              v-model="statusForm.interviewTime"
              type="datetime"
              placeholder="选择日期时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
              clearable
            />
          </el-form-item>
          <el-form-item label="面试地址（选填）" for="interviewAddress">
            <el-input
              id="interviewAddress"
              v-model="statusForm.interviewAddress"
              maxlength="300"
              show-word-limit
              placeholder="如线下办公地址或线上面试链接"
            />
          </el-form-item>
        </template>
        <el-form-item label="HR 联系方式（选填）" for="hrContact">
          <el-input id="hrContact" v-model="statusForm.hrContact" maxlength="120" show-word-limit placeholder="电话 / 微信 / 邮箱等" />
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
import { formatDateTime } from '../../utils/datetime'
import { enterpriseStatusLabel } from '../../utils/applicationStatus'

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
  remark: '',
  interviewTime: null,
  interviewAddress: '',
  hrContact: ''
})

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
  statusForm.interviewTime = row.interviewTime ? formatDateTime(row.interviewTime) : null
  statusForm.interviewAddress = row.interviewAddress || ''
  statusForm.hrContact = row.hrContact || ''
  dialogVisible.value = true
}

async function handleUpdateStatus() {
  if (!selectedApplicationId.value) {
    return
  }
  saving.value = true
  try {
    const payload = {
      status: statusForm.status,
      remark: statusForm.remark || undefined
    }
    if (statusForm.status !== 'REJECTED') {
      if (statusForm.interviewTime) {
        payload.interviewTime = statusForm.interviewTime
      }
      if (statusForm.interviewAddress?.trim()) {
        payload.interviewAddress = statusForm.interviewAddress.trim()
      }
    }
    if (statusForm.hrContact?.trim()) {
      payload.hrContact = statusForm.hrContact.trim()
    }
    await updateApplicationStatus(selectedApplicationId.value, payload)
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

<style scoped>
.application-expand {
  padding: 8px 12px 12px 40px;
  line-height: 1.65;
  max-width: min(1200px, 100%);
}
.application-expand p {
  margin: 0 0 6px;
}
.application-expand .k {
  display: inline-block;
  min-width: 7em;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}
.application-expand .k::after {
  content: '：';
}
</style>
