<template>
  <div class="app-shell">
    <a class="skip-link" href="#main-content">跳转到主要内容</a>

    <header class="app-header">
      <div class="container header-content">
        <div class="header-brand">
          <h1 class="site-title">残疾人就业 Web 平台</h1>
          <p class="site-subtitle">面向求职者、企业与管理员的无障碍就业服务平台</p>
        </div>
        <nav class="main-nav" aria-label="主导航">
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
          <RouterLink v-if="authStore.userRole === 'ADMIN'" class="nav-link nav-link-badge-wrap" to="/admin/audits">
            <el-badge :value="pendingAuditTotal" :hidden="pendingAuditTotal <= 0" :max="99">
              <span class="nav-badge-anchor">认证审核</span>
            </el-badge>
          </RouterLink>
          <RouterLink v-if="authStore.userRole === 'ADMIN'" class="nav-link" to="/admin/feedback">
            用户反馈
          </RouterLink>
          <RouterLink v-if="authStore.userRole === 'ADMIN'" class="nav-link" to="/admin/statistics">
            平台统计
          </RouterLink>
          <RouterLink v-if="authStore.isLoggedIn" class="nav-link nav-link-badge-wrap" to="/messages">
            <el-badge
              :value="messageNavBadgeValue"
              :is-dot="messageNavBadgeIsDot"
              :hidden="messageNavBadgeHidden"
              :max="99"
            >
              <span class="nav-badge-anchor">消息中心</span>
            </el-badge>
          </RouterLink>
          <el-dropdown v-if="authStore.isLoggedIn" trigger="click" @command="handleUserMenuCommand">
            <button type="button" class="nav-link nav-user-trigger" aria-label="用户菜单">
              <span>{{ currentUserName }}</span>
              <strong>{{ roleText }}</strong>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-if="authStore.userRole === 'JOB_SEEKER' || authStore.userRole === 'ENTERPRISE'"
                  command="contactAdmin"
                >
                  联系管理员
                </el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <RouterLink v-if="!authStore.isLoggedIn" class="nav-link" to="/login">登录</RouterLink>
        </nav>
        <div class="header-actions">
          <section class="accessibility-tools" aria-label="无障碍设置">
            <label for="uiScalePercent">页面与字体</label>
            <el-select
              id="uiScalePercent"
              :model-value="accessibilityStore.uiScalePercent"
              style="width: 148px"
              @change="handleUiScaleChange"
            >
              <el-option v-for="opt in uiScalePresets" :key="opt.value" :label="opt.label" :value="opt.value" />
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
            <el-switch
              :model-value="accessibilityStore.voiceEnabled"
              :disabled="!speechSupported"
              active-text="语音播报开"
              inactive-text="语音播报关"
              @change="handleVoiceChange"
            />
            <label for="voiceRate">语速</label>
            <el-input-number
              id="voiceRate"
              :model-value="accessibilityStore.voiceRate"
              :disabled="!speechSupported || !accessibilityStore.voiceEnabled"
              :min="0.8"
              :max="1.6"
              :step="0.1"
              :precision="1"
              controls-position="right"
              style="width: 112px"
              @change="handleVoiceRateChange"
            />
            <label for="voiceVolume">音量</label>
            <el-input-number
              id="voiceVolume"
              :model-value="accessibilityStore.voiceVolume"
              :disabled="!speechSupported || !accessibilityStore.voiceEnabled"
              :min="0"
              :max="1"
              :step="0.1"
              :precision="1"
              controls-position="right"
              style="width: 112px"
              @change="handleVoiceVolumeChange"
            />
            <el-button
              type="info"
              :disabled="!speechSupported || !accessibilityStore.voiceEnabled"
              aria-label="测试语音播报"
              @click="handleVoicePreview"
            >
              试播报
            </el-button>
          </section>
        </div>
      </div>
    </header>

    <main id="main-content" class="app-main container" tabindex="-1">
      <el-alert
        v-if="authStore.isLoggedIn && authStore.isBlacklisted && authStore.userRole !== 'ADMIN'"
        type="warning"
        show-icon
        class="blacklist-banner"
        title="账号已被列入黑名单"
        :closable="false"
        role="status"
      >
        <template #default>
          <span v-if="authStore.userRole === 'JOB_SEEKER'">
            您无法再使用简历投递功能。详情请查看消息中心；如需解除，请联系管理员处理。
          </span>
          <span v-else-if="authStore.userRole === 'ENTERPRISE'">
            您无法新建或上架岗位，在招岗位对求职者不可见。详情请查看消息中心；如需解除，请联系管理员处理。
          </span>
          <span v-else>部分功能已受限，请查看消息中心或联系管理员。</span>
        </template>
      </el-alert>
      <RouterView />
    </main>

    <JobSeekerVoiceAssistant
      v-if="authStore.userRole === 'JOB_SEEKER' && authStore.isLoggedIn"
      ref="jobSeekerVoiceAssistantRef"
    />
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, provide, ref, watch, watchEffect } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'
import { getUnreadCount } from '../api/messages'
import { getAdminPendingVerifyCount } from '../api/admin'
import { useAccessibilityStore, UI_SCALE_PRESETS } from '../stores/accessibility'
import { cancelSpeech, isSpeechSupported, speakHomeOverview, speakMainContentDetails, speakText } from '../accessibility/speech'
import JobSeekerVoiceAssistant from '../components/JobSeekerVoiceAssistant.vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const jobSeekerVoiceAssistantRef = ref(null)

