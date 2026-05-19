<template>
  <div class="day-cell" :class="{ today: day?.isToday, empty: !day }" @click="handleClick" ref="cellRef">
    <template v-if="day">
      <div class="day-header">
        <span class="solar" :class="{ todayText: day.isToday }">{{ day.day }}</span>
        <span class="lunar">{{ day.lunar }}</span>
      </div>
      <div class="todo-strip" v-for="t in visibleTodos" :key="t.id">
        <span class="dot" :class="priorityClass(t.priority)"></span>
        <span :class="{ completed: t.completed === 1 }">{{ t.title }}</span>
      </div>
      <div class="more" v-if="todos.length > maxVisible">+{{ todos.length - maxVisible }} 更多</div>
    </template>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { priorityClass } from '../../utils/priority.js'

const props = defineProps({ day: Object, todos: Array })
const emit = defineEmits(['click'])
const cellRef = ref(null)
const maxVisible = 2

const visibleTodos = computed(() => props.todos?.slice(0, maxVisible) || [])

function handleClick() {
  if (!props.day) return
  emit('click', cellRef.value?.getBoundingClientRect())
}
</script>

<style scoped>
.day-cell {
  border: 1px solid var(--border-color);
  border-radius: var(--radius);
  padding: 2px 3px;
  font-size: 9px;
  text-align: left;
  background: var(--bg-primary);
  cursor: pointer;
  position: relative;
  min-height: 48px;
  overflow: hidden;
}
.day-cell.empty { background: transparent; border: none; cursor: default; }
.day-cell.today { border: 2px solid var(--brand-color); background: var(--brand-light); }
.day-header { display: flex; justify-content: space-between; margin-bottom: 1px; }
.solar { font-weight: 600; font-size: 11px; }
.todayText { color: var(--brand-color); }
.lunar { font-size: 7px; opacity: 0.45; }
.todo-strip { font-size: 7px; margin-top: 1px; display: flex; align-items: center; gap: 2px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.todo-strip .dot { width: 4px; height: 4px; border-radius: 50%; flex-shrink: 0; }
.todo-strip .dot.high { background: var(--danger); }
.todo-strip .dot.mid { background: var(--warning); }
.todo-strip .dot.low { background: var(--success); }
.todo-strip .completed { text-decoration: line-through; opacity: 0.6; }
.more { font-size: 7px; color: var(--brand-color); font-weight: 600; margin-top: 1px; }
</style>
