import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as authApi from '../api/auth'
import * as userApi from '../api/user'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const isLoggedIn = ref(false)

  function init() {
    const token = localStorage.getItem('accessToken')
    if (token) {
      isLoggedIn.value = true
      loadProfile()
    }
  }

  async function loadProfile() {
    try {
      const res = await userApi.getProfile()
      user.value = res.data.data
    } catch (e) {
      // ignore if token expired
    }
  }

  async function loginAction(credentials) {
    const res = await authApi.login(credentials)
    const d = res.data.data
    localStorage.setItem('accessToken', d.accessToken)
    localStorage.setItem('refreshToken', d.refreshToken)
    isLoggedIn.value = true
    user.value = { id: d.userId, username: d.username, avatar: d.avatar }
    return d
  }

  async function registerAction(credentials) {
    const res = await authApi.register(credentials)
    const d = res.data.data
    localStorage.setItem('accessToken', d.accessToken)
    localStorage.setItem('refreshToken', d.refreshToken)
    isLoggedIn.value = true
    user.value = { id: d.userId, username: d.username, avatar: d.avatar }
    return d
  }

  async function logoutAction() {
    try { await authApi.logout() } catch (e) {}
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    isLoggedIn.value = false
    user.value = null
  }

  return { user, isLoggedIn, init, loginAction, registerAction, logoutAction, loadProfile }
})
