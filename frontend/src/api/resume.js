import http from './http'

export function createResume(data) {
  return http.post('/resumes', data)
}

export function getResumeList() {
  return http.get('/resumes')
}

export function getResumeDetail(id) {
  return http.get(`/resumes/${id}`)
}

export function updateResume(id, data) {
  return http.put(`/resumes/${id}`, data)
}

export function deleteResume(id) {
  return http.delete(`/resumes/${id}`)
}
