<template>
  <el-card shadow="never" class="hero-card user-archive-card" aria-label="用户统计与档案">
    <template #header>
      <div class="section-header">
        <h2>用户统计与档案</h2>
        <p class="section-desc">
          分页查看平台注册的求职者与企业账号；无档案时详情列为空。审核记录自本功能上线后产生的管理员操作为准。
        </p>
      </div>
    </template>

    <el-tabs v-model="activeTab" @tab-change="onTabChange">
      <el-tab-pane label="求职者" name="jobseekers">
        <div class="toolbar">
          <el-input
            v-model="jobSeekerKeyword"
            clearable
            placeholder="用户名 / 姓名 / 手机号"
            style="max-width: 280px"
            @keyup.enter="loadJobSeekers(1)"
          />
          <el-button type="primary" :loading="jobSeekerLoading" @click="loadJobSeekers(1)">搜索</el-button>
        </div>
        <el-table v-loading="jobSeekerLoading" :data="jobSeekerRows" border aria-label="求职者用户列表">
          <el-table-column prop="userId" label="用户ID" width="88" />
          <el-table-column prop="username" label="用户名" min-width="120" />
          <el-table-column prop="realName" label="姓名" min-width="100" />
          <el-table-column prop="phone" label="手机" min-width="120" />
          <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
          <el-table-column label="账号状态" width="96">
            <template #default="{ row }">{{ formatUserStatus(row.userStatus) }}</template>
          </el-table-column>
          <el-table-column label="黑名单" width="88">
            <template #default="{ row }">{{ row.blacklisted === 1 ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column prop="userCreatedAt" label="注册时间" min-width="160" />
          <el-table-column prop="profileId" label="档案ID" width="88">
            <template #default="{ row }">{{ row.profileId ?? '-' }}</template>
          </el-table-column>
          <el-table-column prop="disabilityType" label="残疾类型" min-width="110" show-overflow-tooltip>
            <template #default="{ row }">{{ row.disabilityType || '-' }}</template>
          </el-table-column>
          <el-table-column prop="disabilityLevel" label="残疾等级" width="96">
            <template #default="{ row }">{{ row.disabilityLevel || '-' }}</template>
          </el-table-column>
          <el-table-column label="认证状态" width="100">
            <template #default="{ row }">{{ formatVerifyStatus(row.verifyStatus) }}</template>
          </el-table-column>
          <el-table-column prop="verifiedAt" label="最近审核时间" min-width="160">
            <template #default="{ row }">{{ row.verifiedAt || '-' }}</template>
          </el-table-column>
          <el-table-column label="证件" width="88">
            <template #default="{ row }">
              <el-button
                v-if="row.certificatePath"
                type="primary"
                link
                @click="openImagePreview('证件照片', row.certificatePath)"
              >
                查看
              </el-button>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="260" fixed="right">
            <template #default="{ row }">
              <el-button
                type="primary"
                link
                :disabled="!row.profileId"
                @click="openAuditDrawer('JOB_SEEKER_PROFILE', row.profileId)"
              >
                审核记录
              </el-button>
              <el-button
                v-if="row.blacklisted !== 1"
                type="warning"
                link
                @click="openBlacklistDialog('JOB_SEEKER', row)"
              >
                加入黑名单
              </el-button>
              <el-button v-else type="success" link @click="handleUnblacklist(row)">解除黑名单</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pager-wrap">
          <el-pagination
            v-model:current-page="jobSeekerPage"
            v-model:page-size="jobSeekerSize"
            :total="jobSeekerTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @current-change="(p) => loadJobSeekers(p)"
            @size-change="() => loadJobSeekers(1)"
          />
        </div>
      </el-tab-pane>

      <el-tab-pane label="企业" name="enterprises">
        <div class="toolbar">
          <el-input
            v-model="enterpriseKeyword"
            clearable
            placeholder="用户名 / 姓名 / 手机 / 企业名称"
            style="max-width: 320px"
            @keyup.enter="loadEnterprises(1)"
          />
          <el-button type="primary" :loading="enterpriseLoading" @click="loadEnterprises(1)">搜索</el-button>
        </div>
        <el-table v-loading="enterpriseLoading" :data="enterpriseRows" border aria-label="企业用户列表">
          <el-table-column prop="userId" label="用户ID" width="88" />
          <el-table-column prop="username" label="用户名" min-width="120" />
          <el-table-column prop="realName" label="联系人(账号)" min-width="110" show-overflow-tooltip />
          <el-table-column prop="phone" label="手机" min-width="120" />
          <el-table-column prop="email" label="邮箱" min-width="160" show-overflow-tooltip />
          <el-table-column label="账号状态" width="96">
            <template #default="{ row }">{{ formatUserStatus(row.userStatus) }}</template>
          </el-table-column>
          <el-table-column label="黑名单" width="88">
            <template #default="{ row }">{{ row.blacklisted === 1 ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column prop="userCreatedAt" label="注册时间" min-width="160" />
          <el-table-column prop="profileId" label="档案ID" width="88">
            <template #default="{ row }">{{ row.profileId ?? '-' }}</template>
          </el-table-column>
          <el-table-column prop="enterpriseName" label="企业名称" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">{{ row.enterpriseName || '-' }}</template>
          </el-table-column>
          <el-table-column prop="contactPerson" label="联系人" min-width="100">
            <template #default="{ row }">{{ row.contactPerson || '-' }}</template>
          </el-table-column>
          <el-table-column prop="contactPhone" label="联系电话" min-width="120">
            <template #default="{ row }">{{ row.contactPhone || '-' }}</template>
          </el-table-column>
          <el-table-column label="认证状态" width="100">
            <template #default="{ row }">{{ formatVerifyStatus(row.verifyStatus) }}</template>
          </el-table-column>
          <el-table-column prop="verifiedAt" label="最近审核时间" min-width="160">
            <template #default="{ row }">{{ row.verifiedAt || '-' }}</template>
          </el-table-column>
          <el-table-column label="执照" width="88">
            <template #default="{ row }">
              <el-button
                v-if="row.licensePath"
                type="primary"
                link
                @click="openImagePreview('营业执照', row.licensePath)"
              >
                查看
              </el-button>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="260" fixed="right">
            <template #default="{ row }">
              <el-button
                type="primary"
                link
                :disabled="!row.profileId"
                @click="openAuditDrawer('ENTERPRISE_PROFILE', row.profileId)"
              >
                审核记录
              </el-button>
              <el-button
                v-if="row.blacklisted !== 1"
                type="warning"
                link
                @click="openBlacklistDialog('ENTERPRISE', row)"
              >
                加入黑名单
              </el-button>
              <el-button v-else type="success" link @click="handleUnblacklist(row)">解除黑名单</el-button>
            </template>
          </el-table-column>
        </el-table>
        <div class="pager-wrap">
          <el-pagination
            v-model:current-page="enterprisePage"
            v-model:page-size="enterpriseSize"
            :total="enterpriseTotal"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @current-change="(p) => loadEnterprises(p)"
            @size-change="() => loadEnterprises(1)"
          />
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-card>

  <el-drawer v-model="auditDrawerVisible" title="认证审核记录" size="480px" destroy-on-close @closed="onAuditDrawerClosed">
    <el-table v-loading="auditLogLoading" :data="auditLogRows" border size="small" empty-text="暂无审核记录">
      <el-table-column prop="operatedAt" label="时间" width="168" />
      <el-table-column label="结果" width="88">
        <template #default="{ row }">{{ formatVerifyStatus(row.auditStatus) }}</template>
      </el-table-column>
      <el-table-column prop="operatorUsername" label="审核人" width="100" show-overflow-tooltip>
        <template #default="{ row }">{{ row.operatorUsername || '-' }}</template>
      </el-table-column>
      <el-table-column prop="auditComment" label="备注" min-width="120" show-overflow-tooltip>
        <template #default="{ row }">{{ row.auditComment || '-' }}</template>
      </el-table-column>
    </el-table>
  </el-drawer>

  <el-dialog v-model="blacklistDialogVisible" title="加入黑名单" width="520px" destroy-on-close @closed="onBlacklistDialogClosed">
    <p class="blacklist-dialog-hint">拉黑原因将写入系统并通知该用户（必填，最多500字）。</p>
    <el-input
      v-model="blacklistReasonInput"
      type="textarea"
      :rows="4"
      maxlength="500"
      show-word-limit
      placeholder="请填写拉黑原因"
      aria-label="拉黑原因"
    />
    <template #footer>
      <el-button @click="blacklistDialogVisible = false">取消</el-button>
      <el-button type="warning" :loading="blacklistSubmitting" @click="confirmBlacklist">确认加入黑名单</el-button>
    </template>
  </el-dialog>

  <el-dialog
    v-model="imagePreviewVisible"
    :title="imagePreviewTitle"
    width="min(920px, 96vw)"
    class="admin-doc-preview-dialog"
    destroy-on-close
    @closed="onImagePreviewClosed"
  >
    <div class="admin-doc-preview-body">
      <p v-if="imagePreviewFailed" class="admin-doc-preview-error" role="alert">
        图片加载失败，请确认文件存在或稍后重试。
      </p>
      <img
        v-else-if="imagePreviewSrc"
        :src="imagePreviewSrc"
        alt=""
        class="admin-doc-preview-img"
        @error="onPreviewImageError"
      />
    </div>
  </el-dialog>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  blacklistUser,
  getAdminAuditLogs,
  pageAdminEnterpriseUsers,
  pageAdminJobSeekerUsers,
  unblacklistUser
} from '../../api/admin'

