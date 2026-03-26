<template>
  <section class="enterprise-job-page">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <div class="section-header">
          <h2>发布岗位</h2>
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

        <el-form-item label="残疾适配类型" prop="disabilitySupportType" for="disabilitySupportType">
          <el-input
            id="disabilitySupportType"
            v-model="form.disabilitySupportType"
            placeholder="如：听力障碍、肢体障碍（可多项）"
          />
        </el-form-item>

        <el-form-item label="福利待遇" prop="welfare" for="welfare">
          <el-input id="welfare" v-model="form.welfare" type="textarea" :rows="2" placeholder="如：五险一金、弹性工时" />
        </el-form-item>

        <el-form-item label="岗位描述" prop="jobDescription" for="jobDescription">
          <el-input id="jobDescription" v-model="form.jobDescription" type="textarea" :rows="5" placeholder="请填写岗位职责和说明" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSubmit">发布岗位</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createJob } from '../../api/jobs'

const router = useRouter()
const formRef = ref()
const saving = ref(false)

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
  disabilitySupportType: '',
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
      disabilitySupportType: form.disabilitySupportType,
      welfare: form.welfare,
      jobDescription: form.jobDescription
    }
    await createJob(payload)
    ElMessage.success('岗位发布成功，默认状态为草稿')
    await router.push('/enterprise/jobs')
  } finally {
    saving.value = false
  }
}
</script>
