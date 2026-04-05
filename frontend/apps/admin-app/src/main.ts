import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'
import './style.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// Hydrate auth state once on boot (Sync storage read)
const authStore = useAuthStore()
authStore.hydrateAuth()

// If we have a token, fetch user info in background (Cache layer)
if (authStore.token) {
    authStore.fetchUser()
}

app.mount('#app')
