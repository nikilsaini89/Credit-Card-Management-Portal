<template>
  <div class="min-h-screen bg-gray-50 overflow-x-hidden">
    <!-- Header Section -->
    <div class="bg-white border-b border-gray-100">
      <div class="w-full max-w-7xl mx-auto px-2 sm:px-4 lg:px-8 py-4 sm:py-6">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between space-y-3 sm:space-y-0">
          <div>
            <h1 class="text-xl sm:text-2xl font-semibold text-gray-900">Transactions</h1>
            <p class="text-xs sm:text-sm text-gray-500 mt-1">Manage your payment history</p>
          </div>
          <div class="flex flex-col sm:flex-row items-stretch sm:items-center space-y-2 sm:space-y-0 sm:space-x-3">
            <button @click="exportTransactions" class="inline-flex items-center justify-center px-3 py-2 text-xs sm:text-sm font-medium text-gray-700 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors duration-200">
              <svg class="w-3 h-3 sm:w-4 sm:h-4 mr-1 sm:mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
              </svg>
              Export
            </button>
            <button @click="goToCreateTransaction" class="inline-flex items-center justify-center px-3 py-2 text-xs sm:text-sm font-medium text-white bg-blue-600 border border-transparent rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition-colors duration-200">
              <svg class="w-3 h-3 sm:w-4 sm:h-4 mr-1 sm:mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
              </svg>
              New Transaction
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="w-full max-w-7xl mx-auto px-2 sm:px-4 lg:px-8 py-4 sm:py-6">
      <div class="space-y-4 sm:space-y-6 overflow-x-hidden">
        <!-- Main Content -->
        <div>
          <!-- Quick Stats Row -->
          <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 sm:gap-4 mb-4 sm:mb-6">
            <div class="bg-gradient-to-r from-blue-50 to-blue-100 rounded-xl p-3 sm:p-4 border border-blue-200">
              <div class="flex items-center justify-between">
                <div class="flex-1 min-w-0">
                  <p class="text-xs sm:text-sm font-semibold text-blue-800">Quick Stats</p>
                  <p class="text-xs text-blue-600">Overview</p>
                </div>
                <div class="h-6 w-6 sm:h-8 sm:w-8 rounded-lg bg-blue-200 flex items-center justify-center flex-shrink-0 ml-2">
                  <svg class="h-3 w-3 sm:h-4 sm:w-4 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
                  </svg>
                </div>
              </div>
            </div>
            <div class="bg-gradient-to-r from-green-50 to-green-100 rounded-xl p-3 sm:p-4 border border-green-200">
              <div class="flex items-start justify-between">
                <div class="flex-1 min-w-0">
                  <p class="text-xs sm:text-sm font-semibold text-green-800">This Month</p>
                  <p :class="getAmountSize(analytics.thisMonth)" class="text-green-600 font-bold text-sm sm:text-base">{{ formatCurrency(analytics.thisMonth) }}</p>
                  <p class="text-xs text-green-500 mt-1">{{ formatNumber(analytics.thisMonth) }}</p>
                </div>
                <div class="h-6 w-6 sm:h-8 sm:w-8 rounded-lg bg-green-200 flex items-center justify-center flex-shrink-0 ml-2">
                  <svg class="h-3 w-3 sm:h-4 sm:w-4 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path>
                  </svg>
                </div>
              </div>
            </div>
            <div class="bg-gradient-to-r from-purple-50 to-purple-100 rounded-xl p-3 sm:p-4 border border-purple-200">
              <div class="flex items-center justify-between">
                <div class="flex-1 min-w-0">
                  <p class="text-xs sm:text-sm font-semibold text-purple-800">BNPL Plans</p>
                  <p class="text-xs text-purple-600">{{ bnplPlans.length }} active</p>
                </div>
                <div class="h-6 w-6 sm:h-8 sm:w-8 rounded-lg bg-purple-200 flex items-center justify-center flex-shrink-0 ml-2">
                  <svg class="h-3 w-3 sm:h-4 sm:w-4 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
                  </svg>
                </div>
              </div>
            </div>
            <div class="bg-gradient-to-r from-orange-50 to-orange-100 rounded-xl p-3 sm:p-4 border border-orange-200">
              <div class="flex items-center justify-between">
                <div class="flex-1 min-w-0">
                  <p class="text-xs sm:text-sm font-semibold text-orange-800">Change</p>
                  <p class="text-xs text-orange-600">{{ analytics.change >= 0 ? '+' : '' }}{{ analytics.change }}%</p>
                </div>
                <div class="h-6 w-6 sm:h-8 sm:w-8 rounded-lg bg-orange-200 flex items-center justify-center flex-shrink-0 ml-2">
                  <svg class="h-3 w-3 sm:h-4 sm:w-4 text-orange-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 11l5-5m0 0l5 5m-5-5v12"></path>
                  </svg>
                </div>
              </div>
            </div>
          </div>

          <!-- Summary Cards -->
          <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-3 sm:gap-4 mb-4 sm:mb-6">
            <div class="bg-gradient-to-br from-blue-50 to-blue-100 rounded-xl border border-blue-200 p-3 sm:p-4 hover:shadow-md transition-shadow duration-200">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-xs font-medium text-blue-700 uppercase tracking-wide">Total</p>
                  <p class="text-xl sm:text-2xl font-bold text-blue-900 mt-1">{{ summary.totalTransactions }}</p>
                </div>
                <div class="w-8 h-8 sm:w-10 sm:h-10 bg-blue-200 rounded-lg flex items-center justify-center">
                  <svg class="w-4 h-4 sm:w-5 sm:h-5 text-blue-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
                  </svg>
                </div>
              </div>
            </div>

            <div class="bg-gradient-to-br from-red-50 to-red-100 rounded-xl border border-red-200 p-3 sm:p-4 hover:shadow-md transition-shadow duration-200 min-h-[80px] sm:min-h-[100px]">
              <div class="flex flex-col h-full">
                <div class="flex items-center justify-between mb-2">
                  <p class="text-xs font-medium text-red-700 uppercase tracking-wide">Spent</p>
                  <div class="w-6 h-6 sm:w-8 sm:h-8 bg-red-200 rounded-lg flex items-center justify-center flex-shrink-0">
                    <svg class="w-3 h-3 sm:w-4 sm:h-4 text-red-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"></path>
                    </svg>
                  </div>
                </div>
                <div class="flex-1 flex flex-col justify-center">
                  <p :class="getAmountSize(summary.totalSpent)" class="font-bold text-red-900 leading-tight text-sm sm:text-base">{{ formatCurrency(summary.totalSpent) }}</p>
                  <p class="text-xs text-red-600 mt-1">{{ formatNumber(summary.totalSpent) }}</p>
                </div>
              </div>
            </div>

            <div class="bg-gradient-to-br from-purple-50 to-purple-100 rounded-xl border border-purple-200 p-3 sm:p-4 hover:shadow-md transition-shadow duration-200">
              <div class="flex items-center justify-between">
                <div>
                  <p class="text-xs font-medium text-purple-700 uppercase tracking-wide">BNPL</p>
                  <p class="text-xl sm:text-2xl font-bold text-purple-900 mt-1">{{ summary.bnplTransactions }}</p>
                </div>
                <div class="w-8 h-8 sm:w-10 sm:h-10 bg-purple-200 rounded-lg flex items-center justify-center">
                  <svg class="w-4 h-4 sm:w-5 sm:h-5 text-purple-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
                  </svg>
                </div>
              </div>
            </div>

            <div class="bg-gradient-to-br from-green-50 to-green-100 rounded-xl border border-green-200 p-3 sm:p-4 hover:shadow-md transition-shadow duration-200 min-h-[80px] sm:min-h-[100px]">
              <div class="flex flex-col h-full">
                <div class="flex items-center justify-between mb-2">
                  <p class="text-xs font-medium text-green-700 uppercase tracking-wide">Average</p>
                  <div class="w-6 h-6 sm:w-8 sm:h-8 bg-green-200 rounded-lg flex items-center justify-center flex-shrink-0">
                    <svg class="w-3 h-3 sm:w-4 sm:h-4 text-green-700" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
                    </svg>
                  </div>
                </div>
                <div class="flex-1 flex flex-col justify-center">
                  <p :class="getAmountSize(summary.avgTransaction)" class="font-bold text-green-900 leading-tight text-sm sm:text-base">{{ formatCurrency(summary.avgTransaction) }}</p>
                  <p class="text-xs text-green-600 mt-1">{{ formatNumber(summary.avgTransaction) }}</p>
                </div>
              </div>
            </div>
          </div>


          <!-- Analytics Cards on Left Side -->
          <div class="grid grid-cols-1 lg:grid-cols-2 gap-4 sm:gap-6 mb-6 sm:mb-8">
            <!-- Category Breakdown -->
            <div class="bg-gradient-to-br from-white to-purple-50 rounded-xl sm:rounded-2xl shadow-lg border border-purple-100 p-4 sm:p-6 hover:shadow-xl transition-all duration-300">
              <div class="flex items-center justify-between mb-3 sm:mb-4">
                <div class="flex items-center">
                  <div class="h-6 w-6 sm:h-8 sm:w-8 rounded-lg bg-gradient-to-br from-purple-500 to-pink-500 flex items-center justify-center mr-2 sm:mr-3">
                    <svg class="h-3 w-3 sm:h-5 sm:w-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
                    </svg>
                  </div>
                  <h3 class="text-base sm:text-lg font-bold text-gray-900">Categories</h3>
                </div>
              </div>
              
              <div class="space-y-2 sm:space-y-3">
                <div v-for="category in categoryBreakdown" :key="category.name" class="flex items-center justify-between p-2 sm:p-3 bg-white rounded-xl hover:shadow-md transition-all duration-200">
                  <div class="flex items-center flex-1 min-w-0">
                    <div :class="category.color" class="w-3 h-3 sm:w-4 sm:h-4 rounded-full mr-2 sm:mr-4 shadow-sm flex-shrink-0"></div>
                    <span class="text-xs sm:text-sm font-semibold text-gray-700">{{ category.name }}</span>
                  </div>
                  <span class="text-sm sm:text-lg font-bold text-gray-900 flex-shrink-0 ml-2">{{ category.percentage }}%</span>
                </div>
              </div>
            </div>

            <!-- Monthly Trends -->
            <div class="bg-gradient-to-br from-white to-blue-50 rounded-xl sm:rounded-2xl shadow-lg border border-blue-100 p-4 sm:p-6 hover:shadow-xl transition-all duration-300">
              <div class="flex items-center justify-between mb-3 sm:mb-4">
                <div class="flex items-center">
                  <div class="h-6 w-6 sm:h-8 sm:w-8 rounded-lg bg-gradient-to-br from-blue-500 to-cyan-500 flex items-center justify-center mr-2 sm:mr-3">
                    <svg class="h-3 w-3 sm:h-5 sm:w-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path>
                    </svg>
                  </div>
                  <h3 class="text-base sm:text-lg font-bold text-gray-900">Trends</h3>
                </div>
                <span :class="analytics.change >= 0 ? 'text-red-600' : 'text-green-600'" class="text-xs sm:text-sm font-bold">
                  {{ analytics.change >= 0 ? '+' : '' }}{{ analytics.change }}%
                </span>
              </div>
              
              <div class="space-y-3 sm:space-y-4">
                <div class="flex justify-between items-center p-2 sm:p-3 bg-white rounded-xl">
                  <span class="text-xs sm:text-sm font-semibold text-gray-700">This Month</span>
                  <div class="text-right">
                    <span :class="getAmountSize(analytics.thisMonth)" class="font-bold text-blue-600 text-xs sm:text-sm">{{ formatCurrency(analytics.thisMonth) }}</span>
                    <p class="text-xs text-gray-400">{{ formatNumber(analytics.thisMonth) }}</p>
                  </div>
                </div>
                <div class="flex justify-between items-center p-2 sm:p-3 bg-white rounded-xl">
                  <span class="text-xs sm:text-sm font-semibold text-gray-700">Last Month</span>
                  <div class="text-right">
                    <span :class="getAmountSize(analytics.lastMonth)" class="font-bold text-gray-600 text-xs sm:text-sm">{{ formatCurrency(analytics.lastMonth) }}</span>
                    <p class="text-xs text-gray-400">{{ formatNumber(analytics.lastMonth) }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Filter Section -->
          <div class="bg-white rounded-xl shadow-lg border border-gray-100 p-3 sm:p-4 lg:p-6 mb-4 sm:mb-6 lg:mb-8">
            <div class="space-y-3">
              <!-- Search Bar -->
              <div class="w-full">
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg class="h-4 w-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                    </svg>
                  </div>
                  <input
                    v-model="filters.search"
                    type="text"
                    placeholder="Search merchants, amounts, categories..."
                    class="block w-full pl-10 pr-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
              </div>
              
              <!-- Filter Controls -->
              <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-2 sm:gap-3">
                <select v-model="filters.cardId" class="px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 w-full">
                  <option value="">All Cards</option>
                  <option v-for="card in cards" :key="card.id" :value="card.id">
                    {{ card.cardType }} ****{{ card.lastFour }}
                  </option>
                </select>

                <select v-model="filters.category" class="px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 w-full">
                  <option value="">All Categories</option>
                  <option value="shopping">Shopping</option>
                  <option value="food">Food & Dining</option>
                  <option value="travel">Travel</option>
                  <option value="entertainment">Entertainment</option>
                  <option value="other">Other</option>
                </select>

                <select v-model="filters.status" class="px-3 py-2 text-sm border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 w-full">
                  <option value="">All Status</option>
                  <option value="completed">Completed</option>
                  <option value="pending">Pending</option>
                  <option value="failed">Failed</option>
                </select>

                <div class="flex flex-col sm:flex-row items-start sm:items-center space-y-2 sm:space-y-0 sm:space-x-2">
                  <label class="flex items-center">
                    <input
                      v-model="filters.bnplOnly"
                      type="checkbox"
                      class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                    />
                    <span class="ml-2 text-sm text-gray-700">BNPL Only</span>
                  </label>
                  <button
                    @click="clearAllFilters"
                    class="px-3 py-2 text-sm font-medium text-gray-700 bg-gray-100 border border-gray-200 rounded-lg hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-colors duration-200 w-full sm:w-auto"
                  >
                    Clear All
                  </button>
                </div>
              </div>
            </div>
          </div>

          <!-- Transaction Table -->
          <TransactionTable 
            :transactions="filteredTransactions"
            :loading="loading"
            @refresh="fetchTransactions"
          />

          </div>

        </div>

        <!-- BNPL Section - Full Width -->
        <div>
          <BnplSection 
            :bnpl-plans="bnplPlans"
            @pay-emi="handleEmiPayment"
          />
        </div>
      </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useStore } from 'vuex'
