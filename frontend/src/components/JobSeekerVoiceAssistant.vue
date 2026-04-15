<template>
  <div class="voice-ai-root" aria-live="polite">
    <el-button
      class="voice-fab"
      type="primary"
      circle
      :aria-label="fabLabel"
      @click="openDrawer = true"
    >
      <el-icon :size="28"><Microphone /></el-icon>
    </el-button>

    <el-drawer v-model="openDrawer" title="语音智能助手" size="440px" append-to-body>
      <div class="voice-ai-body" role="region" aria-label="语音智能助手面板">
        <p class="voice-ai-tip">
          <strong>按住说话：</strong>在下方区域按住鼠标左键，或<strong>按住空格键</strong>（光标不在输入框、按钮、链接、勾选框时）说话，<strong>松开后自动识别并发送给
            AI</strong>。语音不落库；多轮对话仅在当前浏览器页面临时保存。
        </p>

        <el-checkbox v-model="consentChecked">
          我已阅读并同意：识别文本会经 HTTPS 发送至服务端模型处理；本平台不保存录音文件。
        </el-checkbox>

        <div class="voice-ai-row">
          <span id="ai-read-label">朗读 AI 回复</span>
          <el-switch
            v-model="aiReadAloud"
            :active-text="'开'"
            :inactive-text="'关'"
            aria-labelledby="ai-read-label"
          />
        </div>

        <div
          role="button"
          class="ptt-hold"
          :class="{
            'is-active': pttActive,
            'is-disabled': !consentChecked || interpreting || !speechSupported
          }"
          :tabindex="consentChecked && !interpreting && speechSupported ? 0 : -1"
          :aria-pressed="pttActive ? 'true' : 'false'"
          :aria-disabled="!consentChecked || interpreting || !speechSupported ? 'true' : 'false'"
          aria-label="按住说话，松开结束并发送给智能助手"
          @pointerdown="onHoldPointerDown"
        >
          <span class="ptt-hold-title">{{ pttActive ? '正在聆听，松开结束' : '按住此处说话' }}</span>
          <span class="ptt-hold-sub">也可用空格键按住 / 松开</span>
        </div>

        <el-input
          v-model="transcript"
          type="textarea"
          :rows="3"
          placeholder="识别结果（可修改后手动发送）"
          aria-label="语音识别文本"
        />

        <div class="voice-ai-actions">
          <el-button
            type="success"
            :disabled="!consentChecked || !transcript.trim() || interpreting"
            :loading="interpreting"
            @click="manualSend"
          >
            手动发送给 AI
          </el-button>
          <el-button :disabled="interpreting" @click="clearTranscriptOnly">清空文本</el-button>
          <el-button :disabled="interpreting" @click="clearConversation">清空对话</el-button>
        </div>

        <div v-if="chatLines.length" class="chat-log" role="log" aria-label="对话记录" tabindex="0">
          <div v-for="(line, idx) in chatLines" :key="idx" class="chat-line" :data-role="line.role">
            <span class="chat-role">{{ line.role === 'user' ? '您' : 'AI' }}</span>
            <span class="chat-text">{{ line.content }}</span>
          </div>
        </div>

        <el-alert v-if="sttError" :title="sttError" type="warning" show-icon :closable="false" />
        <el-alert v-if="lastReplyPreview && !sttError" :title="lastReplyPreview" type="info" show-icon :closable="false" />
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Microphone } from '@element-plus/icons-vue'
import { interpretJobSeekerAi } from '../api/jobseekerAi'
import { speakText } from '../accessibility/speech'
import { useAccessibilityStore } from '../stores/accessibility'
import { getJobSeekerVoicePage, resolveVoiceScene } from '../voice/jobSeekerVoiceRegistry'
import {
  isBrowserSpeechRecognitionSupported,
  startContinuousRecognitionSession
} from '../utils/speechRecognition'
import { readVoiceAiConsent, writeVoiceAiConsent } from '../utils/voiceAiConsent'
import { readAiVoiceReadAloud, writeAiVoiceReadAloud } from '../utils/voiceAiReadAloud'

