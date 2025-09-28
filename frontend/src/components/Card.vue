<template>
  <article class="card-visual-wrap">
    <div
      v-if="activeCard"
      class="credit-card"
      :class="{ blocked: activeCard.cardStatus === 'BLOCKED', 'cursor-pointer': showToggle }"
      @click="onCardClick"
    >
      <!-- Card Menu -->
      <div v-if="showMenu" class="card-menu" ref="menuRoot">
        <button
          class="menu-btn"
          @click="menuOpen = !menuOpen"
          :aria-expanded="String(menuOpen)"
          aria-label="Card actions"
          title="Card actions"
        >⋮</button>

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

      <!-- Toggle Box -->
      <div v-if="showToggle" class="absolute top-2 right-2 flex items-center gap-2">
        <span class="text-sm font-medium">Blocked</span>
        <input
          type="checkbox"
          class="toggle-checkbox"
          :checked="activeCard.cardStatus === 'BLOCKED'"
          @click.prevent="() => toggleBlock(activeCard)"
        />
      </div>

      <!-- Status -->
      <div class="status-badge">{{ activeCard.cardStatus }}</div>

      <!-- Eye Button -->
      <button
        class="eye"
        @click="onEyeButtonClick"
        :title="isNumberMasked ? 'Show number' : 'Hide number'"
        :aria-pressed="String(!isNumberMasked)"
      >
        <img :src="isNumberMasked ? EyeClosedIcon : EyeOpenIcon" alt="toggle number" />
      </button>

      <!-- Card Number -->
      <div class="number-row" role="group" aria-label="Card number">
        <span class="mask">
          {{ isNumberMasked
              ? getMaskedCardNumber(activeCard.cardNumber)
              : getFullCardNumber(activeCard.fullPan || activeCard.cardNumber) }}
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
      </div>

      <div class="card-divider" aria-hidden="true"></div>

      <div class="card-limits">
        <div v-if="activeCard?.cvv" class="cvv-row flex items-center gap-2">
          <div>
            <div class="label small">CVV</div>
            <div class="label small">{{ isCvvMasked ? '***' : activeCard.cvv }}</div>
          </div>
          <button class="cvv-eye" @click="toggleCvvMask">
            <img :src="isCvvMasked ? EyeClosedIcon : EyeOpenIcon" alt="toggle cvv" />
          </button>
        </div>

        <div class="text-right ml-auto">
          <div class="label small">Network</div>
          <div class="network-type" :class="activeCard.cardType?.networkType?.toLowerCase()">
            {{ activeCard.cardType?.networkType }}
          </div>
        </div>
      </div>
    </div>

    <div v-else class="notice">No card data available.</div>
  </article>

  <StatusChangeDialog
    v-if="showStatusDialog"
    v-model="showStatusDialog"
    :current-status="activeCard.cardStatus"
    :requested-status="requestedStatus"
    :card-last4="cardLast4"
    @confirm="confirmStatusChange"
    @cancel="cancelStatusChange"
  />
</template>

<script setup>
import EyeOpenIcon from '../assets/eye-open.svg'
import EyeClosedIcon from '../assets/eye-closed.svg'
import { ref, computed, watch } from 'vue'
import StatusChangeDialog from './StatusChangeDialog.vue'

/* Props & Emits */
const props = defineProps({
  card: { type: Object, required: false },
  showMenu: { type: Boolean, default: false },
  showToggle: { type: Boolean, default: false },
  actions: { type: Array, default: () => [] }
})

const emit = defineEmits(['action', 'block', 'request-pan'])

/* State */
const isNumberMasked = ref(true)
const isCvvMasked = ref(true)
let numberTimer = null
let cvvTimer = null
const menuOpen = ref(false)
const menuRoot = ref(null)
const localCard = ref(null)
const showStatusDialog = ref(false)
const pendingCard = ref(null)
const requestedStatus = ref('')

/* Computed */
const activeCard = computed(() => props.card || localCard.value)

const cardLast4 = computed(() => {
  const card = activeCard.value
  if (!card?.cardNumber) return '1234'
  return String(card.cardNumber).replace(/\s+/g, '').slice(-4)
})

