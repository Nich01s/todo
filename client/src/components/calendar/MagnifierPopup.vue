<template>
  <div class="magnifier-backdrop" @click="$emit('close')">
    <div class="magnifier" :class="position" @click.stop>
      <div class="mag-date">{{ day.dateStr }} {{ day.lunar }}</div>
      <div class="mag-todos">
        <div class="mag-item" v-for="t in todos" :key="t.id"
             :class="{ completed: t.completed === 1 }">
          <span class="dot" :class="priorityClass(t.priority)"></span>
          <span>{{ t.title }}</span>
        </div>
        <div class="mag-empty" v-if="!todos.length">暂无待办</div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({ day: Object, todos: Array, position: { type: String, default: 'top-right' } })
defineEmits(['close'])

function priorityClass(p) {
  if (p === 2) return 'high'
  if (p === 1) return 'mid'
  return 'low'
}
</script>

<style scoped>
.magnifier-backdrop { position: fixed; inset: 0; z-index: 1000; background: transparent; }
.magnifier { position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); background: var(--bg-primary); border: 1px solid var(--border-color); border-radius: var(--radius-lg); box-shadow: var(--shadow-popup); padding: 16px 18px; min-width: 240px; max-width: 320px; }
.mag-date { font-weight: 700; font-size: 15px; color: var(--brand-color); margin-bottom: 8px; }
.mag-todos { display: flex; flex-direction: column; gap: 5px; }
.mag-item { display: flex; align-items: center; gap: 6px; font-size: 12px; padding: 5px 6px; border-radius: 4px; background: var(--bg-secondary); }
.mag-item.completed span { text-decoration: line-through; opacity: 0.5; }
.mag-item .dot { width: 6px; height: 6px; border-radius: 50%; }
.mag-item .dot.high { background: var(--danger); }
.mag-item .dot.mid { background: var(--warning); }
.mag-item .dot.low { background: var(--success); }
.mag-empty { font-size: 12px; opacity: 0.4; text-align: center; padding: 12px 0; }
</style>