const route = useRoute()
const router = useRouter()
const accessibilityStore = useAccessibilityStore()

const openDrawer = ref(false)
const consentChecked = ref(readVoiceAiConsent())
const aiReadAloud = ref(readAiVoiceReadAloud())
const transcript = ref('')
const interpreting = ref(false)
const sttError = ref('')
const lastReplyPreview = ref('')
const fabLabel = '打开语音智能助手'
const pttActive = ref(false)

/** @type {{ stop: () => void, abort: () => void, finished: Promise<string> } | null} */
let activeSession = null
let pointerArm = false
let spaceArm = false

const priorTurns = ref([])
const chatLines = ref([])

const speechSupported = computed(() => isBrowserSpeechRecognitionSupported())

watch(openDrawer, (visible) => {
  if (visible && !speechSupported.value) {
    ElMessage.warning('当前浏览器不支持语音识别，请使用 Chrome 或 Edge 重试')
  }
  if (visible) {
    window.addEventListener('keydown', onWindowKeyDown, true)
    window.addEventListener('keyup', onWindowKeyUp, true)
    window.addEventListener('blur', onWindowBlur)
  } else {
    detachWindowListeners()
    pointerArm = false
    spaceArm = false
    abortPttQuiet()
  }
})

watch(consentChecked, (value) => {
  writeVoiceAiConsent(Boolean(value))
})

watch(aiReadAloud, (value) => {
  writeAiVoiceReadAloud(Boolean(value))
})

