<template>
  <article class="card-visual-wrap">
    <div v-if="activeCard" class="credit-card" :class="{ blocked: activeCard.status === 'BLOCKED' }">

      <div class="bank-brand">
        <div class="bank-logo">{{ bankLabel }}</div>
        <div class="bank-sub" v-if="bankLabel !== ' BANK'">{{ bankLabelSub }}</div>
      </div>

      <div v-if="showMenu" class="card-menu" ref="menuRoot">

        <button
          class="menu-btn"
          @click="menuOpen = !menuOpen"
          :aria-expanded="String(menuOpen)"
          aria-label="Card actions"
          title="Card actions"
        >
          ⋮
        </button>

        <div v-if="menuOpen" class="menu-pop" role="menu" aria-label="Card options">
          <div class="menu-panel">
            <button
              v-for="(a, i) in actions"
              :key="i"
              class="menu-item"
              @click="selectAction(a, i)"
              role="menuitem"
            >
              {{ a.label }}
            </button>
          </div>
        </div>
      </div>

      <div class="status-badge">{{ activeCard.status }}</div>

      <div class="network-wrap" aria-hidden="true">
        <div class="network-logo mastercard" v-if="!activeCard.network">
          <span class="mc-left"></span><span class="mc-right"></span>
        </div>
        <div class="network-logo mastercard" v-else-if="activeCard.network.toLowerCase().includes('master')">
          <span class="mc-left"></span><span class="mc-right"></span>
        </div>
        <img v-else-if="activeCard.networkLogo" :src="activeCard.networkLogo" alt="network" class="network-img" />
        <div class="logo-badge" v-if="activeCard.badge">{{ activeCard.badge }}</div>
      </div>

      <button
        class="eye"
        @click="toggleMask"
        :title="masked ? 'Show number' : 'Hide number'"
        :aria-pressed="String(!masked)"
      >
        <svg v-if="!masked" xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7S1 12 1 12z"/>
          <circle cx="12" cy="12" r="3.2"/>
        </svg>
        <svg v-else xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <path d="M17.94 17.94A10.94 10.94 0 0 1 12 19c-7 0-11-7-11-7a19.1 19.1 0 0 1 5.06-5.94"/>
          <path d="M1 1l22 22"/>
          <path d="M9.88 9.88A3.5 3.5 0 0 0 12 15.5c.8 0 1.53-.28 2.12-.72"/>
        </svg>
      </button>

      <div class="number-row" role="group" aria-label="Card number">
        <span class="mask">{{ masked ? maskedNumber(activeCard.number) : fullNumber(activeCard.number) }}</span>
      </div>

      <div class="meta-row">
        <div class="cardholder-name">{{ activeCard.holder || userName || 'RAHUL VERMA' }}</div>

        <div class="card-meta">
          <div class="expires">
            <div class="label">VALID THRU</div>
            <div class="value">{{ activeCard.expiry || '12/26' }}</div>
          </div>
        </div>
      </div>

      <div class="card-divider" aria-hidden="true"></div>

      <div class="card-limits">
        <div class="limit-left">
          <div class="label small">AVAILABLE LIMIT</div>
          <div class="amt success">₹{{ formatNumber(activeCard.availableLimit) }}</div>
        </div>
        <div class="limit-right">
          <div class="label small">TOTAL LIMIT</div>
          <div class="amt">₹{{ formatNumber(activeCard.totalLimit) }}</div>
        </div>
        <div class="network-type" :class="activeCard.network?.toLowerCase()">{{ activeCard.network  }}</div>
      </div>
    </div>

    <div v-else class="notice">No card data available.</div>
  </article>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'

const props = defineProps({
  card: { type: Object, required: false },
  userName: { type: String, default: '' },
  showMenu: { type: Boolean, default: false },
  actions: { type: Array, default: () => [] }
})
const emit = defineEmits(['action', 'block'])

const masked = ref(true)
const menuOpen = ref(false)
const menuRoot = ref(null)

const localCard = ref(null)

const activeCard = computed(() => props.card || localCard.value)

const bankLabel = computed(() => {
  const c = activeCard.value
  if (!c) return 'BANK'
  return (c.bank || c.bankName || c.issuer || ' HDFC BANK').toUpperCase().slice(0, 18)
})
const bankLabelSub = computed(() => {
  return (activeCard.value && activeCard.value.bank && activeCard.value.bank.length > 6) ? 'Bank' : ''
})

const cardLast4 = computed(() => {
  const c = activeCard.value
  if (!c || !c.number) return '1234'
  return String(c.number).replace(/\s+/g, '').slice(-4)
})

