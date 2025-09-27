<template>
  <div class="page-root">
    <main class="container">
      <div class="page-header">
        <div class="title-block">
          <h1 class="page-title">Card Details</h1>
          <p class="page-sub">Manage your {{ card?.cardType?.name || card?.network || 'VISA' }} card ending in <strong>{{ cardLastFour }}</strong></p>
        </div>

        <div class="renderBack">
          <RouterLink to="/cards" class="back">← Back to Cards</RouterLink>
        </div>
      </div>

      <section class="grid">
        <div class="left">
          <Card
            v-if="card"
            :card="cardPresentation"
            :userName="userName"
            :showMenu="false"
            :actions="cardActionsMenu"
            @action="handleCardAction"
            @block="handleCardBlock"
          />

          <div v-else class="notice">No card data available.</div>

          <div class="actions-card">
            <h3>Card Actions</h3>
            <p class="muted">Manage your card settings and limits</p>

            <div class="action-controls">
              <button class="update-btn" @click="openLimitModal" :disabled="loading">↗ Update Credit Limit</button>
            </div>
          </div>
        </div>

        <div class="right">
          <div class="panel overview">
            <h3>Credit Overview</h3>
            <p class="muted">Your current credit utilization</p>

            <div class="overview-grid">
              <div class="ov-left">
                <div class="row">
                  <div class="rlabel">Credit Limit</div>
                  <div class="rval">₹{{ formatINR(card?.creditLimit || card?.totalLimit || fallbackTotalLimit) }}</div>
                </div>

                <div class="row">
                  <div class="rlabel">Outstanding Balance</div>
                  <div class="rval danger">₹{{ formatINR(calculatedOutstanding) }}</div>
                </div>

                <div class="row">
                  <div class="rlabel">Available Credit</div>
                  <div class="rval success">₹{{ formatINR(card?.availableLimit || fallbackAvailableCredit) }}</div>
                </div>

                <div class="util-row">
                  <div class="rlabel">Credit Utilization</div>
                  <div class="util-value util-danger">{{ utilizationPercentage }}%</div>
                </div>

                <div class="progress-wrap">
                  <div class="progress util-danger" :style="{ width: utilizationPercentage + '%' }"></div>
                </div>

                <div class="row card-number-row" v-if="card">
                  <div class="rlabel">Card Number</div>
                  <div class="rval">
                    <span>{{ showFullNumber ? (fullPan || card.cardNumber || card.number) : (card.cardNumber || card.number) }}</span>
                    <button class="link-btn" @click="toggleShowFull" :disabled="loadingPan || loading">
                      {{ showFullNumber ? 'Hide' : (loadingPan ? 'Loading...' : 'Show') }}
                    </button>
                  </div>
                </div>

              </div>
            </div>
          </div>

          <div class="panel transactions">
            <h3>Recent Transactions</h3>
            <p class="muted">Latest activity on this card</p>

            <ul class="tx-list">
              <li v-for="tx in recentTransactionsList" :key="tx.id" class="tx">
                <div class="tx-left">
                  <div class="tx-merchant">{{ tx.merchant }}</div>
                  <div class="tx-meta">{{ tx.category }} • {{ formatDateTime(tx.date) }}</div>
                </div>
                <div class="tx-right">
                  <div :class="['tx-amount', (Number(tx.amount) < 0) ? 'neg' : 'pos']">
                    {{ formatCurrency(tx.amount) }}
                  </div>
                </div>
              </li>
            </ul>
          </div>
        </div>

      </section>
    </main>

    <UpdateCreditLimitModal
      v-if="card"
      v-model="isLimitModalOpen"
      :currentLimit="Number(card.creditLimit ?? card.totalLimit ?? fallbackTotalLimit)"
      :outstanding="calculatedOutstanding"
      @next="handleLimitNext"
    />
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import Card from '../../components/Card.vue'
import UpdateCreditLimitModal from '../../components/UpdateCreditLimitModal.vue'
import { getCardById, getCardPan, updateCardLimit } from '../../services/dashboardService'