function shouldIgnoreSpacePtt(target) {
  const el = target instanceof Element ? target : null
  if (!el) {
    return false
  }
  const tag = el.tagName
  if (tag === 'TEXTAREA') {
    return true
  }
  if (tag === 'SELECT') {
    return true
  }
  if (tag === 'BUTTON' || tag === 'A') {
    return true
  }
  if (tag === 'INPUT') {
    const type = String(el.getAttribute('type') || 'text').toLowerCase()
    if (type === 'button' || type === 'submit' || type === 'reset' || type === 'image') {
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

function detachWindowListeners() {
  window.removeEventListener('keydown', onWindowKeyDown, true)
  window.removeEventListener('keyup', onWindowKeyUp, true)
  window.removeEventListener('blur', onWindowBlur)
}

function onWindowKeyDown(e) {
  if (!openDrawer.value || e.code !== 'Space') {
    return
  }
  if (!consentChecked.value || interpreting.value || !speechSupported.value) {
    return
  }
  if (e.repeat) {
    return
  }
  if (shouldIgnoreSpacePtt(e.target)) {
    return
  }
  e.preventDefault()
  e.stopPropagation()
  spaceArm = true
  startPtt()
}

function onWindowKeyUp(e) {
  if (!openDrawer.value || e.code !== 'Space') {
    return
  }
  if (!spaceArm) {
    return
  }
  spaceArm = false
  if (shouldIgnoreSpacePtt(e.target)) {
    return
  }
  e.preventDefault()
  e.stopPropagation()
  void stopPttAndSend()
}

function onWindowBlur() {
  pointerArm = false
  spaceArm = false
  abortPttQuiet()
}

function onHoldPointerDown(e) {
  if (e.button !== 0) {
    return
  }
  if (!consentChecked.value || interpreting.value || !speechSupported.value) {
    return
  }
  if (e.currentTarget?.getAttribute?.('aria-disabled') === 'true') {
    return
  }
  e.preventDefault()
  pointerArm = true
  window.addEventListener('pointerup', onGlobalPointerUp, true)
  window.addEventListener('pointercancel', onGlobalPointerUp, true)
  startPtt()
}

function onGlobalPointerUp() {
  if (!pointerArm) {
    return
  }
  pointerArm = false
  window.removeEventListener('pointerup', onGlobalPointerUp, true)
  window.removeEventListener('pointercancel', onGlobalPointerUp, true)
  void stopPttAndSend()
}

function startPtt() {
  if (pttActive.value || interpreting.value || !consentChecked.value) {
    return
  }
  if (!speechSupported.value) {
    return
  }
  sttError.value = ''
  lastReplyPreview.value = ''
  try {
    activeSession = startContinuousRecognitionSession({
      onInterim: (line) => {
        transcript.value = line
      }
    })
    pttActive.value = true
  } catch {
    pttActive.value = false
    activeSession = null
    sttError.value = '无法启动麦克风，请检查权限或浏览器'
  }
}

async function stopPttAndSend() {
  if (!pttActive.value || !activeSession) {
    return
  }
  const session = activeSession
  activeSession = null
  pttActive.value = false
  session.stop()
  try {
    const text = await session.finished
    transcript.value = text
    if (!text.trim()) {
      sttError.value = '未识别到有效语音，请重试'
      return
    }
    await runAiPipeline(text)
  } catch (error) {
    const code = error?.message || ''
    if (code === 'NOT_ALLOWED') {
      sttError.value = '麦克风权限被拒绝'
    } else if (code && code !== 'ABORTED') {
      sttError.value = `语音识别中断：${code}`
    }
  }
}

function abortPttQuiet() {
  if (!activeSession) {
    pttActive.value = false
    return
  }
  const session = activeSession
  activeSession = null
  pttActive.value = false
  session.abort()
  session.finished.catch(() => {})
}

function speakAiReply(vo) {
  if (!aiReadAloud.value) {
    return
  }
  const content = String(vo?.spokenReply || '').trim()
  if (!content) {
    return
  }
  speakText(content, {
    force: true,
    rate: accessibilityStore.voiceRate,
    volume: accessibilityStore.voiceVolume
  })
}

function serializeVoForHistory(vo) {
  return JSON.stringify({
    intent: vo.intent,
    spokenReply: vo.spokenReply,
    profile: vo.profile || {},
    resume: vo.resume || {},
    jobFilters: vo.jobFilters || {},
    navigation: vo.navigation || {},
    apply: vo.apply || {}
  })
}

function buildJobsQuery(filters) {
  if (!filters || typeof filters !== 'object') {
    return {}
  }
  const query = {}
  if (filters.keyword) {
    query.keyword = String(filters.keyword)
  }
  if (filters.city) {
    query.city = String(filters.city)
  }
  if (filters.workMode) {
    query.workMode = String(filters.workMode)
  }
  if (filters.disabilitySupportType) {
    query.disabilitySupportType = String(filters.disabilitySupportType)
  }
  return query
}

async function navigateByAi(navigation, jobFilters) {
  const nav = navigation && typeof navigation === 'object' ? navigation : {}
  const target = String(nav.target || '').toUpperCase()
  if (!target) {
    return
  }

  if (target === 'JOB_DETAIL') {
    const id = Number(nav.jobId)
    if (Number.isFinite(id) && id > 0) {
      await router.push(`/jobs/${id}`)
    }
    return
  }

  const pathMap = {
    PROFILE: '/jobseeker/profile',
    RESUME_LIST: '/jobseeker/resumes',
    RESUME_NEW: '/jobseeker/resumes/new',
    JOBS: '/jobs',
    MY_APPLICATIONS: '/applications/my',
    RECOMMEND: '/jobs',
    MESSAGES: '/messages',
    HOME: '/home',
    VERIFY: '/jobseeker/verify'
  }
  const path = pathMap[target]
  if (!path) {
    return
  }

  if (target === 'JOBS' && jobFilters && Object.keys(jobFilters).length) {
    const query = buildJobsQuery(jobFilters)
    if (Object.keys(query).length) {
      await router.push({ path, query })
      return
    }
  }

  await router.push(path)
}

function safeJsonObject(value) {
  try {
    return JSON.parse(JSON.stringify(value ?? {}))
  } catch {
    return {}
  }
}

async function runAiPipeline(text) {
  const trimmed = text.trim()
  if (!trimmed) {
    return
  }
  interpreting.value = true
  sttError.value = ''
  lastReplyPreview.value = ''
  try {
    const scene = resolveVoiceScene(route.name)
    const page = getJobSeekerVoicePage(route.name)
    const rawContext = typeof page?.getPageContext === 'function' ? page.getPageContext() || {} : {}
    const pageContext = safeJsonObject(rawContext)

    const response = await interpretJobSeekerAi({
      scene,
      userText: trimmed,
      pageContext,
      priorTurns: priorTurns.value.map((p) => ({
        userText: p.userText,
        assistantJson: p.assistantJson
      }))
    })
    const vo = response.data || {}

    speakAiReply(vo)
    lastReplyPreview.value = vo.spokenReply ? `AI：${vo.spokenReply}` : ''

    priorTurns.value.push({
      userText: trimmed,
      assistantJson: serializeVoForHistory(vo)
    })
    if (priorTurns.value.length > 8) {
      priorTurns.value.shift()
    }
    chatLines.value.push({ role: 'user', content: trimmed })
    chatLines.value.push({ role: 'assistant', content: vo.spokenReply || '（无文本回复）' })
    if (chatLines.value.length > 24) {
      chatLines.value.splice(0, chatLines.value.length - 24)
    }

    if (typeof page?.onInterpret === 'function') {
      await page.onInterpret(vo)
    }

    if (vo.navigation && Object.keys(vo.navigation).length) {
      await navigateByAi(vo.navigation, vo.jobFilters)
    }
  } catch (error) {
    const message = error?.message || '请求失败'
    ElMessage.error(message)
  } finally {
    interpreting.value = false
  }
}

async function manualSend() {
  if (!consentChecked.value) {
    ElMessage.warning('请先勾选同意说明')
    return
  }
  await runAiPipeline(transcript.value)
}

function clearTranscriptOnly() {
  transcript.value = ''
  sttError.value = ''
}

function clearConversation() {
  priorTurns.value = []
  chatLines.value = []
  clearTranscriptOnly()
  lastReplyPreview.value = ''
}

function openAssistantPanel() {
  openDrawer.value = true
}

defineExpose({
  openAssistant: openAssistantPanel,
  /** 供布局层同步「抽屉是否打开」，避免首页空格+Enter 快捷键与面板内空格冲突 */
  drawerOpen: openDrawer
})

</script>

<style scoped>
.voice-ai-root {
  position: fixed;
  right: 24px;
  bottom: 96px;
  z-index: 3000;
}

.voice-fab {
  width: 56px;
  height: 56px;
  min-width: 56px;
  min-height: 56px;
  padding: 0;
  box-shadow: 0 10px 30px rgba(37, 99, 235, 0.35);
}

.voice-ai-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.voice-ai-tip {
  margin: 0;
  font-size: 13px;
  line-height: 1.65;
  color: var(--el-text-color-secondary);
}

.voice-ai-row {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.ptt-hold {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  min-height: 96px;
  padding: 16px;
  border-radius: 14px;
  border: 2px solid var(--el-color-primary);
  background: var(--el-fill-color-blank);
  color: var(--el-color-primary);
  cursor: pointer;
  font: inherit;
  touch-action: none;
  user-select: none;
}

.ptt-hold.is-disabled {
  opacity: 0.55;
  cursor: not-allowed;
  pointer-events: none;
}

.ptt-hold.is-active {
  background: var(--el-color-primary-light-9);
  border-color: var(--el-color-primary-dark-2);
}

.ptt-hold-title {
  font-size: 17px;
  font-weight: 700;
}

.ptt-hold-sub {
  font-size: 12px;
  opacity: 0.85;
}

.voice-ai-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chat-log {
  max-height: 200px;
  overflow: auto;
  padding: 10px 12px;
  border-radius: 10px;
  background: var(--el-fill-color-light);
  font-size: 13px;
  line-height: 1.55;
}

.chat-line {
  margin-bottom: 10px;
}

.chat-line:last-child {
  margin-bottom: 0;
}

.chat-role {
  display: inline-block;
  min-width: 2.25em;
  font-weight: 700;
  color: var(--el-text-color-secondary);
}

.chat-line[data-role='assistant'] .chat-role {
  color: var(--el-color-primary);
}
</style>
