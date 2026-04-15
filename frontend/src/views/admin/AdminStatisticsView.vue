<template>
  <section class="admin-statistics-page" aria-label="管理员统计页面">
    <el-card shadow="never" class="hero-card stats-hero-card">
      <template #header>
        <div class="section-header">
          <h2>平台基础数据统计</h2>
          <el-button :loading="loading" @click="loadStatistics">刷新统计</el-button>
        </div>
      </template>

      <el-row :gutter="16">
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover">
            <p>求职者用户数</p>
            <h3>{{ statistics.userCount }}</h3>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover">
            <p>企业用户数</p>
            <h3>{{ statistics.enterpriseCount }}</h3>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover">
            <p>岗位总数</p>
            <h3>{{ statistics.jobCount }}</h3>
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="12" :md="6">
          <el-card shadow="hover">
            <p>投递总数</p>
            <h3>{{ statistics.applicationCount }}</h3>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <AdminUserArchivePanel />
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getAdminStatistics } from '../../api/admin'
import AdminUserArchivePanel from './AdminUserArchivePanel.vue'

const loading = ref(false)
const statistics = reactive({
  userCount: 0,
  enterpriseCount: 0,
  jobCount: 0,
  applicationCount: 0
})

async function loadStatistics() {
  loading.value = true
  try {
    const response = await getAdminStatistics()
    statistics.userCount = Number(response.data?.userCount || 0)
    statistics.enterpriseCount = Number(response.data?.enterpriseCount || 0)
    statistics.jobCount = Number(response.data?.jobCount || 0)
    statistics.applicationCount = Number(response.data?.applicationCount || 0)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.admin-statistics-page {
  width: 100%;
  max-width: min(var(--jp-content-max), 100%);
  margin: 0 auto;
}

.hero-card {
  border-radius: 12px;
}

.stats-hero-card :deep(.section-header) {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.stats-hero-card h2 {
  margin: 0;
  font-size: 1.25rem;
}
</style>
