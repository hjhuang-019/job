const DEFAULT_LANG = 'zh-CN'
const DEFAULT_RATE = 1
const DEFAULT_PITCH = 1
const DEFAULT_VOLUME = 1
const DEDUPE_WINDOW_MS = 900

let lastSpokenText = ''
let lastSpokenAt = 0
let lastFocusedElement = null
let lastFocusedAt = 0

function now() {
  return Date.now()
}

function normalizeText(text) {
  return String(text || '')
    .replace(/\s+/g, ' ')
    .trim()
}

function getSpeechApi() {
  if (typeof window === 'undefined') {
    return null
  }
  if (!('speechSynthesis' in window) || !('SpeechSynthesisUtterance' in window)) {
    return null
  }
  return window.speechSynthesis
}

export function isSpeechSupported() {
  return Boolean(getSpeechApi())
}

export function cancelSpeech() {
  const speechApi = getSpeechApi()
  if (!speechApi) {
    return
  }
  speechApi.cancel()
}

export function speakText(text, options = {}) {
  const content = normalizeText(text)
  if (!content) {
    return false
  }

  const speechApi = getSpeechApi()
  if (!speechApi) {
    return false
  }

  const currentAt = now()
  const minIntervalMs = Number(options.minIntervalMs ?? DEDUPE_WINDOW_MS)
  const force = Boolean(options.force)
  if (!force && content === lastSpokenText && currentAt - lastSpokenAt < minIntervalMs) {
    return false
  }

  const utterance = new window.SpeechSynthesisUtterance(content)
  utterance.lang = options.lang || DEFAULT_LANG
  utterance.rate = Number(options.rate ?? DEFAULT_RATE)
  utterance.pitch = Number(options.pitch ?? DEFAULT_PITCH)
  utterance.volume = Number(options.volume ?? DEFAULT_VOLUME)

  if (options.interrupt !== false) {
    speechApi.cancel()
  }
  speechApi.speak(utterance)

  lastSpokenText = content
  lastSpokenAt = currentAt
  return true
}

function textFromLabelledBy(element) {
  const ids = String(element.getAttribute('aria-labelledby') || '')
    .split(/\s+/)
    .map((item) => item.trim())
    .filter(Boolean)

  if (!ids.length) {
    return ''
  }

  const texts = ids
    .map((id) => document.getElementById(id)?.textContent || '')
    .map((item) => normalizeText(item))
    .filter(Boolean)
  return texts.join(' ')
}

function inferRoleName(element) {
  const explicitRole = normalizeText(element.getAttribute('role'))
  if (explicitRole) {
    return explicitRole
  }

  const tag = element.tagName.toLowerCase()
  if (tag === 'a') {
    return '链接'
  }
  if (tag === 'button') {
    return '按钮'
  }
  if (tag === 'select') {
    return '下拉框'
  }
  if (tag === 'textarea') {
    return '文本域'
  }
  if (tag === 'img') {
    return '图片'
  }
  if (tag === 'input') {
    const type = (element.getAttribute('type') || 'text').toLowerCase()
    if (type === 'password') {
      return '密码输入框'
    }
    if (type === 'checkbox') {
      return '复选框'
    }
    if (type === 'radio') {
      return '单选框'
    }
    if (type === 'submit' || type === 'button') {
      return '按钮'
    }
    return '输入框'
  }
  return '控件'
}

function extractElementName(element) {
  const ariaLabel = normalizeText(element.getAttribute('aria-label'))
  if (ariaLabel) {
    return ariaLabel
  }

  const labelledBy = textFromLabelledBy(element)
  if (labelledBy) {
    return labelledBy
  }

  const alt = normalizeText(element.getAttribute('alt'))
  if (alt) {
    return alt
  }

  const placeholder = normalizeText(element.getAttribute('placeholder'))
  if (placeholder) {
    return placeholder
  }

  const title = normalizeText(element.getAttribute('title'))
  if (title) {
    return title
  }

  const text = normalizeText(element.textContent)
  if (text) {
    return text.length > 40 ? `${text.slice(0, 40)}...` : text
  }

  return ''
}

function describeElement(element) {
  const roleName = inferRoleName(element)
  const elementName = extractElementName(element)
  return normalizeText(elementName ? `${roleName}：${elementName}` : roleName)
}

export function installElementSpeechAnnouncer({
  isVoiceEnabled = () => false,
  isElementVoiceEnabled = () => false,
  getRate = () => DEFAULT_RATE,
  getVolume = () => DEFAULT_VOLUME
} = {}) {
  if (!isSpeechSupported() || typeof document === 'undefined') {
    return () => {}
  }

  const selector = [
    'a[href]',
    'button',
    'input',
    'textarea',
    'select',
    '[role="button"]',
    '[role="link"]',
    '[role="tab"]',
    '[role="menuitem"]',
    '[tabindex]:not([tabindex="-1"])'
  ].join(',')

  function onFocusIn(event) {
    if (!isVoiceEnabled() || !isElementVoiceEnabled()) {
      return
    }

    const target = event.target instanceof Element ? event.target.closest(selector) : null
    if (!target) {
      return
    }

    const currentAt = now()
    if (target === lastFocusedElement && currentAt - lastFocusedAt < 500) {
      return
    }

    const description = describeElement(target)
    if (!description) {
      return
    }

    speakText(description, {
      interrupt: false,
      minIntervalMs: 500,
      rate: getRate(),
      volume: getVolume()
    })

    lastFocusedElement = target
    lastFocusedAt = currentAt
  }

  document.addEventListener('focusin', onFocusIn)

  return () => {
    document.removeEventListener('focusin', onFocusIn)
  }
}

