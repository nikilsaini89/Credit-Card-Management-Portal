<template>
  <div class="dashboard">
    <header class="topbar">
      <div class="brand">
        <div class="logo">💳</div>
        <div class="title">CreditCard Portal</div>
      </div>
      <nav class="nav">
        <RouterLink to="/">Dashboard</RouterLink>
        <RouterLink to="/my-cards">My Cards</RouterLink>
        <RouterLink to="/apply-card">Apply Card</RouterLink>
        <RouterLink to="/transactions">Transactions</RouterLink>
        <RouterLink to="/profile">Profile</RouterLink>
      </nav>
      <div class="avatar">RV</div>
    </header>

    <main class="container">
      <section class="hero">
        <h1>Welcome back, {{ user.name }}!</h1>
        <p class="subtitle">Here's your credit card overview</p>

        <div class="kpis">
          <div class="kpi">
            <div class="kpi-title">Active Cards</div>
            <div class="kpi-main">{{ summary.activeCards }}</div>
            <div class="kpi-sub">out of {{ summary.totalCards }} total cards</div>
          </div>

          <div class="kpi">
            <div class="kpi-title">Total Credit Limit</div>
            <div class="kpi-main">₹{{ formatNumber(summary.totalLimit) }}</div>
            <div class="kpi-sub">Across all cards</div>
          </div>

          <div class="kpi">
            <div class="kpi-title">Available Credit</div>
            <div class="kpi-main success">₹{{ formatNumber(summary.availableCredit) }}</div>
            <div class="kpi-sub">Ready to spend</div>
          </div>

          <div class="kpi">
            <div class="kpi-title">Outstanding Balance</div>
            <div class="kpi-main danger">₹{{ formatNumber(summary.outstanding) }}</div>
            <div class="kpi-sub">Total dues</div>
          </div>
        </div>
      </section>

      <section class="content-grid">
        <div class="left-col">
          <div class="section-header">
            <h3 class="section-title">My Cards</h3>
            <RouterLink to="/my-cards" class="view-all">View All</RouterLink>
          </div>

          <div class="card-list">
            <!-- render cards dynamically -->
            <article
              v-for="card in cards"
              :key="card.id"
              class="card card-gradient"
              :class="{ blocked: card.status === 'BLOCKED' }"
            >
              <div class="card-badge">{{ card.status }}</div>

              <!-- network chip -->
              <div class="card-chip">{{ card.network }}</div>

              <!-- three-dot menu -->
              <div class="card-menu" @click.stop="toggleMenu(card.id)">
                ⋮
                <div v-if="openMenu === card.id" class="dropdown">
                  <RouterLink :to="`/my-cards/${card.id}`">View Details</RouterLink>
                  <button @click="onToggleBlock(card)">{{ card.status === 'BLOCKED' ? 'Unblock' : 'Block' }}</button>
                </div>
              </div>

              <!-- number + eye inline -->
              <div class="card-number-row">
                <div
                  class="card-number"
                  @click="toggleCardNumber(card.id)"
                  :title="showFull[card.id] ? 'Hide number' : 'Show number'"
                >
                  {{ showFull[card.id] ? card.number : maskNumber(card.number) }}
                </div>

                <!-- eye sits to the right of the number -->
                <button
                  class="card-eye"
                  @click.stop="toggleCardNumber(card.id)"
                  :aria-label="showFull[card.id] ? 'Hide card number' : 'Show card number'"
                >👁️</button>
              </div>


              <div class="card-row">
                <div>
                  <div class="small">CARDHOLDER</div>
                  <div class="bold">{{ user.name }}</div>
                </div>
                <div>
                  <div class="small">EXPIRES</div>
                  <div class="bold">{{ card.expiry }}</div>
                </div>
              </div>

              <div class="card-footer">
                <div>
                  <div class="small">AVAILABLE LIMIT</div>
                  <div class="amount success">₹{{ formatNumber(card.availableLimit) }}</div>
                </div>
                <div>
                  <div class="small">TOTAL LIMIT</div>
                  <div class="amount">₹{{ formatNumber(card.totalLimit) }}</div>
                </div>
              </div>
            </article>
          </div>
        </div>

        <aside class="right-col">
          <div class="activity">
            <div class="activity-header">
              <h3>Latest Transactions</h3>
              <RouterLink to="/transactions" class="view-all">View All</RouterLink>
            </div>

            <ul class="tx-list">
              <li v-for="tx in transactions" :key="tx.id" class="tx">
                <div>
                  <div class="tx-title">{{ tx.merchant }} <span v-if="tx.mode" class="pill">{{ tx.mode }}</span></div>
                  <div class="tx-sub">{{ tx.category }} • {{ formatDate(tx.date) }}</div>
                </div>
                <div :class="['tx-amount', tx.amount < 0 ? 'danger' : '']">
                  {{ tx.amount < 0 ? '-' : '' }}₹{{ formatNumber(Math.abs(tx.amount)) }}
                </div>
              </li>
            </ul>
          </div>

          <div class="quick-actions">
            <h4>Quick Actions</h4>
            <div class="actions">
              <RouterLink to="/apply-card" class="action">+ Apply Card</RouterLink>
              <RouterLink to="/new-transaction" class="action">↗ New Transaction</RouterLink>
            </div>
          </div>
        </aside>
      </section>
    </main>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "DashboardView",
  data() {
    return {
      user: { name: "User" }, // updated from JSON
      summary: {
        activeCards: 0,
        totalCards: 0,
        totalLimit: 0,
        availableCredit: 0,
        outstanding: 0,
      },
      cards: [],
      transactions: [],
      showFull: {}, // map cardId => boolean
      openMenu: null, // id of card with open menu
    };
  },
  created() {
    this.loadDashboard();
    // click outside to close dropdowns
    document.addEventListener("click", this.globalClick);
  },
  beforeUnmount() {
    document.removeEventListener("click", this.globalClick);
  },
  methods: {
    async loadDashboard() {
      try {
        // mock JSON placed in public/data/dashboard.json
        const res = await axios.get("/data/dashboard.json");
        const d = res.data;

        // user summary
        this.user = d.user || this.user;

        this.summary.activeCards = d.activeCards ?? (d.cards ? d.cards.length : 0);
        this.summary.totalCards = d.totalCards ?? (d.cards ? d.cards.length : 0);
        this.summary.totalLimit = d.totalLimit ?? this.sum(d.cards, "totalLimit");
        this.summary.availableCredit = d.availableCredit ?? this.sum(d.cards, "availableLimit");
        this.summary.outstanding = d.outstanding ?? 0;

        // cards and transactions
        this.cards = d.cards || [];
        this.transactions = d.transactions || [];

        // initialize showFull map
        this.cards.forEach((c) => {
          this.$set(this.showFull, c.id, false);
        });
      } catch (err) {
        // fallback: console error and keep hardcoded defaults as last resort
        // (you may replace this with a friendly UI message)
        // eslint-disable-next-line no-console
        console.error("Failed to load dashboard JSON:", err);
      }
    },

    // helpers
    maskNumber(num) {
      if (!num) return "";
      // if formatted with spaces, take last 4
      const only = num.toString();
      const last = only.slice(-4);
      return "**** **** **** " + last;
    },
    formatNumber(n) {
      if (n == null) return "0";
      return n.toLocaleString("en-IN");
    },
    sum(list = [], key) {
      return list.reduce((acc, it) => acc + (Number(it[key]) || 0), 0);
    },
    formatDate(iso) {
      try {
        const d = new Date(iso);
        const opt = { day: "numeric", month: "short", hour: "2-digit", minute: "2-digit" };
        return d.toLocaleString("en-US", opt);
      } catch {
        return iso;
      }
    },

    // interactions
    toggleCardNumber(cardId) {
      this.$set(this.showFull, cardId, !this.showFull[cardId]);
    },
    toggleMenu(cardId) {
      this.openMenu = this.openMenu === cardId ? null : cardId;
    },
    onToggleBlock(card) {
      // placeholder: flip local status (real action should call backend)
      card.status = card.status === "BLOCKED" ? "ACTIVE" : "BLOCKED";
      this.openMenu = null;
    },
    globalClick(e) {
      // close dropdown if clicked outside
      const close = !e.target.closest(".card-menu");
      if (close) this.openMenu = null;
    },
    isImageString(s) {
      return typeof s === "string" && /\.(png|jpe?g|svg|webp)$/.test(s);
    },
  },
};
</script>

