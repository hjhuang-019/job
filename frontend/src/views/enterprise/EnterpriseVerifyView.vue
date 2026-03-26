<template>
  <section class="enterprise-verify-page">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <div class="section-header">
          <h2>企业认证</h2>
          <el-tag :type="verifyTagType">{{ verifyStatusText }}</el-tag>
        </div>
      </template>

      <el-alert
        title="请先上传营业执照，再提交企业认证。系统仅保存本地相对路径用于演示。"
        type="info"
        show-icon
        :closable="false"
      />

      <el-form label-position="top" class="verify-form">
        <el-form-item label="营业执照路径">
          <el-input :model-value="licensePath || '未上传'" readonly aria-readonly="true" />
        </el-form-item>

        <el-form-item label="上传营业执照" for="licenseUpload">
          <el-upload
            id="licenseUpload"
            class="upload-area"
            drag
            :show-file-list="false"
            :auto-upload="false"
            accept=".jpg,.jpeg,.png,.pdf"
            :on-change="handleFileChange"
          >
            <el-icon><UploadFilled /></el-icon>
            <div class="el-upload__text">拖拽文件到此处，或 <em>点击选择文件</em></div>
            <template #tip>
              <div class="el-upload__tip">支持 jpg/jpeg/png/pdf</div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :disabled="!selectedFile" :loading="uploading" @click="handleUpload">
            上传执照
          </el-button>
          <el-button type="success" :loading="submitting" @click="handleSubmitVerify">
            提交认证
          </el-button>
          <el-button :loading="loading" @click="loadProfile">刷新状态</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getEnterpriseProfile, submitEnterpriseVerify, uploadEnterpriseLicense } from '../../api/enterprise'

const loading = ref(false)
const uploading = ref(false)
const submitting = ref(false)
const selectedFile = ref(null)
const licensePath = ref('')
const verifyStatus = ref('PENDING')

const verifyStatusText = computed(() => {
  return {
    PENDING: '待审核',
    PASS: '已通过',
    REJECT: '未通过'
  }[verifyStatus.value] || '待审核'
})

const verifyTagType = computed(() => {
  return {
    PENDING: 'warning',
    PASS: 'success',
    REJECT: 'danger'
  }[verifyStatus.value] || 'info'
})

function fillProfile(profile) {
  licensePath.value = profile.licensePath || ''
  verifyStatus.value = profile.verifyStatus || 'PENDING'
}

async function loadProfile() {
  loading.value = true
  try {
    const response = await getEnterpriseProfile()
    fillProfile(response.data || {})
  } finally {
    loading.value = false
  }
}

function handleFileChange(file) {
  selectedFile.value = file.raw || null
}

async function handleUpload() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  uploading.value = true
  try {
    const response = await uploadEnterpriseLicense(selectedFile.value)
    licensePath.value = response.data?.licensePath || ''
    verifyStatus.value = 'PENDING'
    selectedFile.value = null
    ElMessage.success('营业执照上传成功')
  } finally {
    uploading.value = false
  }
}

async function handleSubmitVerify() {
  submitting.value = true
  try {
    const response = await submitEnterpriseVerify()
    fillProfile(response.data || {})
    ElMessage.success('企业认证申请已提交')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>
