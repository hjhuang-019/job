import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const DUPLICATE_APPLY_MESSAGE = '已投递过该岗位'

function notifyFailure(message) {
  const text = message || '请求失败'
  if (text === DUPLICATE_APPLY_MESSAGE) {
    ElMessageBox.alert(text, '提示', {
      confirmButtonText: '知道了',
      customClass: 'jp-duplicate-apply-msgbox',
      type: 'info',
      center: true,
      showClose: true
    })
    return
  }
  ElMessage.error(text)
}

const http = axios.create({
  baseURL: '/api',
  timeout: 10000
})

http.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('job-platform-token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

http.interceptors.response.use(
  (response) => {
    const result = response.data
    if (result && typeof result === 'object' && 'code' in result) {
      if (result.code !== 200) {
        const message = result.message || '请求失败'
        notifyFailure(message)
        return Promise.reject(new Error(message))
      }
      return result
    }
    return result
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '请求失败'
    notifyFailure(message)
    return Promise.reject(error)
  }
)

export default http
