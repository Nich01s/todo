<template>
  <div class="auth-page">
    <form class="auth-card" @submit.prevent="handleRegister">
      <h2>注册</h2>
      <input v-model="form.username" placeholder="用户名（3-50位）" required minlength="3" />
      <input v-model="form.password" type="password" placeholder="密码（至少6位）" required minlength="6" />
      <p class="error" v-if="error">{{ error }}</p>
      <button type="submit" :disabled="loading">注册</button>
      <p class="link">已有账号？<router-link to="/login">登录</router-link></p>
    </form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const form = reactive({ username: '', password: '' })
const loading = ref(false)
const error = ref('')

async function handleRegister() {
  error.value = ''
  loading.value = true
  try {
    await auth.registerAction(form)
    router.push('/')
  } catch (e) {
    error.value = e.response?.data?.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.auth-page { display: flex; align-items: center; justify-content: center; min-height: 100vh; background: var(--bg-secondary); }
.auth-card { background: var(--bg-primary); border: 1px solid var(--border-color); border-radius: var(--radius-lg); padding: 32px; width: 360px; display: flex; flex-direction: column; gap: 14px; }
.auth-card h2 { text-align: center; font-size: 22px; }
.auth-card input { padding: 10px; border: 1px solid var(--border-color); border-radius: var(--radius); background: var(--bg-primary); color: var(--text-primary); font-size: 14px; }
.auth-card button { padding: 10px; background: var(--brand-color); color: #fff; border: none; border-radius: var(--radius); font-size: 15px; cursor: pointer; }
.auth-card button:disabled { opacity: 0.6; cursor: not-allowed; }
.error { color: var(--danger); font-size: 13px; }
.link { text-align: center; font-size: 13px; color: var(--text-secondary); }
.link a { color: var(--brand-color); }
</style>
