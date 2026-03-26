<template>
  <section class="enterprise-page">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <div class="section-header">
          <h2>企业中心</h2>
          <el-tag :type="verifyTagType">{{ verifyStatusText }}</el-tag>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="企业名称" prop="enterpriseName" for="enterpriseName">
              <el-input id="enterpriseName" v-model="form.enterpriseName" placeholder="请输入企业名称" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="所属行业" prop="industry" for="industry">
              <el-input id="industry" v-model="form.industry" placeholder="请输入所属行业" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="企业规模" prop="scaleType" for="scaleType">
              <el-select id="scaleType" v-model="form.scaleType" placeholder="请选择企业规模">
                <el-option label="1-20人" value="1-20人" />
                <el-option label="20-99人" value="20-99人" />
                <el-option label="100-499人" value="100-499人" />
                <el-option label="500人以上" value="500人以上" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="联系人" prop="contactPerson" for="contactPerson">
              <el-input id="contactPerson" v-model="form.contactPerson" placeholder="请输入联系人姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="联系电话" prop="contactPhone" for="contactPhone">
              <el-input id="contactPhone" v-model="form.contactPhone" placeholder="请输入11位手机号" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="邮箱" prop="email" for="email">
              <el-input id="email" v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="企业地址" prop="address" for="address">
          <el-input id="address" v-model="form.address" placeholder="请输入企业地址" />
        </el-form-item>

        <el-form-item label="企业简介" prop="description" for="description">
          <el-input id="description" v-model="form.description" type="textarea" :rows="4" placeholder="请输入企业简介" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存资料</el-button>
          <el-button :loading="loading" @click="loadProfile">重新加载</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getEnterpriseProfile, updateEnterpriseProfile } from '../../api/enterprise'

const formRef = ref()
const loading = ref(false)
const saving = ref(false)
const verifyStatus = ref('PENDING')

const form = reactive({
  enterpriseName: '',
  industry: '',
  scaleType: '',
  contactPerson: '',
  contactPhone: '',
  email: '',
  address: '',
  description: ''
})

const rules = {
  enterpriseName: [{ required: true, message: '请输入企业名称', trigger: 'blur' }],
  contactPhone: [{ pattern: /^$|^1\d{10}$/, message: '请输入正确手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确邮箱', trigger: 'blur' }]
}

const verifyStatusText = computed(() => {
  return {
    PENDING: '认证状态：待审核',
    PASS: '认证状态：已通过',
    REJECT: '认证状态：未通过'
  }[verifyStatus.value] || '认证状态：待审核'
})

const verifyTagType = computed(() => {
  return {
    PENDING: 'warning',
    PASS: 'success',
    REJECT: 'danger'
  }[verifyStatus.value] || 'info'
})

function fillForm(profile = {}) {
  form.enterpriseName = profile.enterpriseName || ''
  form.industry = profile.industry || ''
  form.scaleType = profile.scaleType || ''
  form.contactPerson = profile.contactPerson || ''
  form.contactPhone = profile.contactPhone || ''
  form.email = profile.email || ''
  form.address = profile.address || ''
  form.description = profile.description || ''
  verifyStatus.value = profile.verifyStatus || 'PENDING'
}

async function loadProfile() {
  loading.value = true
  try {
    const response = await getEnterpriseProfile()
    fillForm(response.data || {})
  } finally {
    loading.value = false
  }
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    const payload = {
      enterpriseName: form.enterpriseName,
      industry: form.industry,
      scaleType: form.scaleType,
      contactPerson: form.contactPerson,
      contactPhone: form.contactPhone,
      email: form.email,
      address: form.address,
      description: form.description
    }
    const response = await updateEnterpriseProfile(payload)
    fillForm(response.data || {})
    ElMessage.success('企业资料保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>
