import http from './http'

export function getRecommendJobs() {
  return http.get('/recommend/jobs')
}
