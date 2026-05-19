import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

// Mock localStorage with standard browser API
const localStorageMock = (() => {
  let store = {}
  return {
    getItem: vi.fn((key) => store[key] ?? null),
    setItem: vi.fn((key, value) => { store[key] = String(value) }),
    removeItem: vi.fn((key) => { delete store[key] }),
    clear: vi.fn(() => { store = {} })
  }
})()

Object.defineProperty(globalThis, 'localStorage', {
  value: localStorageMock,
  writable: true,
  configurable: true
})

import { useAuthStore } from '../auth'

// Mock API modules
vi.mock('../../api/auth', () => ({
  login: vi.fn(),
  register: vi.fn(),
  logout: vi.fn(),
  refresh: vi.fn()
}))

vi.mock('../../api/user', () => ({
  getProfile: vi.fn()
}))

import * as authApi from '../../api/auth'
import * as userApi from '../../api/user'

describe('useAuthStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorageMock.clear()
    vi.clearAllMocks()
  })

  it('starts logged out', () => {
    const store = useAuthStore()
    expect(store.isLoggedIn).toBe(false)
    expect(store.user).toBeNull()
  })

  it('loginAction sets tokens and user', async () => {
    authApi.login.mockResolvedValue({
      data: { data: { accessToken: 'at', refreshToken: 'rt', userId: 1, username: 'test', avatar: null } }
    })
    const store = useAuthStore()
    await store.loginAction({ username: 'test', password: 'pw' })
    expect(store.isLoggedIn).toBe(true)
    expect(localStorageMock.getItem('accessToken')).toBe('at')
    expect(localStorageMock.getItem('refreshToken')).toBe('rt')
  })

  it('registerAction sets tokens and user', async () => {
    authApi.register.mockResolvedValue({
      data: { data: { accessToken: 'at2', refreshToken: 'rt2', userId: 2, username: 'new', avatar: null } }
    })
    const store = useAuthStore()
    await store.registerAction({ username: 'new', password: 'pw' })
    expect(store.isLoggedIn).toBe(true)
    expect(localStorageMock.getItem('accessToken')).toBe('at2')
  })

  it('logoutAction clears state', async () => {
    authApi.login.mockResolvedValue({
      data: { data: { accessToken: 'at', refreshToken: 'rt', userId: 1, username: 'x', avatar: null } }
    })
    const store = useAuthStore()
    await store.loginAction({ username: 'x', password: 'pw' })
    expect(store.isLoggedIn).toBe(true)

    await store.logoutAction()
    expect(store.isLoggedIn).toBe(false)
    expect(store.user).toBeNull()
    expect(localStorageMock.getItem('accessToken')).toBeNull()
  })

  it('init detects existing token', async () => {
    localStorageMock.setItem('accessToken', 'existing')
    userApi.getProfile.mockResolvedValue({
      data: { data: { id: 3, username: 'returning', avatar: null } }
    })
    const store = useAuthStore()
    store.init()
    expect(store.isLoggedIn).toBe(true)
    // Wait for the async loadProfile to complete
    await new Promise(r => setTimeout(r, 10))
    expect(store.user?.username).toBe('returning')
  })
})
