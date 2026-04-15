/** 求职者侧展示的求职进度文案（与后端状态码一致） */
export function applicationProgressText(status) {
  return (
    {
      SUBMITTED: '已投递',
      VIEWED: '企业已查阅',
      COMMUNICATING: '待面试',
      ACCEPTED: '已录用',
      REJECTED: '流程中止'
    }[status] || status || '—'
  )
}

export function applicationProgressTagType(status) {
  return (
    {
      SUBMITTED: 'info',
      VIEWED: 'warning',
      COMMUNICATING: 'primary',
      ACCEPTED: 'success',
      REJECTED: 'danger'
    }[status] || 'info'
  )
}

/** 企业端操作面板仍用简短状态名 */
export function enterpriseStatusLabel(status) {
  return (
    {
      SUBMITTED: '已投递',
      VIEWED: '已查阅',
      COMMUNICATING: '沟通/待面试',
      ACCEPTED: '已录用',
      REJECTED: '已拒绝'
    }[status] || status
  )
}