const activeTab = ref('jobseekers')

const jobSeekerKeyword = ref('')
const jobSeekerLoading = ref(false)
const jobSeekerRows = ref([])
const jobSeekerPage = ref(1)
const jobSeekerSize = ref(10)
const jobSeekerTotal = ref(0)

const enterpriseKeyword = ref('')
const enterpriseLoading = ref(false)
const enterpriseRows = ref([])
const enterprisePage = ref(1)
const enterpriseSize = ref(10)
const enterpriseTotal = ref(0)

const auditDrawerVisible = ref(false)
const auditLogLoading = ref(false)
const auditLogRows = ref([])

const imagePreviewVisible = ref(false)
const imagePreviewTitle = ref('')
const imagePreviewSrc = ref('')
const imagePreviewFailed = ref(false)

const blacklistDialogVisible = ref(false)
const blacklistReasonInput = ref('')
const blacklistSubmitting = ref(false)
const blacklistTargetUserId = ref(null)

function openBlacklistDialog(_role, row) {
  blacklistTargetUserId.value = row.userId
  blacklistReasonInput.value = ''
  blacklistDialogVisible.value = true
}

function onBlacklistDialogClosed() {
  blacklistTargetUserId.value = null
  blacklistReasonInput.value = ''
}

async function confirmBlacklist() {
  const reason = blacklistReasonInput.value?.trim() || ''
  if (!reason) {
    ElMessage.warning('请填写拉黑原因')
    return
  }
  const uid = blacklistTargetUserId.value
  if (!uid) {
    return
  }
  blacklistSubmitting.value = true
  try {
    await blacklistUser(uid, { reason })
    ElMessage.success('已加入黑名单')
    blacklistDialogVisible.value = false
    await loadJobSeekers(jobSeekerPage.value)
    await loadEnterprises(enterprisePage.value)
  } finally {
    blacklistSubmitting.value = false
  }
}

