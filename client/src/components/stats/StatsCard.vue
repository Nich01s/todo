<template>
  <div class="stats-card">
    <div class="stats-body">
      <div class="stats-left">
        <RingChart :completed="todayCompleted" :total="todayTotal" label="今日完成"
                   title="今日进度" :subtitle="`已完成 ${todayRate}%`" progressColor="#6366f1" subColor="#6366f1"
                   :selected="activeTab === 'today'" @click="switchTo('today')" />
        <RingChart :completed="weekCompleted" :total="weekTotal" label="本周完成"
                   title="本周总览" :subtitle="`剩余任务 ${weekRemaining} 项`" progressColor="#10b981" subColor="#ef4444"
                   :selected="activeTab === 'week'" @click="switchTo('week')" />
      </div>
      <TaskDetailList class="stats-right" :todos="activeTodos"
                      :title="activeTab === 'today' ? '📋 今日' : '📅 本周'"
                      :titleColor="activeTab === 'today' ? '#6366f1' : '#10b981'" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getTodos } from '../../api/todos'
import RingChart from './RingChart.vue'
import TaskDetailList from './TaskDetailList.vue'

const activeTab = ref('today')
const allTodos = ref([])

const todayStr = new Date().toISOString().slice(0, 10)
const todayTodos = computed(() => allTodos.value.filter(t => t.dueDate === todayStr))
const todayCompleted = computed(() => todayTodos.value.filter(t => t.completed === 1).length)
const todayTotal = computed(() => todayTodos.value.length)
const todayRate = computed(() => todayTotal.value > 0 ? Math.round(todayCompleted.value / todayTotal.value * 100) : 0)

const weekTodos = computed(() => {
  const now = new Date()
  const day = now.getDay()
  const monday = new Date(now.getFullYear(), now.getMonth(), now.getDate() - (day === 0 ? 6 : day - 1)).toISOString().slice(0, 10)
  return allTodos.value.filter(t => t.dueDate && t.dueDate >= monday && t.dueDate <= todayStr)
})
const weekCompleted = computed(() => weekTodos.value.filter(t => t.completed === 1).length)
const weekTotal = computed(() => weekTodos.value.length)
const weekRemaining = computed(() => weekTotal.value - weekCompleted.value)

const activeTodos = computed(() => activeTab.value === 'today' ? todayTodos.value : weekTodos.value)

function switchTo(tab) { activeTab.value = tab }

onMounted(async () => {
  try {
    const res = await getTodos({ sort: 'due_date', page: 1, size: 200 })
    allTodos.value = res.data.data.records || []
  } catch (e) { console.error('加载统计数据失败', e) }
})
</script>

<style scoped>
.stats-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 14px;
  flex: 1;
  overflow: hidden;
  display: flex;
}
.stats-body { display: flex; gap: 14px; width: 100%; }
.stats-left { flex: 1.35; display: flex; align-items: center; justify-content: space-around; gap: 12px; }
.stats-right { flex: 1; background: var(--bg-primary); border: 1px solid var(--border-color); border-radius: var(--radius); padding: 10px; overflow-y: auto; }
</style>
