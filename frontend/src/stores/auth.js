import { defineStore } from 'pinia'
import { getCurrentUser, login as loginApi } from '../api/auth'

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
    userRole: (state) => state.userInfo?.role || ''
  },
  actions: {
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
      this.setToken(response.data?.token || '')
      this.setUserInfo(response.data?.userInfo || null)
      return response.data
    },
    async fetchCurrentUser() {
      if (!this.token) {
        this.initialized = true
        return null
      }

      try {
        const response = await getCurrentUser()
        this.setUserInfo(response.data)
        return response.data
      } catch (error) {
        this.clearAuth()
        throw error
      } finally {
        this.initialized = true
      }
    },
    markInitialized() {
      this.initialized = true
    }
  }
})
import { defineStore } from 'pinia'
import { getCurrentUser, login as loginApi, registerEnterprise, registerJobSeeker } from '../api/auth'

const TOKEN_KEY = 'job-platform-token'
const USER_KEY = 'job-platform-user'

function parseStoredUser() {
  const rawUser = localStorage.getItem(USER_KEY)
  if (!rawUser) {
    return null
  }

  try {
    return JSON.parse(rawUser)
  } catch (error) {
    localStorage.removeItem(USER_KEY)
    return null
  }
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    userInfo: parseStoredUser()
  }),
  getters: {
    isLoggedIn: (state) => Boolean(state.token),
    role: (state) => state.userInfo?.role || '',
    roleLabel() {
      return {
        JOB_SEEKER: '求职者',
        ENTERPRISE: '企业',
        ADMIN: '管理员'
      }[this.role] || '访客'
    },
    homePath() {
      return this.getHomePathByRole(this.role)
    }
  },
  actions: {
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
    getHomePathByRole(role) {
      if (role === 'ENTERPRISE') {
        return '/home/enterprise'
      }
      if (role === 'ADMIN') {
        return '/home/admin'
      }
      return '/home/jobseeker'
    },
    async login(formData) {
      const result = await loginApi(formData)
      this.setToken(result.data?.token || '')
      this.setUserInfo(result.data?.userInfo || null)
      return result.data
    },
    async fetchCurrentUser() {
      const result = await getCurrentUser()
      this.setUserInfo(result.data)
      return result.data
    },
    async registerByRole(role, formData) {
      if (role === 'ENTERPRISE') {
        return registerEnterprise(formData)
      }
      return registerJobSeeker(formData)
    },
    logout() {
      this.setToken('')
      this.setUserInfo(null)
    }
  }
})
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { getCurrentUser, login } from '../api/auth'

const TOKEN_KEY = 'job-platform-token'

const ROLE_HOME_MAP = {
  JOB_SEEKER: '/jobseeker/home',
  ENTERPRISE: '/enterprise/home',
  ADMIN: '/admin/home'
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const userInfo = ref(null)
  const initialized = ref(false)
  const loadingUser = ref(false)

  const isLoggedIn = computed(() => Boolean(token.value))
  const role = computed(() => userInfo.value?.role || '')
  const homePath = computed(() => ROLE_HOME_MAP[role.value] || '/home')

  function setToken(value) {
    token.value = value || ''
    if (token.value) {
      localStorage.setItem(TOKEN_KEY, token.value)
    } else {
      localStorage.removeItem(TOKEN_KEY)
    }
  }

  function setUserInfo(value) {
    userInfo.value = value || null
  }

  async function loginAction(formData) {
    const response = await login(formData)
    const loginData = response.data
    setToken(loginData?.token || '')
    setUserInfo(loginData?.userInfo || null)
    initialized.value = true
    return loginData
  }

  async function fetchCurrentUser() {
    if (!token.value) {
      setUserInfo(null)
      initialized.value = true
      return null
    }
    loadingUser.value = true
    try {
      const response = await getCurrentUser()
      setUserInfo(response.data || null)
      initialized.value = true
      return response.data
    } catch (error) {
      clearAuth()
      throw error
    } finally {
      loadingUser.value = false
    }
  }

  async function initializeAuth() {
    if (initialized.value) {
      return userInfo.value
    }
    if (!token.value) {
      initialized.value = true
      return null
    }
    try {
      return await fetchCurrentUser()
    } catch (error) {
      return null
    }
  }

  function clearAuth() {
    setToken('')
    setUserInfo(null)
    initialized.value = true
  }

  return {
    token,
    userInfo,
    initialized,
    loadingUser,
    isLoggedIn,
    role,
    homePath,
    loginAction,
    fetchCurrentUser,
    initializeAuth,
    setToken,
    setUserInfo,
    clearAuth
  }
})