// @ts-ignore
import { useRouter } from 'vue-router'
import TransactionTable from '../../components/TransactionTable.vue'
import BnplSection from '../../components/BnplSection.vue'

// Type definitions
interface Transaction {
  id: string
  transactionId?: string
  serialNo?: number
  merchant: string
  category: string
  cardId: number
  cardType: string
  lastFour: string
  amount: number
  status: string
  isBnpl: boolean
  createdAt: string
}

const store = useStore()
const router = useRouter()

// Reactive data
const loading = ref(false)

// Filters
const filters = ref({
  search: '',
  cardId: '',
  category: '',
  status: '',
  bnplOnly: false,
  dateRange: '',
  dateRangeTo: ''
})

// Computed properties
const transactions = computed(() => store.state?.transactions?.transactions || [])
const cards = computed(() => store.state?.cards?.cards || [])
const bnplPlans = computed(() => store.state?.bnpl?.plans || [
  {
    id: '1',
    merchant: 'Amazon',
    status: 'ACTIVE',
    tenureMonths: 6,
    totalAmount: 2045000, 
    paidAmount: 1022500,  
    remainingAmount: 1022500,
    monthlyEmi: 340833,  
    paidInstallments: 3,
    startDate: '2023-11-15',
    nextDueDate: '2024-02-15'
  },
  {
    id: '2',
    merchant: 'Flipkart',
    status: 'ACTIVE',
    tenureMonths: 12,
    totalAmount: 899000, 
    paidAmount: 180000,   
    remainingAmount: 719000, 
    monthlyEmi: 75000,   
    paidInstallments: 2,
    startDate: '2023-11-20',
    nextDueDate: '2024-01-20'
  },
  {
    id: '3',
    merchant: 'Myntra',
    status: 'ACTIVE',
    tenureMonths: 9,
    totalAmount: 1250000,
    paidAmount: 416667,
    remainingAmount: 833333,
    monthlyEmi: 138889,
    paidInstallments: 3,
    startDate: '2023-12-01',
    nextDueDate: '2024-03-01'
  },
  {
    id: '4',
    merchant: 'Croma',
    status: 'ACTIVE',
    tenureMonths: 18,
    totalAmount: 3500000,
    paidAmount: 583333,
    remainingAmount: 2916667,
    monthlyEmi: 194444,
    paidInstallments: 3,
    startDate: '2023-10-15',
    nextDueDate: '2024-01-15'
  }
])
const analytics = computed(() => store.state?.analytics?.data || {
  thisMonth: 1250000,
  lastMonth: 980000,
  change: 27.6,
  categoryBreakdown: [],
  topMerchants: []
})

