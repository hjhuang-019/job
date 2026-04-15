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

    <el-dialog
      v-model="imagePreviewVisible"
      :title="imagePreviewTitle"
      width="90%"
      align-center
      class="admin-doc-preview-dialog"
      aria-label="证件或执照图片预览"
      @closed="onImagePreviewClosed"
    >
      <div class="admin-doc-preview-body">
        <p v-if="imagePreviewFailed" class="admin-doc-preview-error" role="alert">
          图片未能加载。开发环境下请确认后端已启动（默认
          <code>http://localhost:8080</code>
          ）；若端口不同，可在前端
          <code>.env.development</code>
          中设置
          <code>VITE_UPLOADS_ORIGIN</code>
          。也可点击「新窗口打开原图」排查。
        </p>
        <img
          v-else-if="imagePreviewSrc"
          :key="imagePreviewSrc"
          :src="resolveAssetUrl(imagePreviewSrc)"
          alt=""
          class="admin-doc-preview-img"
          @error="onPreviewImageError"
        />
      </div>
      <template #footer>
        <div class="admin-doc-preview-footer">
          <el-link
            v-if="imagePreviewSrc"
            :href="resolveAssetUrl(imagePreviewSrc)"
            target="_blank"
            rel="noopener noreferrer"
            type="primary"
          >
            新窗口打开原图
          </el-link>
          <el-button @click="imagePreviewVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

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

const imagePreviewVisible = ref(false)
const imagePreviewTitle = ref('')
const imagePreviewSrc = ref('')
const imagePreviewFailed = ref(false)

/**
 * 开发环境下 Vite 只代理了部分路径时，相对地址 /uploads 会打到 5173 并返回 HTML，图片区域会空白。
 * 对 /uploads 直连后端（img 跨端口展示无需 CORS）；生产同域时仍用相对路径即可。
 */
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
  imagePreviewTitle.value = title
  imagePreviewSrc.value = path
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
    window.dispatchEvent(new Event('admin-pending-updated'))
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

<style scoped>
.admin-doc-preview-body {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 120px;
  padding: 12px;
  background: var(--el-fill-color-lighter);
  border-radius: var(--el-border-radius-base);
}

.admin-doc-preview-error {
  margin: 0;
  padding: 8px 12px;
  font-size: 14px;
  line-height: 1.5;
  color: var(--el-text-color-regular);
}

.admin-doc-preview-error code {
  font-size: 12px;
  padding: 0 4px;
  border-radius: 4px;
  background: var(--el-fill-color);
}

.admin-doc-preview-img {
  max-width: 100%;
  max-height: min(70vh, 720px);
  width: auto;
  height: auto;
  object-fit: contain;
}

.admin-doc-preview-footer {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  justify-content: flex-end;
  width: 100%;
}
</style>
