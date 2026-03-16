import http from './http'

export function pingServer() {
  return http.get('/test/ping')
}
