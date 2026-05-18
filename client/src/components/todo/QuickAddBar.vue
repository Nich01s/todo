<template>
  <div class="quick-add">
    <div class="add-row">
      <input v-model="title" placeholder="输入新的待办事项..." @keyup.enter="openDialog" />
      <button class="add-btn" @click="openDialog">添加</button>
    </div>
    <AddTodoDialog v-if="showDialog" :title="title" @confirm="handleConfirm" @close="showDialog = false" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { createTodo } from '../../api/todos'
import AddTodoDialog from './AddTodoDialog.vue'

const emit = defineEmits(['added'])
const title = ref('')
const showDialog = ref(false)

function openDialog() {
  if (!title.value.trim()) return
  showDialog.value = true
}

async function handleConfirm(data) {
  try {
    await createTodo({ title: title.value, ...data })
    title.value = ''
    showDialog.value = false
    emit('added')
  } catch (e) {
    console.error('创建待办失败', e)
  }
}
</script>

<style scoped>
.quick-add { background: var(--bg-secondary); border: 1px solid var(--brand-color); border-radius: var(--radius-lg); padding: 12px; }
.add-row { display: flex; gap: 8px; align-items: center; }
.add-row input { flex: 1; padding: 10px; border: 1px solid var(--border-color); border-radius: var(--radius); background: var(--bg-primary); color: var(--text-primary); font-size: 14px; outline: none; }
.add-row input:focus { border-color: var(--brand-color); }
.add-btn { padding: 10px 18px; background: var(--brand-color); color: #fff; border: none; border-radius: var(--radius); font-size: 14px; cursor: pointer; }
</style>