const categoryBreakdown = computed(() => [
  { name: 'Shopping', percentage: 45, color: 'bg-purple-500' },
  { name: 'Food', percentage: 30, color: 'bg-orange-500' },
  { name: 'Travel', percentage: 25, color: 'bg-blue-500' }
])


const summary = computed(() => ({
  totalTransactions: transactions.value.length || 5,
  totalSpent: transactions.value.reduce((sum: number, t: Transaction) => sum + t.amount, 0) || 25000000,
  bnplTransactions: transactions.value.filter((t: Transaction) => t.isBnpl).length || 2,
  avgTransaction: transactions.value.length > 0 
    ? transactions.value.reduce((sum: number, t: Transaction) => sum + t.amount, 0) / transactions.value.length 
    : 5000000
}))

const filteredTransactions = computed(() => {
  let filtered = transactions.value

  if (filters.value.search) {
    const search = filters.value.search.toLowerCase()
    filtered = filtered.filter((t: Transaction) => 
      t.merchant.toLowerCase().includes(search) ||
      (t.transactionId && t.transactionId.toLowerCase().includes(search)) ||
      t.amount.toString().includes(search)
    )
  }

  if (filters.value.cardId) {
    filtered = filtered.filter((t: Transaction) => t.cardId === parseInt(filters.value.cardId))
  }

  if (filters.value.category) {
    filtered = filtered.filter((t: Transaction) => t.category === filters.value.category)
  }

  if (filters.value.status) {
    filtered = filtered.filter((t: Transaction) => t.status === filters.value.status)
  }

  if (filters.value.bnplOnly) {
    filtered = filtered.filter((t: Transaction) => t.isBnpl)
  }

  return filtered
})

