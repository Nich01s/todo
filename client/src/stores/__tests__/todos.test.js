import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useTodoStore } from '../todos'

vi.mock('../../api/todos', () => ({
  getTodos: vi.fn()
}))

import { getTodos } from '../../api/todos'

describe('useTodoStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('starts empty and not initialized', () => {
    const store = useTodoStore()
    expect(store.all).toEqual([])
    expect(store.initialized).toBe(false)
    expect(store.loading).toBe(false)
  })

  it('fetchAll loads todos and sets initialized', async () => {
    getTodos.mockResolvedValue({
      data: { data: { records: [{ id: 1, title: 'Test', completed: 0, priority: 1 }] } }
    })
    const store = useTodoStore()
    await store.fetchAll()
    expect(store.all).toHaveLength(1)
    expect(store.initialized).toBe(true)
    expect(store.loading).toBe(false)
  })

  it('fetchAll sets error on failure', async () => {
    getTodos.mockRejectedValue(new Error('Network error'))
    const store = useTodoStore()
    await store.fetchAll()
    expect(store.error).toBe('加载待办失败')
    expect(store.initialized).toBe(false)
  })

  it('fetchAll guards against concurrent calls', async () => {
    let resolveFirst
    const firstPromise = new Promise(r => { resolveFirst = r })
    getTodos.mockReturnValueOnce(firstPromise)
    const store = useTodoStore()
    const call1 = store.fetchAll()
    const call2 = store.fetchAll()
    resolveFirst({ data: { data: { records: [] } } })
    await call1
    await call2
    expect(getTodos).toHaveBeenCalledTimes(1)
  })

  it('reset clears state', async () => {
    getTodos.mockResolvedValue({ data: { data: { records: [{ id: 1, title: 'X' }] } } })
    const store = useTodoStore()
    await store.fetchAll()
    store.reset()
    expect(store.all).toEqual([])
    expect(store.initialized).toBe(false)
  })
})