/* Utilities */
function getMaskedCardNumber(num) {
  if (!num) return '**** **** **** 1234'
  const digits = String(num).replace(/\s+/g, '')
  return '**** **** **** ' + digits.slice(-4)
}
function getFullCardNumber(num) {
  if (!num) return '1111 2222 3333 4444'
  return String(num).replace(/\s+/g, '').replace(/(\d{4})(?=\d)/g, '$1 ')
}
function formatNumber(value) {
  return Number(value || 0).toLocaleString('en-IN')
}
function formatExpiry(dateStr) {
  if (!dateStr) return '12/26'
  const parts = String(dateStr).split('-')
  if (parts.length >= 2) {
    const [year, month] = [parts[0], parts[1]]
    return `${month}/${String(year).slice(-2)}`
  }
  return String(dateStr)
}

/* Mask toggles */
function toggleNumberMask(event) {
  event?.stopPropagation()
  isNumberMasked.value = !isNumberMasked.value
  if (numberTimer) clearTimeout(numberTimer)
  if (!isNumberMasked.value) {
    numberTimer = setTimeout(() => { isNumberMasked.value = true }, 2000)
  }
}
function toggleCvvMask(event) {
  event?.stopPropagation()
  isCvvMasked.value = !isCvvMasked.value
  if (cvvTimer) clearTimeout(cvvTimer)
  if (!isCvvMasked.value) {
    cvvTimer = setTimeout(() => { isCvvMasked.value = true }, 2000)
  }
}

function onEyeButtonClick(event) {
  event?.stopPropagation()
  if (!activeCard.value) return

  if (activeCard.value.fullPan) {
    isNumberMasked.value = !isNumberMasked.value
    return
  }

  const cardNum = activeCard.value.cardNumber || ''
  const looksMasked = typeof cardNum === 'string' && cardNum.includes('*')
  if (!looksMasked) {
    isNumberMasked.value = !isNumberMasked.value
    return
  }

  const id = activeCard.value.id ?? activeCard.value.cardId ?? activeCard.value._id
  if (id) emit('request-pan', id)
}

function toggleBlock(card) {
  if (!card) return
  pendingCard.value = card
  requestedStatus.value = card.cardStatus === 'BLOCKED' ? 'ACTIVE' : 'BLOCKED'
  showStatusDialog.value = true
}
function onCardClick(e) {
  if (!props.showToggle) return
  const target = e.target
  if (target.closest('.menu-btn, .menu-pop, .eye, .toggle-checkbox, button')) return
  const id = buildCardId()
  if (id) emit('action', { to: `/cards/${id}` })
  else emit('view-details', activeCard.value?.id)
}
function confirmStatusChange() {
  if (!pendingCard.value) return
  emit('block', { card: pendingCard.value, newStatus: requestedStatus.value })
  showStatusDialog.value = false
  pendingCard.value = null
}
function cancelStatusChange() {
  showStatusDialog.value = false
  pendingCard.value = null
}
function buildCardId() {
  const c = activeCard.value || props.card || localCard.value || {}
  return c.id ?? c._id ?? c.cardId ?? null
}
function handleAction(action, idx) {
  menuOpen.value = false
  if (action && action.to) {
    if (typeof action.to === 'object' && (!action.to.params || !action.to.params.id)) {
      const idFromCard = buildCardId()
      if (idFromCard) action.to = { ...(action.to || {}), params: { ...(action.to.params || {}), id: idFromCard } }
    }
    emit('action', action)
    return
  }
  if (typeof action === 'string') { emit('action', { to: action }); return }
  const cardId = action?.cardId ?? action?.value?.cardId ?? buildCardId()
  if (cardId) { emit('action', { to: `/cards/${cardId}` }); return }
  emit('action', action)
}

function onDocumentClick(e) {
  if (!menuRoot.value) return
  if (!menuRoot.value.contains(e.target)) menuOpen.value = false
}
watch(menuOpen, (open) => {
  if (open) document.addEventListener('click', onDocumentClick)
  else document.removeEventListener('click', onDocumentClick)
})

defineExpose({
  getMaskedCardNumber,
  getFullCardNumber,
  formatNumber,
  cardLast4,
  activeCard,
  formatExpiry
})
</script>

<style scoped src="./styles/card.css" />