function openJobSeekerVoiceAssistant() {
  jobSeekerVoiceAssistantRef.value?.openAssistant?.()
}

provide('openJobSeekerVoiceAssistant', openJobSeekerVoiceAssistant)

const voiceAssistantDrawerOpen = ref(false)
watchEffect(() => {
  const inst = jobSeekerVoiceAssistantRef.value
  voiceAssistantDrawerOpen.value = Boolean(inst?.drawerOpen?.value)
})
provide('voiceAssistantDrawerOpen', voiceAssistantDrawerOpen)
const accessibilityStore = useAccessibilityStore()
const uiScalePresets = UI_SCALE_PRESETS
const unreadCount = ref(0)
const unreadInitialized = ref(false)
const pendingAuditTotal = ref(0)

const messageNavBadgeIsDot = computed(
  () => authStore.userRole === 'ADMIN' && unreadCount.value <= 0 && pendingAuditTotal.value > 0
)
const messageNavBadgeValue = computed(() => {
  if (messageNavBadgeIsDot.value) {
    return undefined
  }
  return unreadCount.value > 0 ? unreadCount.value : undefined
})
const messageNavBadgeHidden = computed(() => {
  if (unreadCount.value > 0) {
    return false
  }
  if (authStore.userRole === 'ADMIN' && pendingAuditTotal.value > 0) {
    return false
  }
  return true
})
const speechSupported = ref(isSpeechSupported())
let detailAnnounceTimerA = 0
let detailAnnounceTimerB = 0
const roleText = computed(() => {
  return {
    JOB_SEEKER: '求职者',
    ENTERPRISE: '企业用户',
    ADMIN: '管理员'
  }[authStore.userRole] || '平台用户'
})
const currentUserName = computed(() => {
  return authStore.userInfo?.realName || authStore.userInfo?.username || '当前用户'
})

async function loadUnreadCount() {
  if (!authStore.isLoggedIn) {
    unreadCount.value = 0
    unreadInitialized.value = false
    return
  }
  const previousUnread = unreadCount.value
  try {
    const response = await getUnreadCount()
    unreadCount.value = Number(response.data?.count || 0)
  } catch (error) {
    unreadCount.value = 0
  }
  if (unreadInitialized.value && unreadCount.value !== previousUnread) {
    if (unreadCount.value > 0) {
      speakWithSettings(`消息中心当前有 ${unreadCount.value} 条未读消息`, {
        interrupt: false,
        minIntervalMs: 1200
      })
    } else {
      speakWithSettings('消息中心未读消息已清空', { interrupt: false, minIntervalMs: 1200 })
    }
  }
  unreadInitialized.value = true
}

async function loadPendingAuditCount() {
  if (!authStore.isLoggedIn || authStore.userRole !== 'ADMIN') {
    pendingAuditTotal.value = 0
    return
  }
  try {
    const response = await getAdminPendingVerifyCount()
    pendingAuditTotal.value = Number(response.data?.total ?? 0)
  } catch (error) {
    pendingAuditTotal.value = 0
  }
}

function refreshHeaderCounts() {
  loadUnreadCount()
  loadPendingAuditCount()
}

async function handleLogout() {
  speakWithSettings('已退出登录', { force: true })
  authStore.clearAuth()
  unreadCount.value = 0
  unreadInitialized.value = false
  pendingAuditTotal.value = 0
  cancelSpeech()
  ElMessage.success('已退出登录')
  await router.push('/login')
}

function onMessageUpdated() {
  refreshHeaderCounts()
}

function onAdminPendingUpdated() {
  loadPendingAuditCount()
}

function handleUserMenuCommand(command) {
  if (command === 'contactAdmin') {
    router.push('/feedback/contact-admin')
    return
  }
  if (command === 'logout') {
    handleLogout()
  }
}

function handleUiScaleChange(value) {
  if (value == null || value === '') {
    return
  }
  accessibilityStore.setUiScalePercent(Number(value))
}

function handleContrastChange(value) {
  accessibilityStore.setHighContrast(value)
}

function handleFocusHighlightChange(value) {
  accessibilityStore.setFocusHighlight(value)
}

function speakWithSettings(text, options = {}) {
  if (!speechSupported.value || !accessibilityStore.voiceEnabled) {
    return false
  }
  return speakText(text, {
    ...options,
    rate: accessibilityStore.voiceRate,
    volume: accessibilityStore.voiceVolume
  })
}

