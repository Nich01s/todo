<template>
  <div class="cal-grid">
    <div class="cal-header" v-for="d in dayHeaders" :key="d">{{ d }}</div>
    <DayCell v-for="(day, i) in days" :key="i" :day="day"
             :todos="todosForDay(day)" @click="(rect) => $emit('dayClick', day, rect)" />
  </div>
  <div class="cal-legend">
    <span><span class="dot high"></span> 高</span>
    <span><span class="dot mid"></span> 中</span>
    <span><span class="dot low"></span> 低</span>
    <span class="hint">💡 单击格子查看详情</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import DayCell from './DayCell.vue'

const props = defineProps({ year: Number, month: Number, todos: Array })
defineEmits(['dayClick'])

const dayHeaders = ['日', '一', '二', '三', '四', '五', '六']

// Simplified Chinese lunar calendar
const LUNAR_MONTH = ['正','二','三','四','五','六','七','八','九','十','冬','腊']
const LUNAR_DAY = ['一','二','三','四','五','六','七','八','九','十','十一','十二','十三','十四','十五','十六','十七','十八','十九','二十','廿一','廿二','廿三','廿四','廿五','廿六','廿七','廿八','廿九','三十']
const CHINESE_HOLIDAYS = {
  '1-1': '元旦', '2-14': '情人节', '3-8': '妇女节',
  '4-1': '愚人节', '4-5': '清明', '5-1': '劳动节',
  '5-4': '青年节', '6-1': '儿童节', '7-1': '建党',
  '8-1': '建军', '9-10': '教师', '10-1': '国庆节',
  '10-31': '万圣节', '12-25': '圣诞'
}

// 简化农历显示（精确农历需集成 lunar-javascript 库）
function getLunarOrHoliday(d) {
  const key = `${props.month}-${d.getDate()}`
  if (CHINESE_HOLIDAYS[key]) return CHINESE_HOLIDAYS[key]
  const lunarDay = ((d.getDate() - 1) % 30)
  const lunarMonth = (props.month - 1) % 12
  return LUNAR_MONTH[lunarMonth] + '月' + LUNAR_DAY[lunarDay]
}

function todosForDay(day) {
  if (!day) return []
  return props.todos.filter(t => t.dueDate === day.dateStr)
}

const days = computed(() => {
  const first = new Date(props.year, props.month - 1, 1)
  const startDay = first.getDay()
  const daysInMonth = new Date(props.year, props.month, 0).getDate()
  const result = []
  for (let i = 0; i < startDay; i++) result.push(null)
  const todayStr = new Date().toISOString().slice(0, 10)
  for (let d = 1; d <= daysInMonth; d++) {
    const date = new Date(props.year, props.month - 1, d)
    const dateStr = `${props.year}-${String(props.month).padStart(2,'0')}-${String(d).padStart(2,'0')}`
    const isToday = dateStr === todayStr
    result.push({ dateStr, day: d, weekDay: date.getDay(), lunar: getLunarOrHoliday(date), isToday })
  }
  return result
})
</script>

<style scoped>
.cal-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 4px; flex: 1; font-size: 10px; text-align: center; }
.cal-header { opacity: 0.4; font-weight: 600; padding: 2px; }
.cal-legend { display: flex; gap: 12px; margin-top: 8px; font-size: 10px; opacity: 0.5; }
.dot { display: inline-block; width: 6px; height: 6px; border-radius: 50%; }
.dot.high { background: var(--danger); }
.dot.mid { background: var(--warning); }
.dot.low { background: var(--success); }
.hint { margin-left: auto; }
</style>