<style scoped>
@import url("https://fonts.googleapis.com/css2?family=Inter:wght@300;400;600;700;800&display=swap");
:root {
  --bg: #f7f7f9;
  --card: #ffffff;
  --muted: #7b7b85;
  --accent: #1f6feb;
}
* {
  box-sizing: border-box;
}
.dashboard {
  font-family: "Inter", system-ui, Arial;
  margin: 0;
  background: var(--bg);
  min-height: 100vh;
}
.topbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 36px;
  background: #fff;
  border-bottom: 1px solid #eee;
}
.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}
.brand .logo {
  font-size: 20px;
}
.brand .title {
  font-weight: 700;
}
.nav {
  display: flex;
  gap: 18px;
  color: var(--muted);
}
.nav a {
  cursor: pointer;
  text-decoration: none;
  color: inherit;
}
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: #0b2540;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}
.container {
  max-width: 1180px;
  margin: 28px auto;
  padding: 0 20px;
}
.hero h1 {
  font-size: 32px;
  margin: 8px 0;
}
.subtitle {
  color: var(--muted);
  margin-bottom: 22px;
}

/* wrapper to keep number + eye on the same line */
.card-number-row {
  display: flex;
  align-items: center;      /* vertically center number and eye */
  gap: 12px;                /* horizontal space between number and eye */
  margin-top: 18px;         /* increase spacing from content above */
}

