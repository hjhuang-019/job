import { defineStore } from 'pinia'
import {
  getCurrentUser,
  login as loginApi,
  registerEnterprise,
  registerJobSeeker
} from '../api/auth'

const TOKEN_KEY = 'job-platform-token'
const USER_KEY = 'job-platform-user'

function readUserFromStorage() {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw)
  } catch (error) {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    userInfo: readUserFromStorage(),
    initialized: false
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    userRole: (state) => state.userInfo?.role || '',
    isBlacklisted: (state) => Boolean(state.userInfo?.blacklisted)
  },
  actions: {
    unwrapPayload(response) {
      // http interceptor already returns response.data; keep compatibility if it changes.
      if (response && typeof response === 'object' && 'data' in response) {
        return response.data
      }
      return response
    },
    setToken(token) {
      this.token = token || ''
      if (this.token) {
        localStorage.setItem(TOKEN_KEY, this.token)
      } else {
        localStorage.removeItem(TOKEN_KEY)
      }
    },
    setUserInfo(userInfo) {
      this.userInfo = userInfo || null
      if (this.userInfo) {
        localStorage.setItem(USER_KEY, JSON.stringify(this.userInfo))
      } else {
        localStorage.removeItem(USER_KEY)
      }
    },
    clearAuth() {
      this.setToken('')
      this.setUserInfo(null)
      this.initialized = true
    },
    markInitialized() {
      this.initialized = true
    },
    getHomePathByRole(role = this.userRole) {
      if (role === 'ENTERPRISE') {
        return '/enterprise/home'
      }
      if (role === 'ADMIN') {
        return '/admin/home'
      }
      return '/home'
    },
    async login(loginForm) {
      const response = await loginApi(loginForm)
      const loginData = this.unwrapPayload(response) || {}
      this.setToken(loginData?.token || '')
      this.setUserInfo(loginData?.userInfo || null)
      this.initialized = true
      return loginData
    },
    async registerByRole(role, formData) {
      if (role === 'ENTERPRISE') {
        return registerEnterprise(formData)
      }
      return registerJobSeeker(formData)
    },
    async fetchCurrentUser() {
      if (!this.token) {
        this.initialized = true
        return null
      }

      try {
        const response = await getCurrentUser()
        const userData = this.unwrapPayload(response) || null
        this.setUserInfo(userData)
        return userData
      } catch (error) {
        this.clearAuth()
        throw error
      } finally {
        this.initialized = true
      }
    }
  }
})