async function handleUnblacklist(row) {
  await ElMessageBox.confirm(`确认将用户「${row.username}」移出黑名单？`, '解除黑名单', {
    type: 'warning',
    confirmButtonText: '确认解除',
    cancelButtonText: '取消'
  })
  await unblacklistUser(row.userId)
  ElMessage.success('已解除黑名单')
  await loadJobSeekers(jobSeekerPage.value)
  await loadEnterprises(enterprisePage.value)
}

function resolveAssetUrl(path) {
  if (!path || typeof path !== 'string') {
    return ''
  }
  const trimmed = path.trim()
  if (!trimmed) {
    return ''
  }
  if (/^https?:\/\//i.test(trimmed)) {
    return trimmed
  }
  const ext = trimmed.split('.').pop()?.toLowerCase() || ''
  if (ext === 'pdf') {
    return ''
  }
  const normalized = trimmed.startsWith('/') ? trimmed : `/${trimmed}`
  if (import.meta.env.DEV && normalized.startsWith('/uploads')) {
    const origin = (
      import.meta.env.VITE_UPLOADS_ORIGIN ||
      import.meta.env.VITE_BACKEND_ORIGIN ||
      'http://localhost:8080'
    ).replace(/\/$/, '')
    return `${origin}${normalized}`
  }
  return normalized
}