export default {
  name: 'CardDetail',
  components: { Card, UpdateCreditLimitModal }, // <-- ConfirmIdentityModal removed
  setup() {
    const route = useRoute()
    const router = useRouter()

    const card = ref(null)
    const userName = ref('')
    const loading = ref(false)
    const error = ref(null)
    const fallbackTotalLimit = ref(0)
    const fallbackAvailableCredit = ref(0)
    const transactions = ref([])

    const isLimitModalOpen = ref(false)
    const pendingLimit = ref(0)
    const updating = ref(false)

    const showFullNumber = ref(false)
    const fullPan = ref(null)
    const loadingPan = ref(false)

    const cardLastFour = computed(() => {
      if (!card.value) return '1234'
      const num = card.value.cardNumber ?? card.value.number
      if (!num) return '1234'
      return String(num).replace(/\s+/g,'').slice(-4)
    })

    const cardPresentation = computed(() => {
      if (!card.value) return null
      return {
        id: card.value.id ?? card.value.cardId,
        name: card.value.cardType?.name ?? card.value.name ?? 'Card',
        number: card.value.cardNumber ?? card.value.number,
        totalLimit: Number(card.value.creditLimit ?? card.value.totalLimit ?? 0),
        availableLimit: Number(card.value.availableLimit ?? 0),
        status: card.value.cardStatus ?? card.value.status
      }
    })

    const calculatedOutstanding = computed(() => {
      if (!card.value) return 0
      if (card.value.outstanding !== undefined && card.value.outstanding !== null) return Number(card.value.outstanding)
      const total = Number(card.value?.creditLimit ?? card.value?.totalLimit ?? (fallbackTotalLimit.value || 0))
      const avail = Number(card.value?.availableLimit ?? fallbackAvailableCredit.value ?? 0)
      return Math.max(0, total - avail)
    })

    const utilizationPercentage = computed(() => {
      const total = Number(card.value?.creditLimit ?? card.value?.totalLimit ?? (fallbackTotalLimit.value || 0))
      const out = Number(calculatedOutstanding.value || 0)
      if (!total) return 0
      return Math.round((out / total) * 100)
    })

    const recentTransactionsList = computed(() => (transactions.value || []).slice(0, 6))

    function formatINR(amount) {
      const numericValue = Number(amount || 0)
      return numericValue.toLocaleString('en-IN')
    }

    function formatCurrency(amount) {
      const numericValue = Number(amount || 0)
      const sign = numericValue < 0 ? '-' : ''
      return sign + '₹' + Math.abs(numericValue).toLocaleString('en-IN')
    }

    function formatDateTime(timestamp) {
      if (!timestamp) return ''
      const dateObj = new Date(timestamp)
      return dateObj.toLocaleString('en-GB', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
    }

    const cardActionsMenu = computed(() => {
      const id = card.value?.id || ''
      return [
        { label: 'View Details', to: id ? `/cards/${id}` : null },
        { label: 'Manage Limits', to: id ? `/cards/${id}/limits` : null },
        { label: card.value?.status === 'BLOCKED' ? 'Unblock Card' : 'Block Card', value: { type: 'toggle-block' } }
      ]
    })

    function handleCardAction({ action }) {
      if (!action) return
      if (action.to) router.push(action.to)
    }

    function handleCardBlock(payload) {
      if (!payload?.card) return
      payload.card.status = payload.newStatus
    }

    function openLimitModal() {
      if (!card.value) return
      isLimitModalOpen.value = true
    }

    async function handleLimitNext(payload) {
      const selectedNewLimit = Number(payload?.newLimit || 0)
      if (!selectedNewLimit) return
      pendingLimit.value = selectedNewLimit
      isLimitModalOpen.value = false
      updating.value = true

      const id = card.value?.id || route.params.id
      if (!id) {
        alert('Invalid card id')
        updating.value = false
        return
      }

      try {
        const res = await updateCardLimit(id, Number(pendingLimit.value))
        const updated = res
        if (updated && (updated.newLimit !== undefined || updated.availableLimit !== undefined)) {
          await loadCard()
        } else if (updated && updated.card) {
          card.value = updated.card
          transactions.value = updated.transactions || []
        } else {
          await loadCard()
        }
        window.dispatchEvent(new CustomEvent('card-updated', { detail: { cardId: id } }))
        pendingLimit.value = 0
        alert('Limit updated successfully')
      } catch (err) {
        console.error('Failed to update limit', err)
        const msg = err?.response?.data?.message || err?.message || 'Update failed'
        alert(`Failed to update limit: ${String(msg)}`)
      } finally {
        updating.value = false
      }
    }

    async function toggleShowFull() {
      if (showFullNumber.value) {
        showFullNumber.value = false
        return
      }
      if (fullPan.value) {
        showFullNumber.value = true
        return
      }
      loadingPan.value = true
      try {
        const id = route.params.id
        const pan = await getCardPan(id)
        fullPan.value = pan
        showFullNumber.value = true
      } catch (err) {
        console.error('Unable to fetch full PAN', err)
        alert('Unable to show full card number. Make sure you are the card owner.')
      } finally {
        loadingPan.value = false
      }
    }

    async function loadCard() {
      loading.value = true;
      error.value = null;
      console.log('loadCard(): route.id =', route.params.id);

      try {
        const id = route.params.id;
        if (!id) {
          error.value = 'No card id provided';
          console.warn('loadCard: no route param id');
          return;
        }

        // debug: show token presence (remove in prod)
        console.log('loadCard: token present=', !!localStorage.getItem('token'));

        // call service (which uses axios instance with interceptor)
        const resp = await getCardById(id);
        console.log('loadCard: getCardById response =', resp);

        // Case A: backend returns a wrapper { card: {...}, transactions: [...] }
        if (resp && resp.card) {
          card.value = resp.card;
          transactions.value = resp.transactions || [];
          console.log('loadCard: set from resp.card');
          return;
        }

        // Case B: backend returns the card object directly
        const looksLikeCard = resp && (resp.id || resp.cardNumber || resp.creditLimit || resp.totalLimit);
        if (looksLikeCard) {
          card.value = resp;
          // if resp contains transactions array (sometimes included), use it
          transactions.value = resp.transactions || [];
          console.log('loadCard: set from direct card object');
          return;
        }

        // Case C: backend returned a dashboard-like object: { cards: [...] }
        if (resp && Array.isArray(resp.cards)) {
          const found = resp.cards.find(c => String(c.id) === String(id) || String(c.cardId) === String(id));
          if (found) {
            card.value = found;
            transactions.value = resp.transactions || [];
            console.log('loadCard: found card in resp.cards');
            return;
          }
        }

        // nothing matched
        console.warn('loadCard: response did not contain card data', resp);
        error.value = 'Card data not available from server';
      } catch (err) {
        console.error('loadCard: failed', err);
        // show sensible message
        if (err?.response) {
          // axios error with response
          console.error('status', err.response.status, 'data', err.response.data);
          if (err.response.status === 401 || err.response.status === 403) {
            error.value = 'You are not authorized. Please login.';
          } else if (err.response.status === 404) {
            error.value = 'Card not found (404).';
          } else {
            error.value = err.response.data?.message || 'Server error while loading card';
          }
        } else {
          error.value = err.message || 'Network error';
        }
      } finally {
        loading.value = false;
      }
    }


    onMounted(loadCard)

    return {
      card,
      userName,
      loading,
      error,
      isLimitModalOpen,
      pendingLimit,
      updating,
      cardLastFour,
      cardPresentation,
      calculatedOutstanding,
      utilizationPercentage,
      recentTransactionsList,
      formatINR,
      formatCurrency,
      formatDateTime,
      cardActionsMenu,
      handleCardAction,
      handleCardBlock,
      openLimitModal,
      handleLimitNext,
      showFullNumber,
      fullPan,
      loadingPan,
      toggleShowFull
    }
  }
}
</script>


<style scoped>
.link-btn {
  background: none;
  border: none;
  color: #2563eb;
  cursor: pointer;
  margin-left: 8px;
  font-size: 0.9rem;
}
.link-btn:disabled { opacity: 0.6; cursor: not-allowed; }

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
  overflow-x:hidden;
}

