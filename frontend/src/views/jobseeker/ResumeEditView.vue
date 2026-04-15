<template>
  <section class="resume-edit-page">
    <el-row :gutter="16">
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="hero-card">
          <template #header>
            <div class="section-header">
              <h2>{{ isEdit ? '编辑简历' : '新增简历' }}</h2>
              <el-button @click="goBack">返回列表</el-button>
            </div>
          </template>

          <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
            <el-form-item label="简历标题" prop="title" for="title">
              <el-input id="title" v-model="form.title" placeholder="请输入简历标题" />
            </el-form-item>

            <el-row :gutter="16">
              <el-col :xs="24" :md="12">
                <el-form-item label="残疾类型" prop="disabilityTypeList" for="disabilityType">
                  <el-select
                    id="disabilityType"
                    v-model="form.disabilityTypeList"
                    multiple
                    filterable
                    placeholder="请选择（可多选）"
                    class="full-width-select"
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
                <el-form-item label="残疾等级" prop="disabilityLevel" for="disabilityLevel">
                  <div class="disability-level-row">
                    <el-select
                      id="disabilityLevel"
                      v-model="form.disabilityLevel"
                      clearable
                      filterable
                      placeholder="请选择"
                      class="disability-level-select full-width-select"
                    >
                      <el-option
                        v-for="opt in DISABILITY_LEVEL_OPTIONS"
                        :key="opt.value"
                        :label="opt.label"
                        :value="opt.value"
                      />
                    </el-select>
                    <p class="disability-level-hint" role="note">
                      请按残疾证所载等级中<strong>最高</strong>一级填写；多重残疾以较重者为准。
                    </p>
                  </div>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item label="教育经历" prop="education" for="education">
              <el-input id="education" v-model="form.education" type="textarea" :rows="3" placeholder="请输入教育经历" />
            </el-form-item>

            <el-form-item label="工作经历" prop="experience" for="experience">
              <el-input id="experience" v-model="form.experience" type="textarea" :rows="3" placeholder="请输入工作经历" />
            </el-form-item>

            <el-form-item label="项目经历" prop="projectExperience" for="projectExperience">
              <el-input
                id="projectExperience"
                v-model="form.projectExperience"
                type="textarea"
                :rows="3"
                placeholder="请输入项目经历"
              />
            </el-form-item>

            <el-form-item label="技能" prop="skills" for="skills">
              <el-input id="skills" v-model="form.skills" type="textarea" :rows="3" placeholder="请输入技能描述" />
            </el-form-item>

            <el-form-item label="自我评价" prop="selfEvaluation" for="selfEvaluation">
              <el-input
                id="selfEvaluation"
                v-model="form.selfEvaluation"
                type="textarea"
                :rows="4"
                placeholder="请输入自我评价"
              />
            </el-form-item>

            <el-form-item label="是否设为默认简历" prop="isDefault" for="isDefault">
              <el-switch id="isDefault" v-model="form.isDefault" active-text="是" inactive-text="否" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="saving" @click="handleSubmit">保存简历</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="status-card">
          <template #header>
            <h2>预览区</h2>
          </template>
          <p><strong>标题：</strong>{{ form.title || '未填写' }}</p>
          <p class="preview-line">
            <strong>残疾类型：</strong>
            <DisabilityTypeTags :value="serializedDisabilityTypesPreview" />
          </p>
          <p class="preview-line">
            <strong>残疾等级：</strong>
            <DisabilityLevelTag :value="form.disabilityLevel" />
          </p>
          <p><strong>教育经历：</strong>{{ form.education || '未填写' }}</p>
          <p><strong>工作经历：</strong>{{ form.experience || '未填写' }}</p>
          <p><strong>项目经历：</strong>{{ form.projectExperience || '未填写' }}</p>
          <p><strong>技能：</strong>{{ form.skills || '未填写' }}</p>
          <p><strong>自我评价：</strong>{{ form.selfEvaluation || '未填写' }}</p>
        </el-card>
      </el-col>
    </el-row>
  </section>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createResume, getResumeDetail, updateResume } from '../../api/resume'
