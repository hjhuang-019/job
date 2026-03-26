<template>
  <section class="home-page" aria-label="首页内容区">
    <el-row :gutter="20">
      <el-col :xs="24" :md="14">
        <el-card shadow="never" class="hero-card">
          <template #header>
            <h2>登录状态</h2>
          </template>
          <p>当前用户：{{ authStore.userInfo?.realName || authStore.userInfo?.username || '未获取' }}</p>
          <p>当前角色：{{ roleText }}</p>
          <img src="/accessibility-banner.svg" alt="无障碍支持示意图：字体、对比度、键盘导航" width="260" />
          <el-button type="success" :loading="loadingUser" @click="handleLoadUser">
            获取当前用户信息
          </el-button>
        </el-card>
      </el-col>

      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="hero-card">
          <template #header>
            <h2>项目骨架已创建</h2>
          </template>
          <p>
            这是残疾人就业 Web 平台的基础首页，已接入 Vue3、Vite、Element Plus、
            Pinia、Vue Router 与 Axios。
          </p>
          <p>
            当前页面提供一个后端连通性测试按钮，用于验证前后端项目是否正常启动。
          </p>
          <el-button type="primary" @click="handlePing" :loading="loading">
            测试后端接口
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :md="24">
        <el-card shadow="never" class="status-card">
          <template #header>
            <h2>接口响应结果</h2>
          </template>

          <el-form label-position="top" aria-label="接口测试结果">
            <el-form-item label="接口状态">
              <el-input :model-value="result.statusText" readonly aria-readonly="true" />
            </el-form-item>
            <el-form-item label="返回消息">
              <el-input :model-value="result.message" readonly aria-readonly="true" />
            </el-form-item>
            <el-form-item label="返回数据">
              <el-input
                :model-value="result.dataText"
                type="textarea"
                :rows="6"
                readonly
                aria-readonly="true"
              />
            </el-form-item>
          </el-form>
          <p class="sr-only" aria-live="polite">当前接口状态：{{ result.statusText }}，消息：{{ result.message }}</p>
        </el-card>
      </el-col>
    </el-row>
  </section>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { pingServer } from '../../api/test'
import { useAuthStore } from '../../stores/auth'

const loading = ref(false)
const loadingUser = ref(false)
const authStore = useAuthStore()
const result = reactive({
  statusText: '未请求',
  message: '点击按钮后显示',
  dataText: ''
})

const roleText = computed(() => {
  return {
    JOB_SEEKER: '求职者',
    ENTERPRISE: '企业',
    ADMIN: '管理员'
  }[authStore.userRole] || '未知'
})

async function handleLoadUser() {
  loadingUser.value = true
  try {
    await authStore.fetchCurrentUser()
  } finally {
    loadingUser.value = false
  }
}

async function handlePing() {
  loading.value = true
  try {
    const response = await pingServer()
    result.statusText = response.code === 200 ? '成功' : '失败'
    result.message = response.message
    result.dataText = JSON.stringify(response.data, null, 2)
  } catch (error) {
    result.statusText = '失败'
    result.message = error.response?.data?.message || '请求异常'
    result.dataText = ''
  } finally {
    loading.value = false
  }
}
</script>
