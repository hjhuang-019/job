import http from './http'

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