.container{
  max-width:1200px;
  margin:24px auto;
  padding:0 16px 80px;
  width:100%;
}
.page-header{
  display:flex;
  gap:16px;
  align-items:center;
  margin-bottom:18px;
  justify-content:space-between;
  flex-wrap:wrap;
}
.back{ color:var(--muted); text-decoration:none; padding:6px 8px; white-space:nowrap; }
.title-block{ display:flex; flex-direction:column; min-width:0; }
.page-title{ margin:0; font-size:28px; font-weight:800; word-break:break-word; }
.page-sub{ color:var(--muted); margin-top:6px; font-size:14px }

.grid{
  display:grid;
  grid-template-columns: 1fr minmax(260px, 35%);
  gap:20px;
  align-items:start;
  width:100%;
}

@media (max-width: 980px){
  .grid{ grid-template-columns: 1fr; gap:16px; }
}

.left{ display:flex; flex-direction:column; gap:14px; min-width:0; }

.actions-card{
  background:#fff; border-radius:12px; padding:16px; box-shadow:0 8px 20px rgba(2,6,23,0.04);
  width:100%;
}
.actions-card h3{ margin:0 0 6px 0; font-size:18px; }
.muted{ color:var(--muted); margin-bottom:12px; }
.action-controls{ display:flex; gap:12px; margin-top:8px; flex-wrap:wrap; }
.action-controls button{ flex:1 1 auto; min-width:0; }
.update-btn{
  background: linear-gradient(180deg,#0b2540,#0b2540); color:#fff; padding:10px 14px; border-radius:10px; border:none; font-weight:800;
  box-shadow:0 10px 24px rgba(11,37,64,0.12);
}
.block-btn{
  background:#fff; border:1px solid #e7e7ea; padding:10px 14px; border-radius:10px; font-weight:700;
}

.right{ display:flex; flex-direction:column; gap:12px; min-width:0; }

.panel{
  background:#fff; border-radius:12px; padding:16px; box-shadow: 0 8px 22px rgba(2,6,23,0.04);
  width:100%;
  overflow:hidden;
}
.panel h3{ margin:0 0 8px 0; font-size:16px; font-weight:800; }
.panel .muted{ margin:0; }

.overview-grid{ display:flex; gap:12px; align-items:flex-start; flex-wrap:wrap; }
.ov-left{ flex:1 1 240px; min-width:0 }
.row{ display:flex; justify-content:space-between; align-items:center; padding:10px 0; border-bottom:1px dashed #f3f4f6; }
.row:last-child{ border-bottom:0; }
.rlabel{ color:var(--muted); font-size:14px; white-space:normal; overflow-wrap:break-word; }
.rval{ font-weight:800; font-size:16px; white-space:nowrap; text-overflow:ellipsis; overflow:hidden; max-width:100%; }
.rval.success{ color:var(--green); }
.rval.danger{ color:var(--danger); }

.ov-right{ width:auto; display:flex; flex-direction:column; align-items:flex-end; gap:8px; min-width:0; }
.util-value{ font-weight:800; }
.progress-wrap{ width:100%; background:#f1f5f9; height:12px; border-radius:999px; overflow:hidden; }
.progress{ height:100%; background:#0b2540; width:0%; transition:width .45s ease; }

.kv{ display:flex; justify-content:space-between; align-items:center; padding:12px 0; border-top:1px solid #f3f5f7; }
.kv:first-of-type{ border-top:0; }

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
.tx-right{ flex:0 0 110px; text-align:right; min-width:90px; }
.tx-amount{ font-weight:800; white-space:nowrap; overflow:hidden; text-overflow:ellipsis; }
.tx-amount.neg{ color:var(--danger); }
.tx-status{ margin-top:8px; font-size:11px; color:var(--muted); background:#f8fafc; padding:6px 10px; border-radius:999px; display:inline-block; }

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

@media (max-width:420px){
  .page-title{ font-size:18px }
  .page-sub{ font-size:12px }
  .progress-wrap{ height:10px }
  .tx-meta{ font-size:12px }
  .tx-right{ flex:0 0 85px; min-width:70px; }
}

.rval.success{ color:#16a34a !important; }
.rval.danger{ color:#ef4444 !important; }
.tx-amount.pos{ color:#16a34a !important; }
.tx-amount.neg{ color:#ef4444 !important; }

.util-row{ display:flex; justify-content:space-between; align-items:center; gap:12px; margin-bottom:10px; }
.util-value{ font-weight:800; font-size:15px; color:#0f1724; }
.util-danger{ color:#ef4444 !important; }

button, a, input, select, textarea{ max-width:100%; }
</style>