import { registerJobSeekerVoicePage } from '../../voice/jobSeekerVoiceRegistry'
import DisabilityLevelTag from '../../components/DisabilityLevelTag.vue'
import DisabilityTypeTags from '../../components/DisabilityTypeTags.vue'
import {
  DISABILITY_LEVEL_OPTIONS,
  DISABILITY_TYPE_OPTIONS,
  normalizeDisabilityLevelFromStorage,
  parseDisabilityTypesFromStorage,
  serializeDisabilityTypesForStorage
} from '../../constants/disability'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const saving = ref(false)

const resumeId = computed(() => Number(route.params.id))
const isEdit = computed(() => Number.isFinite(resumeId.value) && resumeId.value > 0)

const form = reactive({
  title: '',
  disabilityTypeList: [],
  disabilityLevel: '',
  education: '',
  experience: '',
  projectExperience: '',
  skills: '',
  selfEvaluation: '',
  isDefault: false
})

const serializedDisabilityTypesPreview = computed(() =>
  serializeDisabilityTypesForStorage(form.disabilityTypeList)
)

const rules = {
  title: [
    { required: true, message: '请输入简历标题', trigger: 'blur' },
    { max: 100, message: '标题长度不能超过100位', trigger: 'blur' }
  ]
}

function fillForm(data = {}) {
  form.title = data.title || ''
  form.disabilityTypeList = parseDisabilityTypesFromStorage(data.disabilityType)
  form.disabilityLevel = normalizeDisabilityLevelFromStorage(data.disabilityLevel)
  form.education = data.education || ''
  form.experience = data.experience || ''
  form.projectExperience = data.projectExperience || ''
  form.skills = data.skills || ''
  form.selfEvaluation = data.selfEvaluation || ''
  form.isDefault = Boolean(data.isDefault)
}

async function loadDetail() {
  if (!isEdit.value) {
    return
  }
  const response = await getResumeDetail(resumeId.value)
  fillForm(response.data || {})
}

function goBack() {
  router.push('/jobseeker/resumes')
}

function mergeResumeFromAi(patch) {
  if (!patch || typeof patch !== 'object') {
    return
  }
  if (patch.title != null) {
    form.title = String(patch.title)
  }
  if (patch.education != null) {
    form.education = String(patch.education)
  }
  if (patch.experience != null) {
    form.experience = String(patch.experience)
  }
  if (patch.projectExperience != null) {
    form.projectExperience = String(patch.projectExperience)
  }
  if (patch.skills != null) {
    form.skills = String(patch.skills)
  }
  if (patch.selfEvaluation != null) {
    form.selfEvaluation = String(patch.selfEvaluation)
  }
  if (patch.isDefault != null) {
    form.isDefault = Boolean(patch.isDefault)
  }
  if (patch.disabilityType != null) {
    form.disabilityTypeList = parseDisabilityTypesFromStorage(String(patch.disabilityType))
  }
  if (patch.disabilityLevel != null) {
    form.disabilityLevel = normalizeDisabilityLevelFromStorage(String(patch.disabilityLevel))
  }
}

async function onVoiceInterpret(vo) {
  if (vo.resume && Object.keys(vo.resume).length) {
    mergeResumeFromAi(vo.resume)
    ElMessage.success('AI 已填入简历草稿，请核对后保存')
  }
}

let unregisterVoicePage = () => {}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    const payload = {
      title: form.title,
      disabilityType: serializeDisabilityTypesForStorage(form.disabilityTypeList),
      disabilityLevel: form.disabilityLevel || '',
      education: form.education,
      experience: form.experience,
      projectExperience: form.projectExperience,
      skills: form.skills,
      selfEvaluation: form.selfEvaluation,
      isDefault: form.isDefault
    }

    if (isEdit.value) {
      await updateResume(resumeId.value, payload)
      ElMessage.success('简历更新成功')
      await router.push({ name: 'resumeList', query: { _rid: String(resumeId.value) } })
    } else {
      await createResume(payload)
      ElMessage.success('简历新增成功')
      await router.push('/jobseeker/resumes')
    }
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  unregisterVoicePage = registerJobSeekerVoicePage(route.name, {
    getPageContext: () => ({
      mode: isEdit.value ? 'edit' : 'create',
      resumeId: isEdit.value ? resumeId.value : null,
      titlePreview: form.title
    }),
    onInterpret: onVoiceInterpret
  })
  loadDetail()
})

onUnmounted(() => {
  unregisterVoicePage()
})
</script>

<style scoped>
.full-width-select {
  width: 100%;
}

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

.preview-line {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}
</style>
