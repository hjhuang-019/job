<template>
  <section class="enterprise-job-page">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <div class="section-header">
          <h2>岗位编辑</h2>
          <el-button @click="goManage">返回岗位管理</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-row :gutter="16">
          <el-col :xs="24" :md="12">
            <el-form-item label="岗位名称" prop="title" for="title">
              <el-input id="title" v-model="form.title" placeholder="请输入岗位名称" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="12">
            <el-form-item label="岗位分类" prop="category" for="category">
              <el-input id="category" v-model="form.category" placeholder="如：软件开发/客服/运营" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :xs="24" :md="8">
            <el-form-item label="工作城市" prop="city" for="city">
              <el-input id="city" v-model="form.city" placeholder="请输入城市" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="8">
            <el-form-item label="最低薪资（元）" prop="salaryMin" for="salaryMin">
              <el-input-number id="salaryMin" v-model="form.salaryMin" :min="0" :step="1000" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="8">
            <el-form-item label="最高薪资（元）" prop="salaryMax" for="salaryMax">
              <el-input-number id="salaryMax" v-model="form.salaryMax" :min="0" :step="1000" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :xs="24" :md="8">
            <el-form-item label="学历要求" prop="educationRequirement" for="educationRequirement">
              <el-input id="educationRequirement" v-model="form.educationRequirement" placeholder="如：大专及以上" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="8">
            <el-form-item label="经验要求" prop="experienceRequirement" for="experienceRequirement">
              <el-input id="experienceRequirement" v-model="form.experienceRequirement" placeholder="如：1-3年" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :md="8">
            <el-form-item label="工作方式" prop="workMode" for="workMode">
              <el-select id="workMode" v-model="form.workMode" placeholder="请选择工作方式">
                <el-option label="线下办公" value="OFFLINE" />
                <el-option label="远程办公" value="REMOTE" />
                <el-option label="混合办公" value="HYBRID" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="技能要求" prop="skillRequirements" for="skillRequirements">
          <el-input
            id="skillRequirements"
            v-model="form.skillRequirements"
            type="textarea"
            :rows="3"
            placeholder="请填写技能要求"
          />
        </el-form-item>

        <el-form-item label="适合招收的残疾类型" prop="disabilitySupportTypeList" for="disabilitySupportType">
          <el-select
            id="disabilitySupportType"
            v-model="form.disabilitySupportTypeList"
            multiple
            filterable
            placeholder="请选择本岗位适合的残疾类型（可多选）"
            class="job-disability-select"
          >
            <el-option
              v-for="opt in DISABILITY_TYPE_OPTIONS"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
          <p class="job-disability-hint" role="note">
            可多选；保存后以逗号拼接写入岗位要求，供求职者筛选与推荐匹配。
          </p>
        </el-form-item>

        <el-form-item label="福利待遇" prop="welfare" for="welfare">
          <el-input id="welfare" v-model="form.welfare" type="textarea" :rows="2" placeholder="如：五险一金、弹性工时" />
        </el-form-item>

        <el-form-item label="岗位描述" prop="jobDescription" for="jobDescription">
          <el-input id="jobDescription" v-model="form.jobDescription" type="textarea" :rows="5" placeholder="请填写岗位职责和说明" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSubmit">保存岗位</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getJobDetail, updateJob } from '../../api/jobs'
import {
  DISABILITY_TYPE_OPTIONS,
  parseDisabilityTypesFromStorage,
  serializeDisabilityTypesForStorage
} from '../../constants/disability'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const saving = ref(false)

const jobId = computed(() => Number(route.params.id))

const form = reactive({
  title: '',
  category: '',
  city: '',
  salaryMin: 0,
  salaryMax: 0,
  educationRequirement: '',
  experienceRequirement: '',
  workMode: 'OFFLINE',
  skillRequirements: '',
  disabilitySupportTypeList: [],
  welfare: '',
  jobDescription: ''
})

const rules = {
  title: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  city: [{ required: true, message: '请输入工作城市', trigger: 'blur' }],
  salaryMin: [{ required: true, message: '请输入最低薪资', trigger: 'change' }],
  salaryMax: [{ required: true, message: '请输入最高薪资', trigger: 'change' }],
  workMode: [{ required: true, message: '请选择工作方式', trigger: 'change' }]
}

function fillForm(data = {}) {
  form.title = data.title || ''
  form.category = data.category || ''
  form.city = data.city || ''
  form.salaryMin = Number(data.salaryMin || 0)
  form.salaryMax = Number(data.salaryMax || 0)
  form.educationRequirement = data.educationRequirement || ''
  form.experienceRequirement = data.experienceRequirement || ''
  form.workMode = data.workMode || 'OFFLINE'
  form.skillRequirements = data.skillRequirements || ''
  form.disabilitySupportTypeList = parseDisabilityTypesFromStorage(data.disabilitySupportType || '')
  form.welfare = data.welfare || ''
  form.jobDescription = data.jobDescription || ''
}

async function loadDetail() {
  if (!Number.isFinite(jobId.value) || jobId.value <= 0) {
    return
  }
  const response = await getJobDetail(jobId.value)
  fillForm(response.data || {})
}

function goManage() {
  router.push('/enterprise/jobs')
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }
  if (form.salaryMin > form.salaryMax) {
    ElMessage.warning('最低薪资不能高于最高薪资')
    return
  }

  saving.value = true
  try {
    const payload = {
      title: form.title,
      category: form.category,
      city: form.city,
      salaryMin: form.salaryMin,
      salaryMax: form.salaryMax,
      educationRequirement: form.educationRequirement,
      experienceRequirement: form.experienceRequirement,
      workMode: form.workMode,
      skillRequirements: form.skillRequirements,
      disabilitySupportType: serializeDisabilityTypesForStorage(form.disabilitySupportTypeList),
      welfare: form.welfare,
      jobDescription: form.jobDescription
    }
    await updateJob(jobId.value, payload)
    ElMessage.success('岗位更新成功')
    await router.push('/enterprise/jobs')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.job-disability-select {
  width: 100%;
}

.job-disability-hint {
  margin: 8px 0 0;
  font-size: 13px;
  line-height: 1.55;
  color: var(--el-text-color-secondary);
}
</style>
