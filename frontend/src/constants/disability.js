/**
 * 与《残疾人残疾分类和分级》及残疾人证常见类别对齐的选项（展示与存储均为中文文案）。
 * 多选入库时使用英文逗号拼接，与技能标签等字段习惯一致。
 */

export const DISABILITY_TYPE_OPTIONS = [
  { label: '视力残疾', value: '视力残疾' },
  { label: '听力残疾', value: '听力残疾' },
  { label: '言语残疾', value: '言语残疾' },
  { label: '肢体残疾', value: '肢体残疾' },
  { label: '智力残疾', value: '智力残疾' },
  { label: '精神残疾', value: '精神残疾' },
  { label: '多重残疾', value: '多重残疾' },
  { label: '其他残疾', value: '其他残疾' }
]

/** 历史自由填写或口语说法 → 标准选项值（无法映射则丢弃，避免 el-select 出现非法值） */
const DISABILITY_TYPE_ALIASES = {
  视力障碍: '视力残疾',
  视障: '视力残疾',
  听力障碍: '听力残疾',
  听障: '听力残疾',
  语言障碍: '言语残疾',
  言语障碍: '言语残疾',
  哑巴: '言语残疾',
  肢体障碍: '肢体残疾',
  智障: '智力残疾',
  智力障碍: '智力残疾',
  精神障碍: '精神残疾',
  多重: '多重残疾',
  其他: '其他残疾'
}

const ALLOWED_TYPE_VALUES = new Set(DISABILITY_TYPE_OPTIONS.map((o) => o.value))

/**
 * @param {string} raw
 * @returns {string[]}
 */
export function parseDisabilityTypesFromStorage(raw) {
  if (raw == null || String(raw).trim() === '') {
    return []
  }
  const parts = String(raw)
    .split(/[,，、;；\s]+/)
    .map((s) => s.trim())
    .filter(Boolean)
  const out = []
  const seen = new Set()
  for (const p of parts) {
    const mapped = DISABILITY_TYPE_ALIASES[p] || (ALLOWED_TYPE_VALUES.has(p) ? p : null)
    if (mapped && !seen.has(mapped)) {
      seen.add(mapped)
      out.push(mapped)
    }
  }
  return out
}

/**
 * @param {string[]} list
 * @returns {string}
 */
export function serializeDisabilityTypesForStorage(list) {
  if (!Array.isArray(list)) {
    return ''
  }
  return list
    .map((s) => String(s).trim())
    .filter((s) => ALLOWED_TYPE_VALUES.has(s))
    .filter((s, i, a) => a.indexOf(s) === i)
    .join(',')
}

export const DISABILITY_LEVEL_OPTIONS = [
  { label: '一级', value: '一级' },
  { label: '二级', value: '二级' },
  { label: '三级', value: '三级' },
  { label: '四级', value: '四级' }
]

const ALLOWED_LEVEL_VALUES = new Set(DISABILITY_LEVEL_OPTIONS.map((o) => o.value))

const DISABILITY_LEVEL_ALIASES = {
  1: '一级',
  2: '二级',
  3: '三级',
  4: '四级',
  一级: '一级',
  二级: '二级',
  三级: '三级',
  四级: '四级',
  一级残疾: '一级',
  二级残疾: '二级',
  三级残疾: '三级',
  四级残疾: '四级',
  重度: '二级',
  中度: '三级',
  轻度: '四级'
}

/**
 * @param {string} raw
 * @returns {string}
 */
export function normalizeDisabilityLevelFromStorage(raw) {
  if (raw == null || String(raw).trim() === '') {
    return ''
  }
  const s = String(raw).trim()
  if (ALLOWED_LEVEL_VALUES.has(s)) {
    return s
  }
  const mapped = DISABILITY_LEVEL_ALIASES[s]
  if (mapped && ALLOWED_LEVEL_VALUES.has(mapped)) {
    return mapped
  }
  return ''
}
