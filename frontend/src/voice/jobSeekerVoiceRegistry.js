const pages = new Map()

/**
 * @param {string} routeName 与 vue-router 的 route.name 一致
 * @param {{ getPageContext: () => object, onInterpret: (vo: object) => void | Promise<void> }} handlers
 */
export function registerJobSeekerVoicePage(routeName, handlers) {
  if (!routeName) {
    return () => {}
  }
  pages.set(routeName, handlers)
  return () => {
    pages.delete(routeName)
  }
}

export function getJobSeekerVoicePage(routeName) {
  if (!routeName) {
    return null
  }
  return pages.get(routeName) || null
}

export function resolveVoiceScene(routeName) {
  const map = {
    jobSeekerProfile: 'PROFILE',
    resumeCreate: 'RESUME',
    resumeEdit: 'RESUME',
    jobList: 'JOB_LIST',
    jobDetail: 'JOB_DETAIL',
    myApplications: 'MY_APPLICATIONS',
    messageCenter: 'MESSAGES',
    jobSeekerVerify: 'VERIFY',
    jobSeekerHome: 'HOME'
  }
  return map[routeName] || 'GLOBAL'
}
