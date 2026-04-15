<template>
  <section class="profile-page" aria-label="求职者个人中心页面">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <div class="section-header">
          <h2>求职者个人中心</h2>
          <el-tag :type="verifyTagType">{{ verifyStatusText }}</el-tag>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        aria-label="求职者资料表单"
        @focusin="handleProfileFormFocusin"
      >
        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="真实姓名" prop="realName" for="realName" data-voice-field="realName" data-voice-label="真实姓名">
              <el-input id="realName" v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="手机号" prop="phone" for="phone" data-voice-field="phone" data-voice-label="手机号">
              <el-input id="phone" v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="邮箱" prop="email" for="email" data-voice-field="email" data-voice-label="邮箱">
              <el-input id="email" v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item
              label="期望城市"
              prop="expectedCity"
              for="expectedCity"
              data-voice-field="expectedCity"
              data-voice-label="期望城市"
            >
              <el-input id="expectedCity" v-model="form.expectedCity" placeholder="例如：上海" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item
              label="残疾类型"
              prop="disabilityTypeList"
              for="disabilityType"
              data-voice-field="disabilityTypeList"
              data-voice-label="残疾类型"
            >
              <el-select
                id="disabilityType"
                v-model="form.disabilityTypeList"
                multiple
                filterable
                placeholder="请选择残疾类型（可多选）"
                aria-label="残疾类型，可多选"
                class="disability-type-select"
              >
                <el-option
                  v-for="opt in DISABILITY_TYPE_OPTIONS"
                  :key="opt.value"
                  :label="opt.label"
                  :value="opt.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item
              label="残疾等级"
              prop="disabilityLevel"
              for="disabilityLevel"
              data-voice-field="disabilityLevel"
              data-voice-label="残疾等级"
            >
              <div class="disability-level-row">
                <el-select
                  id="disabilityLevel"
                  v-model="form.disabilityLevel"
                  clearable
                  filterable
                  placeholder="请选择残疾等级"
                  class="disability-level-select"
                  aria-label="残疾等级"
                >
                  <el-option
                    v-for="opt in DISABILITY_LEVEL_OPTIONS"
                    :key="opt.value"
                    :label="opt.label"
                    :value="opt.value"
                  />
                </el-select>
                <p class="disability-level-hint" role="note">
                  请按残疾证所载等级中<strong>最高</strong>一级填写；存在多重残疾时以较重者为准。
                </p>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item
              label="求职意向"
              prop="expectedJob"
              for="expectedJob"
              data-voice-field="expectedJob"
              data-voice-label="求职意向"
            >
              <el-input id="expectedJob" v-model="form.expectedJob" placeholder="例如：Java后端开发工程师" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item
              label="期望薪资"
              prop="expectedSalary"
              for="expectedSalary"
              data-voice-field="expectedSalary"
              data-voice-label="期望薪资"
            >
              <el-input id="expectedSalary" v-model="form.expectedSalary" placeholder="例如：8k-12k" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="技能标签" prop="skillsList" for="skillsList" data-voice-field="skillsList" data-voice-label="技能标签">
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

        <el-form-item
          label="个人简介"
          prop="introduction"
          for="introduction"
          data-voice-field="introduction"
          data-voice-label="个人简介"
        >
          <el-input
            id="introduction"
            v-model="form.introduction"
            type="textarea"
            :rows="4"
            placeholder="请填写个人简介"
          />
        </el-form-item>

        <el-form-item
          label="是否接受远程工作"
          prop="acceptRemote"
          for="acceptRemote"
          data-voice-field="acceptRemote"
          data-voice-label="是否接受远程工作"
        >
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
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getJobSeekerProfile, updateJobSeekerProfile } from '../../api/jobseeker'
import { isSpeechSupported, speakText } from '../../accessibility/speech'
import { useAccessibilityStore } from '../../stores/accessibility'
import { registerJobSeekerVoicePage } from '../../voice/jobSeekerVoiceRegistry'
import {
  DISABILITY_LEVEL_OPTIONS,
  DISABILITY_TYPE_OPTIONS,
  normalizeDisabilityLevelFromStorage,
  parseDisabilityTypesFromStorage,
  serializeDisabilityTypesForStorage
} from '../../constants/disability'

const route = useRoute()
const formRef = ref()
const accessibilityStore = useAccessibilityStore()
const loading = ref(false)
const saving = ref(false)
const verifyStatus = ref('PENDING')

