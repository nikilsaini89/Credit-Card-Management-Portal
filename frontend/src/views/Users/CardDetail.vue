<template>
  <div class="page-root">
    <!-- Topbar -->
    <NavigationBar/>

    <!-- Main -->
    <main class="container">
      <div class="page-header">
        <div class="title-block">
          <h1 class="page-title">Card Details</h1>
          <p class="page-sub">Manage your {{ card?.network || 'VISA' }} card ending in <strong>{{ cardLast4 }}</strong></p>
        </div>

        <div class="renderBack ">
          <RouterLink to="/cards" class="back">← Back to Cards</RouterLink>
        </div>
      </div>

      <section class="grid">
        <!-- LEFT: card component + actions -->
        <div class="left">
          <!-- Reusable card component placed above actions -->
          <Card
            v-if="card"
            :card="card"
            :userName="userName"
            :showMenu="false"
            :actions="cardMenu"
            @action="onCardAction"
            @block="onCardBlock"
          />

          <div v-else class="notice">No card data available.</div>

          <div class="actions-card">
            <h3>Card Actions</h3>
            <p class="muted">Manage your card settings and limits</p>

            <div class="action-controls">
              <!-- OPEN modal on click -->
              <button class="update-btn" @click="openLimitModal">↗ Update Credit Limit</button>
              <button class="block-btn" @click="toggleBlock(card)">
                {{ card && card.status === 'BLOCKED' ? 'Unblock Card' : 'Block Card' }}
              </button>
            </div>
          </div>
        </div>

        <!-- RIGHT: overview and details -->
        <div class="right">
          <div class="panel overview">
            <h3>Credit Overview</h3>
            <p class="muted">Your current credit utilization</p>

            <div class="overview-grid">
              <div class="ov-left">
                <div class="row">
                  <div class="rlabel">Credit Limit</div>
                  <div class="rval">₹{{ formatNumber(card?.totalLimit || dataTotalLimit) }}</div>
                </div>

                <div class="row">
                  <div class="rlabel">Outstanding Balance</div>
                  <div class="rval danger">₹{{ formatNumber(cardOutstanding) }}</div>
                </div>

                <div class="row">
                  <div class="rlabel">Available Credit</div>
                  <div class="rval success">₹{{ formatNumber(card?.availableLimit || dataAvailableCredit) }}</div>
                </div>

                <div class="util-row">
                  <div class="rlabel">Credit Utilization</div>
                  <div class="util-value util-danger">{{ utilizationPercent }}%</div>
                </div>

                <div class="progress-wrap">
                  <div class="progress util-danger" :style="{ width: utilizationPercent + '%' }"></div>
                </div>
              </div>
            </div>
          </div>

          <div class="panel transactions">
            <h3>Recent Transactions</h3>
            <p class="muted">Latest activity on this card</p>

            <ul class="tx-list">
              <li v-for="t in recentTransactions" :key="t.id" class="tx">
                <div class="tx-left">
                  <div class="tx-merchant">{{ t.merchant }}</div>
                  <div class="tx-meta">{{ t.category }} • {{ formatDate(t.date) }}</div>
                </div>
                <div class="tx-right">
                  <div :class="['tx-amount', (Number(t.amount) < 0) ? 'neg' : 'pos']">
                    {{ formatAmount(t.amount) }}
                  </div>
                </div>
              </li>
            </ul>
          </div>
        </div>

      </section>
    </main>

    <!-- Update Credit Limit Modal (renders when showLimitModal is true) -->
    <UpdateCreditLimitModal
      v-if="card"
      v-model="showLimitModal"
      :currentLimit="card?.totalLimit"
      :outstanding="cardOutstanding"
      @next="handleLimitNext"
    />

    <!-- Confirm Identity Modal (new) -->
    <ConfirmIdentityModal
      v-if="card"
      v-model="showConfirmModal"
      :amount="pendingNewLimit"
      :serverError="confirmServerError"
      @verified="onVerifiedPassword"
    />
  </div>
</template>

<script>
import axios from 'axios'
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import NavigationBar from '../../components/NavigationBar.vue'
import Card from '../../components/Card.vue'
import UpdateCreditLimitModal from '../../components/UpdateCreditLimitModal.vue'
import ConfirmIdentityModal from '../../components/ConfirmIdentityModal.vue'

