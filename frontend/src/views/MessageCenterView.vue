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

      <el-alert
        v-if="authStore.userRole === 'ADMIN' && pendingVerifyTotal > 0"
        type="warning"
        :closable="false"
        show-icon
        class="message-admin-pending-alert"
        role="status"
      >
        当前有 <strong>{{ pendingVerifyTotal }}</strong> 条待处理认证，请前往
        <RouterLink class="message-admin-pending-link" to="/admin/audits">认证审核</RouterLink>
        处理。
      </el-alert>

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
        <el-table-column prop="content" label="内容" min-width="320">
          <template #default="{ row }">
            <EnterpriseApplyMessageContent :row="row" :user-role="authStore.userRole" />
          </template>
        </el-table-column>
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
import { onMounted, onUnmounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMessages, getUnreadCount, markMessageRead } from '../api/messages'
import { getAdminPendingVerifyCount } from '../api/admin'
import { useAuthStore } from '../stores/auth'
import { registerJobSeekerVoicePage } from '../voice/jobSeekerVoiceRegistry'
import EnterpriseApplyMessageContent from '../components/EnterpriseApplyMessageContent.vue'

const route = useRoute()
const authStore = useAuthStore()
const loading = ref(false)
const messages = ref([])
const unreadCount = ref(0)
const pendingVerifyTotal = ref(0)

function typeText(type) {
  return {
    SYSTEM: '系统通知',
    APPLY_STATUS: '投递状态',
    APPLY_RECEIVED: '新简历投递',
    AUDIT_NOTICE: '审核通知',
    USER_FEEDBACK: '用户反馈'
  }[type] || type
}

async function loadPendingVerifyHint() {
  if (authStore.userRole !== 'ADMIN') {
    pendingVerifyTotal.value = 0
    return
  }
  try {
    const resp = await getAdminPendingVerifyCount()
    pendingVerifyTotal.value = Number(resp.data?.total ?? 0)
  } catch (error) {
    pendingVerifyTotal.value = 0
  }
}

async function loadMessages() {
  loading.value = true
  try {
    const promises = [getMessages(), getUnreadCount()]
    if (authStore.userRole === 'ADMIN') {
      promises.push(getAdminPendingVerifyCount())
    }
    const results = await Promise.all(promises)
    messages.value = results[0].data || []
    unreadCount.value = Number(results[1].data?.count || 0)
    if (authStore.userRole === 'ADMIN' && results[2]) {
      pendingVerifyTotal.value = Number(results[2].data?.total ?? 0)
    } else {
      pendingVerifyTotal.value = 0
    }
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

let unregisterVoicePage = () => {}

function onAdminPendingUpdated() {
  loadPendingVerifyHint()
}

onMounted(() => {
  if (authStore.userRole === 'JOB_SEEKER') {
    unregisterVoicePage = registerJobSeekerVoicePage(route.name, {
      getPageContext: () => ({
        unread: unreadCount.value,
        messages: messages.value.slice(0, 10).map((m) => ({
          title: m.title,
          read: m.read,
          type: m.messageType
        }))
      }),
      onInterpret: () => {}
    })
  }
  loadMessages()
  window.addEventListener('admin-pending-updated', onAdminPendingUpdated)
})

onUnmounted(() => {
  unregisterVoicePage()
  window.removeEventListener('admin-pending-updated', onAdminPendingUpdated)
})
</script>

<style scoped>
.message-admin-pending-alert {
  margin-bottom: 16px;
}

.message-admin-pending-link {
  font-weight: 600;
  text-decoration: underline;
}
</style>
