import { createRouter, createWebHistory } from "vue-router";

// Lazy load Dashboard
const Dashboard = () => import("../views/Users/Dashboard.vue");

const routes = [
  {
    path: "/",
    name: "Dashboard",
    component: Dashboard,
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

export default router;
