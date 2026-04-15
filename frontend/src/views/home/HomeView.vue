<template>
  <section class="dashboard-page" aria-label="平台首页" v-loading="loading">
    <div class="dashboard-hero">
      <div class="hero-main">
        <p class="hero-eyebrow">{{ heroEyebrow }}</p>
        <h2>欢迎回来，{{ displayName }}</h2>
        <p class="hero-description">{{ heroDescription }}</p>

        <div class="hero-actions">
          <el-button type="primary" size="large" @click="goTo(primaryAction.path)">
            {{ primaryAction.label }}
          </el-button>
          <el-button size="large" @click="goTo('/messages')">查看消息</el-button>
        </div>
      </div>

      <div class="hero-side">
        <div class="hero-side-item">
          <span>当前身份</span>
          <strong>{{ roleText }}</strong>
        </div>
        <div class="hero-side-item">
          <span>未读消息</span>
          <strong>{{ unreadCount }}</strong>
        </div>
        <div class="hero-side-item">
          <span>首页定位</span>
          <strong>{{ roleSummary }}</strong>
        </div>
      </div>
    </div>

    <section v-if="authStore.userRole === 'JOB_SEEKER'" class="voice-assistant-cta">
      <div
        class="voice-assistant-cta-hit"
        role="button"
        tabindex="0"
        aria-label="打开语音与 AI 求职助手"
        aria-describedby="voice-assistant-cta-desc"
        @click="openVoiceAssistant"
        @keydown.enter.prevent="openVoiceAssistant"
        @keydown.space.prevent="openVoiceAssistant"
      >
        <el-card shadow="never" class="voice-assistant-cta-card">
          <div class="voice-assistant-cta-inner">
            <div class="voice-assistant-cta-text">
              <h3 class="voice-assistant-cta-title">语音与 AI 求职助手</h3>
              <p id="voice-assistant-cta-desc" class="voice-assistant-cta-desc">
                按住说话，由 AI 理解意图并辅助填写资料、简历与岗位相关操作。点击本卡片任意区域即可打开助手；在首页还可<strong>按住空格再按 Enter</strong>打开（光标不在输入框、按钮、链接时）；也可点击页面右下角的麦克风图标。
              </p>
            </div>
            <div class="voice-assistant-cta-pill" aria-hidden="true">打开语音智能助手</div>
          </div>
        </el-card>
      </div>
    </section>

    <div class="overview-grid">
      <article v-for="card in overviewCards" :key="card.label" class="overview-card">
        <p>{{ card.label }}</p>
        <h3>{{ card.value }}</h3>
        <span>{{ card.tip }}</span>
      </article>
    </div>

    <el-row :gutter="20">
      <el-col :xs="24" :xl="14">
        <el-card shadow="never" class="dashboard-card">
          <template #header>
            <div class="section-header">
              <h2>{{ mainPanelTitle }}</h2>
              <el-button @click="loadHomeData">刷新首页</el-button>
            </div>
          </template>

          <div v-if="authStore.userRole === 'JOB_SEEKER'" class="panel-list">
            <div v-for="job in recommendJobs.slice(0, 4)" :key="job.id" class="panel-list-item">
              <div class="panel-list-content">
                <div class="panel-list-title-row">
                  <h3>{{ job.title }}</h3>
                  <el-tag type="info">岗位浏览</el-tag>
                </div>
                <p>{{ job.enterpriseName || '企业信息待完善' }}</p>
                <p class="panel-list-desc">
                  {{ job.city || '城市待完善' }} · {{ workModeText(job.workMode) }} · 与「岗位浏览」同一列表，已按残疾类型适配度优先排序
                </p>
              </div>
              <el-button type="primary" plain @click="goTo(`/jobs/${job.id}`)">查看岗位</el-button>
            </div>
            <el-empty
              v-if="recommendJobs.length === 0"
              description="暂无在招岗位，请稍后在岗位浏览中查看或完善个人资料"
            />
          </div>

          <div v-else-if="authStore.userRole === 'ENTERPRISE'" class="panel-list">
            <div v-for="job in enterpriseJobs.slice(0, 4)" :key="job.id" class="panel-list-item">
              <div class="panel-list-content">
                <div class="panel-list-title-row">
                  <h3>{{ job.title }}</h3>
                  <el-tag :type="jobStatusTagType(job.status)">{{ jobStatusText(job.status) }}</el-tag>
                </div>
                <p>{{ job.city || '城市待完善' }} · {{ workModeText(job.workMode) }}</p>
                <p class="panel-list-desc">薪资范围：{{ formatSalary(job) }}</p>
              </div>
              <el-button plain @click="goTo('/enterprise/jobs')">进入管理</el-button>
            </div>
            <el-empty v-if="enterpriseJobs.length === 0" description="暂无已发布岗位，去创建第一个招聘岗位吧" />
          </div>

          <div v-else class="audit-grid">
            <div class="audit-column">
              <div class="audit-column-header">
                <h3>待审求职者</h3>
                <el-tag type="warning">{{ pendingJobSeekers.length }} 条</el-tag>
              </div>
              <div v-if="pendingJobSeekers.length" class="audit-list">
                <div v-for="item in pendingJobSeekers.slice(0, 4)" :key="`jobseeker-${item.id}`" class="audit-item">
                  <strong>{{ item.realName || item.username }}</strong>
                  <span>{{ item.phone || '手机号待补充' }}</span>
                </div>
              </div>
              <el-empty v-else description="当前无待审核求职者" />
            </div>

            <div class="audit-column">
              <div class="audit-column-header">
                <h3>待审企业</h3>
                <el-tag type="warning">{{ pendingEnterprises.length }} 条</el-tag>
              </div>
              <div v-if="pendingEnterprises.length" class="audit-list">
                <div v-for="item in pendingEnterprises.slice(0, 4)" :key="`enterprise-${item.id}`" class="audit-item">
                  <strong>{{ item.enterpriseName || item.username }}</strong>
                  <span>{{ item.contactPerson || '联系人待补充' }}</span>
                </div>
              </div>
              <el-empty v-else description="当前无待审核企业" />
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="dashboard-card">
          <template #header>
            <h2>{{ secondaryPanelTitle }}</h2>
          </template>

          <div v-if="authStore.userRole === 'JOB_SEEKER'" class="status-panel">
            <div class="progress-row">
              <span>资料完整度</span>
              <strong>{{ seekerProfileCompletion }}%</strong>
            </div>
            <el-progress :percentage="seekerProfileCompletion" :stroke-width="12" />

            <div class="checklist">
              <div class="checklist-title">
                <h3>待完善信息</h3>
                <el-tag :type="seekerMissingFields.length ? 'warning' : 'success'">
                  {{ seekerMissingFields.length ? '待补充' : '已完善' }}
                </el-tag>
              </div>
              <p v-if="seekerMissingFields.length">{{ seekerMissingFields.join('、') }}</p>
              <p v-else>个人核心信息已较完整，可继续维护简历并投递岗位。</p>
            </div>

            <div class="status-timeline" v-if="seekerApplications.length">
              <div v-for="item in seekerApplications.slice(0, 4)" :key="item.id" class="timeline-item">
                <div>
                  <h4>{{ item.jobTitle }}</h4>
                  <p>{{ item.enterpriseName || '企业' }}</p>
                </div>
                <el-tag :type="applicationStatusTagType(item.status)">{{ applicationStatusText(item.status) }}</el-tag>
              </div>
            </div>
            <el-empty v-else description="暂无投递记录，快去浏览岗位吧" />
          </div>

          <div v-else-if="authStore.userRole === 'ENTERPRISE'" class="status-panel">
            <div class="progress-row">
              <span>企业资料完整度</span>
              <strong>{{ enterpriseProfileCompletion }}%</strong>
            </div>
            <el-progress :percentage="enterpriseProfileCompletion" :stroke-width="12" status="success" />

            <div class="checklist">
              <div class="checklist-title">
                <h3>认证状态</h3>
                <el-tag :type="verifyStatusTagType(enterpriseProfile.verifyStatus)">
                  {{ verifyStatusText(enterpriseProfile.verifyStatus) }}
                </el-tag>
              </div>
              <p v-if="enterpriseMissingFields.length">建议继续完善：{{ enterpriseMissingFields.join('、') }}</p>
              <p v-else>企业资料信息已较完整，可直接进入岗位管理和投递处理。</p>
            </div>

            <div class="tips-grid">
              <div class="tip-card">
                <span>已开放岗位</span>
                <strong>{{ enterpriseOpenJobs }}</strong>
              </div>
              <div class="tip-card">
                <span>草稿岗位</span>
                <strong>{{ enterpriseDraftJobs }}</strong>
              </div>
            </div>
          </div>

          <div v-else class="status-panel">
            <div class="tips-grid">
              <div class="tip-card">
                <span>待审核总数</span>
                <strong>{{ pendingJobSeekers.length + pendingEnterprises.length }}</strong>
              </div>
              <div class="tip-card">
                <span>平台消息提醒</span>
                <strong>{{ unreadCount }}</strong>
              </div>
            </div>

            <div class="checklist">
              <div class="checklist-title">
                <h3>管理建议</h3>
              </div>
              <p>优先处理认证审核，再结合统计页查看平台用户、岗位与投递整体运行情况。</p>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :xl="10">
        <el-card shadow="never" class="dashboard-card">
          <template #header>
            <h2>快捷入口</h2>
          </template>

          <div class="shortcut-grid">
            <button
              v-for="action in quickActions"
              :key="action.path"
              type="button"
              class="shortcut-button"
              @click="goTo(action.path)"
            >
              <strong>{{ action.label }}</strong>
              <span>{{ action.tip }}</span>
            </button>
          </div>
        </el-card>

        <el-card shadow="never" class="dashboard-card dashboard-card--recent-messages">
          <template #header>
            <div class="section-header recent-messages-header">
              <h2>最近消息</h2>
              <el-tag type="danger" size="large">未读 {{ unreadCount }}</el-tag>
            </div>
          </template>

          <div v-if="latestMessages.length" class="message-list message-list--home">
            <div v-for="message in latestMessages" :key="message.id" class="message-item">
              <div class="message-top">
                <strong>{{ message.title }}</strong>
                <el-tag size="small" :type="message.read ? 'info' : 'danger'">
                  {{ message.read ? '已读' : '未读' }}
                </el-tag>
              </div>
              <p class="message-item-body">
                <EnterpriseApplyMessageContent :row="message" :user-role="authStore.userRole" />
              </p>
              <span>{{ messageTypeText(message.messageType) }} · {{ message.createdAt || '刚刚' }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无消息通知" />
        </el-card>

      </el-col>
    </el-row>
  </section>
</template>

<script setup>
import { computed, inject, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAdminStatistics, getPendingEnterprises, getPendingJobSeekers } from '../../api/admin'
import { getMyApplications } from '../../api/applications'
import { getEnterpriseProfile } from '../../api/enterprise'
import { getMyJobs, getPublishedJobs } from '../../api/jobs'
import { getMessages, getUnreadCount } from '../../api/messages'
import { getJobSeekerProfile } from '../../api/jobseeker'
import { useAuthStore } from '../../stores/auth'
import EnterpriseApplyMessageContent from '../../components/EnterpriseApplyMessageContent.vue'
import { registerJobSeekerVoicePage } from '../../voice/jobSeekerVoiceRegistry'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const openJobSeekerVoiceAssistant = inject('openJobSeekerVoiceAssistant', () => {})
const voiceAssistantDrawerOpen = inject('voiceAssistantDrawerOpen', ref(false))

function openVoiceAssistant() {
  if (typeof openJobSeekerVoiceAssistant === 'function') {
    openJobSeekerVoiceAssistant()
  }
}

/** 与语音助手内空格 PTT 一致：在可输入/可点击控件上不抢快捷键 */
function shouldIgnoreVoiceHotkeyTarget(target) {
  const el = target instanceof Element ? target : null
  if (!el) {
    return false
  }
  const tag = el.tagName
  if (tag === 'TEXTAREA' || tag === 'SELECT') {
    return true
  }
  if (tag === 'BUTTON' || tag === 'A') {
    return true
  }
  if (tag === 'INPUT') {
    const type = String(el.getAttribute('type') || 'text').toLowerCase()
    if (type === 'button' || type === 'submit' || type === 'reset' || type === 'image' || type === 'checkbox' || type === 'radio') {
      return true
    }
    return true
  }
  if (el.isContentEditable) {
    return true
  }
  const role = el.getAttribute('role')
  if (role === 'textbox' || role === 'combobox' || role === 'listbox' || role === 'menuitem') {
    return true
  }
  if (el.closest('.el-input__inner, .el-textarea__inner, .el-select__input, [role="switch"]')) {
    return true
  }
  return false
}

const spaceHeldForAssistant = ref(false)

function isSeekerHomeVoiceShortcutActive() {
  return (
    authStore.userRole === 'JOB_SEEKER' &&
    authStore.isLoggedIn &&
    route.name === 'jobSeekerHome' &&
    !voiceAssistantDrawerOpen.value
  )
}

function onVoiceShortcutKeyDown(e) {
  if (e.code === 'Space') {
    if (e.repeat) {
      return
    }
    if (!isSeekerHomeVoiceShortcutActive()) {
      return
    }
    if (shouldIgnoreVoiceHotkeyTarget(e.target)) {
      return
    }
    e.preventDefault()
    spaceHeldForAssistant.value = true
    return
  }
  if (e.code === 'Enter' || e.code === 'NumpadEnter') {
    if (!spaceHeldForAssistant.value) {
      return
    }
    if (!isSeekerHomeVoiceShortcutActive()) {
      spaceHeldForAssistant.value = false
      return
    }
    if (shouldIgnoreVoiceHotkeyTarget(e.target)) {
      return
    }
    e.preventDefault()
    e.stopPropagation()
    spaceHeldForAssistant.value = false
    openVoiceAssistant()
  }
}

function onVoiceShortcutKeyUp(e) {
  if (e.code === 'Space') {
    spaceHeldForAssistant.value = false
  }
}

watch(voiceAssistantDrawerOpen, (open) => {
  if (open) {
    spaceHeldForAssistant.value = false
  }
})

const loading = ref(false)
const unreadCount = ref(0)
const latestMessages = ref([])
const recommendJobs = ref([])
const seekerApplications = ref([])
const enterpriseJobs = ref([])
const pendingJobSeekers = ref([])
const pendingEnterprises = ref([])
const seekerProfile = ref({})
const enterpriseProfile = ref({})
const adminStatistics = reactive({
  userCount: 0,
  enterpriseCount: 0,
  jobCount: 0,
  applicationCount: 0
})

const roleText = computed(() => {
  return {
    JOB_SEEKER: '求职者',
    ENTERPRISE: '企业用户',
    ADMIN: '管理员'
  }[authStore.userRole] || '平台用户'
})

const displayName = computed(() => {
  if (authStore.userRole === 'ENTERPRISE') {
    return enterpriseProfile.value.enterpriseName || authStore.userInfo?.realName || authStore.userInfo?.username || '企业用户'
  }
  return authStore.userInfo?.realName || authStore.userInfo?.username || '用户'
})

const heroEyebrow = computed(() => {
  return {
    JOB_SEEKER: 'Job Seeker Dashboard',
    ENTERPRISE: 'Enterprise Dashboard',
    ADMIN: 'Admin Dashboard'
  }[authStore.userRole] || 'Dashboard'
})

const heroDescription = computed(() => {
  return {
    JOB_SEEKER: '这里集中展示你的求职进展、推荐岗位与资料完善情况。',
    ENTERPRISE: '这里可快速查看企业认证状态、岗位运营情况和消息提醒。',
    ADMIN: '这里汇总平台核心统计与待审核任务。'
  }[authStore.userRole] || '欢迎进入平台首页。'
})

const roleSummary = computed(() => {
  return {
    JOB_SEEKER: '求职工作台',
    ENTERPRISE: '招聘工作台',
    ADMIN: '平台控制台'
  }[authStore.userRole] || '综合门户'
})

const primaryAction = computed(() => {
  return {
    JOB_SEEKER: { label: '浏览岗位', path: '/jobs' },
    ENTERPRISE: { label: '发布岗位', path: '/enterprise/jobs/publish' },
    ADMIN: { label: '进入审核', path: '/admin/audits' }
  }[authStore.userRole] || { label: '返回首页', path: '/' }
})

const mainPanelTitle = computed(() => {
  return {
    JOB_SEEKER: '在招岗位（与岗位浏览一致）',
    ENTERPRISE: '岗位概览',
    ADMIN: '待审核事项'
  }[authStore.userRole] || '重点信息'
})

const secondaryPanelTitle = computed(() => {
  return {
    JOB_SEEKER: '求职进展',
    ENTERPRISE: '企业状态',
    ADMIN: '管理概览'
  }[authStore.userRole] || '补充信息'
})

const seekerProfileCompletion = computed(() => {
  return calculateCompletion(
    seekerProfile.value,
    ['realName', 'phone', 'email', 'expectedCity', 'expectedSalary', 'expectedJob', 'skills', 'introduction', 'disabilityType', 'disabilityLevel']
  )
})

const enterpriseProfileCompletion = computed(() => {
  return calculateCompletion(
    enterpriseProfile.value,
    ['enterpriseName', 'industry', 'scaleType', 'contactPerson', 'contactPhone', 'email', 'address', 'description']
  )
})

const seekerMissingFields = computed(() => {
  return collectMissingFields(seekerProfile.value, {
    realName: '真实姓名',
    phone: '手机号',
    email: '邮箱',
    expectedCity: '期望城市',
    expectedSalary: '期望薪资',
    expectedJob: '求职意向',
    skills: '技能标签',
    introduction: '个人简介',
    disabilityType: '残疾类型',
    disabilityLevel: '残疾等级'
  })
})

const enterpriseMissingFields = computed(() => {
  return collectMissingFields(enterpriseProfile.value, {
    enterpriseName: '企业名称',
    industry: '所属行业',
    scaleType: '企业规模',
    contactPerson: '联系人',
    contactPhone: '联系电话',
    email: '邮箱',
    address: '企业地址',
    description: '企业简介'
  })
})

const enterpriseOpenJobs = computed(() => {
  return enterpriseJobs.value.filter((item) => item.status === 'OPEN').length
})

const enterpriseDraftJobs = computed(() => {
  return enterpriseJobs.value.filter((item) => item.status === 'DRAFT').length
})

const overviewCards = computed(() => {
  if (authStore.userRole === 'JOB_SEEKER') {
    return [
      { label: '资料完整度', value: `${seekerProfileCompletion.value}%`, tip: '完善信息可提升匹配效果' },
      { label: '在招岗位（预览）', value: recommendJobs.value.length, tip: '与岗位浏览同源，按残疾类型适配优先' },
      { label: '我的投递', value: seekerApplications.value.length, tip: '随时查看投递状态' },
      { label: '未读消息', value: unreadCount.value, tip: '及时掌握最新通知' }
    ]
  }

  if (authStore.userRole === 'ENTERPRISE') {
    return [
      { label: '资料完整度', value: `${enterpriseProfileCompletion.value}%`, tip: '完善后更利于审核通过' },
      { label: '岗位总数', value: enterpriseJobs.value.length, tip: '反映企业招聘活跃度' },
      { label: '开放岗位', value: enterpriseOpenJobs.value, tip: '当前可投递的岗位数量' },
      { label: '未读消息', value: unreadCount.value, tip: '查看系统与流程提醒' }
    ]
  }

  return [
    { label: '求职者数', value: adminStatistics.userCount, tip: '平台注册求职用户' },
    { label: '企业数', value: adminStatistics.enterpriseCount, tip: '平台注册企业用户' },
    { label: '岗位数', value: adminStatistics.jobCount, tip: '平台累计岗位规模' },
    { label: '投递数', value: adminStatistics.applicationCount, tip: '平台累计投递总量' }
  ]
})

const quickActions = computed(() => {
  if (authStore.userRole === 'JOB_SEEKER') {
    return [
      { label: '完善资料', tip: '补齐求职者信息', path: '/jobseeker/profile' },
      { label: '简历管理', tip: '维护多份简历', path: '/jobseeker/resumes' },
      { label: '岗位浏览', tip: '查看平台岗位', path: '/jobs' },
      { label: '我的投递', tip: '跟进求职状态', path: '/applications/my' }
    ]
  }

  if (authStore.userRole === 'ENTERPRISE') {
    return [
      { label: '企业信息', tip: '完善企业资料', path: '/enterprise/profile' },
      { label: '企业认证', tip: '提交认证材料', path: '/enterprise/verify' },
      { label: '发布岗位', tip: '新增招聘职位', path: '/enterprise/jobs/publish' },
      { label: '岗位管理', tip: '维护招聘状态', path: '/enterprise/jobs' }
    ]
  }

  return [
    { label: '认证审核', tip: '处理待审用户', path: '/admin/audits' },
    { label: '用户反馈', tip: '处理黑名单申诉等工单', path: '/admin/feedback' },
    {
      label: '平台统计',
      tip: '基础数据汇总与求职者、企业用户档案查询',
      path: '/admin/statistics'
    },
    { label: '消息中心', tip: '查看平台通知', path: '/messages' },
    { label: '首页刷新', tip: '同步最新概况', path: '/admin/home' }
  ]
})

function calculateCompletion(source, fields) {
  if (!fields.length) {
    return 0
  }
  const filledCount = fields.filter((field) => {
    const value = source?.[field]
    if (Array.isArray(value)) {
      return value.length > 0
    }
    return Boolean(String(value || '').trim())
  }).length
  return Math.round((filledCount / fields.length) * 100)
}

function collectMissingFields(source, labels) {
  return Object.entries(labels)
    .filter(([key]) => !String(source?.[key] || '').trim())
    .map(([, label]) => label)
}

function verifyStatusText(status) {
  return {
    PENDING: '待审核',
    PASS: '已通过',
    REJECT: '未通过'
  }[status] || '待完善'
}

function verifyStatusTagType(status) {
  return {
    PENDING: 'warning',
    PASS: 'success',
    REJECT: 'danger'
  }[status] || 'info'
}

function applicationStatusText(status) {
  return {
    SUBMITTED: '已投递',
    VIEWED: '已查看',
    COMMUNICATING: '沟通中',
    ACCEPTED: '已录用',
    REJECTED: '已拒绝'
  }[status] || status
}

function applicationStatusTagType(status) {
  return {
    SUBMITTED: 'info',
    VIEWED: 'warning',
    COMMUNICATING: 'primary',
    ACCEPTED: 'success',
    REJECTED: 'danger'
  }[status] || 'info'
}

function jobStatusText(status) {
  return {
    DRAFT: '草稿',
    OPEN: '招聘中',
    CLOSED: '已下架'
  }[status] || status
}

function jobStatusTagType(status) {
  return {
    DRAFT: 'info',
    OPEN: 'success',
    CLOSED: 'warning'
  }[status] || 'info'
}

function workModeText(workMode) {
  return {
    OFFLINE: '线下',
    REMOTE: '远程',
    HYBRID: '混合'
  }[workMode] || '待完善'
}

function messageTypeText(type) {
  return {
    SYSTEM: '系统通知',
    APPLY_STATUS: '投递状态',
    AUDIT_NOTICE: '审核通知',
    USER_FEEDBACK: '用户反馈'
  }[type] || type || '系统消息'
}

function formatSalary(job) {
  if (job?.salaryMin && job?.salaryMax) {
    return `${job.salaryMin} - ${job.salaryMax}`
  }
  return '面议'
}

function goTo(path) {
  router.push(path)
}

async function loadCommonData() {
  const [countResult, messageResult] = await Promise.allSettled([getUnreadCount(), getMessages()])

  if (countResult.status === 'fulfilled') {
    unreadCount.value = Number(countResult.value.data?.count || 0)
  }

  if (messageResult.status === 'fulfilled') {
    latestMessages.value = (messageResult.value.data || []).slice(0, 4)
  }
}

async function loadJobSeekerHome() {
  const [profileResult, applicationResult, recommendResult] = await Promise.allSettled([
    getJobSeekerProfile(),
    getMyApplications(),
    getPublishedJobs({ pageNum: 1, pageSize: 12 })
  ])

  if (profileResult.status === 'fulfilled') {
    seekerProfile.value = profileResult.value.data || {}
  }
  if (applicationResult.status === 'fulfilled') {
    seekerApplications.value = applicationResult.value.data || []
  }
  if (recommendResult.status === 'fulfilled') {
    recommendJobs.value = recommendResult.value.data?.records || []
  }
}

async function loadEnterpriseHome() {
  const [profileResult, jobsResult] = await Promise.allSettled([getEnterpriseProfile(), getMyJobs()])

  if (profileResult.status === 'fulfilled') {
    enterpriseProfile.value = profileResult.value.data || {}
  }
  if (jobsResult.status === 'fulfilled') {
    enterpriseJobs.value = jobsResult.value.data || []
  }
}

async function loadAdminHome() {
  const [statisticsResult, seekerResult, enterpriseResult] = await Promise.allSettled([
    getAdminStatistics(),
    getPendingJobSeekers(),
    getPendingEnterprises()
  ])

  if (statisticsResult.status === 'fulfilled') {
    adminStatistics.userCount = Number(statisticsResult.value.data?.userCount || 0)
    adminStatistics.enterpriseCount = Number(statisticsResult.value.data?.enterpriseCount || 0)
    adminStatistics.jobCount = Number(statisticsResult.value.data?.jobCount || 0)
    adminStatistics.applicationCount = Number(statisticsResult.value.data?.applicationCount || 0)
  }
  if (seekerResult.status === 'fulfilled') {
    pendingJobSeekers.value = seekerResult.value.data || []
  }
  if (enterpriseResult.status === 'fulfilled') {
    pendingEnterprises.value = enterpriseResult.value.data || []
  }
}

async function loadHomeData() {
  loading.value = true
  try {
    await loadCommonData()
    if (authStore.userRole === 'JOB_SEEKER') {
      await loadJobSeekerHome()
    } else if (authStore.userRole === 'ENTERPRISE') {
      await loadEnterpriseHome()
    } else if (authStore.userRole === 'ADMIN') {
      await loadAdminHome()
    }
  } finally {
    loading.value = false
  }
}

function getJobSeekerHomeVoiceContext() {
  return {
    displayName: displayName.value,
    profileCompletion: seekerProfileCompletion.value,
    recommendPreview: recommendJobs.value.slice(0, 8).map((j) => ({
      jobId: j.id,
      title: j.title
    })),
    applicationsCount: seekerApplications.value.length
  }
}

let unregisterVoicePage = () => {}

onMounted(() => {
  if (authStore.userRole === 'JOB_SEEKER' && route.name === 'jobSeekerHome') {
    unregisterVoicePage = registerJobSeekerVoicePage('jobSeekerHome', {
      getPageContext: getJobSeekerHomeVoiceContext,
      onInterpret: () => {}
    })
  }
  window.addEventListener('keydown', onVoiceShortcutKeyDown, true)
  window.addEventListener('keyup', onVoiceShortcutKeyUp, true)
  loadHomeData()
})

onUnmounted(() => {
  window.removeEventListener('keydown', onVoiceShortcutKeyDown, true)
  window.removeEventListener('keyup', onVoiceShortcutKeyUp, true)
  spaceHeldForAssistant.value = false
  unregisterVoicePage()
})
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.voice-assistant-cta-hit {
  display: block;
  cursor: pointer;
  border-radius: 24px;
  outline: none;
}

.voice-assistant-cta-hit:hover .voice-assistant-cta-card {
  box-shadow: 0 8px 28px rgba(37, 99, 235, 0.12);
  border-color: var(--el-color-primary-light-5);
}

.voice-assistant-cta-hit:active .voice-assistant-cta-card {
  transform: scale(0.992);
}

.voice-assistant-cta-hit:focus-visible .voice-assistant-cta-card {
  outline: 3px solid var(--el-color-primary);
  outline-offset: 3px;
}

.voice-assistant-cta-card {
  border-radius: 24px;
  border: 1px solid var(--el-border-color-lighter);
  transition:
    box-shadow 0.2s ease,
    border-color 0.2s ease,
    transform 0.15s ease;
}

.voice-assistant-cta-card :deep(.el-card__body) {
  padding: 28px 32px;
}

@media (min-width: 768px) {
  .voice-assistant-cta-card :deep(.el-card__body) {
    padding: 32px 40px;
  }
}

.voice-assistant-cta-inner {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 24px 32px;
  min-height: 120px;
}

.voice-assistant-cta-text {
  flex: 1;
  min-width: min(100%, 300px);
}

.voice-assistant-cta-title {
  margin: 0 0 12px;
  font-size: clamp(20px, 2.2vw, 24px);
  font-weight: 700;
  line-height: 1.35;
  color: var(--el-text-color-primary);
}

.voice-assistant-cta-desc {
  margin: 0;
  font-size: clamp(15px, 1.4vw, 17px);
  line-height: 1.7;
  color: var(--el-text-color-secondary);
}

.voice-assistant-cta-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  min-height: 56px;
  min-width: min(100%, 260px);
  padding: 14px 36px;
  font-size: 18px;
  font-weight: 600;
  line-height: 1.2;
  color: #ffffff;
  text-align: center;
  border-radius: 14px;
  background: var(--el-color-primary);
  box-shadow: 0 4px 14px rgba(37, 99, 235, 0.35);
  pointer-events: none;
  user-select: none;
}

