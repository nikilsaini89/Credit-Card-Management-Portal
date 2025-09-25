<template>
  <header class="topbar">
    <div class="topbar-inner">
      <!-- Brand -->
      <div class="brand">
        <div class="brand-logo" aria-hidden="true">
          <!-- Clean SVG Credit Card Icon -->
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="22"
            height="22"
            viewBox="0 0 24 24"
            fill="none"
            stroke="white"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <rect x="2" y="5" width="20" height="14" rx="2" ry="2" />
            <line x1="2" y1="10" x2="22" y2="10" />
            <line x1="6" y1="15" x2="6.01" y2="15" />
            <line x1="10" y1="15" x2="14" y2="15" />
          </svg>
        </div>
        <div class="brand-title">CreditCard Portal</div>
      </div>

      <!-- Navigation Links -->
      <nav class="topnav">
        <RouterLink
          v-for="link in links"
          :key="link.to"
          :to="link.to"
          class="nav-item"
          :class="{ active: isActive(link.to) }"
        >
          {{ link.label }}
        </RouterLink>
      </nav>

      <!-- Avatar -->
      <div class="avatar">
        <RouterLink to="/logout">{{ userInitials }}</RouterLink>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from "vue"
import { useRoute } from "vue-router"

const userName = "Abhay Dhek"

const userInitials = computed(() => {
  const parts = userName.split(" ")
  return parts.map(p => p[0]).join("").toUpperCase()
})

const links = [
  { to: "/", label: "Dashboard" },
  { to: "/cards", label: "My Cards" },
  { to: "/apply-card", label: "Apply Card" },
  { to: "/transactions", label: "Transactions" },
  { to: "/profile", label: "Profile" }
]

const route = useRoute()

const isActive = (path) => {
  if (!route || !route.path) return false
  if (path === "/") return route.path === "/"
  return route.path.startsWith(path)
}
</script>

<style scoped>
/* TOPBAR */
.topbar {
  background: #fff;
  border-bottom: 1px solid #ececec;
}
.topbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 18px 28px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* BRAND */
.brand {
  display: flex;
  gap: 12px;
  align-items: center;
}
.brand-logo {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  background: var(--accent, #0b2540);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 18px rgba(11, 37, 64, 0.12);
}
.brand-logo svg {
  width: 22px;
  height: 22px;
}
.brand-title {
  font-weight: 700;
  color: var(--accent, #0b2540);
  font-size: 18px;
}

/* NAVIGATION */
.topnav {
  display: flex;
  gap: 20px;
  align-items: center;
  color: var(--muted, #6b7280);
}

.topnav .nav-item {
  text-decoration: none;
  color: inherit;
  padding: 6px 10px;
  border-radius: 8px;
  font-weight: 600;
  transition: background 0.2s, color 0.2s, transform 0.06s;
}

/* Hover → Yellow */
.topnav .nav-item:hover {
  background: #ffd60a; /* Bright Yellow */
  color: #0b2540; /* Dark text for readability */
  transform: translateY(-1px);
}

/* Active → Yellow */
.topnav .nav-item.active {
  background: #ffd60a;
  color: #0b2540;
  box-shadow: 0 4px 12px rgba(255, 214, 10, 0.4);
}

/* AVATAR */
.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: var(--accent, #0b2540);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 16px;
}

/* RESPONSIVE */
@media (max-width: 768px) {
  .topbar-inner {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  .topnav {
    flex-wrap: wrap;
    gap: 12px;
  }
}
</style>
