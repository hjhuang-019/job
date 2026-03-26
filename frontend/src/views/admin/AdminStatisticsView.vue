<template>
  <section class="admin-statistics-page" aria-label="管理员统计页面">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <div class="section-header">
          <h2>平台基础统计</h2>
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
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getAdminStatistics } from '../../api/admin'

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
