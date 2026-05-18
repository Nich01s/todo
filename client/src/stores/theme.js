import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const theme = ref(localStorage.getItem('theme') || 'auto')

  function apply() {
    let resolved = theme.value
    if (resolved === 'auto') {
      resolved = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
    }
    document.documentElement.setAttribute('data-theme', resolved)
  }

  function toggle() {
    if (theme.value === 'auto') {
      theme.value = 'dark'
    } else if (theme.value === 'dark') {
      theme.value = 'light'
    } else {
      theme.value = 'auto'
    }
    localStorage.setItem('theme', theme.value)
    apply()
  }

  function init() {
    apply()
    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', apply)
  }

  return { theme, toggle, init, apply }
})