@media (max-width: 600px) {
  .voice-assistant-cta-pill {
    width: 100%;
    min-width: 0;
    min-height: 52px;
  }
}

.dashboard-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(280px, 360px);
  gap: 20px;
  padding: 32px;
  border-radius: 28px;
  background:
    radial-gradient(circle at top right, rgba(255, 255, 255, 0.22), transparent 32%),
    linear-gradient(135deg, #2563eb 0%, #1d4ed8 45%, #0f766e 100%);
  color: #ffffff;
  box-shadow: 0 22px 44px rgba(37, 99, 235, 0.18);
}

.hero-eyebrow {
  margin: 0 0 12px;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  opacity: 0.82;
}

.hero-main h2 {
  margin: 0;
  font-size: clamp(28px, 4vw, 40px);
}

.hero-description {
  max-width: 720px;
  margin: 16px 0 0;
  font-size: 16px;
  line-height: 1.85;
  opacity: 0.94;
}

.hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 26px;
}

.hero-side {
  display: grid;
  gap: 12px;
}

.hero-side-item {
  padding: 18px 18px 16px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(10px);
}

.hero-side-item span {
  display: block;
  margin-bottom: 10px;
  font-size: 13px;
  opacity: 0.8;
}

.hero-side-item strong {
  font-size: 22px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.overview-card {
  padding: 22px 22px 20px;
  border-radius: 22px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
}

.overview-card p {
  margin: 0;
  color: #64748b;
}

.overview-card h3 {
  margin: 12px 0 8px;
  color: #0f172a;
  font-size: 32px;
}

.overview-card span {
  color: #94a3b8;
  font-size: 13px;
}

.dashboard-card {
  margin-bottom: 20px;
  border: 1px solid #e2e8f0;
  border-radius: 24px;
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.05);
}