function openImagePreview(title, path) {
  if (!path) {
    return
  }
  const url = resolveAssetUrl(path)
  const ext = path.trim().split('.').pop()?.toLowerCase() || ''
  if (ext === 'pdf') {
    ElMessage.info('执照为 PDF 时请在企业端下载或由服务器直接访问该文件链接查看')
    return
  }
  if (!url) {
    ElMessage.warning('无法预览该文件')
    return
  }
  imagePreviewTitle.value = title
  imagePreviewSrc.value = url
  imagePreviewFailed.value = false
  imagePreviewVisible.value = true
}

function onPreviewImageError() {
  imagePreviewFailed.value = true
  ElMessage.error('图片加载失败')
}

function onImagePreviewClosed() {
  imagePreviewSrc.value = ''
  imagePreviewFailed.value = false
}

function formatUserStatus(status) {
  if (status === 1 || status === '1') {
    return '正常'
  }
  if (status === 0 || status === '0') {
    return '禁用'
  }
  return status != null ? String(status) : '-'
}

function formatVerifyStatus(status) {
  if (!status) {
    return '-'
  }
  const u = String(status).toUpperCase()
  if (u === 'APPROVED' || u === 'PASS') {
    return '已通过'
  }
  if (u === 'REJECTED' || u === 'REJECT') {
    return '已驳回'
  }
  if (u === 'PENDING') {
    return '待审核'
  }
  return status
}

async function loadJobSeekers(page) {
  jobSeekerLoading.value = true
  try {
    const p = page || jobSeekerPage.value
    jobSeekerPage.value = p
    const res = await pageAdminJobSeekerUsers({
      page: p,
      size: jobSeekerSize.value,
      keyword: jobSeekerKeyword.value?.trim() || undefined
    })
    const body = res.data || {}
    jobSeekerRows.value = body.records || []
    jobSeekerTotal.value = Number(body.total || 0)
  } finally {
    jobSeekerLoading.value = false
  }
}

async function loadEnterprises(page) {
  enterpriseLoading.value = true
  try {
    const p = page || enterprisePage.value
    enterprisePage.value = p
    const res = await pageAdminEnterpriseUsers({
      page: p,
      size: enterpriseSize.value,
      keyword: enterpriseKeyword.value?.trim() || undefined
    })
    const body = res.data || {}
    enterpriseRows.value = body.records || []
    enterpriseTotal.value = Number(body.total || 0)
  } finally {
    enterpriseLoading.value = false
  }
}

function onTabChange(name) {
  if (name === 'jobseekers' && jobSeekerRows.value.length === 0 && jobSeekerTotal.value === 0) {
    loadJobSeekers(1)
  }
  if (name === 'enterprises' && enterpriseRows.value.length === 0 && enterpriseTotal.value === 0) {
    loadEnterprises(1)
  }
}

async function openAuditDrawer(businessType, businessId) {
  if (!businessId) {
    return
  }
  auditDrawerVisible.value = true
  auditLogLoading.value = true
  auditLogRows.value = []
  try {
    const res = await getAdminAuditLogs({ businessType, businessId })
    auditLogRows.value = res.data || []
  } finally {
    auditLogLoading.value = false
  }
}

function onAuditDrawerClosed() {
  auditLogRows.value = []
}

onMounted(() => {
  loadJobSeekers(1)
})
</script>

<style scoped>
.hero-card {
  border-radius: 12px;
}

.user-archive-card {
  margin-top: 20px;
}

.section-header h2 {
  margin: 0 0 8px;
  font-size: 1.25rem;
}

.section-desc {
  margin: 0;
  color: var(--el-text-color-secondary);
  font-size: 0.875rem;
  line-height: 1.5;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
  align-items: center;
}

.pager-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.blacklist-dialog-hint {
  margin: 0 0 12px;
  font-size: 13px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}

.admin-doc-preview-body {
  min-height: 120px;
}

.admin-doc-preview-error {
  color: var(--el-color-danger);
}

.admin-doc-preview-img {
  display: block;
  max-width: 100%;
  max-height: 70vh;
  margin: 0 auto;
  object-fit: contain;
}
</style>
