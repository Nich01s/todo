<template>
  <div class="detail-list">
    <div class="detail-title" :style="{ color: titleColor }">{{ title }}</div>
    <div class="detail-section">
      <div class="section-label">✅ 已完成 ({{ completedList.length }})</div>
      <div class="detail-item done" v-for="t in completedList" :key="t.id">
        <span class="dot" :class="priorityClass(t.priority)"></span>
        <span>{{ t.title }}</span>
      </div>
    </div>
    <div class="detail-section">
      <div class="section-label">⏳ 待完成 ({{ pendingList.length }})</div>
      <div class="detail-item pending" v-for="t in pendingList" :key="t.id">
        <span class="dot" :class="priorityClass(t.priority)"></span>
        <span>{{ t.title }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { priorityClass } from '../../utils/priority.js'

const props = defineProps({ todos: Array, title: String, titleColor: { type: String, default: '#6366f1' } })
const completedList = computed(() => props.todos.filter(t => t.completed === 1))
const pendingList = computed(() => props.todos.filter(t => t.completed !== 1))
</script>

<style scoped>
.detail-list { overflow-y: auto; font-size: 10px; }
.detail-title { font-weight: 600; font-size: 11px; margin-bottom: 8px; }
.detail-section { margin-bottom: 6px; }
.section-label { font-size: 9px; opacity: 0.4; margin-bottom: 3px; }
.detail-item { display: flex; align-items: center; gap: 4px; padding: 3px 6px; border-radius: 3px; margin-bottom: 3px; }
.detail-item.done { background: rgba(16,185,129,0.08); text-decoration: line-through; opacity: 0.6; }
.detail-item.pending { background: var(--bg-secondary); }
.detail-item .dot { width: 5px; height: 5px; border-radius: 50%; flex-shrink: 0; }
.detail-item .dot.high { background: var(--danger); }
.detail-item .dot.mid { background: var(--warning); }
.detail-item .dot.low { background: var(--success); }
</style>
