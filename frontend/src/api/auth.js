import http from './http'

export function login(data) {
  return http.post('/auth/login', data)
}

export function registerJobSeeker(data) {
  return http.post('/auth/register/jobseeker', data)
}

export function registerEnterprise(data) {
  return http.post('/auth/register/enterprise', data)
}

export function getCurrentUser() {
  return http.get('/auth/me')
}
