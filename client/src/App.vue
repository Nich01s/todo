<template>
  <div :class="{ 'theme-transition': transitioning }">
    <AppHeader v-if="auth.isLoggedIn" />
    <router-view />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import AppHeader from './components/AppHeader.vue'

const auth = useAuthStore()
const router = useRouter()
const transitioning = ref(false)

watch(() => auth.isLoggedIn, (val) => {
  if (!val) router.push('/login')
})
</script>
