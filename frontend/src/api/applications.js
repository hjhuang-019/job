import http from './http'

export function createApplication(data) {
  return http.post('/applications', data)
}

export function getMyApplications() {
  return http.get('/applications/my')
}

export function getJobApplications(jobId) {
  return http.get(`/applications/job/${jobId}`)
}

export function updateApplicationStatus(id, data) {
  return http.put(`/applications/${id}/status`, data)
}