function maskedNumber(n) {
  if (!n) return '**** **** **** 1234'
  const s = String(n).replace(/\s+/g, '')
  return '**** **** **** ' + s.slice(-4)
}
function fullNumber(n) {
  if (!n) return '1111 2222 3333 4444'
  return String(n).replace(/\s+/g, '').replace(/(\d{4})(?=\d)/g, '$1 ')
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
  emit('block', { card: c, newStatus: c.status === 'BLOCKED' ? 'ACTIVE' : 'BLOCKED' })
}

function selectAction(action, idx) {
  menuOpen.value = false
  emit('action', { action, idx })
}

function onDocClick(e) {
  if (!menuRoot.value) return
  if (!menuRoot.value.contains(e.target)) menuOpen.value = false
}
watch(menuOpen, (open) => {
  if (open) document.addEventListener('click', onDocClick)
  else document.removeEventListener('click', onDocClick)
})
function sanitizeText(v) {
  if (v === undefined || v === null) return ''
  return String(v).replace(/</g, '') 
}

const safeBankLabel = computed(() => sanitizeText( (activeCard.value && (activeCard.value.bank || activeCard.value.bankName || activeCard.value.issuer)) || 'BANK' ).toUpperCase().slice(0,18))
const safeBankLabelSub = computed(() => sanitizeText(activeCard.value?.bank && activeCard.value.bank.length > 6 ? 'Bank' : ''))
const safeHolder = computed(() => sanitizeText(activeCard.value?.holder || props.userName || 'RAHUL VERMA'))
const safeNetwork = computed(() => sanitizeText(activeCard.value?.network || 'VISA'))

onMounted(async () => {
  const url = (typeof import.meta !== 'undefined' && import.meta.env && import.meta.env.BASE_URL)
    ? new URL('/data/card.json', import.meta.env.BASE_URL).href
    : '/data/card.json';

  try {
    console.info('[Card] attempting to fetch mock JSON from:', url);
    const resp = await fetch(url, { cache: 'no-store' });
    console.info('[Card] fetch response status:', resp.status, resp.statusText);

    if (!resp.ok) {
      console.warn('[Card] mock fetch returned non-OK status:', resp.status);
      return;
    }

    const data = await resp.json();
    console.info('[Card] mock JSON loaded:', data);

    if (!props.card) {
      localCard.value = data;
      console.info('[Card] active card set from local mock (props.card absent).');
    } else {
      console.info('[Card] props.card present — mock loaded but NOT overriding prop.');
    }
  } catch (err) {
    console.error('[Card] failed to load /data/card.json mock — error:', err);
  }
});

defineExpose({ toggleMask, toggleBlock, maskedNumber, fullNumber, formatNumber, formatAmount, formatDate, cardLast4, activeCard })
</script>