// Methods
const formatNumber = (num: number) => {
  return new Intl.NumberFormat('en-IN').format(num)
}

const formatCurrency = (amount: number) => {
  if (amount >= 10000000) { // 1 Crore
    return `₹${(amount / 10000000).toFixed(1)}Cr`
  } else if (amount >= 100000) { // 1 Lakh
    return `₹${(amount / 100000).toFixed(1)}L`
  } else if (amount >= 1000) { // 1 Thousand
    return `₹${(amount / 1000).toFixed(1)}K`
  } else {
    return `₹${amount.toFixed(0)}`
  }
}

const getAmountSize = (amount: number) => {
  if (amount >= 10000000) return 'text-lg'
  if (amount >= 100000) return 'text-xl' 
  if (amount >= 1000) return 'text-2xl'
  return 'text-2xl'
}

const goToCreateTransaction = () => {
  router.push('/create-transaction')
}

const clearAllFilters = () => {
  filters.value = {
    search: '',
    cardId: '',
    category: '',
    status: '',
    bnplOnly: false,
    dateRange: '',
    dateRangeTo: ''
  }
}

const exportTransactions = async () => {
  try {
    const params = new URLSearchParams()
    if (filters.value.cardId) params.append('cardId', filters.value.cardId)
    if (filters.value.category) params.append('category', filters.value.category)
    if (filters.value.status) params.append('status', filters.value.status)
    if (filters.value.bnplOnly) params.append('bnplOnly', 'true')
    if (filters.value.dateRange) params.append('startDate', filters.value.dateRange)
    if (filters.value.dateRangeTo) params.append('endDate', filters.value.dateRangeTo)
    
    const response = await fetch(`http://localhost:8080/api/export/transactions/csv?${params}`)
    if (response.ok) {
      const blob = await response.blob()
      const url = window.URL.createObjectURL(blob)
      const link = document.createElement('a')
      link.href = url
      link.download = `transactions_${new Date().toISOString().split('T')[0]}.csv`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      window.URL.revokeObjectURL(url)
    } else {
      exportTransactionsFrontend()
    }
  } catch (error) {
    console.error('Backend export failed, using frontend fallback:', error)
    exportTransactionsFrontend()
  }
}

const exportTransactionsFrontend = () => {
  const headers = ['Date', 'Merchant', 'Amount', 'Category', 'Status', 'Card', 'Type']
  const csvContent = [
    headers.join(','),
    ...filteredTransactions.value.map((transaction: Transaction) => [
      new Date(transaction.createdAt).toLocaleDateString('en-IN'),
      `"${transaction.merchant}"`,
      transaction.amount,
      transaction.category,
      transaction.status,
      `"${transaction.cardType} ****${transaction.lastFour}"`,
      transaction.isBnpl ? 'BNPL' : 'Regular'
    ].join(','))
  ].join('\n')

  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `transactions_${new Date().toISOString().split('T')[0]}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

const handleEmiPayment = (planId: string, amount: number) => {
  console.log('EMI Payment:', planId, amount)
}

const fetchTransactions = async () => {
  loading.value = true
  try {
    await store.dispatch('transactions/fetchTransactions', 1) 
  } catch (error) {
    console.error('Error fetching transactions:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchTransactions()
  store.dispatch('cards/fetchCards', 1)
  store.dispatch('merchants/fetchMerchants')
  store.dispatch('bnpl/fetchActivePlans')
  store.dispatch('analytics/fetchAnalytics')
})
</script>