/* 首页「最近消息」：加宽侧栏 + 更大留白与正文字号 */
.dashboard-card--recent-messages :deep(.el-card__header) {
  padding: 20px 24px;
}

.dashboard-card--recent-messages :deep(.el-card__body) {
  padding: 22px 24px 28px;
  min-height: 340px;
}

.recent-messages-header h2 {
  font-size: 1.35rem;
  font-weight: 700;
  margin: 0;
  letter-spacing: 0.02em;
}

.message-list--home {
  gap: 16px;
}

.dashboard-card--recent-messages .message-item {
  padding: 20px 22px 22px;
  border-radius: 20px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
  box-shadow: 0 8px 22px rgba(15, 23, 42, 0.07);
}

.dashboard-card--recent-messages .message-top strong {
  font-size: 1.0625rem;
  font-weight: 600;
  line-height: 1.35;
}

.dashboard-card--recent-messages .message-item-body {
  margin-top: 14px !important;
  font-size: 0.9375rem;
  line-height: 1.8;
  color: #475569;
}

/* 避免全局 .message-item span { display:block } 把站内信正文里的链接拆成多块 */
.dashboard-card--recent-messages .message-item-body :deep(span),
.dashboard-card--recent-messages .message-item-body :deep(a) {
  display: inline;
  margin-top: 0;
  font-size: inherit;
}

