import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from 'axios'

const api = axios.create({ baseURL: '/api' })

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
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// 响应拦截：401 → 跳转登录
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(err)
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
