import { createRouter, createWebHistory } from "vue-router";
import AuthView from "../views/Users/AuthView.vue";
import ProfileView from "../views/Users/ProfileView.vue";
import store from "../store";
import Login from "../views/Login.vue";

// Lazy load Dashboard
const Dashboard = () => import("../views/Users/Dashboard.vue");
const CardDetail = () => import("../views/Users/CardDetail.vue");
const routes = [
  {
    path: "/",
    name: "Login",
    component: Login,
  },
  {
    path: "/dashboard",
    name: "Dashboard",
    component: Dashboard,
    meta: { requiresAuth: true }
  },
  {
    path: "/card/:id",
    name: "CardDetail",
    component: CardDetail,
    props: true,
    meta: { requiresAuth: true }
  },
  {
    path: "/profile",
    name: "Profile",
    component: ProfileView,
    meta: { requiresAuth: true }
  },
  // {
  //   path: "/my-cards",
  //   name: "MyCards",
  //   component: () => import("../views/Users/MyCards.vue"), 
  // },
  // {
  //   path: "/transactions",
  //   name: "Transactions",
  //   component: () => import("../views/Users/Transactions.vue"), 
  // },
  // {
  //   path: "/apply-card",
  //   name: "ApplyCard",
  //   component: { template: "<div>Apply Card Page (placeholder)</div>" },
  // },
  // {
  //   path: "/new-transaction",
  //   name: "NewTransaction",
  //   component: { template: "<div>New Transaction Page (placeholder)</div>" },
  // },
  // {
  //   path: "/profile",
  //   name: "Profile",
  //   component: { template: "<div>Profile Page (placeholder)</div>" },
  // },
  // {
  //   path: "/my-cards/:id",
  //   name: "CardDetail",
  //   component: { template: "<div>Card Details Page (placeholder)</div>" },
  // },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});


router.beforeEach((to, from, next) => {
  const isAuthenticated = store.getters["auth/isAuthenticated"];

  if (to.meta.requiresAuth && !isAuthenticated) {
    next({ name: "Login" });
  } else {
    next();
  }
});

export default router;
