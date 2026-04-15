const KEY = 'job-platform-voice-ai-read-aloud'

/** 默认开启 AI 回复朗读 */
export function readAiVoiceReadAloud() {
  if (typeof localStorage === 'undefined') {
    return true
  }
  const v = localStorage.getItem(KEY)
  if (v === null) {
    return true
  }
  return v === '1'
}

export function writeAiVoiceReadAloud(enabled) {
  if (typeof localStorage === 'undefined') {
    return
  }
  localStorage.setItem(KEY, enabled ? '1' : '0')
}
