<template>
  <div class="ring-chart-wrapper" :class="{ active: selected }" @click="$emit('click')">
    <svg :width="size" :height="size" :viewBox="`0 0 ${vp} ${vp}`">
      <circle :cx="center" :cy="center" :r="radius" fill="none" :stroke="trackColor" stroke-width="8" />
      <circle :cx="center" :cy="center" :r="radius" fill="none" :stroke="progressColor"
              stroke-width="8" :stroke-dasharray="circumference" :stroke-dashoffset="dashoffset"
              stroke-linecap="round" :transform="`rotate(-90, ${center}, ${center})`" />
    </svg>
    <div class="ring-center">
      <div class="ring-num" :style="{ color: progressColor }">{{ completed }}/{{ total }}</div>
      <div class="ring-label">{{ label }}</div>
    </div>
    <div class="ring-footer">
      <div class="ring-title">{{ title }}</div>
      <div class="ring-sub" :style="{ color: subColor }">{{ subtitle }}</div>
    </div>
    <div class="ring-check" v-if="selected">✓</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  completed: Number, total: Number, label: String, title: String, subtitle: String,
  progressColor: { type: String, default: '#6366f1' }, subColor: String, selected: Boolean,
  size: { type: Number, default: 100 }, trackColor: { type: String, default: '#eee' }
})
defineEmits(['click'])

const vp = computed(() => props.size + 10)
const center = computed(() => vp.value / 2)
const radius = computed(() => props.size / 2 - 8)
const circumference = computed(() => 2 * Math.PI * radius.value)
const dashoffset = computed(() => {
  const rate = props.total > 0 ? props.completed / props.total : 0
  return circumference.value * (1 - rate)
})
</script>

<style scoped>
.ring-chart-wrapper {
  position: relative; text-align: center; cursor: pointer;
  padding: 10px 16px; border-radius: 12px; border: 2px solid var(--border-color);
  background: var(--bg-primary); display: flex; flex-direction: column; align-items: center; gap: 4px;
  transition: border-color 0.2s, background 0.2s;
}
.ring-chart-wrapper.active { border-color: var(--brand-color); background: var(--brand-light); }
.ring-center { position: absolute; top: 44%; left: 50%; transform: translate(-50%, -50%); text-align: center; }
.ring-num { font-size: 18px; font-weight: 700; }
.ring-label { font-size: 9px; opacity: 0.7; }
.ring-footer { text-align: center; font-size: 11px; margin-top: auto; }
.ring-title { font-weight: 600; }
.ring-sub { font-size: 13px; font-weight: 700; }
.ring-check { position: absolute; top: -6px; right: -6px; width: 18px; height: 18px; background: var(--brand-color); border-radius: 50%; color: #fff; font-size: 10px; display: flex; align-items: center; justify-content: center; }
</style>
