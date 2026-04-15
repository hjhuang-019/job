import http from './http'

export function interpretJobSeekerAi(payload) {
  return http.post('/jobseeker/ai/interpret', payload, { timeout: 90000 })
}
