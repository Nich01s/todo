<template>
  <div class="dialog-overlay" @click.self="$emit('close')">
    <div class="dialog-card">
      <h4>设置待办详情</h4>
      <label>截止日期</label>
      <input type="date" v-model="form.dueDate" />
      <label>分类</label>
      <select v-model="form.categoryId">
        <option :value="null">无分类</option>
        <option v-for="c in categories" :value="c.id" :key="c.id">{{ c.name }}</option>
      </select>
      <label>优先级</label>
      <div class="priority-group">
        <label class="pri-opt"><input type="radio" v-model="form.priority" :value="0" /> 低</label>
        <label class="pri-opt"><input type="radio" v-model="form.priority" :value="1" checked /> 中</label>
        <label class="pri-opt"><input type="radio" v-model="form.priority" :value="2" /> 高</label>
      </div>
      <div class="dialog-actions">
        <button class="cancel" @click="$emit('close')">取消</button>
        <button class="confirm" @click="$emit('confirm', form)">确认添加</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { getCategories } from '../../api/categories'

defineEmits(['confirm', 'close'])
const form = reactive({ dueDate: null, categoryId: null, priority: 1 })
const categories = ref([])

onMounted(async () => {
  try {
    const res = await getCategories()
    categories.value = res.data.data || []
  } catch (e) {}
})
</script>

<style scoped>
.dialog-overlay { position: fixed; inset: 0; z-index: 1000; background: rgba(0,0,0,0.3); display: flex; align-items: center; justify-content: center; }
.dialog-card { background: var(--bg-primary); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 24px; width: 380px; display: flex; flex-direction: column; gap: 10px; }
.dialog-card h4 { font-size: 16px; }
.dialog-card label { font-size: 13px; opacity: 0.6; }
.dialog-card input, .dialog-card select { padding: 8px; border: 1px solid var(--border-color); border-radius: var(--radius); background: var(--bg-primary); color: var(--text-primary); font-size: 14px; }
.priority-group { display: flex; gap: 16px; }
.pri-opt { display: flex; align-items: center; gap: 4px; font-size: 14px; cursor: pointer; }
.dialog-actions { display: flex; gap: 8px; justify-content: flex-end; margin-top: 8px; }
.cancel { padding: 8px 16px; background: var(--bg-tertiary); border: 1px solid var(--border-color); border-radius: var(--radius); cursor: pointer; font-size: 14px; color: var(--text-secondary); }
.confirm { padding: 8px 16px; background: var(--brand-color); color: #fff; border: none; border-radius: var(--radius); cursor: pointer; font-size: 14px; }
</style>
