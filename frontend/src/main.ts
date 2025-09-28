import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import "./style.css";

import Vue3Toastify from "vue3-toastify";
import "vue3-toastify/dist/index.css";
import { initializeAuth } from "./utils/initializeAuth";
import { logout } from "./services/authService";

const app = createApp(App);

// Initialize auth and handle routing
initializeAuth().then(auth => {
  console.log('Auth result:', auth); // Debug log
  
  if (!auth) {
    logout();
    router.push("/login");
    return;
  }
  
  // Add a small delay to ensure store is populated
  setTimeout(() => {
    const userRole = store.getters["auth/userRole"];
    console.log('User role:', userRole); // Debug log
    
    if (userRole === 'ADMIN') {
      console.log('Routing to AdminDashboard'); // Debug log
      router.push({ name: "AdminDashboard" });
    } else {
      console.log('Routing to Dashboard'); // Debug log
      router.push({ name: "Dashboard" });
    }
  }, 100); // Small delay to ensure store is populated
  
}).catch(error => {
  console.error('Auth initialization error:', error);
  router.push({ name: "Login" });
});

app.use(router);
app.use(store);
app.use(Vue3Toastify, {
  autoClose: 5000,
  position: "top-right"
});

app.mount("#app");
