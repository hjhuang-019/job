<template>
  <section class="profile-page" aria-label="求职者个人中心页面">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <div class="section-header">
          <h2>求职者个人中心</h2>
          <el-tag :type="verifyTagType">{{ verifyStatusText }}</el-tag>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" aria-label="求职者资料表单">
        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="真实姓名" prop="realName" for="realName">
              <el-input id="realName" v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="手机号" prop="phone" for="phone">
              <el-input id="phone" v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="邮箱" prop="email" for="email">
              <el-input id="email" v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="期望城市" prop="expectedCity" for="expectedCity">
              <el-input id="expectedCity" v-model="form.expectedCity" placeholder="例如：上海" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="残疾类型" prop="disabilityType" for="disabilityType">
              <el-input id="disabilityType" v-model="form.disabilityType" placeholder="例如：听力障碍" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="残疾等级" prop="disabilityLevel" for="disabilityLevel">
              <el-input id="disabilityLevel" v-model="form.disabilityLevel" placeholder="例如：二级" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="求职意向" prop="expectedJob" for="expectedJob">
              <el-input id="expectedJob" v-model="form.expectedJob" placeholder="例如：Java后端开发工程师" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="期望薪资" prop="expectedSalary" for="expectedSalary">
              <el-input id="expectedSalary" v-model="form.expectedSalary" placeholder="例如：8k-12k" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="技能标签" prop="skillsList" for="skillsList">
          <el-select
            id="skillsList"
            v-model="form.skillsList"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="输入后回车添加技能标签"
          />
        </el-form-item>

        <el-form-item label="个人简介" prop="introduction" for="introduction">
          <el-input
            id="introduction"
            v-model="form.introduction"
            type="textarea"
            :rows="4"
            placeholder="请填写个人简介"
          />
        </el-form-item>

        <el-form-item label="是否接受远程工作" prop="acceptRemote" for="acceptRemote">
          <el-switch
            id="acceptRemote"
            v-model="form.acceptRemote"
            active-text="接受"
            inactive-text="不接受"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" aria-label="保存求职者资料" @click="handleSave">保存资料</el-button>
          <el-button :loading="loading" @click="loadProfile">重新加载</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getJobSeekerProfile, updateJobSeekerProfile } from '../../api/jobseeker'

const formRef = ref()
const loading = ref(false)
const saving = ref(false)
const verifyStatus = ref('PENDING')

const form = reactive({
  realName: '',
  phone: '',
  email: '',
  disabilityType: '',
  disabilityLevel: '',
  skillsList: [],
  expectedCity: '',
  expectedSalary: '',
  expectedJob: '',
  acceptRemote: false,
  introduction: ''
})

const rules = {
  phone: [{ pattern: /^$|^1\d{10}$/, message: '请输入正确手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确邮箱格式', trigger: 'blur' }]
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

function fillForm(profile) {
  form.realName = profile.realName || ''
  form.phone = profile.phone || ''
  form.email = profile.email || ''
  form.disabilityType = profile.disabilityType || ''
  form.disabilityLevel = profile.disabilityLevel || ''
  form.skillsList = profile.skills ? profile.skills.split(',').map((item) => item.trim()).filter(Boolean) : []
  form.expectedCity = profile.expectedCity || ''
  form.expectedSalary = profile.expectedSalary || ''
  form.expectedJob = profile.expectedJob || ''
  form.acceptRemote = Boolean(profile.acceptRemote)
  form.introduction = profile.introduction || ''
  verifyStatus.value = profile.verifyStatus || 'PENDING'
}

async function loadProfile() {
  loading.value = true
  try {
    const response = await getJobSeekerProfile()
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
      realName: form.realName,
      phone: form.phone,
      email: form.email,
      disabilityType: form.disabilityType,
      disabilityLevel: form.disabilityLevel,
      skills: form.skillsList.join(','),
      expectedCity: form.expectedCity,
      expectedSalary: form.expectedSalary,
      expectedJob: form.expectedJob,
      acceptRemote: form.acceptRemote,
      introduction: form.introduction
    }
    const response = await updateJobSeekerProfile(payload)
    fillForm(response.data || {})
    ElMessage.success('资料保存成功')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

