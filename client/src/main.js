import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'
import { useThemeStore } from './stores/theme'
import './styles/variables.css'
import './styles/theme.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)

const themeStore = useThemeStore()
themeStore.init()
const authStore = useAuthStore()
authStore.init()

app.mount('#app')
