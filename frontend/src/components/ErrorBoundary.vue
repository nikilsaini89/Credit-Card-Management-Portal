<template>
  <div v-if="hasError" class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white rounded-lg p-6 max-w-md w-full mx-4">
      <div class="flex items-center mb-4">
        <div class="flex-shrink-0">
          <svg class="h-8 w-8 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z"></path>
          </svg>
        </div>
        <div class="ml-3">
          <h3 class="text-lg font-medium text-gray-900">Something went wrong</h3>
        </div>
      </div>
      
      <div class="mb-4">
        <p class="text-sm text-gray-600">{{ errorMessage }}</p>
      </div>
      
      <div class="flex justify-end space-x-3">
        <button
          @click="retry"
          class="px-4 py-2 text-sm font-medium text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:ring-offset-2 transition-all duration-200"
          style="background: #ffd60a; color: #0b2540;"
        >
          Try Again
        </button>
        <button
          @click="dismiss"
          class="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 border border-gray-300 rounded-lg hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-all duration-200"
        >
          Dismiss
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface Props {
  error: string | null
}

const props = defineProps<Props>()

const emit = defineEmits<{
  retry: []
  dismiss: []
}>()

const hasError = computed(() => !!props.error)
const errorMessage = computed(() => props.error || 'An unexpected error occurred')

const retry = () => {
  emit('retry')
}

const dismiss = () => {
  emit('dismiss')
}
</script>
