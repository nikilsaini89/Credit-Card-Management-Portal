import { createRouter, createWebHistory } from "vue-router";
import AuthView from "../views/Users/AuthView.vue";
import UserProfile from "../components/UserProfile.vue";
import store from "../store";
import Login from "../views/Login.vue";
import CardApplicationPage from "../views/Users/CardApplicationPage.vue";
import CardApplicationHistoryPage from "../views/Users/CardApplicationHistoryPage.vue";


const Dashboard = () => import("../views/Users/Dashboard.vue");
const CardDetail = () => import("../views/Users/CardDetail.vue");
const TransactionHistory = () => import("../views/Users/TransactionHistory.vue");
const CreateTransaction = () => import("../views/Users/CreateTransaction.vue");
const routes = [
  {
    path: "/",
    name: "Login",
    component: Login,
  },
    {
    path: "/register",
    name: "Register",
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
    component: UserProfile,
    meta: { requiresAuth: true }
  },
  {
    path: "/logout",
    name: "Logout",
    component: Login,
    meta: { requiresAuth: true }
  },
    {
    path: "/transactions",
    name: "TransactionHistory",
    component: TransactionHistory,
  },
  {
    path: "/create-transaction",
    name: "CreateTransaction",
    component: CreateTransaction,
  },
  {
    path: "/cards",
    name: "MyCards",
    component: () => import("../views/Users/MyCards.vue"),
  },
  {
    path: "/admin",
    name: "AdminReviewCenter",
    component: () => import("../views/AdminReviewCenter.vue"),
  },
  {
    path: "/apply-card",
    name: "ApplyCard",
    component: { template: "<div class='p-8 text-center'><h1 class='text-2xl font-bold text-gray-900'>Apply Card</h1><p class='text-gray-600 mt-2'>This feature is coming soon!</p></div>" },
  },
  {
    path: "/apply-card",
    name: "ApplyCard",
    component: CardApplicationPage,
  },
  {
    path : "/my-applications",
    name : "MyApplications",
    component : CardApplicationHistoryPage
  }
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
