<template>
  <section class="verify-page">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <div class="section-header">
          <h2>求职者认证</h2>
          <el-tag :type="verifyTagType">{{ verifyStatusText }}</el-tag>
        </div>
      </template>

      <el-alert
        title="请上传残疾证图片并提交认证，上传后系统仅保存相对路径用于演示。"
        type="info"
        show-icon
        :closable="false"
      />

      <el-form label-position="top" class="verify-form">
        <el-form-item label="当前证件路径">
          <el-input :model-value="certificatePath || '未上传'" readonly aria-readonly="true" />
        </el-form-item>

        <el-form-item label="上传残疾证图片" for="certificateUpload">
          <el-upload
            id="certificateUpload"
            class="upload-area"
            drag
            :show-file-list="false"
            :auto-upload="false"
            accept="image/*"
            :on-change="handleFileChange"
          >
            <el-icon><UploadFilled /></el-icon>
            <div class="el-upload__text">拖拽图片到此处，或 <em>点击选择文件</em></div>
            <template #tip>
              <div class="el-upload__tip">支持 jpg/jpeg/png/gif/webp</div>
            </template>
          </el-upload>
          <p v-if="selectedFileName" class="selected-file-tip">已选择：{{ selectedFileName }}</p>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :disabled="!selectedFile" :loading="uploading" @click="handleUpload">
            上传证件
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
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { getJobSeekerProfile, submitJobSeekerVerify, uploadCertificate } from '../../api/jobseeker'
import { registerJobSeekerVoicePage } from '../../voice/jobSeekerVoiceRegistry'

const route = useRoute()
const loading = ref(false)
const uploading = ref(false)
const submitting = ref(false)
const selectedFile = ref(null)
const selectedFileName = ref('')
const certificatePath = ref('')
const verifyStatus = ref('PENDING')
const ALLOW_IMAGE_EXT = new Set(['jpg', 'jpeg', 'png', 'gif', 'webp'])

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
  certificatePath.value = profile.certificatePath || ''
  verifyStatus.value = profile.verifyStatus || 'PENDING'
}

async function loadProfile() {
  loading.value = true
  try {
    const response = await getJobSeekerProfile()
    fillProfile(response.data || {})
  } finally {
    loading.value = false
  }
}

function handleFileChange(file) {
  const rawFile = file?.raw || null
  if (!rawFile) {
    selectedFile.value = null
    selectedFileName.value = ''
    return
  }
  const ext = rawFile.name?.split('.').pop()?.toLowerCase()
  if (!ext || !ALLOW_IMAGE_EXT.has(ext)) {
    selectedFile.value = null
    selectedFileName.value = ''
    ElMessage.error('仅支持 jpg/jpeg/png/gif/webp 格式图片')
    return
  }
  selectedFile.value = rawFile
  selectedFileName.value = rawFile.name || ''
}

async function handleUpload() {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择图片文件')
    return
  }
  uploading.value = true
  try {
    const response = await uploadCertificate(selectedFile.value)
    certificatePath.value = response.data?.certificatePath || ''
    verifyStatus.value = 'PENDING'
    ElMessage.success('证件上传成功')
    selectedFile.value = null
    selectedFileName.value = ''
  } catch (error) {
    // Error toast is handled by http interceptor.
  } finally {
    uploading.value = false
  }
}

async function handleSubmitVerify() {
  submitting.value = true
  try {
    const response = await submitJobSeekerVerify()
    fillProfile(response.data || {})
    ElMessage.success('认证申请已提交')
  } finally {
    submitting.value = false
  }
}

let unregisterVoicePage = () => {}

onMounted(() => {
  unregisterVoicePage = registerJobSeekerVoicePage(route.name, {
    getPageContext: () => ({
      verifyStatus: verifyStatus.value,
      certificateUploaded: Boolean(certificatePath.value),
      selectedFileName: selectedFileName.value || null
    }),
    onInterpret: () => {}
  })
  loadProfile()
})

onUnmounted(() => {
  unregisterVoicePage()
})
</script>

