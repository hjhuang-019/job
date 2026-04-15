import { defineStore } from 'pinia'

const STORAGE_KEY = 'job-platform-accessibility'

/** 页面显示比例（%），同时放大文字与界面控件 */
export const UI_SCALE_MIN = 80
export const UI_SCALE_MAX = 200
export const UI_SCALE_DEFAULT = 100

export const UI_SCALE_PRESETS = [
  { label: '85%', value: 85 },
  { label: '90%', value: 90 },
  { label: '100%（标准）', value: 100 },
  { label: '115%', value: 115 },
  { label: '130%', value: 130 },
  { label: '150%', value: 150 },
  { label: '170%', value: 170 },
  { label: '200%', value: 200 }
]

function clampUiScalePercent(n) {
  const x = Number(n)
  if (!Number.isFinite(x)) {
    return UI_SCALE_DEFAULT
  }
  return Math.min(UI_SCALE_MAX, Math.max(UI_SCALE_MIN, Math.round(x)))
}

function readSettings() {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw)
  } catch (error) {
    localStorage.removeItem(STORAGE_KEY)
    return null
  }
}

function writeSettings(settings) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(settings))
}

export const useAccessibilityStore = defineStore('accessibility', {
  state: () => ({
    uiScalePercent: UI_SCALE_DEFAULT,
    highContrast: false,
    focusHighlight: true,
    voiceEnabled: false,
    voiceRate: 1,
    voiceVolume: 1
  }),
  getters: {
    /** 相对浏览器默认是否明显放大（兼容旧「大字体」语义） */
    isLargeFont: (state) => state.uiScalePercent >= 115
  },
  actions: {
    init() {
      const saved = readSettings()
      if (saved) {
        if (saved.uiScalePercent != null) {
          this.uiScalePercent = clampUiScalePercent(saved.uiScalePercent)
        } else if (saved.fontSizeMode === 'large') {
          this.uiScalePercent = 125
        } else {
          this.uiScalePercent = UI_SCALE_DEFAULT
        }
        this.highContrast = Boolean(saved.highContrast)
        this.focusHighlight = saved.focusHighlight !== false
        this.voiceEnabled = Boolean(saved.voiceEnabled)
        this.voiceRate = Number(saved.voiceRate) > 0 ? Number(saved.voiceRate) : 1
        this.voiceVolume =
          Number(saved.voiceVolume) >= 0 && Number(saved.voiceVolume) <= 1 ? Number(saved.voiceVolume) : 1
      }
      this.applyToDocument()
    },
    setUiScalePercent(percent) {
      this.uiScalePercent = clampUiScalePercent(percent)
      this.persistAndApply()
    },
    setHighContrast(enabled) {
      this.highContrast = Boolean(enabled)
      this.persistAndApply()
    },
    setFocusHighlight(enabled) {
      this.focusHighlight = Boolean(enabled)
      this.persistAndApply()
    },
    setVoiceEnabled(enabled) {
      this.voiceEnabled = Boolean(enabled)
      this.persistAndApply()
    },
    setVoiceRate(rate) {
      const numericRate = Number(rate)
      this.voiceRate = Number.isFinite(numericRate) && numericRate > 0 ? numericRate : 1
      this.persistAndApply()
    },
    setVoiceVolume(volume) {
      const numericVolume = Number(volume)
      this.voiceVolume = Number.isFinite(numericVolume) && numericVolume >= 0 && numericVolume <= 1 ? numericVolume : 1
      this.persistAndApply()
    },
    persistAndApply() {
      writeSettings({
        uiScalePercent: this.uiScalePercent,
        highContrast: this.highContrast,
        focusHighlight: this.focusHighlight,
        voiceEnabled: this.voiceEnabled,
        voiceRate: this.voiceRate,
        voiceVolume: this.voiceVolume
      })
      this.applyToDocument()
    },
    applyToDocument() {
      const root = document.documentElement
      const scale = this.uiScalePercent / 100
      root.style.setProperty('--jp-ui-scale', String(Number.isFinite(scale) && scale > 0 ? scale : 1))
      try {
        if (this.uiScalePercent === UI_SCALE_DEFAULT) {
          root.style.removeProperty('zoom')
        } else if (Number.isFinite(scale) && scale > 0) {
          root.style.zoom = String(scale)
        } else {
          root.style.removeProperty('zoom')
        }
      } catch {
        root.style.removeProperty('zoom')
      }
      delete root.dataset.fontSize
      root.dataset.theme = this.highContrast ? 'contrast' : 'default'
      if (this.focusHighlight) {
        document.body.classList.add('focus-highlight-enabled')
      } else {
        document.body.classList.remove('focus-highlight-enabled')
      }
    }
  }
})