function normalizeCellText(text) {
  const normalized = normalizeText(text)
  if (!normalized) {
    return ''
  }
  return normalized.length > 32 ? `${normalized.slice(0, 32)}...` : normalized
}

function readTableRows(container, maxRows = 10, maxColumns = 5) {
  const rowElements = Array.from(container.querySelectorAll('.el-table__body tbody tr'))
  if (!rowElements.length) {
    return []
  }
  return rowElements.slice(0, maxRows).map((row, rowIndex) => {
    const cells = Array.from(row.querySelectorAll('td'))
      .map((cell) => normalizeCellText(cell.innerText || cell.textContent || ''))
      .filter(Boolean)
      .slice(0, maxColumns)
    if (!cells.length) {
      return ''
    }
    return `第 ${rowIndex + 1} 条：${cells.join('，')}`
  }).filter(Boolean)
}

function readFallbackParagraph(container) {
  const candidates = Array.from(
    container.querySelectorAll('h2, h3, .el-alert__title, .el-empty__description, .el-card__header')
  )
  const texts = candidates
    .map((item) => normalizeCellText(item.innerText || item.textContent || ''))
    .filter(Boolean)
  return texts.slice(0, 3).join('，')
}

export function speakMainContentDetails({
  containerSelector = '#main-content',
  maxRows = 10,
  maxColumns = 5,
  rate = DEFAULT_RATE,
  volume = DEFAULT_VOLUME
} = {}) {
  if (!isSpeechSupported() || typeof document === 'undefined') {
    return false
  }
  const container = document.querySelector(containerSelector)
  if (!container) {
    return false
  }

  const rows = readTableRows(container, maxRows, maxColumns)
  if (rows.length) {
    const content = `页面数据列表已加载，本次播报前 ${rows.length} 条。${rows.join('；')}`
    return speakText(content, {
      force: true,
      minIntervalMs: 800,
      rate,
      volume
    })
  }

  const fallbackText = readFallbackParagraph(container)
  if (!fallbackText) {
    return false
  }
  return speakText(`页面内容：${fallbackText}`, {
    force: true,
    minIntervalMs: 800,
    rate,
    volume
  })
}

function readHomeOverview(container) {
  const welcomeText = normalizeCellText(
    container.querySelector('.hero-main h2')?.textContent || container.querySelector('.hero-main h2')?.innerText || ''
  )
  const heroDescription = normalizeCellText(
    container.querySelector('.hero-description')?.textContent || container.querySelector('.hero-description')?.innerText || ''
  )

  const heroSideItems = Array.from(container.querySelectorAll('.hero-side-item'))
    .map((item) => {
      const label = normalizeCellText(item.querySelector('span')?.textContent || item.querySelector('span')?.innerText || '')
      const value = normalizeCellText(item.querySelector('strong')?.textContent || item.querySelector('strong')?.innerText || '')
      if (!label || !value) {
        return ''
      }
      return `${label}${value}`
    })
    .filter(Boolean)
    .slice(0, 3)

  const cards = Array.from(container.querySelectorAll('.overview-card'))
    .map((card) => {
      const label = normalizeCellText(card.querySelector('p')?.textContent || card.querySelector('p')?.innerText || '')
      const value = normalizeCellText(card.querySelector('h3')?.textContent || card.querySelector('h3')?.innerText || '')
      if (!label || !value) {
        return ''
      }
      return `${label}${value}`
    })
    .filter(Boolean)
    .slice(0, 4)

  const parts = []
  if (welcomeText) {
    parts.push(welcomeText)
  }
  if (heroDescription) {
    parts.push(heroDescription)
  }
  if (heroSideItems.length) {
    parts.push(`首页摘要：${heroSideItems.join('，')}`)
  }
  if (cards.length) {
    parts.push(`概览数据：${cards.join('，')}`)
  }

  return parts.join('。')
}

export function speakHomeOverview({
  containerSelector = '#main-content',
  rate = DEFAULT_RATE,
  volume = DEFAULT_VOLUME
} = {}) {
  if (!isSpeechSupported() || typeof document === 'undefined') {
    return false
  }
  const container = document.querySelector(containerSelector)
  if (!container) {
    return false
  }
  const summary = readHomeOverview(container)
  if (!summary) {
    return false
  }
  return speakText(`首页信息：${summary}`, {
    force: true,
    minIntervalMs: 800,
    rate,
    volume
  })
}
