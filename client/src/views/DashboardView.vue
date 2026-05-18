<template>
  <div class="dashboard">
    <CalendarPanel class="dash-left" />
    <div class="dash-right">
      <WeatherWidget />
      <StatsCard />
      <QuickAddBar @added="refresh" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import CalendarPanel from '../components/calendar/CalendarPanel.vue'
import WeatherWidget from '../components/weather/WeatherWidget.vue'
import StatsCard from '../components/stats/StatsCard.vue'
import QuickAddBar from '../components/todo/QuickAddBar.vue'

const refreshKey = ref(0)
function refresh() { refreshKey.value++ }
</script>

<style scoped>
.dashboard {
  display: flex;
  gap: 14px;
  padding: 16px;
  height: calc(100vh - 56px);
  overflow: hidden;
}
.dash-left { flex: 1; min-width: 0; }
.dash-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 0;
}
</style>
