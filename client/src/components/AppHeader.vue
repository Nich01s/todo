<template>
  <header class="app-header">
    <h1 class="logo" @click="$router.push('/')">📋 待办清单</h1>
    <div class="header-right">
      <button class="theme-btn" @click="theme.toggle()" :title="themeLabel">
        {{ themeIcon }}
      </button>
      <div class="user-info" v-if="auth.user">
        <img :src="auth.user.avatar || defaultAvatar" class="avatar" />
        <span class="username">{{ auth.user.username }}</span>
        <button class="logout-btn" @click="handleLogout">退出</button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useThemeStore } from '../stores/theme'

const auth = useAuthStore()
const theme = useThemeStore()
const router = useRouter()

const defaultAvatar = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 40 40"><circle cx="20" cy="16" r="8" fill="%23cbd5e1"/><ellipse cx="20" cy="34" rx="12" ry="8" fill="%23cbd5e1"/></svg>'

const themeIcon = computed(() => {
  const t = theme.theme
  if (t === 'auto') return '🔄'
  return t === 'dark' ? '🌙' : '☀️'
})

const themeLabel = computed(() => {
  const t = theme.theme
  if (t === 'auto') return '自动模式'
  return t === 'dark' ? '暗色模式' : '亮色模式'
})

async function handleLogout() {
  await auth.logoutAction()
  router.push('/login')
}
</script>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  height: 56px;
  background: var(--bg-secondary);
  border-bottom: 1px solid var(--border-color);
}
.logo { font-size: 18px; cursor: pointer; user-select: none; }
.header-right { display: flex; align-items: center; gap: 12px; }
.theme-btn { background: none; border: 1px solid var(--border-color); border-radius: var(--radius); padding: 6px 10px; cursor: pointer; font-size: 16px; color: var(--text-primary); }
.user-info { display: flex; align-items: center; gap: 8px; font-size: 14px; }
.avatar { width: 32px; height: 32px; border-radius: 50%; object-fit: cover; border: 1px solid var(--border-color); }
.username { color: var(--text-secondary); }
.logout-btn { background: none; border: 1px solid var(--border-color); border-radius: var(--radius); padding: 4px 10px; cursor: pointer; font-size: 12px; color: var(--text-secondary); }
</style>
