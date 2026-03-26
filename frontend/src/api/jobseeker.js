import http from './http'

export function getJobSeekerProfile() {
  return http.get('/jobseeker/profile')
}

export function updateJobSeekerProfile(data) {
  return http.put('/jobseeker/profile', data)
}

export function uploadCertificate(file) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/jobseeker/certificate', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function submitJobSeekerVerify() {
  return http.post('/jobseeker/verify')
}
