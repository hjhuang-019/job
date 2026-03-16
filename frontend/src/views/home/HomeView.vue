<template>
  <section class="home-page">
    <el-row :gutter="20">
      <el-col :xs="24" :md="14">
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

      <el-col :xs="24" :md="10">
        <el-card shadow="never" class="status-card">
          <template #header>
            <h2>接口响应结果</h2>
          </template>

          <el-form label-position="top">
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
        </el-card>
      </el-col>
    </el-row>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { pingServer } from '../../api/test'

const loading = ref(false)
const result = reactive({
  statusText: '未请求',
  message: '点击按钮后显示',
  dataText: ''
})

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