export default {
  name: 'CardDetail',
  components: { NavigationBar, Card, UpdateCreditLimitModal, ConfirmIdentityModal },
  setup() {
    const route = useRoute()
    const router = useRouter()

    const card = ref(null)
    const userName = ref('')
    const loading = ref(true)
    const error = ref(null)
    const masked = ref(true)
    const dataTotalLimit = ref(0)
    const dataAvailableCredit = ref(0)
    const dataOutstanding = ref(0)
    const allTransactions = ref([])

    // Modal controls
    const showLimitModal = ref(false)
    const showConfirmModal = ref(false)
    const pendingNewLimit = ref(0) // amount selected from UpdateCreditLimitModal
    const confirmServerError = ref(null) // server-side error message to pass into confirm modal

    const cardLast4 = computed(() => {
      if (!card.value || !card.value.number) return '1234'
      return String(card.value.number).replace(/\s+/g,'').slice(-4)
    })

    const cardOutstanding = computed(() => {
      if (!card.value) return dataOutstanding.value || 0
      if (card.value.outstanding !== undefined) return Number(card.value.outstanding)
      const tot = Number(card.value.totalLimit || dataTotalLimit.value || 0)
      const avail = Number(card.value.availableLimit ?? dataAvailableCredit.value ?? 0)
      return Math.max(0, tot - avail)
    })

    const utilizationPercent = computed(() => {
      const tot = Number(card.value?.totalLimit || dataTotalLimit.value || 0)
      const out = Number(cardOutstanding.value || 0)
      if (!tot) return 0
      return Math.round((out / tot) * 100)
    })

    const recentTransactions = computed(() => {
      return (allTransactions.value || []).slice(0, 6)
    })

    function maskedNumber(n) {
      if (!n) return '**** **** **** 1234'
      const s = String(n).replace(/\s+/g,'')
      return '**** **** **** ' + s.slice(-4)
    }
    function fullNumber(n) {
      if (!n) return '1111 2222 3333 4444'
      return String(n).replace(/\s+/g,'').replace(/(\d{4})(?=\d)/g, '$1 ')
    }
    function formatNumber(n) {
      const v = Number(n || 0); return v.toLocaleString('en-IN')
    }
    function formatAmount(n) {
      const v = Number(n || 0)
      const sign = v < 0 ? '-' : ''
      return sign + '₹' + Math.abs(v).toLocaleString('en-IN')
    }
    function formatDate(ts) {
      if (!ts) return ''
      const d = new Date(ts)
      return d.toLocaleString('en-GB', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
    }

    function toggleMask() { masked.value = !masked.value }
    function toggleBlock(c) {
      if (!c) return
      c.status = c.status === 'BLOCKED' ? 'ACTIVE' : 'BLOCKED'
    }

    const cardMenu = computed(() => {
      const id = card.value?.id || ''
      return [
        { label: 'View Details', to: id ? `/cards/${id}` : null },
        { label: 'Manage Limits', to: id ? `/cards/${id}/limits` : null },
        { label: card.value?.status === 'BLOCKED' ? 'Unblock Card' : 'Block Card', value: { type: 'toggle-block' } }
      ]
    })

    function onCardAction({ action }) {
      if (!action) return
      if (action.to) {
        router.push(action.to)
      } else if (action.value?.type === 'toggle-block') {
        toggleBlock(card.value)
      } else {
        console.log('card action', action)
      }
    }

    function onCardBlock(payload) {
      if (!payload?.card) return
      payload.card.status = payload.newStatus
    }

    // when user selects new limit in UpdateCreditLimitModal
    function handleLimitNext(payload) {
      const newLimit = Number(payload?.newLimit || 0)
      if (!newLimit) return
      pendingNewLimit.value = newLimit
      // close the first modal and open confirm modal
      showLimitModal.value = false
      confirmServerError.value = null
      setTimeout(() => showConfirmModal.value = true, 160)
    }

    // when ConfirmIdentityModal emits verified (with password), verify and persist
    async function onVerifiedPassword({ password }) {
      confirmServerError.value = null
      try {
        // Example API flow:
        // 1) verify password
        // const verifyResp = await axios.post('/api/auth/verify-password', { password })
        // if (!verifyResp.data.ok) throw new Error('Invalid password')

        // 2) update limit
        // await axios.post('/api/cards/update-limit', { cardId: card.value?.id, newLimit: pendingNewLimit.value })

        // For demo (no backend), we'll simulate success:
        await new Promise(r => setTimeout(r, 600))

        // Update local card on success
        if (card.value) {
          card.value.totalLimit = Number(pendingNewLimit.value)
        }

        // close confirm modal
        showConfirmModal.value = false

        // optionally show toast / message
        console.log('Limit successfully updated to', pendingNewLimit.value)
      } catch (err) {
        // surface server error to modal via confirmServerError prop
        console.error('verification/update failed', err)
        const msg = err?.response?.data?.message || (err.message || 'Verification failed')
        confirmServerError.value = String(msg)
        // keep modal open so user can retry
      }
    }

    // helpers to open/close initial modal
    function openLimitModal() {
      if (!card.value) {
        console.warn('No card selected to update limit for.')
        return
      }
      showLimitModal.value = true
    }
    function closeLimitModal() {
      showLimitModal.value = false
    }

    const load = async () => {
      try {
        const res = await axios.get('/data/dashboard.json')
        const data = res.data
        userName.value = data.user?.name || 'User'
        dataTotalLimit.value = (data.totalLimit ?? data.cards?.reduce((s, i) => s + (Number(i.totalLimit) || 0), 0)) || 0

        dataAvailableCredit.value = data.availableCredit ?? 0
        dataOutstanding.value = data.outstanding ?? 0
        allTransactions.value = data.transactions || []

        const idToFind = route.params.id || (data.cards?.[0]?.id)
        card.value = (data.cards || []).find(c => String(c.id) === String(idToFind)) || data.cards?.[0] || null
      } catch (err) {
        console.error(err)
        error.value = 'Failed to load data'
      } finally {
        loading.value = false
      }
    }

    onMounted(load)

    return {
      card, userName, loading, error, masked,
      cardLast4, maskedNumber, fullNumber,
      formatNumber, formatAmount, formatDate,
      utilizationPercent, recentTransactions,
      cardOutstanding, dataTotalLimit, dataAvailableCredit,
      toggleMask, toggleBlock, cardMenu, onCardAction, onCardBlock,
      // modal bindings returned to template
      showLimitModal, handleLimitNext, openLimitModal, closeLimitModal,
      // confirm modal
      showConfirmModal, pendingNewLimit, confirmServerError, onVerifiedPassword
    }
  }
}
</script>

<style scoped>
:root{
  --bg: #f6f7f9;
  --accent: #0b2540;
  --muted: #6b7280;
  --green: #16a34a;
  --danger: #ef4444;
}

*{box-sizing:border-box}
.page-root{
  font-family: 'Inter', system-ui, -apple-system, 'Segoe UI', Roboto, Arial;
  background:var(--bg);
  min-height:100vh;
  color:#0f1724;
  overflow-x:hidden; /* prevent horizontal scroll */
}

/* PAGE HEADER */
.container{
  max-width:1200px;
  margin:24px auto;
  padding:0 16px 80px; /* smaller side padding to avoid overflow */
  width:100%;
}
.page-header{
  display:flex;
  gap:16px;
  align-items:center;
  margin-bottom:18px;
  justify-content:space-between;
  flex-wrap:wrap; /* allow items to wrap */
}
.back{ color:var(--muted); text-decoration:none; padding:6px 8px; white-space:nowrap; }
.title-block{ display:flex; flex-direction:column; min-width:0; } /* min-width:0 avoids overflow inside flex */
.page-title{ margin:0; font-size:28px; font-weight:800; word-break:break-word; }
.page-sub{ color:var(--muted); margin-top:6px; font-size:14px }

/* GRID
   Use a fluid right column (clamped) instead of fixed 420px which caused overflow.
   Left column gets remaining space. */
.grid{
  display:grid;
  grid-template-columns: 1fr minmax(260px, 35%); /* right column will not exceed 35% of container */
  gap:20px;
  align-items:start;
  width:100%;
}

/* If container is small, make single column */
@media (max-width: 980px){
  .grid{ grid-template-columns: 1fr; gap:16px; }
}

/* LEFT */
.left{ display:flex; flex-direction:column; gap:14px; min-width:0; }

/* ACTIONS */
.actions-card{
  background:#fff; border-radius:12px; padding:16px; box-shadow:0 8px 20px rgba(2,6,23,0.04);
  width:100%;
}
.actions-card h3{ margin:0 0 6px 0; font-size:18px; }
.muted{ color:var(--muted); margin-bottom:12px; }
.action-controls{ display:flex; gap:12px; margin-top:8px; flex-wrap:wrap; }
.action-controls button{ flex:1 1 auto; min-width:0; } /* allow wrapping and shrinking */
.update-btn{
  background: linear-gradient(180deg,#0b2540,#0b2540); color:#fff; padding:10px 14px; border-radius:10px; border:none; font-weight:800;
  box-shadow:0 10px 24px rgba(11,37,64,0.12);
}
.block-btn{
  background:#fff; border:1px solid #e7e7ea; padding:10px 14px; border-radius:10px; font-weight:700;
}

/* RIGHT */
.right{ display:flex; flex-direction:column; gap:12px; min-width:0; }

/* panels */
.panel{
  background:#fff; border-radius:12px; padding:16px; box-shadow: 0 8px 22px rgba(2,6,23,0.04);
  width:100%;
  overflow:hidden; /* safeguard against child overflow */
}
.panel h3{ margin:0 0 8px 0; font-size:16px; font-weight:800; }
.panel .muted{ margin:0; }

/* overview */
.overview-grid{ display:flex; gap:12px; align-items:flex-start; flex-wrap:wrap; }
.ov-left{ flex:1 1 240px; min-width:0 } /* allow shrinking */
.row{ display:flex; justify-content:space-between; align-items:center; padding:10px 0; border-bottom:1px dashed #f3f4f6; }
.row:last-child{ border-bottom:0; }
.rlabel{ color:var(--muted); font-size:14px; white-space:normal; overflow-wrap:break-word; }
.rval{ font-weight:800; font-size:16px; white-space:nowrap; text-overflow:ellipsis; overflow:hidden; max-width:100%; }
.rval.success{ color:var(--green); }
.rval.danger{ color:var(--danger); }

/* util */
.ov-right{ width:auto; display:flex; flex-direction:column; align-items:flex-end; gap:8px; min-width:0; }
.util-value{ font-weight:800; }
.progress-wrap{ width:100%; background:#f1f5f9; height:12px; border-radius:999px; overflow:hidden; }
.progress{ height:100%; background:#0b2540; width:0%; transition:width .45s ease; }

/* details kv */
.kv{ display:flex; justify-content:space-between; align-items:center; padding:12px 0; border-top:1px solid #f3f5f7; }
.kv:first-of-type{ border-top:0; }

/* transactions */
.tx-list{ list-style:none; padding:0; margin:8px 0 0; }
.tx{
  display:flex;
  justify-content:space-between;
  align-items:flex-start;
  gap:12px;
  padding:12px 0;
  border-bottom:1px solid #f6f7f9;
  min-width:0;
}
.tx:last-child{ border-bottom:none; }
.tx-left{ min-width:0; max-width:100%; }
.tx-merchant{ font-weight:700; word-break:break-word; }
.tx-meta{ color:var(--muted); font-size:13px; margin-top:6px; white-space:normal; }
.tx-right{ flex:0 0 110px; text-align:right; min-width:90px; } /* fixed small area for amounts */
.tx-amount{ font-weight:800; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.tx-amount.neg{ color:var(--danger); }
.tx-status{ margin-top:8px; font-size:11px; color:var(--muted); background:#f8fafc; padding:6px 10px; border-radius:999px; display:inline-block; }

/* responsive tweaks */
@media (max-width: 760px){
  .container{ padding:0 12px 120px; }
  .page-title{ font-size:20px; }
  .page-sub{ font-size:13px; }
  .renderBack{ order:2; width:100%; margin-top:8px; }
  .grid{ grid-template-columns: 1fr; gap:12px; }
  .action-controls{ flex-direction:column; }
  .action-controls button{ width:100%; }
  .panel{ padding:12px; }
  .panel h3{ font-size:15px; }
  .overview-grid{ flex-direction:column; gap:10px; }
  .ov-right{ width:100%; align-items:flex-start; }
  .row{ padding:8px 0 }
  .rlabel{ font-size:13px }
  .rval{ font-size:15px; }
  .tx-right{ flex:0 0 95px; min-width:80px; }
}

/* very small phones */
@media (max-width:420px){
  .page-title{ font-size:18px }
  .page-sub{ font-size:12px }
  .progress-wrap{ height:10px }
  .tx-meta{ font-size:12px }
  .tx-right{ flex:0 0 85px; min-width:70px; }
}

/* color overrides */
.rval.success{ color:#16a34a !important; }
.rval.danger{ color:#ef4444 !important; }
.tx-amount.pos{ color:#16a34a !important; }
.tx-amount.neg{ color:#ef4444 !important; }

/* utility classes */
.util-row{ display:flex; justify-content:space-between; align-items:center; gap:12px; margin-bottom:10px; }
.util-value{ font-weight:800; font-size:15px; color:#0f1724; }
.util-danger{ color:#ef4444 !important; }

/* ensure interactive elements can shrink and not overflow */
button, a, input, select, textarea{ max-width:100%; }
</style>
