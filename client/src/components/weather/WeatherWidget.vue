<template>
  <div class="weather-widget">
    <div class="weather-row">
      <button class="pin-btn" @click="showCitySearch = true" title="切换城市">
        <span class="pin-icon">📍</span>
      </button>
      <div class="weather-icon">{{ weatherIcon }}</div>
      <div class="weather-desc">{{ currentText }}</div>
      <div class="weather-temp">{{ weather.current?.temp }}°</div>
      <div class="weather-humidity">湿度 {{ weather.current?.humidity }}%</div>
      <span class="weather-city">{{ weather.city }}</span>
      <div class="weather-divider"></div>
      <div class="forecast-row">
        <div class="fc-day" v-for="(d, i) in weather.forecast" :key="i">
          <div class="fc-label">{{ i === 0 ? '明天' : dayOfWeek(i + 1) }}</div>
          <div class="fc-icon">{{ forecastIcon(d) }}</div>
          <div class="fc-high">{{ d.tempMax }}°</div>
          <div class="fc-low">{{ d.tempMin }}°</div>
        </div>
      </div>
    </div>
    <CitySearchDialog v-if="showCitySearch" @select="onCitySelect" @close="showCitySearch = false" />
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useWeatherStore } from '../../stores/weather'
import CitySearchDialog from './CitySearchDialog.vue'

const weather = useWeatherStore()
const showCitySearch = ref(false)

const currentText = computed(() => weather.current?.text || '--')
const weatherIcon = computed(() => {
  const t = weather.current?.text || ''
  if (t.includes('晴')) return '☀️'
  if (t.includes('云')) return '⛅'
  if (t.includes('雨')) return '🌧'
  if (t.includes('雪')) return '❄️'
  return '🌈'
})

function forecastIcon(d) {
  const t = d.textDay || ''
  if (t.includes('晴')) return '☀️'
  if (t.includes('云')) return '⛅'
  if (t.includes('雨')) return '🌧'
  return '🌈'
}

function dayOfWeek(offset) {
  const days = ['周日','周一','周二','周三','周四','周五','周六']
  return days[(new Date().getDay() + offset) % 7]
}

function onCitySelect(name, id) {
  weather.setCity(name, id)
  showCitySearch.value = false
}

onMounted(() => { weather.fetchWeather() })
</script>

<style scoped>
.weather-widget {
  background: linear-gradient(135deg, var(--brand-light), var(--bg-secondary));
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 12px 16px;
  position: relative;
}
.weather-row { display: flex; align-items: center; gap: 12px; overflow: hidden; }
.pin-btn { background: none; border: none; cursor: pointer; font-size: 18px; padding: 0; flex-shrink: 0; }
.pin-icon { filter: drop-shadow(0 1px 2px rgba(0,0,0,0.2)); }
.weather-icon { font-size: 34px; flex-shrink: 0; }
.weather-desc { font-size: 10px; opacity: 0.5; flex-shrink: 0; }
.weather-temp { font-size: 26px; font-weight: 300; flex-shrink: 0; }
.weather-humidity { font-size: 11px; opacity: 0.45; flex-shrink: 0; }
.weather-city { font-weight: 700; font-size: 15px; flex-shrink: 0; }
.weather-divider { width: 1px; height: 36px; background: rgba(128,128,128,0.15); flex-shrink: 0; }
.forecast-row { display: flex; flex: 1; justify-content: space-around; }
.fc-day { text-align: center; }
.fc-label { font-size: 9px; opacity: 0.45; }
.fc-icon { font-size: 18px; }
.fc-high { font-size: 10px; font-weight: 600; }
.fc-low { font-size: 8px; opacity: 0.35; }
</style>
