<template>
  <transition name="fade">
    <div v-if="localVisible" class="backdrop-confirm" @click.self="cancel" role="dialog" aria-modal="true">
      <div class="modal-confirm" @keydown.esc="cancel" ref="modalRootRef" tabindex="-1">
        <header class="modal-header">
          <h2 class="modal-title">Confirm Identity</h2>
          <button class="close-button" @click="cancel" aria-label="Close">✕</button>
        </header>

        <div class="modal-body">
          <p class="description-text">
            Please enter your password to confirm the credit limit change to
            <strong>₹{{ formatINR(displayAmount) }}</strong>.
          </p>

          <label class="label-text">Password</label>
          <input
            ref="passwordInputRef"
            v-model="passwordInput"
            type="password"
            class="password-input"
            placeholder="Enter your password"
            @keyup.enter="submitForm"
            :disabled="isLoading"
            autocomplete="current-password"
          />

          <div v-if="errorMessage" class="error-message">{{ errorMessage }}</div>
        </div>

        <footer class="modal-footer">
          <button class="button button-ghost" @click="cancel" :disabled="isLoading">Cancel</button>
          <button class="button button-primary" @click="submitForm" :disabled="isLoading || !passwordInput">
            <span v-if="isLoading">Verifying…</span>
            <span v-else>Confirm Change</span>
          </button>
        </footer>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'

const props = withDefaults(defineProps<{
  modelValue?: boolean
  amount?: number
}>(), {
  modelValue: false,
  amount: 0
})

const emit = defineEmits<{
  (event: 'update:modelValue', isVisible: boolean): void
  (event: 'verified', payload: { password: string }): void
}>()

const localVisible = ref<boolean>(props.modelValue ?? false)
const displayAmount = props.amount ?? 0

const passwordInput = ref<string>('')
const isLoading = ref(false)
const errorMessage = ref<string | null>(null)
const passwordInputRef = ref<HTMLInputElement | null>(null)
const modalRootRef = ref<HTMLElement | null>(null)

watch(() => props.modelValue, async (newModelValue) => {
  localVisible.value = !!newModelValue
  if (newModelValue) {
    await nextTick()
    setTimeout(() => passwordInputRef.value?.focus(), 40)
  } else {
    passwordInput.value = ''
    errorMessage.value = null
    isLoading.value = false
  }
})

watch(localVisible, (newLocalVisible) => emit('update:modelValue', newLocalVisible))

function cancel() {
  localVisible.value = false
}

function formatINR(amount?: number) {
  return Number(amount || 0).toLocaleString('en-IN')
}

async function submitForm() {
  errorMessage.value = null
  if (!passwordInput.value) {
    errorMessage.value = 'Please enter your password'
    return
  }

  isLoading.value = true
  try {
    emit('verified', { password: passwordInput.value })
    setTimeout(() => {
      isLoading.value = false
      passwordInput.value = ''
    }, 300)
  } catch (caughtError) {
    errorMessage.value = 'Verification failed. Try again.'
    isLoading.value = false
  }
}
</script>


<style scoped>
.backdrop-confirm {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 3000;
  padding: 20px;
}
.modal-confirm {
  width: 560px;
  max-width: 96%;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 20px 50px rgba(2,6,23,0.45);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  outline: none;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px;
  border-bottom: 1px solid #f1f1f1;
}
.modal-title {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  color: #0b2540;
}
.close-button {
  background: transparent;
  border: 0;
  font-size: 18px;
  cursor: pointer;
  color: #6b7280;
  padding: 8px;
  border-radius: 8px;
}
.modal-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.description-text {
  color: #555;
  margin: 0 0 6px;
  font-size: 15px;
  line-height: 1.4;
}
.label-text {
  font-weight: 700;
  color: #111827;
  margin-bottom: 6px;
}
.password-input {
  width: 100%;
  padding: 14px 16px;
  border-radius: 12px;
  border: 2px solid #0b2540;
  outline: none;
  font-size: 15px;
  box-sizing: border-box;
}
.password-input::placeholder {
  color: #94a3b8;
  font-weight: 600;
}
.password-input:disabled {
  background: #f8fafc;
  cursor: not-allowed;
}
.error-message {
  color: #ef4444;
  font-size: 13px;
  margin-top: 6px;
}
.modal-footer {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid #f1f1f1;
}
.button {
  padding: 10px 14px;
  border-radius: 8px;
  font-weight: 700;
  cursor: pointer;
  min-width: 120px;
}
.button-ghost {
  background: #fff;
  border: 1px solid #e6e9ee;
  color: #111827;
}
.button-primary {
  background: #6b7280;
  color: #fff;
  border: none;
}
.button-primary[disabled] {
  opacity: 0.6;
  cursor: not-allowed;
}
@media (max-width: 520px) {
  .modal-confirm {
    width: 92%;
  }
  .button {
    min-width: 96px;
    padding: 10px;
  }
}
.fade-enter-active, .fade-leave-active {
  transition: opacity .18s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
