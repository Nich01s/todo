<template>
  <div class="search-overlay" @click.self="$emit('close')">
    <div class="search-dialog">
      <input v-model="keyword" placeholder="搜索城市..." @input="search" />
      <div class="search-results">
        <div class="city-item" v-for="c in results" :key="c.id" @click="$emit('select', c.name, c.id)">
          {{ c.name }}
        </div>
        <div class="city-item empty" v-if="!results.length && keyword">未找到相关城市</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['select', 'close'])
const keyword = ref('')
const results = ref([])

const API_KEY = 'YOUR_HEFENG_API_KEY'

async function search() {
  if (!keyword.value) { results.value = []; return }
  try {
    const res = await fetch(
      `https://geoapi.qweather.com/v2/city/lookup?location=${keyword.value}&key=${API_KEY}`
    )
    const data = await res.json()
    results.value = (data.location || []).map(c => ({ name: c.name, id: c.id }))
  } catch (e) { results.value = [] }
}
</script>

<style scoped>
.search-overlay { position: fixed; inset: 0; z-index: 1001; background: rgba(0,0,0,0.3); display: flex; align-items: flex-start; justify-content: center; padding-top: 15%; }
.search-dialog { background: var(--bg-primary); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 16px; width: 320px; max-height: 300px; overflow-y: auto; }
.search-dialog input { width: 100%; padding: 8px; border: 1px solid var(--border-color); border-radius: var(--radius); background: var(--bg-primary); color: var(--text-primary); font-size: 14px; }
.search-results { margin-top: 8px; }
.city-item { padding: 8px; cursor: pointer; border-radius: var(--radius); font-size: 14px; }
.city-item:hover { background: var(--bg-tertiary); }
.city-item.empty { cursor: default; opacity: 0.4; }
</style>
