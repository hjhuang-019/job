import http from './http'

export function pageAdminJobSeekerUsers(params) {
  return http.get('/admin/users/job-seekers', { params })
}

export function pageAdminEnterpriseUsers(params) {
  return http.get('/admin/users/enterprises', { params })
}

export function getAdminAuditLogs(params) {
  return http.get('/admin/audit-logs', { params })
}

export function blacklistUser(id, data) {
  return http.put(`/admin/users/${id}/blacklist`, data)
}

export function unblacklistUser(id) {
  return http.put(`/admin/users/${id}/unblacklist`)
}

export function getPendingJobSeekers() {
  return http.get('/admin/jobseekers/pending')
}

export function verifyJobSeeker(id, data) {
  return http.put(`/admin/jobseekers/${id}/verify`, data)
}

export function getPendingEnterprises() {
  return http.get('/admin/enterprises/pending')
}

export function verifyEnterprise(id, data) {
  return http.put(`/admin/enterprises/${id}/verify`, data)
}

export function getAdminStatistics() {
  return http.get('/admin/statistics')
}

/**
 * 待审数量：并行拉取已有待审列表并汇总（与认证审核页数据源一致，不依赖单独统计接口）。
 */
export async function getAdminPendingVerifyCount() {
  const [jobSeekerResponse, enterpriseResponse] = await Promise.all([
    getPendingJobSeekers(),
    getPendingEnterprises()
  ])
  const jobSeekerPending = jobSeekerResponse.data?.length ?? 0
  const enterprisePending = enterpriseResponse.data?.length ?? 0
  return {
    code: 200,
    data: {
      jobSeekerPending,
      enterprisePending,
      total: jobSeekerPending + enterprisePending
    }
  }
}
