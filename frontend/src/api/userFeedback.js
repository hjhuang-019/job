import http from './http'

export function submitUserFeedback(data) {
  return http.post('/user-feedback', data)
}

export function getMyUserFeedback(params) {
  return http.get('/user-feedback/my', { params })
}

export function getAdminUserFeedback(params) {
  return http.get('/admin/user-feedback', { params })
}

export function resolveAdminUserFeedback(id, data) {
  return http.put(`/admin/user-feedback/${id}/resolve`, data)
}