/* number style (slightly bigger, keep clickable look) */
.card-number {
  font-size: 22px;
  letter-spacing: 4px;
  cursor: pointer;
  line-height: 1;           /* control vertical spacing */
  margin: 0;                /* no extra margin because wrapper handles spacing */
}

/* eye as small button right of number */
.card-eye {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: rgba(255,255,255,0.06);
  color: #fff;
  width: 30px;
  height: 30px;
  border-radius: 6px;
  cursor: pointer;
  padding: 0;
  font-size: 16px;
  line-height: 1;
}

/* subtle hover for eye */
.card-eye:hover {
  background: rgba(255,255,255,0.12);
}

/* if you need the card-number to be slightly lower from the top content 
   (you can tweak the margin-top value above) */

   
.kpis {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 18px;
}
.kpi {
  background: var(--card);
  padding: 18px;
  border-radius: 12px;
  box-shadow: 0 2px 0 rgba(20, 20, 20, 0.02);
}
.kpi-title {
  color: var(--muted);
  font-weight: 600;
}
.kpi-main {
  font-size: 20px;
  font-weight: 800;
  margin-top: 6px;
}
.kpi-sub {
  color: #9aa0a6;
  margin-top: 8px;
}
.kpi .success {
  color: #12a454;
}
.kpi .danger {
  color: #ff4d4f;
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 420px;
  gap: 34px;
  margin-top: 26px;
  align-items: start;
}
.left-col .section-title {
  margin: 0 0 12px 6px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.view-all {
  background: transparent;
  border: 1px solid #eee;
  padding: 6px 10px;
  border-radius: 8px;
  font-size: 14px;
  text-decoration: none;
}
.card-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
  /* if you want horizontal scroll, switch to row flex and overflow-x */
}
.card {
  background: #0f2b4a;
  padding: 24px;
  border-radius: 14px;
  color: #fff;
  min-height: 220px;
  position: relative;
  overflow: hidden;
  width: 560px; /* increased width to match screenshot proportions */
}
.card.card-gradient {
  background: linear-gradient(135deg, #07112b 0%, #1a3a8a 40%, #1e6bf0 100%);
}
.card.blocked {
  opacity: 0.95;
}
.card-badge {
  position: absolute;
  left: 18px;
  top: 18px;
  background: rgba(255, 255, 255, 0.08);
  padding: 6px 10px;
  border-radius: 8px;
  font-weight: 700;
}
.card-chip {
  position: absolute;
  right: 48px; /* slightly moved left to make space for menu */
  top: 18px;
  background: rgba(255, 255, 255, 0.12);
  padding: 8px 12px;
  border-radius: 8px;
  font-weight: 700;
}
.card-menu {
  position: absolute;
  right: 18px;
  top: 18px;
  cursor: pointer;
  user-select: none;
}
.card-eye {
  position: absolute;
  left: 18px;
  top: 56px;
  opacity: 0.9;
}
.card-logo {
  margin-top: 12px;
  font-size: 24px;
}
.card-logo img {
  height: 22px;
  display: inline-block;
}
.card-number {
  font-size: 22px;
  letter-spacing: 4px;
  margin-top: 12px;
  cursor: pointer;
}
.card-row {
  display: flex;
  justify-content: space-between;
  margin-top: 18px;
}
.small {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.7);
  letter-spacing: 1px;
}
.bold {
  font-weight: 700;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  padding-top: 12px;
}
.amount {
  font-weight: 700;
}
.amount.success {
  color: #25c36a;
}

.right-col .activity {
  background: var(--card);
  padding: 18px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(20, 20, 20, 0.04);
}
.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.activity-header h3 {
  margin: 0;
}
.tx-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.tx {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f2f2f4;
}
.tx:last-child {
  border-bottom: 0;
}
.tx-title {
  font-weight: 700;
}
.tx-sub {
  font-size: 13px;
  color: var(--muted);
}
.tx-amount.danger {
  color: #ff4d4f;
  font-weight: 700;
}
.pill {
  background: #f0f4f8;
  border-radius: 12px;
  padding: 4px 8px;
  margin-left: 6px;
  font-size: 12px;
  color: #333;
}

.quick-actions {
  margin-top: 20px;
}
.quick-actions h4 {
  margin-bottom: 12px;
}
.actions {
  display: flex;
  gap: 12px;
}
.action {
  flex: 1;
  padding: 14px;
  border-radius: 10px;
  border: 1px solid #eee;
  background: #fff;
  text-decoration: none;
  color: inherit;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

/* dropdown */
.dropdown {
  position: absolute;
  right: 0;
  top: 28px;
  background: white;
  color: black;
  border: 1px solid #ddd;
  border-radius: 6px;
  padding: 6px 12px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  z-index: 10;
}
.dropdown a,
.dropdown button {
  background: none;
  border: none;
  padding: 4px;
  text-align: left;
  cursor: pointer;
}

/* Responsive */
@media (max-width: 980px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
  .kpis {
    grid-template-columns: repeat(2, 1fr);
  }
  .card {
    width: 100%;
  }
}
</style>
