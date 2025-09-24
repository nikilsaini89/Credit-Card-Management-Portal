<template>
  <transition name="fade">
    <div v-if="modelValue" class="confirm-backdrop" @click.self="onCancel" role="dialog" aria-modal="true">
      <div class="confirm-modal" @keydown.esc="onCancel" ref="root" tabindex="-1">
        <!-- header -->
        <header class="confirm-header">
          <h2 class="confirm-title">Confirm Identity</h2>
          <button class="close" @click="onCancel" aria-label="Close">✕</button>
        </header>

        <!-- body -->
        <div class="confirm-body">
          <p class="desc">
            Please enter your password to confirm the credit limit change to
            <strong>₹{{ formatNumber(amount) }}</strong>.
          </p>

          <label class="lbl">Password</label>
          <input
            ref="pwInput"
            v-model="password"
            type="password"
            class="pw-input"
            placeholder="Enter your password"
            @keyup.enter="submit"
            :disabled="loading"
            autocomplete="current-password"
          />

          <div v-if="error" class="error">{{ error }}</div>
        </div>

        <!-- footer -->
        <footer class="confirm-footer">
          <button class="btn ghost" @click="onCancel" :disabled="loading">Cancel</button>
          <button class="btn primary" @click="submit" :disabled="loading || !password">
            <span v-if="loading">Verifying…</span>
            <span v-else>Confirm Change</span>
          </button>
        </footer>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'

/**
 * Props / Emits
 * - v-model -> modelValue (visible state)
 * - amount -> number to show in text
 * - emits 'verified' with { password } when user confirms
 */
const props = withDefaults(defineProps<{
  modelValue?: boolean
  amount?: number
}>(), {
  modelValue: false,
  amount: 0
})

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'verified', payload: { password: string }): void
}>()

const visible = ref<boolean>(props.modelValue ?? false)
const amount = props.amount ?? 0

const password = ref<string>('')
const loading = ref(false)
const error = ref<string | null>(null)
const pwInput = ref<HTMLInputElement | null>(null)
const root = ref<HTMLElement | null>(null)

// Keep local visible in sync with parent v-model
watch(() => props.modelValue, async (v) => {
  visible.value = !!v
  if (v) {
    // focus input after render
    await nextTick()
    setTimeout(() => pwInput.value?.focus(), 40)
  } else {
    password.value = ''
    error.value = null
    loading.value = false
  }
})

// When local visible changes (e.g. user clicks backdrop), notify parent
watch(visible, (v) => emit('update:modelValue', v))

function onCancel() {
  visible.value = false
}

/**
 * formatNumber - nicely format INR-style numbers
 */
function formatNumber(n?: number) {
  return Number(n || 0).toLocaleString('en-IN')
}

/**
 * submit - validate and emit verified event for parent to handle verification + update
 * Parent should call backend to verify password and update limit; parent will close modal by setting v-model=false
 */
async function submit() {
  error.value = null
  if (!password.value) {
    error.value = 'Please enter your password'
    return
  }

  loading.value = true
  try {
    // Emit to parent for verification
    emit('verified', { password: password.value })
    // do not auto-close here — parent will close the modal on success by setting modelValue=false
    // But we'll clear local password and loading after a short delay to keep UI responsive if parent closes
    setTimeout(() => {
      loading.value = false
      // keep password cleared for security
      password.value = ''
    }, 300)
  } catch (err) {
    // Should not normally reach here because emit won't throw; parent handles server errors and can reopen/keep modal
    error.value = 'Verification failed. Try again.'
    loading.value = false
  }
}
</script>

<style scoped>
.confirm-backdrop {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex; align-items: center; justify-content: center;
  z-index: 3000; padding: 20px;
}
.confirm-modal {
  width: 560px; max-width: 96%;
  background: #fff; border-radius: 10px;
  box-shadow: 0 20px 50px rgba(2,6,23,0.45);
  display:flex; flex-direction:column; overflow:hidden;
  outline: none;
}
.confirm-header {
  display:flex; justify-content:space-between; align-items:center;
  padding:18px 20px; border-bottom:1px solid #f1f1f1;
}
.confirm-title { margin:0; font-size:22px; font-weight:800; color:#0b2540; }
.close {
  background:transparent; border:0; font-size:18px; cursor:pointer; color:#6b7280;
  padding:8px; border-radius:8px;
}
.confirm-body {
  padding:20px; display:flex; flex-direction:column; gap:12px;
}
.desc { color:#555; margin:0 0 6px; font-size:15px; line-height:1.4; }
.lbl { font-weight:700; color:#111827; margin-bottom:6px; }

/* big rounded input similar to screenshot */
.pw-input {
  width:100%;
  padding:14px 16px;
  border-radius:12px;
  border:2px solid #0b2540;
  outline:none;
  font-size:15px;
  box-sizing:border-box;
}
.pw-input::placeholder { color:#94a3b8; font-weight:600; }
.pw-input:disabled { background:#f8fafc; cursor:not-allowed; }

.error { color:#ef4444; font-size:13px; margin-top:6px; }

/* footer */
.confirm-footer {
  display:flex; gap:12px; justify-content:flex-end; padding:16px 20px;
  border-top:1px solid #f1f1f1;
}
.btn { padding:10px 14px; border-radius:8px; font-weight:700; cursor:pointer; min-width:120px; }
.btn.ghost { background:#fff; border:1px solid #e6e9ee; color:#111827; }
.btn.primary { background:#6b7280; color:#fff; border:none; }
.btn.primary[disabled] { opacity:0.6; cursor:not-allowed; }

/* small responsive tweak */
@media (max-width:520px) {
  .confirm-modal { width: 92%; }
  .btn { min-width: 96px; padding:10px; }
}

/* transitions */
.fade-enter-active, .fade-leave-active { transition: opacity .18s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
