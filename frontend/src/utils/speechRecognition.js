export function getSpeechRecognitionCtor() {
  if (typeof window === 'undefined') {
    return null
  }
  return window.SpeechRecognition || window.webkitSpeechRecognition || null
}

export function isBrowserSpeechRecognitionSupported() {
  return Boolean(getSpeechRecognitionCtor())
}

/**
 * 单次语音识别（浏览器 Web Speech API），识别结果不落库。
 * @param {object} options
 * @param {string} [options.lang]
 * @param {(text: string) => void} [options.onInterim]
 * @param {AbortSignal} [options.signal]
 */
export function runSpeechRecognitionOnce(options = {}) {
  const { lang = 'zh-CN', onInterim, signal } = options
  const Ctor = getSpeechRecognitionCtor()
  if (!Ctor) {
    return Promise.reject(new Error('BROWSER_UNSUPPORTED'))
  }

  return new Promise((resolve, reject) => {
    const rec = new Ctor()
    rec.lang = lang
    rec.interimResults = true
    rec.continuous = false

    let lastFinal = ''
    let aborted = false

    const onAbort = () => {
      aborted = true
      try {
        rec.stop()
      } catch {
        /* ignore */
      }
    }

    if (signal) {
      if (signal.aborted) {
        reject(new Error('ABORTED'))
        return
      }
      signal.addEventListener('abort', onAbort, { once: true })
    }

    rec.onresult = (event) => {
      let interim = ''
      for (let i = event.resultIndex; i < event.results.length; i += 1) {
        const piece = event.results[i]
        if (piece.isFinal) {
          lastFinal += piece[0].transcript
        } else {
          interim += piece[0].transcript
        }
      }
      if (onInterim) {
        onInterim(`${lastFinal}${interim}`.trim())
      }
    }

    rec.onerror = (event) => {
      const code = event.error || 'recognition_error'
      if (code === 'aborted' || aborted) {
        reject(new Error('ABORTED'))
        return
      }
      if (code === 'not-allowed') {
        reject(new Error('NOT_ALLOWED'))
        return
      }
      reject(new Error(code))
    }

    rec.onend = () => {
      if (signal) {
        signal.removeEventListener('abort', onAbort)
      }
      if (aborted) {
        reject(new Error('ABORTED'))
        return
      }
      resolve(lastFinal.trim())
    }

    try {
      rec.start()
    } catch (error) {
      reject(error)
    }
  })
}

/**
 * 按住说话：continuous 识别，调用 stop() 结束并等待 finished Promise。
 * @param {object} [options]
 * @param {string} [options.lang]
 * @param {(text: string) => void} [options.onInterim]
 * @returns {{ stop: () => void, abort: () => void, finished: Promise<string> }}
 */
export function startContinuousRecognitionSession(options = {}) {
  const { lang = 'zh-CN', onInterim } = options
  const Ctor = getSpeechRecognitionCtor()
  if (!Ctor) {
    throw new Error('BROWSER_UNSUPPORTED')
  }

  const rec = new Ctor()
  rec.lang = lang
  rec.continuous = true
  rec.interimResults = true

  let finalBuffer = ''
  let settled = false
  let aborted = false

  const finished = new Promise((resolve, reject) => {
    rec.onresult = (event) => {
      let interim = ''
      for (let i = event.resultIndex; i < event.results.length; i += 1) {
        const piece = event.results[i]
        if (piece.isFinal) {
          finalBuffer += piece[0].transcript
        } else {
          interim += piece[0].transcript
        }
      }
      if (onInterim) {
        onInterim(`${finalBuffer}${interim}`.trim())
      }
    }

    rec.onerror = (event) => {
      const code = event.error || 'recognition_error'
      if (settled) {
        return
      }
      settled = true
      if (code === 'aborted' || aborted) {
        reject(new Error('ABORTED'))
        return
      }
      if (code === 'not-allowed') {
        reject(new Error('NOT_ALLOWED'))
        return
      }
      reject(new Error(code))
    }

    rec.onend = () => {
      if (settled) {
        return
      }
      settled = true
      if (aborted) {
        reject(new Error('ABORTED'))
        return
      }
      resolve(finalBuffer.trim())
    }
  })

  function stop() {
    try {
      rec.stop()
    } catch {
      /* ignore */
    }
  }

  function abort() {
    if (settled) {
      return
    }
    aborted = true
    try {
      rec.abort()
    } catch {
      try {
        rec.stop()
      } catch {
        /* ignore */
      }
    }
  }

  try {
    rec.start()
  } catch (error) {
    settled = true
    throw error
  }

  return { stop, abort, finished }
}
