import { defineStore } from 'pinia'

const STORAGE_KEY = 'job-platform-accessibility'

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
    fontSizeMode: 'normal',
    highContrast: false,
    focusHighlight: true
  }),
  getters: {
    isLargeFont: (state) => state.fontSizeMode === 'large'
  },
  actions: {
    init() {
      const saved = readSettings()
      if (saved) {
        this.fontSizeMode = saved.fontSizeMode === 'large' ? 'large' : 'normal'
        this.highContrast = Boolean(saved.highContrast)
        this.focusHighlight = saved.focusHighlight !== false
      }
      this.applyToDocument()
    },
    setFontSizeMode(mode) {
      this.fontSizeMode = mode === 'large' ? 'large' : 'normal'
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
    persistAndApply() {
      writeSettings({
        fontSizeMode: this.fontSizeMode,
        highContrast: this.highContrast,
        focusHighlight: this.focusHighlight
      })
      this.applyToDocument()
    },
    applyToDocument() {
      const root = document.documentElement
      root.dataset.fontSize = this.fontSizeMode
      root.dataset.theme = this.highContrast ? 'contrast' : 'default'
      if (this.focusHighlight) {
        document.body.classList.add('focus-highlight-enabled')
      } else {
        document.body.classList.remove('focus-highlight-enabled')
      }
    }
  }
})
