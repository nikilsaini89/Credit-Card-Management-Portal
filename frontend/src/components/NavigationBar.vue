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

      <!-- Desktop Navigation Links (hidden on small screens) -->
      <nav class="topnav desktop-nav" role="navigation" aria-label="Primary">
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

      <!-- Hamburger (mobile only) -->
      <button
        class="hamburger"
        @click="toggleMenu"
        :aria-expanded="isMenuOpen.toString()"
        aria-controls="mobile-nav"
        aria-label="Toggle navigation"
        type="button"
      >
        <span class="hamb-line" :class="{ open: isMenuOpen }"></span>
        <span class="hamb-line" :class="{ open: isMenuOpen }"></span>
        <span class="hamb-line" :class="{ open: isMenuOpen }"></span>
      </button>

      <!-- Avatar -->
      <div class="avatar desktop-avatar">
        <RouterLink to="/logout">{{ userInitials }}</RouterLink>
      </div>
    </div>

    <!-- Mobile Nav Overlay / Panel (inserted only when open) -->
    <div
      id="mobile-nav"
      v-if="isMenuOpen"
      class="mobile-nav"
      @click.self="closeMenu"
      role="dialog"
      aria-modal="true"
      aria-label="Mobile navigation overlay"
    >
      <aside class="mobile-panel" role="menu" aria-label="Mobile Primary">
        <div class="mobile-panel-top">
          <div class="brand-mini">
            <div class="brand-logo-mini" aria-hidden="true">
              <!-- small svg -->
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="white"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <rect x="2" y="5" width="20" height="14" rx="2" ry="2" />
                <line x1="2" y1="10" x2="22" y2="10" />
              </svg>
            </div>
            <div class="brand-title-mini">CreditCard Portal</div>
          </div>

          <button class="close-btn" @click="closeMenu" aria-label="Close menu" type="button">
            ✕
          </button>
        </div>

        <nav class="mobile-links">
          <RouterLink
            v-for="link in links"
            :key="link.to + '-mobile'"
            :to="link.to"
            class="mobile-link"
            :class="{ active: isActive(link.to) }"
            @click="closeMenu"
            role="menuitem"
          >
            {{ link.label }}
          </RouterLink>
        </nav>

        <footer class="mobile-footer">
          <div class="avatar mobile-avatar">{{ userInitials }}</div>
          <div class="mobile-user">
            <div class="mobile-username">Hello, {{ userName }}</div>
            <RouterLink to="/profile" class="mobile-profile" @click="closeMenu">View profile</RouterLink>
          </div>
        </footer>
      </aside>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from "vue";
import { useRoute } from "vue-router";

const userName = "Abhay Dhek";

const userInitials = computed(() => {
  const parts = userName.split(" ");
  return parts.map((p) => p[0]).join("").toUpperCase();
});

const links = [
  { to: "/", label: "Dashboard" },
  { to: "/cards", label: "My Cards" },
  { to: "/apply-card", label: "Apply Card" },
  { to: "/transactions", label: "Transactions" },
  { to: "/profile", label: "Profile" },
];

const route = useRoute();
const isMenuOpen = ref(false);

const toggleMenu = () => {
  isMenuOpen.value = !isMenuOpen.value;
  lockBodyScroll(isMenuOpen.value);
};

const closeMenu = () => {
  isMenuOpen.value = false;
  lockBodyScroll(false);
};

const lockBodyScroll = (lock) => {
  document.documentElement.style.overflow = lock ? "hidden" : "";
  document.body.style.overflow = lock ? "hidden" : "";
};

const onKeydown = (e) => {
  if (e.key === "Escape" && isMenuOpen.value) {
    closeMenu();
  }
};

onMounted(() => {
  window.addEventListener("keydown", onKeydown);

  // close menu on route change
  watch(
    () => route.fullPath,
    () => {
      closeMenu();
    }
  );
});

onBeforeUnmount(() => {
  window.removeEventListener("keydown", onKeydown);
  lockBodyScroll(false);
});

const isActive = (path) => {
  if (!route || !route.path) return false;
  if (path === "/") return route.path === "/";
  return route.path.startsWith(path);
};
</script>

