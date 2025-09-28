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
  
  // Wait for the store to be fully populated
  const checkUserRole = () => {
    const userRole = store.getters["auth/userRole"];
    const user = store.getters["auth/user"];
    console.log('User role:', userRole); // Debug log
    console.log('User data:', user); // Debug log
    console.log('Store state:', store.state); // Debug log
    
    if (userRole === 'ADMIN') {
      console.log('Routing to AdminDashboard'); // Debug log
      router.push({ name: "AdminDashboard" });
    } else if (userRole === 'USER') {
      console.log('Routing to Dashboard'); // Debug log
      router.push({ name: "Dashboard" });
    } else {
      // If user role is not set yet, wait a bit more
      console.log('User role not set yet, waiting...');
      setTimeout(checkUserRole, 100);
    }
  };
  
  // Start checking for user role
  checkUserRole();
  
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
