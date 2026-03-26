import http from './http'

export function getEnterpriseProfile() {
  return http.get('/enterprise/profile')
}

export function updateEnterpriseProfile(data) {
  return http.put('/enterprise/profile', data)
}

export function uploadEnterpriseLicense(file) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/enterprise/license', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function submitEnterpriseVerify() {
  return http.post('/enterprise/verify')
}
