import api from './index.js'
export const login = (data) => api.post('/auth/login', data)
export const register = (data) => api.post('/auth/register', data)
export const refresh = (data) => api.post('/auth/refresh', data)
export const logout = () => api.post('/auth/logout')