<style scoped>
.credit-card {
  position: relative;
  border-radius: 14px;
  padding: 22px 26px;
  color: #f3eaea;
  width: 430px;
  min-height: 200px;
  background: linear-gradient(135deg, #6a1292 0%, #0d3b72 55%, #b71c2e 100%);
  background-blend-mode: multiply;
  box-shadow: 0 18px 36px rgba(30, 175, 219, 0.28);
  overflow: visible;
  z-index: 20;
  transform: translateY(-8px);
  font-family: "Inter", system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial;
}

.credit-card::before {
  content: '';
  position: absolute;
  left: -40%;
  top: -10%;
  width: 70%;
  height: 140%;
  background: radial-gradient(ellipse at left center, rgba(0,0,0,0.45) 0%, transparent 60%);
  pointer-events:none;
}

.bank-brand {
  position: absolute;
  left: 50%;
  top: 12px;
  transform: translateX(-50%);
  display:flex;
  align-items: baseline;
  gap: 6px;
  z-index: 40; 
}
.bank-logo {
  font-weight: 900;
  font-size: 18px;
  letter-spacing: 0.6px;
  color: #ffffff;
  background: linear-gradient(90deg,#ffffff 0%, rgba(255,255,255,0.85) 40%);
  color: #022a5a;
  padding: 2px 8px;
  border-radius: 4px;
  box-shadow: 0 3px 8px rgba(0,0,0,0.25);
}
.bank-sub {
  font-size: 11px;
  color: rgba(255,255,255,0.86);
  margin-top: 2px;
}

.card-menu {
  position: absolute;
  right: 18px;
  top: 16px;
  cursor: pointer;
  user-select: none;
  font-size: 20px;
  line-height: 1;
  color: rgba(255,255,255,0.95);
}
.menu-btn {
  background: transparent;
  border: none;
  color: inherit;
  font-size: 20px;
  line-height: 1;
}

.status-badge {
  position: absolute;
  left: 16px;
  top: 10px;   
  background: rgba(0,0,0,0.44);
  padding: 6px 10px;
  border-radius: 8px;
  font-weight: 700;
  font-size: 12px;
  color: #fff;
  box-shadow: 0 6px 18px rgba(0,0,0,0.25);
  z-index: 50;   
}

.card-menu {
  position: absolute;
  right: 16px;
  top: 14px;
  z-index: 60;
}
.menu-btn {
  background: transparent;
  border: none;
  color: rgba(255,255,255,0.95);
  font-size: 20px;
  line-height: 1;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display:flex;
  align-items:center;
  justify-content:center;
  cursor:pointer;
}
.menu-btn:hover { background: rgba(255,255,255,0.04); }

.menu-pop {
  position: absolute;
  right: 0;
  top: 44px; 
  min-width: 180px;
  display: block;
  z-index: 80;
}
.menu-panel {
  background: rgba(255,255,255,0.98);
  color: #022a5a;
  border-radius: 10px;
  box-shadow: 0 10px 30px rgba(2,6,23,0.25);
  overflow: hidden;
  padding: 6px 0;
  border: 1px solid rgba(2,6,23,0.06);
}
.menu-item {
  display:block;
  width:100%;
  text-align:left;
  padding: 10px 14px;
  background: transparent;
  border: none;
  font-size: 13px;
  cursor: pointer;
}
.menu-item:hover {
  background: rgba(2,6,23,0.06);
}

.meta-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  margin-top: 12px;
}

.cardholder-name {
  font-size: 14px;
  font-weight: 800;
  color: rgba(255,255,255,0.98);
  letter-spacing: 0.2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 58%; 
}

.card-meta {
  margin: 0;
  display:flex;
  align-items:flex-end;
  gap: 8px;
}
.card-meta .label {
  font-size: 11px;
  color: rgba(255,255,255,0.78);
  margin-bottom: 4px;
}
.card-meta .value {
  font-size: 14px;
  font-weight: 800;
}

@media (max-width: 420px) {
  .meta-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  .cardholder-name { max-width: 100%; }
  .card-meta { align-self: flex-end; }
  .menu-pop { right: -8px; } 
}

.network-type.visa {
  font-family: "Segoe UI", Tahoma, sans-serif;
  font-weight: 900;
  font-size: 18px;
  color: #7376a3; 
  letter-spacing: 2px;
}

.network-type.mastercard {
  font-family: "Arial Black", Gadget, sans-serif;
  font-weight: 800;
  font-size: 15px;
  color: #eb001b;
}

.network-type.rupay {
  font-family: "Verdana", sans-serif;
  font-style: italic;
  font-size: 15px;
  color: #005baa; 
}

.eye {
  position: absolute;
  right: 76px;
  top: 60px;
  background: rgba(255,255,255,0.04);
  width: 30px;
  height: 26px;
  display:flex;
  align-items:center;
  justify-content:center;
  border-radius: 8px;
  cursor: pointer;
  color: #ffffff;
  border: 1px solid rgba(255,255,255,0.04);
  transition: background 0.12s ease, transform .12s;
}
.eye:hover { transform: translateY(-2px); }
.eye svg { width:18px; height:18px; stroke: currentColor; }

.number-row {
  margin-top: 40px;   
  margin-left: 18px;
  margin-right: 18px;
  white-space: nowrap;
  overflow: hidden;
}

.number-row .mask {
  font-size: 22px;
  letter-spacing: 4px;
  font-weight: 700;
  font-family: ui-monospace, "Roboto Mono", Menlo, monospace;
  line-height: 1;
  display:block;
}

.network-type {
  position: absolute;
  bottom: 14px;
  right: 50px;
}
.card-divider {
  height: 1px;
  background: rgba(255,255,255,0.06);
  margin: 12px -26px 0;
}

.card-limits {
  display:flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding: 0 8px 4px 18px;
}
.card-limits .cardholder-name {
  font-size: 14px;
  font-weight: 800;
  margin-bottom: 6px;
  color: rgba(255,255,255,0.98);
  letter-spacing: 0.2px;
}
.card-limits .label.small {
  font-size:11px;
  color: rgba(255,255,255,0.78);
  margin-bottom:6px;
}
.card-limits .amt {
  font-weight:900;
  font-size:17px;
}
.card-limits .amt.success {
  color: #b7f5c9;
}

.credit-card.blocked {
  opacity: 0.8;
  filter: grayscale(0.15);
}

@media (max-width: 520px) {
  .credit-card { width: 320px; padding: 18px; min-height: 200px; }
  .number-row .mask { font-size: 18px; letter-spacing: 3px; }
  .emv { left: 18px; top: 82px; }
  .network-wrap { right: 14px; top: 48px; }
  .eye { right: 78px; top: 12px; }
  .bank-logo { font-size: 16px; }
}
</style>