.dashboard-card--recent-messages .message-item > span:last-of-type {
  display: block;
  margin-top: 14px !important;
  font-size: 0.8125rem;
  color: #94a3b8;
}

.dashboard-card--recent-messages :deep(.el-empty) {
  padding: 48px 16px;
}

.panel-list,
.message-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.panel-list-item,
.message-item,
.timeline-item,
.audit-item,
.tip-card,
.shortcut-button {
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  background: #f8fafc;
}

.panel-list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
}

.panel-list-content {
  flex: 1;
}

.panel-list-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.panel-list-title-row h3,
.checklist-title h3,
.audit-column-header h3 {
  margin: 0;
  color: #0f172a;
  font-size: 18px;
}

.panel-list-content p,
.message-item p,
.timeline-item p,
.audit-item span,
.checklist p {
  margin: 0;
  color: #64748b;
  line-height: 1.75;
}

.panel-list-desc {
  margin-top: 6px !important;
}

.audit-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.audit-column {
  padding: 18px;
  border-radius: 20px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.audit-column-header,
.checklist-title,
.message-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.audit-list,
.status-timeline {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 14px;
}

.audit-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 16px;
}

.status-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.progress-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #0f172a;
  font-weight: 600;
}

.checklist {
  padding: 18px 20px;
  border-radius: 18px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
}

