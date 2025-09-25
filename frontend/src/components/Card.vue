<template>
  <article class="card-visual-wrap">
    <div v-if="activeCard" class="credit-card" :class="{ blocked: activeCard.cardStatus === 'BLOCKED' }">

      <!-- Bank Brand -->
      <div class="bank-brand">
        <div class="bank-logo">{{ bankLabel }}</div>
        <div class="bank-sub" v-if="bankLabel !== 'BANK'">{{ bankLabelSub }}</div>
      </div>

      <!-- Card Menu -->
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
              v-for="(action, idx) in actions"
              :key="idx"
              class="menu-item"
              @click="handleAction(action, idx)"
              role="menuitem"
            >
              {{ action.label }}
            </button>
          </div>
        </div>
      </div>

      <!-- Status -->
      <div class="status-badge">{{ activeCard.cardStatus }}</div>

      <!-- Network Logos -->
      <div class="network-wrap" aria-hidden="true">
        <div class="network-logo mastercard" v-if="!activeCard.cardType?.networkType">
          <span class="mc-left"></span><span class="mc-right"></span>
        </div>
        <div class="network-logo mastercard" v-else-if="activeCard.cardType.networkType.toLowerCase().includes('master')">
          <span class="mc-left"></span><span class="mc-right"></span>
        </div>
        <img v-else-if="activeCard.cardType?.networkLogo" :src="activeCard.cardType.networkLogo" alt="network" class="network-img" />
        <div class="logo-badge" v-if="activeCard.badge">{{ activeCard.badge }}</div>
      </div>

      <!-- Eye Button -->
      <button
        class="eye"
        @click="toggleMask"
        :title="isMasked ? 'Show number' : 'Hide number'"
        :aria-pressed="String(!isMasked)"
      >
        <img :src="EyeOpenIcon" alt="show number" v-if="!isMasked" />
        <img :src="EyeClosedIcon" alt="hide number" v-else />
      </button>

      <!-- Card Number -->
      <div class="number-row" role="group" aria-label="Card number">
        <span class="mask">
          {{ isMasked ? getMaskedCardNumber(activeCard.cardNumber) : getFullCardNumber(activeCard.cardNumber) }}
        </span>
      </div>

      <!-- Card Meta -->
      <div class="meta-row">
        <div class="cardholder-name">{{ activeCard.cardHolderName || 'RAHUL VERMA' }}</div>
        <div class="card-meta">
          <div class="expires">
            <div class="label">VALID THRU</div>
            <div class="value">{{ formatExpiry(activeCard.expiryDate) }}</div>
          </div>
        </div>
      </div>

      <div class="card-divider" aria-hidden="true"></div>

      <!-- Card Limits -->
      <div class="card-limits">
        <div class="limit-left">
          <div class="label small">AVAILABLE LIMIT</div>
          <div class="amt success">₹{{ formatNumber(activeCard.availableLimit) }}</div>
        </div>
        <div class="limit-right">
          <div class="label small">TOTAL LIMIT</div>
          <div class="amt">₹{{ formatNumber(activeCard.creditLimit) }}</div>
        </div>
        <div class="network-type" :class="activeCard.cardType?.networkType?.toLowerCase()">
          {{ activeCard.cardType?.networkType }}
        </div>
      </div>
    </div>

    <div v-else class="notice">No card data available.</div>
  </article>
</template>

<script setup>
import EyeOpenIcon from '../assets/eye-open.svg'
import EyeClosedIcon from '../assets/eye-closed.svg'
import { ref, computed, watch, onMounted } from 'vue'

/* ------------------ Props & Emits ------------------ */
const props = defineProps({
  card: { type: Object, required: false },
  showMenu: { type: Boolean, default: false },
  actions: { type: Array, default: () => [] }
})
const emit = defineEmits(['action', 'block'])

/* ------------------ State ------------------ */
const isMasked = ref(true)
const menuOpen = ref(false)
const menuRoot = ref(null)
const localCard = ref(null)

/* ------------------ Regex ------------------ */
const REGEX_REMOVE_SPACES = /\s+/g
const REGEX_GROUP_CARD_DIGITS = /(\d{4})(?=\d)/g

/* ------------------ Computed ------------------ */
const activeCard = computed(() => props.card || localCard.value)

const bankLabel = computed(() => {
  const card = activeCard.value
  if (!card) return 'BANK'
  return (card.bank || card.bankName || card.issuer || 'BANK').toUpperCase().slice(0, 18)
})

const bankLabelSub = computed(() => {
  const card = activeCard.value
  return card?.bank && card.bank.length > 6 ? 'Bank' : ''
})

const cardLast4 = computed(() => {
  const card = activeCard.value
  if (!card?.cardNumber) return '1234'
  return card.cardNumber.replace(REGEX_REMOVE_SPACES, '').slice(-4)
})

/* ------------------ Utility Functions ------------------ */
function getMaskedCardNumber(num) {
  if (!num) return '**** **** **** 1234'
  const digits = String(num).replace(REGEX_REMOVE_SPACES, '')
  return '**** **** **** ' + digits.slice(-4)
}

function getFullCardNumber(num) {
  if (!num) return '1111 2222 3333 4444'
  return String(num).replace(REGEX_REMOVE_SPACES, '').replace(REGEX_GROUP_CARD_DIGITS, '$1 ')
}

function formatNumber(value) {
  const num = Number(value || 0)
  return num.toLocaleString('en-IN')
}

function formatExpiry(dateStr) {
  if (!dateStr) return '12/26'
  const [year, month] = dateStr.split('-')
  return `${month}/${year.slice(-2)}`
}

/* ------------------ Event Handlers ------------------ */
function toggleMask() { isMasked.value = !isMasked.value }

function toggleBlock(card) {
  if (!card) return
  emit('block', { card, newStatus: card.cardStatus === 'BLOCKED' ? 'ACTIVE' : 'BLOCKED' })
}

function handleAction(action, idx) {
  menuOpen.value = false
  emit('action', { action, idx })
}

/* ------------------ Outside Click Watcher ------------------ */
function onDocumentClick(e) {
  if (!menuRoot.value) return
  if (!menuRoot.value.contains(e.target)) menuOpen.value = false
}
watch(menuOpen, (open) => {
  if (open) document.addEventListener('click', onDocumentClick)
  else document.removeEventListener('click', onDocumentClick)
})

/* ------------------ Data Loading ------------------ */
// onMounted(async () => {
//   const url = (typeof import.meta !== 'undefined' && import.meta.env?.BASE_URL)
//     ? new URL('/data/card.json', import.meta.env.BASE_URL).href
//     : '/data/card.json'

//   try {
//     const resp = await fetch(url, { cache: 'no-store' })
//     if (!resp.ok) return

//     const data = await resp.json()
//     if (!props.card) localCard.value = data
//   } catch (err) {}
// })

/* ------------------ Expose ------------------ */
defineExpose({
  toggleMask,
  toggleBlock,
  getMaskedCardNumber,
  getFullCardNumber,
  formatNumber,
  cardLast4,
  activeCard,
  formatExpiry
})
</script>

<style scoped src="./styles/card.css" />
