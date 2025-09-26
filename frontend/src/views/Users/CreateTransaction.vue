<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Header Section -->
    <div class="bg-white border-b border-gray-100 shadow-sm">
      <div class="w-full py-4 sm:py-6">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between space-y-3 sm:space-y-0">
          <div class="flex items-center">
            <button @click="goBack" class="mr-4 p-2 text-gray-400 hover:text-gray-600 transition-colors duration-200">
              <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
              </svg>
            </button>
            <div>
              <h1 class="text-xl sm:text-2xl font-bold text-gray-900">Create New Transaction</h1>
              <p class="text-xs sm:text-sm text-gray-600 mt-1">Fill in the details to process your payment</p>
            </div>
          </div>
          <div class="flex items-center space-x-3">
            <button @click="goBack" class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:ring-offset-2 transition-all duration-200 shadow-sm">
              Cancel
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="space-y-4 sm:space-y-6">
      <!-- Main Content -->
      <div>
        <!-- Transaction Form Card -->
        <div class="bg-white rounded-xl shadow-lg border border-gray-100 p-4 sm:p-6">
            <!-- Form Header -->
            <div class="flex items-center mb-6">
              <div class="h-8 w-8 rounded-lg flex items-center justify-center mr-3" style="background: #ffd60a;">
                <svg class="h-5 w-5" style="color: #0b2540;" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
                </svg>
              </div>
              <h3 class="text-lg font-bold text-gray-900">Transaction Details</h3>
            </div>
            
            <!-- Form -->
            <form @submit.prevent="submitTransaction" class="space-y-6">
              <!-- Card Selection -->
              <div class="w-[55%]">
                <label class="block text-sm font-semibold text-gray-700 mb-3">Select Card</label>
                <div class="relative">
                  <button
                    @click="toggleCardDropdown"
                    class="w-full px-4 py-3 text-sm border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:border-yellow-400 transition-all duration-200 bg-white hover:bg-gray-50 text-left flex items-center justify-between"
                  >
                    <div class="flex items-center">
                      <svg class="h-5 w-5 text-gray-400 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"></path>
                      </svg>
                      <span class="text-gray-500">{{ getCardDisplayText() }}</span>
                    </div>
                    <svg class="w-4 h-4 text-gray-400 transition-transform duration-200" :class="{ 'rotate-180': showCardDropdown }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
                    </svg>
                  </button>
                  
                  <!-- Custom Card Dropdown Overlay -->
                  <div v-if="showCardDropdown" class="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-200 rounded-xl shadow-lg z-50 overflow-hidden">
                    <div class="p-2 space-y-1">
                      <button
                        @click="selectCard('')"
                        class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200"
                      >
                        Choose your card
                      </button>
                      <button
                        v-for="card in cards"
                        :key="card.id"
                        @click="selectCard(card.id)"
                        class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200 flex items-center"
                      >
                        <span class="inline-block w-3 h-3 rounded-full bg-blue-500 mr-3"></span>
                        <span class="text-gray-700 font-medium">{{ card.cardType }} ****{{ card.lastFour }}</span>
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Merchant Selection -->
              <div class="w-[55%]">
                <label class="block text-sm font-semibold text-gray-700 mb-3">Merchant</label>
                <div class="relative">
                  <button
                    @click="toggleMerchantDropdown"
                    class="w-full px-4 py-3 text-sm border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:border-yellow-400 transition-all duration-200 bg-white hover:bg-gray-50 text-left flex items-center justify-between"
                  >
                    <div class="flex items-center">
                      <svg class="h-5 w-5 text-gray-400 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16m14 0h2m-2 0h-5m-9 0H3m2 0h5M9 7h1m-1 4h1m4-4h1m-1 4h1m-5 10v-5a1 1 0 011-1h2a1 1 0 011 1v5m-4 0h4"></path>
                      </svg>
                      <span class="text-gray-500">{{ getMerchantDisplayText() }}</span>
                    </div>
                    <svg class="w-4 h-4 text-gray-400 transition-transform duration-200" :class="{ 'rotate-180': showMerchantDropdown }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
                    </svg>
                  </button>
                  
                  <!-- Custom Merchant Dropdown Overlay -->
                  <div v-if="showMerchantDropdown" class="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-200 rounded-xl shadow-lg z-50 overflow-hidden">
                    <div class="p-2 space-y-1">
                      <button
                        @click="selectMerchant('')"
                        class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200"
                      >
                        Select merchant
                      </button>
                      <button
                        v-for="merchant in merchants"
                        :key="merchant.id"
                        @click="selectMerchant(merchant.id)"
                        class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200 flex items-center"
                      >
                        <span class="inline-block w-3 h-3 rounded-full bg-green-500 mr-3"></span>
                        <span class="text-gray-700 font-medium">{{ merchant.name }}</span>
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Amount Input -->
              <div class="w-[52%]">
                <label class="block text-sm font-semibold text-gray-700 mb-3">Amount</label>
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                    <span class="text-gray-500 text-lg font-semibold">₹</span>
                  </div>
                  <input
                    v-model="form.amount"
                    type="number"
                    step="0.01"
                    min="0.01"
                    placeholder="0.00"
                    class="block w-full pl-12 pr-4 py-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:border-yellow-400 transition-all duration-200 bg-white"
                    required
                  />
                </div>
              </div>

              <!-- Category Selection -->
              <div class="w-[55%]">
                <label class="block text-sm font-semibold text-gray-700 mb-3">Category</label>
                <div class="relative">
                  <button
                    @click="toggleCategoryDropdown"
                    class="w-full px-4 py-3 text-sm border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:border-yellow-400 transition-all duration-200 bg-white hover:bg-gray-50 text-left flex items-center justify-between"
                  >
                    <span class="text-gray-500">{{ getCategoryDisplayText() }}</span>
                    <svg class="w-4 h-4 text-gray-400 transition-transform duration-200" :class="{ 'rotate-180': showCategoryDropdown }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
                    </svg>
                  </button>
                  
                  <!-- Custom Category Dropdown Overlay -->
                  <div v-if="showCategoryDropdown" class="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-200 rounded-xl shadow-lg z-50 overflow-hidden">
                    <div class="p-2 space-y-1">
                      <button
                        @click="selectCategory('')"
                        class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200"
                      >
                        Select category
                      </button>
                      <button
                        @click="selectCategory('shopping')"
                        class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200 flex items-center"
                      >
                        <span class="text-lg mr-3">🛒</span>
                        <span class="text-gray-700 font-medium">Shopping</span>
                      </button>
                      <button
                        @click="selectCategory('food')"
                        class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200 flex items-center"
                      >
                        <span class="text-lg mr-3">🍔</span>
                        <span class="text-gray-700 font-medium">Food & Dining</span>
                      </button>
                      <button
                        @click="selectCategory('travel')"
                        class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200 flex items-center"
                      >
                        <span class="text-lg mr-3">✈️</span>
                        <span class="text-gray-700 font-medium">Travel</span>
                      </button>
                      <button
                        @click="selectCategory('entertainment')"
                        class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200 flex items-center"
                      >
                        <span class="text-lg mr-3">🎬</span>
                        <span class="text-gray-700 font-medium">Entertainment</span>
                      </button>
                      <button
                        @click="selectCategory('utilities')"
                        class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200 flex items-center"
                      >
                        <span class="text-lg mr-3">⚡</span>
                        <span class="text-gray-700 font-medium">Utilities</span>
                      </button>
                      <button
                        @click="selectCategory('healthcare')"
                        class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200 flex items-center"
                      >
                        <span class="text-lg mr-3">🏥</span>
                        <span class="text-gray-700 font-medium">Healthcare</span>
                      </button>
                      <button
                        @click="selectCategory('education')"
                        class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200 flex items-center"
                      >
                        <span class="text-lg mr-3">📚</span>
                        <span class="text-gray-700 font-medium">Education</span>
                      </button>
                      <button
                        @click="selectCategory('other')"
                        class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200 flex items-center"
                      >
                        <span class="text-lg mr-3">📦</span>
                        <span class="text-gray-700 font-medium">Other</span>
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- BNPL Toggle -->
              <div class="bg-gradient-to-r from-yellow-50 to-yellow-100 rounded-xl p-6 border border-yellow-200">
                <div class="flex items-center justify-between mb-4">
                  <div class="flex items-center">
                    <div class="w-12 h-12 rounded-xl flex items-center justify-center mr-4" style="background: #ffd60a;">
                      <svg class="w-6 h-6" style="color: #0b2540;" fill="none" stroke="currentColor" viewBox="0 0 24 24">
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
                    <div class="w-14 h-7 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-yellow-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-6 after:w-6 after:transition-all peer-checked:bg-yellow-500"></div>
                  </label>
                </div>

                <!-- BNPL Options -->
                <div v-if="form.isBnpl" class="space-y-4 animate-fadeIn">
                  <div class="w-[55%]">
                    <label class="block text-sm font-semibold text-gray-700 mb-2">Tenure</label>
                    <div class="relative">
                      <button
                        @click="toggleTenureDropdown"
                        class="w-full px-4 py-3 text-sm border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:border-yellow-400 transition-all duration-200 bg-white hover:bg-gray-50 text-left flex items-center justify-between"
                      >
                        <span class="text-gray-500">{{ getTenureDisplayText() }}</span>
                        <svg class="w-4 h-4 text-gray-400 transition-transform duration-200" :class="{ 'rotate-180': showTenureDropdown }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
                        </svg>
                      </button>
                      
                      <!-- Custom Tenure Dropdown Overlay -->
                      <div v-if="showTenureDropdown" class="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-200 rounded-xl shadow-lg z-50 overflow-hidden">
                        <div class="p-2 space-y-1">
                          <button
                            @click="selectTenure('')"
                            class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200"
                          >
                            Select tenure
                          </button>
                          <button
                            @click="selectTenure('3')"
                            class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200 flex items-center"
                          >
                            <span class="inline-block w-3 h-3 rounded-full bg-purple-500 mr-3"></span>
                            <span class="text-gray-700 font-medium">3 months</span>
                          </button>
                          <button
                            @click="selectTenure('6')"
                            class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200 flex items-center"
                          >
                            <span class="inline-block w-3 h-3 rounded-full bg-purple-500 mr-3"></span>
                            <span class="text-gray-700 font-medium">6 months</span>
                          </button>
                          <button
                            @click="selectTenure('9')"
                            class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200 flex items-center"
                          >
                            <span class="inline-block w-3 h-3 rounded-full bg-purple-500 mr-3"></span>
                            <span class="text-gray-700 font-medium">9 months</span>
                          </button>
                          <button
                            @click="selectTenure('12')"
                            class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200 flex items-center"
                          >
                            <span class="inline-block w-3 h-3 rounded-full bg-purple-500 mr-3"></span>
                            <span class="text-gray-700 font-medium">12 months</span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div v-if="form.tenureMonths && form.amount" class="bg-white rounded-xl p-4 border border-yellow-200">
                    <div class="flex justify-between items-center">
                      <span class="text-lg font-semibold text-gray-700">Monthly EMI:</span>
                      <span class="text-2xl font-bold" style="color: #0b2540;">₹{{ formatNumber(monthlyEmi) }}</span>
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
                  class="flex-1 px-6 py-3 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:ring-offset-2 transition-all duration-200 shadow-sm"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  :disabled="loading"
                  class="flex-1 px-6 py-3 text-sm font-medium text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 shadow-lg hover:shadow-xl"
                  style="background: #ffd60a; color: #0b2540;"
                >
                  <span v-if="!loading">Create Transaction</span>
                  <span v-else class="flex items-center justify-center">
                    <svg class="animate-spin -ml-1 mr-3 h-5 w-5" style="color: #0b2540;" fill="none" viewBox="0 0 24 24">
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

