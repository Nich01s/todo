import api from './index.js'
export const getDailyStats = () => api.get('/stats/daily')
export const getWeeklyStats = () => api.get('/stats/weekly')
