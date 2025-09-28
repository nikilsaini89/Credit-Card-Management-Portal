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
  
  if (!auth) {
    logout();
    router.push("/login");
    return;
  }
  
  // Check user role after auth is initialized
  const userRole = store.getters["auth/userRole"];  
  if (userRole === 'ADMIN') {
    router.push({ name: "AdminDashboard" });
  } else {
    router.push({ name: "Dashboard" });
  }
}).catch(error => {
  router.push({ name: "Login" });
});

app.use(router);
app.use(store);
app.use(Vue3Toastify, {
  autoClose: 5000,
  position: "top-right"
});

app.mount("#app");
