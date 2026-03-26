<template>
  <section class="admin-audit-page" aria-label="管理员审核列表页面">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <div class="section-header">
          <h2>认证审核列表</h2>
          <el-button :loading="loading" @click="loadAll">刷新</el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="待审求职者" name="jobseekers">
          <el-table v-loading="loading" :data="jobSeekerRows" border aria-label="待审核求职者列表">
            <el-table-column prop="username" label="用户名" min-width="120" />
            <el-table-column prop="realName" label="姓名" min-width="120" />
            <el-table-column prop="phone" label="手机号" min-width="140" />
            <el-table-column prop="disabilityType" label="残疾类型" min-width="140" />
            <el-table-column prop="disabilityLevel" label="残疾等级" min-width="120" />
            <el-table-column label="证件" min-width="140">
              <template #default="{ row }">
                <a v-if="row.certificatePath" :href="row.certificatePath" target="_blank" rel="noopener noreferrer">查看证件</a>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="210" fixed="right">
              <template #default="{ row }">
                <el-button type="success" link @click="handleVerify('JOB_SEEKER', row, 'PASS')">通过</el-button>
                <el-button type="danger" link @click="openRejectDialog('JOB_SEEKER', row)">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="待审企业" name="enterprises">
          <el-table v-loading="loading" :data="enterpriseRows" border aria-label="待审核企业列表">
            <el-table-column prop="username" label="账号" min-width="120" />
            <el-table-column prop="enterpriseName" label="企业名称" min-width="180" />
            <el-table-column prop="contactPerson" label="联系人" min-width="120" />
            <el-table-column prop="contactPhone" label="联系电话" min-width="140" />
            <el-table-column label="执照" min-width="140">
              <template #default="{ row }">
                <a v-if="row.licensePath" :href="row.licensePath" target="_blank" rel="noopener noreferrer">查看执照</a>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="210" fixed="right">
              <template #default="{ row }">
                <el-button type="success" link @click="handleVerify('ENTERPRISE', row, 'PASS')">通过</el-button>
                <el-button type="danger" link @click="openRejectDialog('ENTERPRISE', row)">驳回</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="rejectDialogVisible" title="驳回认证" width="520px">
      <el-form label-position="top" aria-label="认证驳回表单">
        <el-form-item label="驳回备注（可选）" for="rejectComment">
          <el-input
            id="rejectComment"
            v-model="rejectComment"
            type="textarea"
            :rows="4"
            maxlength="500"
            show-word-limit
            placeholder="请输入驳回原因，便于用户后续整改"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="confirmReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getPendingEnterprises,
  getPendingJobSeekers,
  verifyEnterprise,
  verifyJobSeeker
} from '../../api/admin'

const activeTab = ref('jobseekers')
const loading = ref(false)
const submitting = ref(false)
const jobSeekerRows = ref([])
const enterpriseRows = ref([])
const rejectDialogVisible = ref(false)
const rejectComment = ref('')
const rejectTarget = ref(null)

async function loadAll() {
  loading.value = true
  try {
    const [jobSeekerResponse, enterpriseResponse] = await Promise.all([
      getPendingJobSeekers(),
      getPendingEnterprises()
    ])
    jobSeekerRows.value = jobSeekerResponse.data || []
    enterpriseRows.value = enterpriseResponse.data || []
  } finally {
    loading.value = false
  }
}

async function handleVerify(type, row, status, comment = '') {
  submitting.value = true
  try {
    if (type === 'JOB_SEEKER') {
      await verifyJobSeeker(row.id, { status, comment })
      ElMessage.success('求职者认证审核已更新')
    } else {
      await verifyEnterprise(row.id, { status, comment })
      ElMessage.success('企业认证审核已更新')
    }
    await loadAll()
  } finally {
    submitting.value = false
  }
}

function openRejectDialog(type, row) {
  rejectTarget.value = { type, row }
  rejectComment.value = ''
  rejectDialogVisible.value = true
}

async function confirmReject() {
  if (!rejectTarget.value) {
    return
  }
  const { type, row } = rejectTarget.value
  await handleVerify(type, row, 'REJECT', rejectComment.value)
  rejectDialogVisible.value = false
}

onMounted(() => {
  loadAll()
})
</script>