const form = reactive({
  realName: '',
  phone: '',
  email: '',
  disabilityTypeList: [],
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

function canSpeak() {
  return isSpeechSupported() && accessibilityStore.voiceEnabled
}

function speakWithSettings(text, options = {}) {
  if (!canSpeak()) {
    return false
  }
  return speakText(text, {
    ...options,
    rate: accessibilityStore.voiceRate,
    volume: accessibilityStore.voiceVolume
  })
}

function fieldVoiceValue(field) {
  if (field === 'disabilityTypeList') {
    return form.disabilityTypeList.length ? form.disabilityTypeList.join('、') : ''
  }
  if (field === 'skillsList') {
    return form.skillsList.length ? form.skillsList.join('、') : ''
  }
  if (field === 'acceptRemote') {
    return form.acceptRemote ? '接受' : '不接受'
  }
  return String(form[field] || '').trim()
}

function handleProfileFormFocusin(event) {
  if (!canSpeak()) {
    return
  }
  const target = event.target instanceof Element ? event.target : null
  if (!target) {
    return
  }
  const item = target.closest('[data-voice-label]')
  if (!item) {
    return
  }
  const label = item.getAttribute('data-voice-label') || ''
  const field = item.getAttribute('data-voice-field') || ''
  const value = fieldVoiceValue(field)
  const placeholder =
    target.getAttribute('placeholder') ||
    target.getAttribute('aria-placeholder') ||
    item.querySelector('input, textarea')?.getAttribute('placeholder') ||
    ''
  const detail = value || placeholder || '当前为空'
  speakWithSettings(`${label}，${detail}`, { interrupt: false, minIntervalMs: 500 })
}

function announceProfileFormOverview() {
  if (!canSpeak()) {
    return
  }
  speakWithSettings(
    `个人中心资料表单已加载。包含真实姓名、手机号、邮箱、期望城市、残疾类型、残疾等级、求职意向、期望薪资、技能标签、个人简介和是否接受远程工作。当前${verifyStatusText.value}`,
    { force: true }
  )
}

function announceProfileFieldValues() {
  if (!canSpeak()) {
    return
  }
  const fieldPairs = [
    ['真实姓名', fieldVoiceValue('realName')],
    ['手机号', fieldVoiceValue('phone')],
    ['邮箱', fieldVoiceValue('email')],
    ['期望城市', fieldVoiceValue('expectedCity')],
    ['残疾类型', fieldVoiceValue('disabilityTypeList')],
    ['残疾等级', fieldVoiceValue('disabilityLevel')],
    ['求职意向', fieldVoiceValue('expectedJob')],
    ['期望薪资', fieldVoiceValue('expectedSalary')],
    ['技能标签', fieldVoiceValue('skillsList')],
    ['是否接受远程工作', fieldVoiceValue('acceptRemote')]
  ]
  const content = fieldPairs.map(([label, value]) => `${label}${value || '未填写'}`).join('，')
  speakWithSettings(`当前资料：${content}`, { force: true, minIntervalMs: 700 })
}

function fillForm(profile) {
  form.realName = profile.realName || ''
  form.phone = profile.phone || ''
  form.email = profile.email || ''
  form.disabilityTypeList = parseDisabilityTypesFromStorage(profile.disabilityType)
  form.disabilityLevel = normalizeDisabilityLevelFromStorage(profile.disabilityLevel)
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
    announceProfileFormOverview()
    announceProfileFieldValues()
  } finally {
    loading.value = false
  }
}

function mergeProfileFromAi(profile) {
  if (!profile || typeof profile !== 'object') {
    return
  }
  const p = profile
  if (p.realName != null) {
    form.realName = String(p.realName)
  }
  if (p.phone != null) {
    form.phone = String(p.phone)
  }
  if (p.email != null) {
    form.email = String(p.email)
  }
  if (p.disabilityType != null) {
    form.disabilityTypeList = parseDisabilityTypesFromStorage(String(p.disabilityType))
  }
  if (p.disabilityLevel != null) {
    form.disabilityLevel = normalizeDisabilityLevelFromStorage(String(p.disabilityLevel))
  }
  if (p.skills != null) {
    const s = String(p.skills)
    form.skillsList = s
      .split(/[,，、]/)
      .map((item) => item.trim())
      .filter(Boolean)
  }
  if (p.expectedCity != null) {
    form.expectedCity = String(p.expectedCity)
  }
  if (p.expectedSalary != null) {
    form.expectedSalary = String(p.expectedSalary)
  }
  if (p.expectedJob != null) {
    form.expectedJob = String(p.expectedJob)
  }
  if (p.acceptRemote != null) {
    form.acceptRemote = Boolean(p.acceptRemote)
  }
  if (p.introduction != null) {
    form.introduction = String(p.introduction)
  }
}

async function onVoiceInterpret(vo) {
  if (vo.profile && Object.keys(vo.profile).length) {
    mergeProfileFromAi(vo.profile)
    ElMessage.success('AI 已填入资料草稿，请核对后点击保存')
    speakWithSettings('已根据您的口述填入资料草稿，请确认后保存', { force: true })
  }
}

let unregisterVoicePage = () => {}

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
      disabilityType: serializeDisabilityTypesForStorage(form.disabilityTypeList),
      disabilityLevel: form.disabilityLevel || '',
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
    speakWithSettings('个人中心资料保存成功', { force: true })
    announceProfileFieldValues()
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  unregisterVoicePage = registerJobSeekerVoicePage(route.name, {
    getPageContext: () => ({
      verifyStatus: verifyStatus.value,
      formPreview: {
        realName: form.realName,
        phone: form.phone,
        email: form.email,
        expectedCity: form.expectedCity,
        expectedJob: form.expectedJob
      }
    }),
    onInterpret: onVoiceInterpret
  })
  loadProfile()
})

onUnmounted(() => {
  unregisterVoicePage()
})
</script>

<style scoped>
.disability-level-row {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

@media (min-width: 768px) {
  .disability-level-row {
    flex-direction: row;
    align-items: flex-start;
    gap: 16px;
  }

  .disability-level-select {
    flex: 0 0 160px;
    max-width: 200px;
  }
}

.disability-level-hint {
  margin: 0;
  flex: 1;
  font-size: 13px;
  line-height: 1.55;
  color: var(--el-text-color-secondary);
}

/* 多选时展示全部类型名称（不使用 collapse-tags，避免出现「+1」折叠） */
.disability-type-select :deep(.el-select__tags) {
  flex-wrap: wrap;
}
</style>
