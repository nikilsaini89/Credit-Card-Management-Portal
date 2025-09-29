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
initializeAuth().then(auth => {
  console.log("checker1");
  if (!auth) {
    console.log("checker2");
    logout();
    router.push("/login");
    console.log("checker3");
  }
});
app.use(router);
app.use(store);
app.use(Vue3Toastify, {
  autoClose: 5000,
  position: "top-right"
});
router.isReady().then(() => {
  console.log("router ready chal gya");
  const token = localStorage.getItem('token');

  const current = router.currentRoute.value;
  console.log('current route on ready:', current.fullPath, current.name);

  // Only auto-redirect when user is on root or login page (not when visiting a specific page)
  const shouldRedirect =
    current.fullPath === '/' ||
    current.name === null ||
    current.name === 'Login' ||
    current.fullPath === '' ;

  if (!token) {
    // no token -> go to login
    router.push({ name: 'Login' });
    return;
  }

  if (!shouldRedirect) {
    // user is already navigating to a specific route (e.g. /cards/12), do nothing
    console.log('preserving navigation to', current.fullPath);
    return;
  }

  // token exists and we are on a base route — decide destination by role
  try {
    const userRole = store.getters["auth/userRole"];
    console.log('User role:', userRole);
    if (userRole === 'ADMIN') {
      router.push({ name: "AdminDashboard" });
    } else {
      router.push({ name: "Dashboard" });
    }
  } catch (error) {
    console.error('Error checking authentication:', error);
    router.push({ name: "Login" });
  }
});

app.mount("#app");
