<template>
  <section class="recommend-page">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <div class="section-header">
          <h2>推荐岗位</h2>
          <el-button @click="loadRecommendJobs" :loading="loading">刷新推荐</el-button>
        </div>
      </template>

      <el-alert
        title="推荐规则：技能匹配50分、残疾适配25分、工作方式15分、城市匹配10分。"
        type="info"
        :closable="false"
        show-icon
      />

      <el-table v-loading="loading" :data="jobs" border style="margin-top: 16px">
        <el-table-column prop="title" label="岗位名称" min-width="180" />
        <el-table-column prop="enterpriseName" label="企业名称" min-width="180" />
        <el-table-column label="匹配分数" width="130">
          <template #default="{ row }">
            <el-tag :type="scoreTagType(row.matchScore)">{{ row.matchScore }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="matchReason" label="推荐依据" min-width="320" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="goDetail(row.jobId)">查看岗位</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-empty v-if="!loading && jobs.length === 0" description="暂无推荐岗位，请先完善求职者资料" />
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getRecommendJobs } from '../../api/recommend'

const router = useRouter()
const loading = ref(false)
const jobs = ref([])

function scoreTagType(score) {
  if (score >= 70) {
    return 'success'
  }
  if (score >= 40) {
    return 'warning'
  }
  return 'info'
}

async function loadRecommendJobs() {
  loading.value = true
  try {
    const response = await getRecommendJobs()
    jobs.value = response.data || []
  } finally {
    loading.value = false
  }
}

function goDetail(jobId) {
  router.push(`/jobs/${jobId}`)
}

onMounted(() => {
  loadRecommendJobs()
})
</script>