.timeline-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px;
}

.timeline-item h4 {
  margin: 0 0 6px;
  color: #0f172a;
  font-size: 16px;
}

.tips-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.tip-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 18px;
}

.tip-card span {
  color: #64748b;
}

.tip-card strong {
  color: #0f172a;
  font-size: 28px;
}

.shortcut-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.shortcut-button {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  padding: 18px;
  cursor: pointer;
  text-align: left;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.shortcut-button:hover {
  transform: translateY(-2px);
  border-color: #93c5fd;
  box-shadow: 0 10px 22px rgba(37, 99, 235, 0.12);
}

.shortcut-button strong,
.message-item strong {
  color: #0f172a;
}

.shortcut-button span,
.message-item span {
  color: #64748b;
  line-height: 1.6;
}

.message-item,
.message-item span {
  display: block;
  margin-top: 10px;
  font-size: 12px;
}

@media (max-width: 1200px) {
  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 960px) {
  .dashboard-hero,
  .audit-grid,
  .shortcut-grid,
  .tips-grid {
    grid-template-columns: 1fr;
  }

  .panel-list-item,
  .timeline-item {
    flex-direction: column;
    align-items: flex-start;
  }
}

@media (max-width: 640px) {
  .dashboard-hero {
    padding: 24px 20px;
  }

  .overview-grid {
    grid-template-columns: 1fr;
  }
}
</style>
