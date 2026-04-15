/**
 * 兼容后端 LocalDateTime 常见 JSON 形态：ISO 字符串或 [y,m,d,h,mi,s] 数组
 */
export function formatDateTime(val) {
  if (val == null || val === '') {
    return ''
  }
  if (Array.isArray(val) && val.length >= 3) {
    const [y, m, d, h = 0, mi = 0, s = 0] = val
    const pad = (n) => String(n).padStart(2, '0')
    const date = `${y}-${pad(m)}-${pad(d)}`
    if (val.length >= 6) {
      return `${date} ${pad(h)}:${pad(mi)}:${pad(s)}`
    }
    return date
  }
  if (typeof val === 'string') {
    return val.replace('T', ' ').slice(0, 19)
  }
  return String(val)
}
