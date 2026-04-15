<template>
  <section class="application-page">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <div class="section-header">
          <h2>我的投递</h2>
          <el-button @click="goJobs">继续投递</el-button>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="applications"
        border
        empty-text="暂无投递记录，去岗位列表投递试试吧"
        row-key="id"
      >
        <el-table-column type="expand" width="48">
          <template #default="{ row }">
            <div class="application-expand" role="region" :aria-label="`「${row.jobTitle}」投递详情`">
              <p><span class="k">企业回复</span>{{ row.remark || '—' }}</p>
              <p><span class="k">面试时间</span>{{ formatDateTime(row.interviewTime) || '—' }}</p>
              <p><span class="k">面试地址</span>{{ row.interviewAddress || '—' }}</p>
              <p><span class="k">HR 联系方式</span>{{ row.hrContact || '—' }}</p>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="jobTitle" label="岗位名称" min-width="160" />
        <el-table-column prop="enterpriseName" label="企业名称" min-width="150" />
        <el-table-column prop="resumeName" label="投递简历" min-width="120" />
        <el-table-column label="求职进度" width="120">
          <template #default="{ row }">
            <el-tag :type="applicationProgressTagType(row.status)">
              {{ applicationProgressText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理时间" min-width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.handledTime) || '—' }}
          </template>
        </el-table-column>
        <el-table-column label="投递时间" min-width="170">
          <template #default="{ row }">
            {{ formatDateTime(row.applyTime) || '—' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyApplications } from '../../api/applications'
import { formatDateTime } from '../../utils/datetime'
import { applicationProgressTagType, applicationProgressText } from '../../utils/applicationStatus'
import { registerJobSeekerVoicePage } from '../../voice/jobSeekerVoiceRegistry'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const applications = ref([])

function goJobs() {
  router.push('/jobs')
}

async function loadMyApplications() {
  loading.value = true
  try {
    const response = await getMyApplications()
    applications.value = response.data || []
  } catch (error) {
    ElMessage.error('加载投递记录失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

let unregisterVoicePage = () => {}

onMounted(() => {
  unregisterVoicePage = registerJobSeekerVoicePage(route.name, {
    getPageContext: () => ({
      applications: applications.value.map((row, index) => ({
        index: index + 1,
        jobTitle: row.jobTitle,
        enterpriseName: row.enterpriseName,
        status: row.status,
        resumeName: row.resumeName
      }))
    }),
    onInterpret: () => {}
  })
  loadMyApplications()
})

onUnmounted(() => {
  unregisterVoicePage()
})
</script>

<style scoped>
.application-expand {
  padding: 8px 12px 12px 40px;
  line-height: 1.65;
  max-width: min(1200px, 100%);
}
.application-expand p {
  margin: 0 0 6px;
}
.application-expand .k {
  display: inline-block;
  min-width: 7em;
  color: var(--el-text-color-secondary);
  font-weight: 500;
}
.application-expand .k::after {
  content: '：';
}
</style>
