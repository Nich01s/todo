<template>
  <div class="month-nav">
    <button @click="$emit('prev')">◀</button>
    <span class="month-title">{{ current.year }}年 {{ current.month }}月</span>
    <button @click="$emit('next')">▶</button>
  </div>
</template>

<script setup>
defineProps({ current: Object })
defineEmits(['prev', 'next'])
</script>

<style scoped>
.month-nav { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.month-nav button { background: none; border: none; cursor: pointer; font-size: 14px; color: var(--text-secondary); padding: 4px 8px; }
.month-title { font-weight: 700; font-size: 16px; }
</style>