// Dropdown state
const showCardDropdown = ref(false)
const showMerchantDropdown = ref(false)
const showCategoryDropdown = ref(false)
const showTenureDropdown = ref(false)

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

// Dropdown methods
const toggleCardDropdown = () => {
  showCardDropdown.value = !showCardDropdown.value
  showMerchantDropdown.value = false
  showCategoryDropdown.value = false
  showTenureDropdown.value = false
}

const toggleMerchantDropdown = () => {
  showMerchantDropdown.value = !showMerchantDropdown.value
  showCardDropdown.value = false
  showCategoryDropdown.value = false
  showTenureDropdown.value = false
}

const toggleCategoryDropdown = () => {
  showCategoryDropdown.value = !showCategoryDropdown.value
  showCardDropdown.value = false
  showMerchantDropdown.value = false
  showTenureDropdown.value = false
}

const toggleTenureDropdown = () => {
  showTenureDropdown.value = !showTenureDropdown.value
  showCardDropdown.value = false
  showMerchantDropdown.value = false
  showCategoryDropdown.value = false
}

const selectCard = (cardId: string) => {
  form.value.cardId = cardId
  showCardDropdown.value = false
}

const selectMerchant = (merchantId: string) => {
  form.value.merchantAccountId = merchantId
  showMerchantDropdown.value = false
}

