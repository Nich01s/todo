<template>
  <div class="calendar-panel">
    <MonthNav :current="current" @prev="prevMonth" @next="nextMonth" />
    <div class="cal-error" v-if="error">{{ error }}</div>
    <CalendarGrid :year="current.year" :month="current.month" :todos="todos" @dayClick="onDayClick" />
    <MagnifierPopup v-if="selectedDay" :day="selectedDay" :todos="selectedTodos" :position="popPos"
                    @close="selectedDay = null" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTodos } from '../../api/todos'
import MonthNav from './MonthNav.vue'
import CalendarGrid from './CalendarGrid.vue'
import MagnifierPopup from './MagnifierPopup.vue'

const current = ref({ year: new Date().getFullYear(), month: new Date().getMonth() + 1 })
const todos = ref([])
const selectedDay = ref(null)
const popPos = ref('top-right')
const error = ref('')

function prevMonth() {
  if (current.value.month === 1) { current.value.month = 12; current.value.year-- }
  else current.value.month--
  loadTodos()
}
function nextMonth() {
  if (current.value.month === 12) { current.value.month = 1; current.value.year++ }
  else current.value.month++
  loadTodos()
}

const selectedTodos = computed(() => {
  if (!selectedDay.value) return []
  return todos.value.filter(t => t.dueDate === selectedDay.value.dateStr)
})

function onDayClick(day, rect) {
  selectedDay.value = day
  const col = rect ? rect.left / rect.width : 0.5
  const row = rect ? rect.top / rect.height : 0.5
  if (col > 0.67) popPos.value = 'top-left'
  else if (row > 0.75) popPos.value = 'top-right'
  else popPos.value = 'top-right'
}

async function loadTodos() {
  try {
    const res = await getTodos({ sort: 'due_date', page: 1, size: 200 })
    todos.value = res.data.data.records || []
  } catch (e) {
    error.value = '加载待办失败'
    console.error('加载待办失败', e)
  }
}

onMounted(loadTodos)
</script>

<style scoped>
.cal-error { padding: 8px 12px; background: rgba(239,68,68,0.1); color: var(--danger); border-radius: var(--radius); font-size: 13px; margin-bottom: 8px; }
.calendar-panel {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 12px;
  display: flex;
  flex-direction: column;
  height: 100%;
}
</style>
