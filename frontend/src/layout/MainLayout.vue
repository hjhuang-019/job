<template>
  <div class="app-shell">
    <a class="skip-link" href="#main-content">跳转到主要内容</a>

    <header class="app-header">
      <div class="container header-content">
        <div>
          <h1 class="site-title">残疾人就业 Web 平台</h1>
          <p class="site-subtitle">面向求职者、企业与管理员的就业服务基础骨架</p>
        </div>
        <nav aria-label="主导航">
          <RouterLink class="nav-link" to="/">首页</RouterLink>
          <RouterLink v-if="authStore.userRole === 'JOB_SEEKER'" class="nav-link" to="/jobseeker/profile">
            个人中心
          </RouterLink>
          <RouterLink v-if="authStore.userRole === 'JOB_SEEKER'" class="nav-link" to="/jobseeker/verify">
            求职者认证
          </RouterLink>
          <RouterLink v-if="authStore.userRole === 'JOB_SEEKER'" class="nav-link" to="/jobseeker/resumes">
            简历管理
          </RouterLink>
          <RouterLink v-if="authStore.userRole === 'JOB_SEEKER'" class="nav-link" to="/jobs">
            岗位浏览
          </RouterLink>
          <RouterLink v-if="authStore.userRole === 'JOB_SEEKER'" class="nav-link" to="/recommend/jobs">
            推荐岗位
          </RouterLink>
          <RouterLink v-if="authStore.userRole === 'JOB_SEEKER'" class="nav-link" to="/applications/my">
            我的投递
          </RouterLink>
          <RouterLink v-if="authStore.userRole === 'ENTERPRISE'" class="nav-link" to="/enterprise/profile">
            企业中心
          </RouterLink>
          <RouterLink v-if="authStore.userRole === 'ENTERPRISE'" class="nav-link" to="/enterprise/verify">
            企业认证
          </RouterLink>
          <RouterLink v-if="authStore.userRole === 'ENTERPRISE'" class="nav-link" to="/enterprise/jobs/publish">
            发布岗位
          </RouterLink>
          <RouterLink v-if="authStore.userRole === 'ENTERPRISE'" class="nav-link" to="/enterprise/jobs">
            岗位管理
          </RouterLink>
          <RouterLink v-if="authStore.userRole === 'ADMIN'" class="nav-link" to="/admin/audits">
            认证审核
          </RouterLink>
          <RouterLink v-if="authStore.userRole === 'ADMIN'" class="nav-link" to="/admin/statistics">
            平台统计
          </RouterLink>
          <RouterLink v-if="authStore.isLoggedIn" class="nav-link" to="/messages">
            <el-badge :value="unreadCount" :hidden="unreadCount <= 0" :max="99">
              <span>消息中心</span>
            </el-badge>
          </RouterLink>
          <RouterLink v-if="!authStore.isLoggedIn" class="nav-link" to="/login">登录</RouterLink>
          <a
            v-if="authStore.isLoggedIn"
            href="#"
            class="nav-link"
            role="button"
            @click.prevent="handleLogout"
          >
            退出登录
          </a>
        </nav>
        <section class="accessibility-tools" aria-label="无障碍设置">
          <label for="fontSizeMode">字体大小</label>
          <el-select
            id="fontSizeMode"
            :model-value="accessibilityStore.fontSizeMode"
            style="width: 120px"
            @change="handleFontSizeChange"
          >
            <el-option label="标准" value="normal" />
            <el-option label="大字体" value="large" />
          </el-select>
          <el-switch
            :model-value="accessibilityStore.highContrast"
            active-text="高对比度"
            inactive-text="默认主题"
            @change="handleContrastChange"
          />
          <el-switch
            :model-value="accessibilityStore.focusHighlight"
            active-text="焦点高亮"
            inactive-text="焦点普通"
            @change="handleFocusHighlightChange"
          />
          <el-button disabled type="info" aria-label="语音播报功能预留，当前未启用">语音播报（预留）</el-button>
        </section>
      </div>
    </header>

    <main id="main-content" class="app-main container" tabindex="-1">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { getUnreadCount } from '../api/messages'
import { useAccessibilityStore } from '../stores/accessibility'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const accessibilityStore = useAccessibilityStore()
const unreadCount = ref(0)

async function loadUnreadCount() {
  if (!authStore.isLoggedIn) {
    unreadCount.value = 0
    return
  }
  try {
    const response = await getUnreadCount()
    unreadCount.value = Number(response.data?.count || 0)
  } catch (error) {
    unreadCount.value = 0
  }
}

async function handleLogout() {
  authStore.clearAuth()
  unreadCount.value = 0
  ElMessage.success('已退出登录')
  await router.push('/login')
}

function onMessageUpdated() {
  loadUnreadCount()
}

function handleFontSizeChange(value) {
  accessibilityStore.setFontSizeMode(value)
}

function handleContrastChange(value) {
  accessibilityStore.setHighContrast(value)
}

function handleFocusHighlightChange(value) {
  accessibilityStore.setFocusHighlight(value)
}

watch(
  () => route.fullPath,
  () => {
    loadUnreadCount()
  }
)

onMounted(() => {
  loadUnreadCount()
  window.addEventListener('message-updated', onMessageUpdated)
})

onBeforeUnmount(() => {
  window.removeEventListener('message-updated', onMessageUpdated)
})
</script>
