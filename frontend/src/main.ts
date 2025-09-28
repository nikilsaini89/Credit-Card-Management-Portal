import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import "./style.css";

import Vue3Toastify from "vue3-toastify";
import "vue3-toastify/dist/index.css";

const app = createApp(App);
app.use(router);
app.use(store);
app.use(Vue3Toastify, {
  autoClose: 5000,
  position: "top-right"
});
router.isReady().then(() => {
  const token = localStorage.getItem('token');
  
  if (token) {
    try {
      const userRole = store.getters["auth/userRole"];
      if (userRole === 'ADMIN') {
        router.push({ name: "AdminDashboard" });
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
app.mount("#app");
