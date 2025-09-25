// src/main.ts
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'   // adjust path if your router file is elsewhere
import store from './store'

const app = createApp(App)
app.use(store)
app.use(router)
app.mount('#app')