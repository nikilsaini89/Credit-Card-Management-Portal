<template>
  <div class="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100">
    <!-- Header -->
    <div class="bg-white shadow-sm border-b border-gray-200">
      <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <button @click="goBack" class="mr-4 p-2 text-gray-400 hover:text-gray-600 transition-colors duration-200">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
              </svg>
            </button>
            <div>
              <h1 class="text-2xl font-bold text-gray-900">Create New Transaction</h1>
              <p class="text-gray-600">Fill in the details to process your payment</p>
            </div>
          </div>
          <div class="flex items-center space-x-3">
            <button @click="goBack" class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors duration-200">
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div class="bg-white rounded-2xl shadow-lg border border-gray-100 overflow-hidden">
        <!-- Form -->
        <form @submit.prevent="submitTransaction" class="p-8 space-y-6">
          <!-- Card Selection -->
          <div>
            <label class="block text-sm font-semibold text-gray-700 mb-3">Select Card</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"></path>
                </svg>
              </div>
              <select 
                v-model="form.cardId" 
                class="block w-full pl-12 pr-4 py-4 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 bg-white text-lg"
                required
              >
                <option value="">Choose your card</option>
                <option v-for="card in cards" :key="card.id" :value="card.id">
                  {{ card.cardType }} ****{{ card.lastFour }} ({{ card.cardName }})
                </option>
              </select>
            </div>
          </div>

          <!-- Merchant Selection -->
          <div>
            <label class="block text-sm font-semibold text-gray-700 mb-3">Merchant</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path>
                </svg>
              </div>
              <select 
                v-model="form.merchantAccountId" 
                class="block w-full pl-12 pr-4 py-4 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 bg-white text-lg"
                required
              >
                <option value="">Select merchant</option>
                <option v-for="merchant in merchants" :key="merchant.id" :value="merchant.id">
                  {{ merchant.name }}
                </option>
              </select>
            </div>
          </div>

          <!-- Amount Input -->
          <div>
            <label class="block text-sm font-semibold text-gray-700 mb-3">Amount</label>
            <div class="relative">
              <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <span class="text-gray-500 text-xl font-semibold">₹</span>
              </div>
              <input
                v-model="form.amount"
                type="number"
                step="0.01"
                min="0.01"
                placeholder="0.00"
                class="block w-full pl-12 pr-4 py-4 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 bg-white text-lg"
                required
              />
            </div>
          </div>

          <!-- Category Selection -->
          <div>
            <label class="block text-sm font-semibold text-gray-700 mb-3">Category</label>
            <div class="relative">
              <select 
                v-model="form.category" 
                class="block w-full px-4 py-4 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 bg-white text-lg"
                required
              >
                <option value="">Select category</option>
                <option value="shopping">🛍️ Shopping</option>
                <option value="food">🍽️ Food & Dining</option>
                <option value="travel">✈️ Travel</option>
                <option value="entertainment">🎬 Entertainment</option>
                <option value="utilities">⚡ Utilities</option>
                <option value="healthcare">🏥 Healthcare</option>
                <option value="education">📚 Education</option>
                <option value="other">📦 Other</option>
              </select>
            </div>
          </div>

          <!-- BNPL Toggle -->
          <div class="bg-gradient-to-r from-purple-50 to-blue-50 rounded-2xl p-6 border border-purple-100">
            <div class="flex items-center justify-between mb-4">
              <div class="flex items-center">
                <div class="w-12 h-12 bg-purple-100 rounded-xl flex items-center justify-center mr-4">
                  <svg class="w-6 h-6 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
                  </svg>
                </div>
                <div>
                  <h4 class="text-lg font-semibold text-gray-900">Buy Now, Pay Later</h4>
                  <p class="text-sm text-gray-600">Split your payment into monthly installments</p>
                </div>
              </div>
              <label class="relative inline-flex items-center cursor-pointer">
                <input 
                  v-model="form.isBnpl" 
                  type="checkbox" 
                  class="sr-only peer"
                />
                <div class="w-14 h-7 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-purple-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-6 after:w-6 after:transition-all peer-checked:bg-purple-600"></div>
              </label>
            </div>

            <!-- BNPL Options -->
            <div v-if="form.isBnpl" class="space-y-4 animate-fadeIn">
              <div>
                <label class="block text-sm font-semibold text-gray-700 mb-2">Tenure</label>
                <select 
                  v-model="form.tenureMonths" 
                  class="block w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500 transition-all duration-200 bg-white"
                  required
                >
                  <option value="">Select tenure</option>
                  <option value="3">3 months</option>
                  <option value="6">6 months</option>
                  <option value="9">9 months</option>
                  <option value="12">12 months</option>
                </select>
              </div>

              <div v-if="form.tenureMonths && form.amount" class="bg-white rounded-xl p-4 border border-purple-200">
                <div class="flex justify-between items-center">
                  <span class="text-lg font-semibold text-gray-700">Monthly EMI:</span>
                  <span class="text-2xl font-bold text-purple-600">₹{{ formatNumber(monthlyEmi) }}</span>
                </div>
                <div class="mt-2 text-sm text-gray-600">
                  Total amount: ₹{{ formatNumber(parseFloat(form.amount) || 0) }} over {{ form.tenureMonths }} months
                </div>
              </div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="flex space-x-4 pt-6">
            <button
              type="button"
              @click="goBack"
              class="flex-1 px-6 py-4 text-lg font-medium text-gray-700 bg-white border-2 border-gray-300 rounded-xl hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-all duration-200"
            >
              Cancel
            </button>
            <button
              type="submit"
              :disabled="loading"
              class="flex-1 px-6 py-4 text-lg font-medium text-white bg-blue-600 border-2 border-transparent rounded-xl hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
            >
              <span v-if="!loading">Create Transaction</span>
              <span v-else class="flex items-center justify-center">
                <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                Processing...
              </span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'

interface Card {
  id: number
  cardType: string
  lastFour: string
  cardName: string
}

interface Merchant {
  id: number
  name: string
}

const router = useRouter()
const store = useStore()

// Form data
const form = ref({
  cardId: '',
  merchantAccountId: '',
  amount: '',
  category: '',
  isBnpl: false,
  tenureMonths: ''
})

const loading = ref(false)

// Computed properties
const cards = computed(() => store.state?.cards?.cards || [])
const merchants = computed(() => store.state?.merchants?.merchants || [])

const monthlyEmi = computed(() => {
  if (!form.value.amount || !form.value.tenureMonths) return 0
  const amount = parseFloat(form.value.amount)
  const tenure = parseInt(form.value.tenureMonths)
  if (amount <= 0 || tenure <= 0) return 0
  return amount / tenure
})

// Methods
const goBack = () => {
  router.push('/transactions')
}

const formatNumber = (value: number) => {
  return new Intl.NumberFormat('en-IN', { 
    minimumFractionDigits: 0, 
    maximumFractionDigits: 2 
  }).format(value)
}

const submitTransaction = async () => {
  if (loading.value) return
  
  loading.value = true
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // Reset form
    form.value = {
      cardId: '',
      merchantAccountId: '',
      amount: '',
      category: '',
      isBnpl: false,
      tenureMonths: ''
    }
    
    // Navigate back to transactions page
    router.push('/transactions')
  } catch (error) {
    console.error('Error creating transaction:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  // Fetch cards and merchants if not already loaded
  store.dispatch('cards/fetchCards')
  store.dispatch('merchants/fetchMerchants')
})
</script>

<style scoped>
.animate-fadeIn {
  animation: fadeIn 0.3s ease-in-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
