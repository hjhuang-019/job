import http from './http'

export function getMessages() {
  return http.get('/messages')
}

export function markMessageRead(id) {
  return http.put(`/messages/${id}/read`)
}

export function getUnreadCount() {
  return http.get('/messages/unread-count')
}
