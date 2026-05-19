import { defineStore } from 'pinia'
import { ref } from 'vue'

// 和风天气 API Key — 使用前请替换为你的 Key（免费注册: https://dev.qweather.com/）
const API_KEY = import.meta.env.VITE_HEFENG_API_KEY || ''

export const useWeatherStore = defineStore('weather', () => {
  const city = ref(localStorage.getItem('weatherCity') || '杭州')
  const cityId = ref(localStorage.getItem('weatherCityId') || '101210101')
  const current = ref(null)
  const forecast = ref([])
  const loading = ref(false)

  async function fetchWeather() {
    loading.value = true
    try {
      const nowRes = await fetch(
        `https://devapi.qweather.com/v7/weather/now?location=${cityId.value}&key=${API_KEY}`
      )
      const nowData = await nowRes.json()
      if (nowData.code === '200') {
        current.value = nowData.now
      }

      const forecastRes = await fetch(
        `https://devapi.qweather.com/v7/weather/3d?location=${cityId.value}&key=${API_KEY}`
      )
      const forecastData = await forecastRes.json()
      if (forecastData.code === '200') {
        forecast.value = forecastData.daily
      }
    } catch (e) {
      console.error('天气获取失败', e)
    } finally {
      loading.value = false
    }
  }

  function setCity(name, id) {
    city.value = name
    cityId.value = id
    localStorage.setItem('weatherCity', name)
    localStorage.setItem('weatherCityId', id)
    fetchWeather()
  }

  return { city, cityId, current, forecast, loading, fetchWeather, setCity }
})
