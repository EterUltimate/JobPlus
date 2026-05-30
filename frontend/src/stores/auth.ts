import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios, { type InternalAxiosRequestConfig } from 'axios'

const API_UNAVAILABLE_MESSAGE = '无法连接后端服务，请先启动 JobPlus 网关服务（默认 8080，备用 18080）'

const apiBaseURLs = import.meta.env.VITE_API_BASE_URL
  ? [normalizeApiBaseURL(import.meta.env.VITE_API_BASE_URL)]
  : import.meta.env.DEV
    ? ['/api']
    : ['http://localhost:18080/api', 'http://localhost:8080/api']

const api = axios.create({
  baseURL: apiBaseURLs[0],
  timeout: 8000,
})

type RetryableRequestConfig = InternalAxiosRequestConfig & {
  _apiBaseURLIndex?: number
}

function normalizeApiBaseURL(url: string) {
  return url.replace(/\/+$/, '')
}

function normalizeApiError(err: unknown) {
  if (axios.isAxiosError(err)) {
    if (!err.response) {
      return new Error(API_UNAVAILABLE_MESSAGE)
    }

    const data = err.response.data as { message?: string } | undefined
    return new Error(data?.message || err.message || '请求失败')
  }

  return err
}

type UserRole = 'SEEKER' | 'HR' | 'ADMIN'

interface UserInfo {
  id: number
  username: string
  role: UserRole
  realName?: string
  phone?: string
  email?: string
  avatar?: string
}

// 请求拦截：注入 Token
api.interceptors.request.use(config => {
  const retryConfig = config as RetryableRequestConfig
  if (typeof retryConfig._apiBaseURLIndex !== 'number') {
    const currentBaseURL = String(retryConfig.baseURL || api.defaults.baseURL || apiBaseURLs[0])
    const currentIndex = apiBaseURLs.indexOf(currentBaseURL)
    retryConfig._apiBaseURLIndex = currentIndex >= 0 ? currentIndex : 0
  }

  const token = localStorage.getItem('token')
  if (token) retryConfig.headers.Authorization = `Bearer ${token}`
  return retryConfig
})

// 响应拦截：401 → 跳转登录
api.interceptors.response.use(
  res => res,
  err => {
    const config = err.config as RetryableRequestConfig | undefined
    if (axios.isAxiosError(err) && !err.response && config) {
      const nextIndex = (config._apiBaseURLIndex ?? 0) + 1
      if (nextIndex < apiBaseURLs.length) {
        config._apiBaseURLIndex = nextIndex
        config.baseURL = apiBaseURLs[nextIndex]
        return api.request(config)
      }
    }

    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.hash = '/login'
    }
    return Promise.reject(normalizeApiError(err))
  }
)

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => userInfo.value?.role || '')

  function clearAuthState() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
  }

  async function login(username: string, password: string) {
    const { data } = await api.post('/auth/login', { username, password })
    if (data.code === 200) {
      token.value = data.data.token
      userInfo.value = data.data
      localStorage.setItem('token', data.data.token)
      return true
    }
    throw new Error(data.message)
  }

  async function register(payload: Record<string, unknown>) {
    const { data } = await api.post('/auth/register', payload)
    if (data.code === 200) {
      token.value = data.data.token
      userInfo.value = data.data
      localStorage.setItem('token', data.data.token)
      return true
    }
    throw new Error(data.message)
  }

  async function fetchMe() {
    try {
      const { data } = await api.get('/auth/me')
      if (data.code === 200) {
        userInfo.value = data.data
      } else {
        clearAuthState()
      }
    } catch {
      clearAuthState()
    }
  }

  async function logout() {
    try {
      await api.post('/auth/logout')
    } catch {
      // Ignore network/server failures and always clear local auth state.
    }
    clearAuthState()
  }

  async function init() {
    if (token.value) {
      await fetchMe()
    }
  }

  return { token, userInfo, isLoggedIn, role, login, register, fetchMe, logout, init, api }
})

export { api }
