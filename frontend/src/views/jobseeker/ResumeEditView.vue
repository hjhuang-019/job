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
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createResume, getResumeDetail, updateResume } from '../../api/resume'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const saving = ref(false)

const resumeId = computed(() => Number(route.params.id))
const isEdit = computed(() => Number.isFinite(resumeId.value) && resumeId.value > 0)

const form = reactive({
  title: '',
  education: '',
  experience: '',
  projectExperience: '',
  skills: '',
  selfEvaluation: '',
  isDefault: false
})

const rules = {
  title: [
    { required: true, message: '请输入简历标题', trigger: 'blur' },
    { max: 100, message: '标题长度不能超过100位', trigger: 'blur' }
  ]
}

function fillForm(data = {}) {
  form.title = data.title || ''
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

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) {
    return
  }

  saving.value = true
  try {
    const payload = {
      title: form.title,
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
    } else {
      await createResume(payload)
      ElMessage.success('简历新增成功')
    }
    await router.push('/jobseeker/resumes')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>
