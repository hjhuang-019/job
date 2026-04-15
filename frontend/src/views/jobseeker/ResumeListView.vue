<template>
  <section class="resume-page">
    <el-row :gutter="16">
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="hero-card">
          <template #header>
            <div class="section-header">
              <h2>简历列表</h2>
              <el-button type="primary" @click="goCreate">新增简历</el-button>
            </div>
          </template>

          <el-table
            v-loading="loading"
            :data="resumeList"
            border
            empty-text="暂无简历，请先点击“新增简历”创建"
            @row-click="selectResume"
            style="width: 100%"
          >
            <el-table-column prop="title" label="标题" min-width="160" />
            <el-table-column label="残疾类型" min-width="200">
              <template #default="{ row }">
                <DisabilityTypeTags :value="row.disabilityType || ''" />
              </template>
            </el-table-column>
            <el-table-column label="残疾等级" width="100" align="center">
              <template #default="{ row }">
                <DisabilityLevelTag :value="row.disabilityLevel || ''" />
              </template>
            </el-table-column>
            <el-table-column label="默认简历" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.isDefault" type="success">默认</el-tag>
                <span v-else>否</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="260" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="goEdit(row.id)">编辑</el-button>
                <el-button link type="warning" :disabled="row.isDefault" @click.stop="handleSetDefault(row)">
                  设为默认
                </el-button>
                <el-button link type="danger" @click.stop="handleDelete(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="status-card">
          <template #header>
            <h2>简历预览</h2>
          </template>

          <template v-if="selectedResume">
            <p><strong>标题：</strong>{{ selectedResume.title }}</p>
            <p class="preview-tags-row">
              <strong>残疾类型：</strong>
              <DisabilityTypeTags :value="selectedResume.disabilityType || ''" />
            </p>
            <p class="preview-tags-row">
              <strong>残疾等级：</strong>
              <DisabilityLevelTag :value="selectedResume.disabilityLevel || ''" />
            </p>
            <p><strong>教育经历：</strong>{{ selectedResume.education || '暂无' }}</p>
            <p><strong>工作经历：</strong>{{ selectedResume.experience || '暂无' }}</p>
            <p><strong>项目经历：</strong>{{ selectedResume.projectExperience || '暂无' }}</p>
            <p><strong>技能：</strong>{{ selectedResume.skills || '暂无' }}</p>
            <p><strong>自我评价：</strong>{{ selectedResume.selfEvaluation || '暂无' }}</p>
          </template>
          <el-empty v-else description="请选择一份简历查看预览" />
        </el-card>
      </el-col>
    </el-row>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deleteResume, getResumeList, updateResume } from '../../api/resume'
import DisabilityLevelTag from '../../components/DisabilityLevelTag.vue'
import DisabilityTypeTags from '../../components/DisabilityTypeTags.vue'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const resumeList = ref([])
const selectedResume = ref(null)

function goCreate() {
  router.push('/jobseeker/resumes/new')
}

function goEdit(id) {
  router.push(`/jobseeker/resumes/${id}/edit`)
}

function selectResume(row) {
  selectedResume.value = row
}

async function loadResumes() {
  loading.value = true
  try {
    const response = await getResumeList()
    resumeList.value = response.data || []
    const rid = route.query._rid
    if (rid != null && String(rid).trim() !== '') {
      const match = resumeList.value.find((r) => String(r.id) === String(rid))
      selectedResume.value = match || resumeList.value[0] || null
      router.replace({ name: 'resumeList', query: {} })
    } else {
      selectedResume.value = resumeList.value[0] || null
    }
  } catch (error) {
    ElMessage.error('加载简历列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function handleSetDefault(row) {
  await updateResume(row.id, {
    title: row.title,
    disabilityType: row.disabilityType || '',
    disabilityLevel: row.disabilityLevel || '',
    education: row.education,
    experience: row.experience,
    projectExperience: row.projectExperience,
    skills: row.skills,
    selfEvaluation: row.selfEvaluation,
    isDefault: true
  })
  ElMessage.success('已设置为默认简历')
  await loadResumes()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除简历《${row.title}》吗？`, '提示', {
    type: 'warning'
  })
  await deleteResume(row.id)
  ElMessage.success('删除成功')
  await loadResumes()
}

onMounted(() => {
  loadResumes()
})
</script>

<style scoped>
.preview-tags-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}
</style>
