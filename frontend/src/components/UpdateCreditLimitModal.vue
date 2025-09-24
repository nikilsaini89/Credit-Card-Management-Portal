<template>
  <transition name="fade">
    <div v-if="visible" class="modal-backdrop" @click.self="onCancel">
      <div class="modal">
        <!-- header -->
        <div class="modal-header">
          <div>
            <h2 class="title">Update Credit Limit</h2>
          </div>
          <button class="close-btn" @click="onCancel">✕</button>
        </div>

        <!-- body -->
        <div class="modal-body">
          <div class="row">
            <div class="label">Current limit:</div>
            <div class="value">₹{{ formatNumber(currentLimit) }}</div>
          </div>

          <div class="row">
            <div class="label">Outstanding balance:</div>
            <div class="value">₹{{ formatNumber(outstanding) }}</div>
          </div>

          <div class="field">
            <label class="label">New Credit Limit</label>
            <input
              type="number"
              v-model.number="newLimit"
              :min="outstanding"
              class="input"
            />
            <div class="hint">Minimum: ₹{{ formatNumber(outstanding) }}</div>
          </div>

          <div class="note">
            <strong>Note:</strong> Changes &gt; 20% require admin approval and may take 1–2 business days.
          </div>
        </div>

        <!-- footer -->
        <div class="modal-footer">
          <button class="btn ghost" @click="onCancel">Cancel</button>
          <button class="btn primary" :disabled="!canSubmit" @click="onSubmit">
            Next: Verify Password
          </button>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'

const props = withDefaults(defineProps<{
  modelValue?: boolean
  currentLimit?: number
  outstanding?: number
}>(), {
  modelValue: false,
  currentLimit: 0,
  outstanding: 0
})

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
  (e: 'next', payload: { newLimit: number }): void
}>()

const visible = ref<boolean>(props.modelValue ?? false)
const newLimit = ref<number>(props.currentLimit ?? 0)

watch(() => props.modelValue, v => { visible.value = !!v })
watch(visible, v => emit('update:modelValue', v))

const canSubmit = computed(() => {
  return newLimit.value && newLimit.value >= (props.outstanding || 0)
})

function onCancel() {
  visible.value = false
}

function onSubmit() {
  if (!canSubmit.value) return
  emit('next', { newLimit: Number(newLimit.value) })
  visible.value = false
}

function formatNumber(n?: number) {
  return Number(n || 0).toLocaleString('en-IN')
}
</script>

<style scoped>
.modal-backdrop {
  position: fixed; inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex; align-items: center; justify-content: center;
  z-index: 2000;
  padding: 20px;
}
.modal {
  width: 500px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.15);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px;
  border-bottom: 1px solid #f1f1f1;
}
.title {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  color: #0b2540;
}
.close-btn {
  border: none;
  background: transparent;
  font-size: 18px;
  cursor: pointer;
  color: #6b7280;
}
.modal-body {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.row {
  display: flex;
  justify-content: space-between;
  font-size: 14px;
}
.label { color: #6b7280; }
.value { font-weight: 700; }
.field { display: flex; flex-direction: column; gap: 6px; }
.input {
  width: 100%;
  padding: 10px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 15px;
}
.hint {
  font-size: 12px;
  color: #6b7280;
}
.note {
  background: #f9fafb;
  border-radius: 8px;
  padding: 10px;
  font-size: 13px;
  color: #374151;
}
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 20px;
  border-top: 1px solid #f1f1f1;
}
.btn {
  padding: 10px 14px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
}
.btn.ghost {
  background: #fff;
  border: 1px solid #d1d5db;
  color: #111827;
}
.btn.primary {
  background: #0b2540;
  color: #fff;
  border: none;
}
.btn.primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.fade-enter-active, .fade-leave-active { transition: opacity .2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