function getRoleMenus() {
  if (!authStore.isLoggedIn) {
    return ['首页', '登录']
  }
  if (authStore.userRole === 'JOB_SEEKER') {
    return ['首页', '个人中心', '求职者认证', '简历管理', '岗位浏览', '推荐岗位', '我的投递', '消息中心']
  }
  if (authStore.userRole === 'ENTERPRISE') {
    return ['首页', '企业中心', '企业认证', '发布岗位', '岗位管理', '消息中心']
  }
  if (authStore.userRole === 'ADMIN') {
    return ['首页', '认证审核', '用户反馈', '平台统计', '消息中心']
  }
  return ['首页', '消息中心']
}

function announceModuleAndCurrentPage() {
  const menuText = getRoleMenus().join('、')
  const pageTitle = route.meta?.title || '首页'
  speakWithSettings(`模块目录：${menuText}。当前页面：${pageTitle}`, { minIntervalMs: 700 })
}

function clearDetailAnnounceTimers() {
  if (detailAnnounceTimerA) {
    window.clearTimeout(detailAnnounceTimerA)
    detailAnnounceTimerA = 0
  }
  if (detailAnnounceTimerB) {
    window.clearTimeout(detailAnnounceTimerB)
    detailAnnounceTimerB = 0
  }
}

function announcePageDetails() {
  if (!speechSupported.value || !accessibilityStore.voiceEnabled) {
    return
  }
  if (route.name === 'jobList') {
    return
  }

  const currentPath = route.fullPath
  clearDetailAnnounceTimers()
  detailAnnounceTimerA = window.setTimeout(() => {
    if (route.fullPath !== currentPath) {
      return
    }
    if (
      route.name === 'jobSeekerHome' ||
      route.name === 'enterpriseHome' ||
      route.name === 'adminHome'
    ) {
      const homeSummarySuccess = speakHomeOverview({
        rate: accessibilityStore.voiceRate,
        volume: accessibilityStore.voiceVolume
      })
      if (homeSummarySuccess) {
        return
      }
    }
    const firstTrySuccess = speakMainContentDetails({
      maxRows: 10,
      maxColumns: 5,
      rate: accessibilityStore.voiceRate,
      volume: accessibilityStore.voiceVolume
    })
    if (firstTrySuccess) {
      return
    }
    detailAnnounceTimerB = window.setTimeout(() => {
      if (route.fullPath !== currentPath) {
        return
      }
      speakMainContentDetails({
        maxRows: 10,
        maxColumns: 5,
        rate: accessibilityStore.voiceRate,
        volume: accessibilityStore.voiceVolume
      })
    }, 1200)
  }, 700)
}

function handleVoiceChange(value) {
  if (value && !speechSupported.value) {
    ElMessage.warning('当前浏览器不支持语音播报')
    accessibilityStore.setVoiceEnabled(false)
    return
  }
  accessibilityStore.setVoiceEnabled(value)
  if (!value) {
    cancelSpeech()
    return
  }
  speakText('已开启语音播报', {
    force: true,
    rate: accessibilityStore.voiceRate,
    volume: accessibilityStore.voiceVolume
  })
  announceModuleAndCurrentPage()
  announcePageDetails()
}

function handleVoiceRateChange(value) {
  accessibilityStore.setVoiceRate(value)
  speakWithSettings('语速已调整', { force: true })
}

function handleVoiceVolumeChange(value) {
  accessibilityStore.setVoiceVolume(value)
  speakWithSettings('音量已调整', { force: true })
}

function handleVoicePreview() {
  if (!speechSupported.value) {
    ElMessage.warning('当前浏览器不支持语音播报')
    return
  }
  if (!accessibilityStore.voiceEnabled) {
    ElMessage.info('请先开启语音播报')
    return
  }
  speakWithSettings('语音播报测试成功。支持页面标题与业务数据播报。', {
    force: true
  })
}

watch(
  () => route.fullPath,
  () => {
    refreshHeaderCounts()
    announceModuleAndCurrentPage()
    announcePageDetails()
  }
)

onMounted(() => {
  refreshHeaderCounts()
  announceModuleAndCurrentPage()
  announcePageDetails()
  window.addEventListener('message-updated', onMessageUpdated)
  window.addEventListener('admin-pending-updated', onAdminPendingUpdated)
})

onBeforeUnmount(() => {
  clearDetailAnnounceTimers()
  window.removeEventListener('message-updated', onMessageUpdated)
  window.removeEventListener('admin-pending-updated', onAdminPendingUpdated)
})
</script>

<style scoped>
.blacklist-banner {
  margin-bottom: 16px;
}

.nav-link-badge-wrap {
  display: inline-flex;
  align-items: center;
}

.nav-badge-anchor {
  display: inline-block;
  line-height: 1.25;
}

</style>
