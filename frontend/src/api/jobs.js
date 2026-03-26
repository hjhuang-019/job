import http from './http'

export function createJob(data) {
  return http.post('/jobs', data)
}

export function getPublishedJobs(params) {
  return http.get('/jobs', { params })
}

export function updateJob(id, data) {
  return http.put(`/jobs/${id}`, data)
}

export function deleteJob(id) {
  return http.delete(`/jobs/${id}`)
}

export function getMyJobs() {
  return http.get('/jobs/my')
}

export function updateJobStatus(id, status) {
  return http.put(`/jobs/${id}/status`, { status })
}

export function getJobDetail(id) {
  return http.get(`/jobs/${id}`)
}