const selectCategory = (category: string) => {
  form.value.category = category
  showCategoryDropdown.value = false
}

const selectTenure = (tenure: string) => {
  form.value.tenureMonths = tenure
  showTenureDropdown.value = false
}

const getCardDisplayText = () => {
  if (!form.value.cardId) return 'Choose your card'
  const selectedCard = cards.value.find((card: any) => card.id === form.value.cardId)
  return selectedCard ? `${selectedCard.cardType} ****${selectedCard.lastFour}` : 'Choose your card'
}

const getMerchantDisplayText = () => {
  if (!form.value.merchantAccountId) return 'Select merchant'
  const selectedMerchant = merchants.value.find((merchant: any) => merchant.id === form.value.merchantAccountId)
  return selectedMerchant ? selectedMerchant.name : 'Select merchant'
}

const getCategoryDisplayText = () => {
  if (!form.value.category) return 'Select category'
  const categoryMap: Record<string, string> = {
    shopping: '🛒 Shopping',
    food: '🍔 Food & Dining',
    travel: '✈️ Travel',
    entertainment: '🎬 Entertainment',
    utilities: '⚡ Utilities',
    healthcare: '🏥 Healthcare',
    education: '📚 Education',
    other: '📦 Other'
  }
  return categoryMap[form.value.category] || 'Select category'
}

const getTenureDisplayText = () => {
  if (!form.value.tenureMonths) return 'Select tenure'
  return `${form.value.tenureMonths} months`
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
