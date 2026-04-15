/** 新简历投递站内信尾部（历史文案兼容） */
const APPLY_RECEIVED_TAIL_PATTERNS = [
  '请到「岗位管理 → 投递记录」查看并处理。',
  '请到「投递记录」查看并处理。',
  '请到「 投递记录」查看并处理。'
]

/**
 * @param {string} content
 * @returns {{ prefix: string, matched: boolean }}
 */
export function splitEnterpriseApplyReceivedMessage(content) {
  const text = content == null ? '' : String(content)
  for (const tail of APPLY_RECEIVED_TAIL_PATTERNS) {
    const i = text.indexOf(tail)
    if (i !== -1) {
      return { prefix: text.slice(0, i), matched: true }
    }
  }
  return { prefix: text, matched: false }
}
