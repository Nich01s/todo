import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getTodos } from '../api/todos'

export const useTodoStore = defineStore('todos', () => {
  const all = ref([])
  const loading = ref(false)
  const error = ref('')
  const initialized = ref(false)

  async function fetchAll() {
    if (loading.value) return
    loading.value = true
    error.value = ''
    try {
      const res = await getTodos({ sort: 'due_date', page: 1, size: 200 })
      all.value = res.data.data.records || []
      initialized.value = true
    } catch (e) {
      error.value = '加载待办失败'
      console.error('加载待办失败', e)
    } finally {
      loading.value = false
    }
  }

  function reset() {
    all.value = []
    loading.value = false
    error.value = ''
    initialized.value = false
  }

  return { all, loading, error, initialized, fetchAll, reset }
})
