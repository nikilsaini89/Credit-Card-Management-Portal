<template>
  <div class="dashboard">
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
            <div class="kpi-main">${{ formatCurrency(summary.totalLimit) }}</div>
            <div class="kpi-sub">across all cards</div>
          </div>

          <div class="kpi">
            <div class="kpi-title">Available Credit</div>
            <div class="kpi-main">${{ formatCurrency(summary.availableCredit) }}</div>
            <div class="kpi-sub">ready to use</div>
          </div>

          <div class="kpi">
            <div class="kpi-title">Outstanding Balance</div>
            <div class="kpi-main">${{ formatCurrency(summary.outstanding) }}</div>
            <div class="kpi-sub">current balance</div>
          </div>
        </div>
      </section>

      <!-- My Cards Section -->
      <section class="cards-section">
        <div class="section-header">
          <h2>My Cards</h2>
          <router-link to="/my-cards" class="view-all-link">View All</router-link>
        </div>

        <div v-if="loading" class="loading-container">
          <div class="loading-spinner"></div>
          <p>Loading cards...</p>
        </div>

        <div v-else-if="myCards.length === 0" class="empty-state">
          <p>No cards found. Apply for a new card to get started!</p>
        </div>

        <div v-else class="cards-grid">
          <Card
            v-for="card in myCards.slice(0, 3)"
            :key="card.id"
            :card="card"
            :showFull="showFull[card.id]"
            @toggle="toggleCard"
            @action="onCardAction"
          />
        </div>
      </section>

      <!-- Recent Transactions Section -->
      <section class="transactions-section">
        <div class="section-header">
          <h2>Recent Transactions</h2>
          <router-link to="/transactions" class="view-all-link">View All</router-link>
        </div>

        <div v-if="transactions.length === 0" class="empty-state">
          <p>No recent transactions found.</p>
        </div>

        <div v-else class="transactions-list">
          <div
            v-for="transaction in transactions.slice(0, 5)"
            :key="transaction.id"
            class="transaction-item"
          >
            <div class="transaction-info">
              <div class="transaction-merchant">{{ transaction.merchant }}</div>
              <div class="transaction-date">{{ formatDate(transaction.date) }}</div>
            </div>
            <div class="transaction-amount" :class="{ 'negative': transaction.amount < 0 }">
              {{ transaction.amount < 0 ? '-' : '+' }}${{ Math.abs(transaction.amount).toFixed(2) }}
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script>
import axios from "axios";
import Card from "../../components/Card.vue";
import { useRouter } from "vue-router";
import { mapGetters, mapActions } from 'vuex';
import { LOCAL_STORAGE } from '../../constants/constants';

export default {
  name: "Dashboard",
  components: {
    Card,
  },
  data() {
    return {
      user: {
        name: "John Doe",
        email: "john.doe@example.com",
      },
      summary: {
        activeCards: 0,
        totalCards: 0,
        totalLimit: 0,
        availableCredit: 0,
        outstanding: 0,
      },
      transactions: [],
      showFull: {},
      openMenu: null,
    };
  },
  computed: {
    ...mapGetters('myCards', [
      'myCards',
      'loading'
    ])
  },
  async created() {
    await this.loadDashboard();
    document.addEventListener("click", this.globalClick);
  },
  beforeUnmount() {
    document.removeEventListener("click", this.globalClick);
  },
  methods: {
    ...mapActions('myCards', ['fetchCards']),
    
    async loadDashboard() {
      try {
        // Fetch cards from store
        await this.fetchCards();
        
        // Load other data from JSON
        const response = await axios.get("/data/dashboard.json");
        const data = response.data;

        this.user = data.user || this.user;
        this.transactions = data.transactions || [];

        // Update summary based on store cards
        this.summary.activeCards = this.myCards.filter(card => card.cardStatus === 'ACTIVE').length;
        this.summary.totalCards = this.myCards.length;
        this.summary.totalLimit = this.sum(this.myCards, "creditLimit");
        this.summary.availableCredit = this.sum(this.myCards, "availableLimit");
        this.summary.outstanding = this.summary.totalLimit - this.summary.availableCredit;

        this.myCards.forEach((cardItem) => {
          this.$set(this.showFull, cardItem.id, false);
        });
      } catch (err) {
        console.error("Failed to load dashboard data:", err);
      }
    },

    sum(items, property) {
      return items.reduce((total, item) => total + (item[property] || 0), 0);
    },

    formatCurrency(amount) {
      return new Intl.NumberFormat("en-US", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2,
      }).format(amount);
    },

    formatDate(dateString) {
      return new Date(dateString).toLocaleDateString("en-US", {
        month: "short",
        day: "numeric",
        year: "numeric",
      });
    },

    toggleCard(cardId) {
      this.$set(this.showFull, cardId, !this.showFull[cardId]);
    },

    getCardMenuItems(card) {
      return [
        { label: "View Details", to: { name: "CardDetail", params: { id: card.id } } },
        { label: card.cardStatus === "BLOCKED" ? "Unblock" : "Block", value: { type: "toggle-block" } }
      ];
    },

    onCardAction({ action, card }) {
      if (!action) return;
      
      // Store card details in localStorage when viewing details
      if (action.to && action.to.name === "CardDetail") {
        localStorage.setItem(LOCAL_STORAGE.SELECTED_CARD, JSON.stringify(card));
      }
      
      if (action.to) {
        if (this.$router) this.$router.push(action.to);
      } else if (action.value) {
        this.handleCardAction(card, action.value);
      }
    },

    handleCardAction(card, action) {
      if (action.type === "toggle-block") {
        this.toggleCardBlock(card);
      }
    },

    async toggleCardBlock(card) {
      try {
        const newStatus = card.cardStatus === "BLOCKED" ? "ACTIVE" : "BLOCKED";
        await this.$store.dispatch('myCards/updateCardStatus', {
          cardId: card.id,
          newStatus: newStatus
        });
        
        // Show success message
        this.$toast?.success(`Card ${newStatus === "ACTIVE" ? "unblocked" : "blocked"} successfully`);
      } catch (error) {
        console.error("Failed to update card status:", error);
        this.$toast?.error("Failed to update card status");
      }
    },

    globalClick() {
      this.openMenu = null;
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

.hero h1 {
  font-size: 32px;
  margin: 8px 0;
}
.subtitle {
  color: var(--muted);
  margin-bottom: 22px;
}
.card-number-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 18px;
}
.card-number {
  font-size: 22px;
  letter-spacing: 4px;
  cursor: pointer;
  line-height: 1;
  margin: 0;
}
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
.card-eye:hover {
  background: rgba(255,255,255,0.12);
}
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
}
.card {
  background: #0f2b4a;
  padding: 24px;
  border-radius: 14px;
  color: #fff;
  min-height: 220px;
  position: relative;
  overflow: hidden;
  width: 560px;
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
  right: 48px;
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
  html, body {
    max-width: 100%;
    overflow-x: hidden;
  }
  .card {
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
  }
  .container {
    max-width: 100%;
    overflow-x: hidden;
  }
}
</style>