<style scoped>
/* TOPBAR */
.topbar {
  background: #fff;
  border-bottom: 1px solid #ececec;
  position: relative;
  z-index: 60;
}
.topbar-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
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

/* NAVIGATION (desktop) */
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
  transition: background 0.15s, color 0.15s, transform 0.06s;
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
  box-shadow: 0 4px 12px rgba(255, 214, 10, 0.25);
}

/* AVATAR (desktop) */
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
  text-decoration: none;
}
.desktop-avatar a {
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* HAMBURGER (hidden on desktop, shown on mobile via media query) */
.hamburger {
  display: none;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  background: transparent;
  border: none;
  padding: 8px;
  width: 44px;
  height: 44px;
  cursor: pointer;
  z-index: 200; /* sits above other elements */
  -webkit-tap-highlight-color: transparent;
}

/* each bar */
.hamb-line {
  display: block;
  width: 22px;
  height: 2px;
  border-radius: 2px;
  background: var(--muted, #6b7280);
  transition: transform 0.18s ease, opacity 0.18s ease;
  transform-origin: center;
}

/* transform into an X when .open is applied to each span */
.hamb-line.open:nth-child(1) {
  transform: translateY(6px) rotate(45deg);
}
.hamb-line.open:nth-child(2) {
  opacity: 0;
  transform: scaleX(0.1);
}
.hamb-line.open:nth-child(3) {
  transform: translateY(-6px) rotate(-45deg);
}

/* accessibility focus style */
.hamburger:focus {
  outline: 2px solid rgba(31,111,235,0.16);
  outline-offset: 3px;
}

/* MOBILE NAV OVERLAY & PANEL (visible when v-if inserts it) */
.mobile-nav {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex; /* backdrop + panel layout */
  align-items: stretch;
  justify-content: flex-start;
  background: rgba(0, 0, 0, 0.38); /* backdrop */
}

/* slide-in panel */
.mobile-panel {
  width: 300px;
  max-width: 86%;
  height: 100%;
  background: #fff;
  padding: 18px;
  box-shadow: 6px 0 28px rgba(10, 20, 30, 0.18);
  display: flex;
  flex-direction: column;
  gap: 12px;
  transform: translateX(0);
  animation: slideIn 220ms ease forwards;
  pointer-events: auto;
}

/* small brand at top of panel */
.brand-mini {
  display: flex;
  align-items: center;
  gap: 10px;
}
.brand-logo-mini {
  width: 38px;
  height: 38px;
  border-radius: 8px;
  background: var(--accent, #0b2540);
  display: flex;
  align-items: center;
  justify-content: center;
}
.brand-title-mini {
  font-weight: 800;
  color: var(--accent, #0b2540);
}

/* top area with close button */
.mobile-panel-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

/* close button */
.close-btn {
  background: transparent;
  border: none;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
}

/* mobile links */
.mobile-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 6px;
}
.mobile-link {
  display: block;
  padding: 12px 10px;
  border-radius: 8px;
  text-decoration: none;
  color: #222;
  font-weight: 700;
}
.mobile-link.active {
  background: #ffd60a;
  color: #0b2540;
  box-shadow: 0 6px 18px rgba(255, 214, 10, 0.12);
}

/* footer */
.mobile-footer {
  margin-top: auto;
  display: flex;
  gap: 12px;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #eee;
}
.mobile-avatar {
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: var(--accent, #0b2540);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}
.mobile-user .mobile-username {
  font-weight: 700;
}
.mobile-profile {
  display: inline-block;
  margin-top: 4px;
  font-size: 13px;
  color: var(--muted, #6b7280);
  text-decoration: none;
}

/* simple slideIn keyframe */
@keyframes slideIn {
  from {
    transform: translateX(-12px);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}

/* RESPONSIVE RULES */
@media (max-width: 768px) {
  .desktop-nav {
    display: none;
  }
  .hamburger {
    display: inline-flex;
  }
  .desktop-avatar {
    display: none;
  }
}

/* Prevent horizontal scroll caused by the panel */
html,
body {
  max-width: 100%;
  overflow-x: hidden;
}
</style>
