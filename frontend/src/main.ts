import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import './style.css'

const app = createApp(App)
app.use(store)
app.use(router)

// Handle initial routing based on authentication status
router.isReady().then(() => {
  const token = localStorage.getItem('token');
  
  if (token) {
    try {
      const userRole = store.getters["auth/userRole"];
      if (userRole === 'ADMIN') {
        router.push({ name: "AdminReviewCenter" });
      } else {
        router.push({ name: "Dashboard" });
      }
    } catch (error) {
      console.error('Error checking authentication:', error);
      router.push({ name: "Login" });
    }
  } else {
    router.push({ name: "Login" });
  }
});

app.mount('#app')