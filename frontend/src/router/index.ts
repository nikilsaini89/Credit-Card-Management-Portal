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
    path: "/login",
    redirect: "/" 
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
    meta: { requiresAuth: true, requiresUser: true }
  },
  {
    path: "/card/:id",
    name: "CardDetail",
    component: CardDetail,
    props: true,
    meta: { requiresAuth: true, requiresUser: true }
  },
  {
    path: "/profile",
    name: "Profile",
    component: UserProfile,
    meta: { requiresAuth: true, requiresUser: true }
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
    meta: { requiresAuth: true, requiresUser: true }
  },
  {
    path: "/create-transaction",
    name: "CreateTransaction",
    component: CreateTransaction,
    meta: { requiresAuth: true, requiresUser: true }
  },
  {
    path: "/cards",
    name: "MyCards",
    component: () => import("../views/Users/MyCards.vue"),
    meta: { requiresAuth: true, requiresUser: true }
  },
  {
    path: "/admin-dashboard",
    name: "AdminDashboard",
    component: () => import("../views/AdminDashboard.vue"),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: "/admin-review",
    name: "AdminReviewCenter",
    component: () => import("../views/AdminReviewCenter.vue"),
    meta: { requiresAuth: true, requiresAdmin: true }
  },
  {
    path: "/apply-card",
    name: "ApplyCard",
    component: { template: "<div class='p-8 text-center'><h1 class='text-2xl font-bold text-gray-900'>Apply Card</h1><p class='text-gray-600 mt-2'>This feature is coming soon!</p></div>" },
    meta: { requiresAuth: true, requiresUser: true }
  },
  {
    path: "/apply-card",
    name: "ApplyCard",
    component: CardApplicationPage,
    meta: { requiresAuth: true, requiresUser: true }
  },
  {
    path : "/my-applications",
    name : "MyApplications",
    component : CardApplicationHistoryPage,
    meta: { requiresAuth: true, requiresUser: true }
  }
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

// Function to check if user should be redirected based on role
const getInitialRoute = () => {
  const token = localStorage.getItem('token');
  
  if (!token) {
    return { name: "Login" };
  }

  try {
    const decoded = store.getters["auth/userRole"];
    if (decoded === 'ADMIN') {
      return { name: "AdminDashboard" };
    } else {
      return { name: "Dashboard" };
    }
  } catch (error) {
    console.error('Error decoding token:', error);
    return { name: "Login" };
  }
};

router.beforeEach((to, from, next) => {
  const isAuthenticated = store.getters["auth/isAuthenticated"];
  const isAdmin = store.getters["auth/isAdmin"];
  const userRole = store.getters["auth/userRole"];

  // If trying to access admin route but not admin
  if (to.meta.requiresAdmin && !isAdmin) {
    next({ name: "Dashboard" });
    return;
  }

  // If trying to access user route but admin
  if (to.meta.requiresUser && isAdmin) {
    next({ name: "AdminDashboard" });
    return;
  }

  // If trying to access protected route but not authenticated
  if (to.meta.requiresAuth && !isAuthenticated) {
    next({ name: "Login" });
    return;
  }

  // If accessing root path, redirect based on role
  if (to.path === '/' && isAuthenticated) {
    const initialRoute = getInitialRoute();
    next(initialRoute);
    return;
  }

  next();
});

export default router;
