const CONSENT_KEY = 'job-platform-voice-ai-consent-v1'

export function readVoiceAiConsent() {
  if (typeof localStorage === 'undefined') {
    return false
  }
  return localStorage.getItem(CONSENT_KEY) === '1'
}

export function writeVoiceAiConsent(granted) {
  if (typeof localStorage === 'undefined') {
    return
  }
  if (granted) {
    localStorage.setItem(CONSENT_KEY, '1')
  } else {
    localStorage.removeItem(CONSENT_KEY)
  }
}
