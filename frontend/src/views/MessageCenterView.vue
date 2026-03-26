<template>
  <section class="message-page">
    <el-card shadow="never" class="hero-card">
      <template #header>
        <div class="section-header">
          <h2>消息中心</h2>
          <div>
            <el-tag type="danger">未读 {{ unreadCount }}</el-tag>
            <el-button style="margin-left: 8px" @click="loadMessages">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table
        v-loading="loading"
        :data="messages"
        border
        empty-text="暂无消息通知"
      >
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.read ? 'info' : 'danger'">{{ row.read ? '已读' : '未读' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="messageType" label="类型" width="130">
          <template #default="{ row }">{{ typeText(row.messageType) }}</template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="content" label="内容" min-width="320" />
        <el-table-column prop="createdAt" label="时间" min-width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="!row.read" type="primary" link @click="handleRead(row)">标记已读</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getMessages, getUnreadCount, markMessageRead } from '../api/messages'

const loading = ref(false)
const messages = ref([])
const unreadCount = ref(0)

function typeText(type) {
  return {
    SYSTEM: '系统通知',
    APPLY_STATUS: '投递状态',
    AUDIT_NOTICE: '审核通知'
  }[type] || type
}

async function loadMessages() {
  loading.value = true
  try {
    const [listResp, countResp] = await Promise.all([getMessages(), getUnreadCount()])
    messages.value = listResp.data || []
    unreadCount.value = Number(countResp.data?.count || 0)
  } catch (error) {
    ElMessage.error('加载消息失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

async function handleRead(row) {
  await markMessageRead(row.id)
  ElMessage.success('已标记为已读')
  await loadMessages()
  window.dispatchEvent(new Event('message-updated'))
}

onMounted(() => {
  loadMessages()
})
</script>
