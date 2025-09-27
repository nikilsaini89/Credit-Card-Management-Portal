<template>
  <div class="min-h-screen bg-gray-50 overflow-x-hidden">
    <!-- Header Section -->
    <div class="bg-white border-b border-gray-100 shadow-sm">
      <div class="w-full py-4 sm:py-6">
        <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between space-y-3 sm:space-y-0">
          <div>
            <h1 class="text-xl sm:text-2xl font-bold text-gray-900">Transaction History</h1>
            <p class="text-xs sm:text-sm text-gray-600 mt-1">View and manage your payment history</p>
          </div>
          <div class="flex flex-col sm:flex-row items-stretch sm:items-center space-y-2 sm:space-y-0 sm:space-x-3">
            <button @click="exportTransactions" class="inline-flex items-center justify-center px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:ring-offset-2 transition-all duration-200 shadow-sm">
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 10v6m0 0l-3-3m3 3l3-3m2 8H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
              </svg>
              Export PDF
            </button>
            <button @click="goToCreateTransaction" class="inline-flex items-center justify-center px-4 py-2 text-sm font-medium text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-yellow-400 focus:ring-offset-2 transition-all duration-200 shadow-lg hover:shadow-xl" style="background: #ffd60a; color: #0b2540;">
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path>
              </svg>
              New Transaction
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="w-full py-4 sm:py-6 overflow-x-hidden">
      <div class="space-y-4 sm:space-y-6">
        <!-- Main Content -->
        <div>
          <!-- Bill Cycle Information -->
          <div class="bg-white rounded-xl shadow-lg border border-gray-100 p-4 sm:p-6 mb-6" v-if="!store.state.bills?.loading && bill">
            <div class="flex items-center justify-between mb-4">
              <div class="flex items-center">
                <div class="h-8 w-8 rounded-lg flex items-center justify-center mr-3" style="background: #ffd60a;">
                  <svg class="h-5 w-5" style="color: #0b2540;" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
                  </svg>
                </div>
                <h3 class="text-lg font-bold text-gray-900">Credit Card Bill</h3>
              </div>
              <div class="text-right">
                <div class="text-sm font-medium text-gray-600">Statement Date</div>
                <div class="text-lg font-bold" style="color: #0b2540;">
                  {{ bill ? new Date(bill.statementDate).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' }) : 'Loading...' }}
                </div>
              </div>
            </div>
            
            <div class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
              <div class="bg-gray-50 rounded-lg p-4">
                <div class="text-sm font-medium text-gray-600 mb-1">Total Statement Amount</div>
                <div class="text-2xl font-bold" style="color: #0b2540;">
                  {{ bill ? `₹ ${formatNumber(bill.totalStatementAmount)}` : 'Loading...' }}
                </div>
              </div>
              <div class="bg-gray-50 rounded-lg p-4">
                <div class="text-sm font-medium text-gray-600 mb-1">Amount Due</div>
                <div class="text-2xl font-bold text-red-600">
                  {{ bill ? `₹ ${formatNumber(bill.amountDue)}` : 'Loading...' }}
                </div>
                <div class="text-xs text-gray-500 mt-1">
                  {{ bill ? `Due on ${new Date(bill.dueDate).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}` : 'Loading...' }}
                </div>
              </div>
              <div class="bg-gray-50 rounded-lg p-4">
                <div class="text-sm font-medium text-gray-600 mb-1">Available Credit</div>
                <div class="text-2xl font-bold text-green-600">
                  {{ bill ? `₹ ${formatNumber(bill.availableCredit)}` : 'Loading...' }}
                </div>
                <div class="text-xs text-gray-500 mt-1">
                  {{ bill ? `Out of ₹${formatNumber(bill.creditLimit)} limit` : 'Loading...' }}
                </div>
              </div>
              <div class="bg-gray-50 rounded-lg p-4">
                <div class="text-sm font-medium text-gray-600 mb-1">Credit Limit Usage</div>
                <div class="flex items-center justify-between mb-2">
                  <div class="text-2xl font-bold" style="color: #0b2540;">
                    {{ bill ? `${bill.creditLimitUsage.toFixed(1)}%` : 'Loading...' }}
                  </div>
                  <div class="text-xs text-gray-500">
                    {{ bill ? `₹${formatNumber(bill.creditLimit - bill.availableCredit)} / ₹${formatNumber(bill.creditLimit)}` : 'Loading...' }}
                  </div>
                </div>
                <!-- Gauge Meter -->
                <div class="relative w-full h-3 bg-gray-200 rounded-full overflow-hidden">
                  <div class="absolute top-0 left-0 h-full rounded-full transition-all duration-1000" 
                       :style="{ 
                         width: bill ? `${Math.min(bill.creditLimitUsage, 100)}%` : '0%', 
                         background: 'linear-gradient(90deg, #10b981 0%, #f59e0b 50%, #ef4444 100%)' 
                       }">
                  </div>
                </div>
                <div class="text-xs text-gray-500 mt-1">
                  {{ bill ? bill.spendingTrend : 'Loading...' }}
                </div>
              </div>
            </div>
            
            <div class="mt-4 flex justify-between items-center">
              <div class="flex items-center text-sm text-gray-600">
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                </svg>
                Post bill payment updated amount due will get reflected after 1-2 hours.
              </div>
              <button @click="handleBillPayment" class="px-6 py-2 text-sm font-medium text-white rounded-lg shadow-lg hover:shadow-xl transition-all duration-200" style="background: #0b2540;">
                PAY NOW
              </button>
            </div>
          </div>

          <!-- Loading State for Bill -->
          <div class="bg-white rounded-xl shadow-lg border border-gray-100 p-4 sm:p-6 mb-6" v-if="store.state.bills?.loading || !bill">
            <div class="flex items-center justify-center py-8">
              <div class="animate-spin rounded-full h-8 w-8 border-b-2" style="border-color: #0b2540;"></div>
              <span class="ml-3 text-gray-600">Loading bill information...</span>
            </div>
            <div class="mt-4 text-center">
              <button @click="fetchBill" class="px-4 py-2 text-sm font-medium text-white rounded-lg" style="background: #0b2540;">
                Retry Bill Fetch
              </button>
            </div>
          </div>

          <!-- Quick Stats Row -->
          <div class="grid grid-cols-2 sm:grid-cols-2 xl:grid-cols-4 gap-3 sm:gap-4 mb-6">
            <div class="bg-white rounded-xl p-4 sm:p-6 shadow-lg border border-gray-100 hover:shadow-xl transition-all duration-300">
              <div class="flex items-center justify-between">
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-semibold text-gray-600">Total Transactions</p>
                  <p class="text-2xl font-bold mt-1" style="color: #0b2540;">{{ summary.totalTransactions }}</p>
                </div>
                <div class="h-10 w-10 rounded-lg flex items-center justify-center flex-shrink-0 ml-2" style="background: #ffd60a;">
                  <svg class="h-5 w-5" style="color: #0b2540;" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v10a2 2 0 002 2h8a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
                  </svg>
                </div>
              </div>
            </div>
            
            <div class="bg-white rounded-xl p-4 sm:p-6 shadow-lg border border-gray-100 hover:shadow-xl transition-all duration-300">
              <div class="flex items-center justify-between">
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-semibold text-gray-600">Total Spent</p>
                  <p :class="getAmountSize(summary.totalSpent)" class="font-bold mt-1 text-red-600">{{ formatCurrency(summary.totalSpent) }}</p>
                  <p class="text-xs text-gray-500 mt-1">{{ formatNumber(summary.totalSpent) }}</p>
                </div>
                <div class="h-10 w-10 rounded-lg flex items-center justify-center flex-shrink-0 ml-2 bg-red-100">
                  <svg class="h-5 w-5 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1"></path>
                  </svg>
                </div>
              </div>
            </div>
            
            <div class="bg-white rounded-xl p-4 sm:p-6 shadow-lg border border-gray-100 hover:shadow-xl transition-all duration-300">
              <div class="flex items-center justify-between">
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-semibold text-gray-600">BNPL Plans</p>
                  <p class="text-2xl font-bold mt-1" style="color: #0b2540;">{{ bnplPlans.length }}</p>
                  <p class="text-xs text-gray-500 mt-1">active plans</p>
                </div>
                <div class="h-10 w-10 rounded-lg flex items-center justify-center flex-shrink-0 ml-2 bg-purple-100">
                  <svg class="h-5 w-5 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
                  </svg>
                </div>
              </div>
            </div>
            
            <div class="bg-white rounded-xl p-4 sm:p-6 shadow-lg border border-gray-100 hover:shadow-xl transition-all duration-300">
              <div class="flex items-center justify-between">
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-semibold text-gray-600">Average Transaction</p>
                  <p :class="getAmountSize(summary.avgTransaction)" class="font-bold mt-1 text-green-600">{{ formatCurrency(summary.avgTransaction) }}</p>
                  <p class="text-xs text-gray-500 mt-1">{{ formatNumber(summary.avgTransaction) }}</p>
                </div>
                <div class="h-10 w-10 rounded-lg flex items-center justify-center flex-shrink-0 ml-2 bg-green-100">
                  <svg class="h-5 w-5 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
                  </svg>
                </div>
              </div>
            </div>
          </div>



          <!-- Analytics Charts Section -->
          <div class="grid grid-cols-1 xl:grid-cols-2 gap-4 sm:gap-6 mb-8">
            <!-- Category Breakdown Chart -->
            <div class="bg-white rounded-xl shadow-lg border border-gray-100 p-6">
              <div class="flex items-center justify-between mb-6">
                <div class="flex items-center">
                  <div class="h-8 w-8 rounded-lg flex items-center justify-center mr-3" style="background: #ffd60a;">
                    <svg class="h-5 w-5" style="color: #0b2540;" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 3.055A9.001 9.001 0 1020.945 13H11V3.055z"></path>
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.488 9H15V3.512A9.025 9.025 0 0120.488 9z"></path>
                    </svg>
                  </div>
                  <h3 class="text-lg font-bold text-gray-900">Spending by Category</h3>
                </div>
              </div>
              
              <!-- Chart Container -->
              <div class="h-64 flex items-center justify-center">
                <canvas ref="categoryChart" class="w-full h-full"></canvas>
              </div>
              
              <!-- Legend -->
              <div class="mt-4 grid grid-cols-2 gap-2">
                <div v-for="category in categoryBreakdown" :key="category.name" class="flex items-center justify-between p-2 bg-gray-50 rounded-lg">
                  <div class="flex items-center">
                    <div :class="category.color" class="w-3 h-3 rounded-full mr-2"></div>
                    <span class="text-sm font-medium text-gray-700">{{ category.name }}</span>
                  </div>
                  <span class="text-sm font-bold" style="color: #0b2540;">{{ category.percentage }}%</span>
                </div>
              </div>
            </div>

            <!-- Monthly Trends Chart -->
            <div class="bg-white rounded-xl shadow-lg border border-gray-100 p-6">
              <div class="flex items-center justify-between mb-6">
                <div class="flex items-center">
                  <div class="h-8 w-8 rounded-lg flex items-center justify-center mr-3" style="background: #ffd60a;">
                    <svg class="h-5 w-5" style="color: #0b2540;" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6"></path>
                    </svg>
                  </div>
                  <h3 class="text-lg font-bold text-gray-900">Monthly Trends</h3>
                </div>
                <div class="text-right">
                  <div class="text-sm font-medium text-gray-600">Growth</div>
                  <div :class="analytics.change >= 0 ? 'text-green-600' : 'text-red-600'" class="text-lg font-bold">
                    {{ analytics.change >= 0 ? '+' : '' }}{{ analytics.change }}%
                  </div>
                </div>
              </div>
              
              <!-- Chart Container -->
              <div class="h-64 flex items-center justify-center">
                <canvas ref="trendsChart" class="w-full h-full"></canvas>
              </div>
              
              <!-- Summary Stats -->
              <div class="mt-4 grid grid-cols-2 gap-4">
                <div class="text-center p-3 bg-gray-50 rounded-lg">
                  <div class="text-sm font-medium text-gray-600">This Month</div>
                  <div :class="getAmountSize(analytics.thisMonth)" class="font-bold mt-1" style="color: #0b2540;">{{ formatCurrency(analytics.thisMonth) }}</div>
                </div>
                <div class="text-center p-3 bg-gray-50 rounded-lg">
                  <div class="text-sm font-medium text-gray-600">Last Month</div>
                  <div :class="getAmountSize(analytics.lastMonth)" class="font-bold mt-1 text-gray-600">{{ formatCurrency(analytics.lastMonth) }}</div>
                </div>
              </div>
            </div>
          </div>

          <!-- Filter Section -->
          <div class="bg-white rounded-3xl shadow-xl border border-gray-100 p-4 sm:p-6 md:p-8 mb-8">
            <div class="space-y-6 sm:space-y-8">
              <!-- Search Bar -->
              <div class="w-full sm:w-2/3 md:w-1/2 mx-auto">
                <div class="relative">
                  <div class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                    <svg class="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                    </svg>
                  </div>
                  <input
                    v-model="filters.search"
                    type="text"
                    placeholder="Search transactions..."
                    class="block w-full pl-12 pr-4 py-3 sm:py-4 text-sm border border-gray-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 bg-gray-50 hover:bg-white"
                  />
                </div>
              </div>
              
              <!-- Filter Controls -->
              <div class="space-y-6">
                <!-- Filter Labels -->
                <div class="grid grid-cols-1 md:grid-cols-3 gap-4 sm:gap-6 md:gap-8">
                  <div class="space-y-2">
                    <label class="text-sm font-semibold text-gray-700">Payment Card</label>
                    <div class="relative">
                      <button
                        @click="toggleCardDropdown"
                        class="w-full px-4 py-3 text-sm border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 bg-white hover:bg-gray-50 text-left flex items-center justify-between"
                      >
                        <span class="text-gray-500">{{ getCardDisplayText() }}</span>
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
                            All Cards
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

                  <div class="space-y-2">
                    <label class="text-sm font-semibold text-gray-700">Category</label>
                    <div class="relative">
                      <button
                        @click="toggleCategoryDropdown"
                        class="w-full px-4 py-3 text-sm border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 bg-white hover:bg-gray-50 text-left flex items-center justify-between"
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
                            All Categories
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

                  <div class="space-y-2">
                    <label class="text-sm font-semibold text-gray-700">Transaction Status</label>
                    <div class="relative">
                      <button
                        @click="toggleStatusDropdown"
                        class="w-full px-4 py-3 text-sm border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all duration-200 bg-white hover:bg-gray-50 text-left flex items-center justify-between"
                      >
                        <span class="text-gray-500">{{ getStatusDisplayText() }}</span>
                        <svg class="w-4 h-4 text-gray-400 transition-transform duration-200" :class="{ 'rotate-180': showStatusDropdown }" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7"></path>
                        </svg>
                      </button>
                      
                      <!-- Custom Dropdown Overlay -->
                      <div v-if="showStatusDropdown" class="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-200 rounded-xl shadow-lg z-50 overflow-hidden">
                        <div class="p-2 space-y-1">
                          <button
                            @click="selectStatus('')"
                            class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200"
                          >
                            All Status
                          </button>
                          <button
                            @click="selectStatus('completed')"
                            class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200 flex items-center"
                          >
                            <span class="inline-block w-3 h-3 rounded-full bg-green-500 mr-3"></span>
                            <span class="text-green-700 font-medium">Completed</span>
                          </button>
                          <button
                            @click="selectStatus('pending')"
                            class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200 flex items-center"
                          >
                            <span class="inline-block w-3 h-3 rounded-full bg-yellow-500 mr-3"></span>
                            <span class="text-yellow-700 font-medium">Pending</span>
                          </button>
                          <button
                            @click="selectStatus('failed')"
                            class="w-full px-3 py-2 text-sm text-left hover:bg-yellow-500 hover:text-white rounded-lg transition-colors duration-200 flex items-center"
                          >
                            <span class="inline-block w-3 h-3 rounded-full bg-red-500 mr-3"></span>
                            <span class="text-red-700 font-medium">Failed</span>
                          </button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Actions Row -->
                <div class="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 pt-4">
                  <label class="flex items-center space-x-3 cursor-pointer group" @click="toggleBnpl">
                    <div class="relative">
                      <div class="w-11 h-6 bg-gray-200 rounded-full transition-colors duration-200 group-hover:bg-gray-300" :class="{ 'bg-yellow-500': filters.bnplOnly }">
                        <div class="w-5 h-5 bg-white rounded-full shadow-md transform transition-transform duration-200 mt-0.5" :class="{ 'translate-x-5': filters.bnplOnly, 'translate-x-0.5': !filters.bnplOnly }"></div>
                      </div>
                    </div>
                    <span class="text-sm font-medium text-gray-700">Buy Now, Pay Later only</span>
                  </label>

                  <button
                    @click="clearAllFilters"
                    class="flex items-center space-x-2 px-4 py-2 text-sm font-medium text-gray-600 bg-gray-100 border border-gray-200 rounded-xl hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2 transition-all duration-200"
                  >
                    <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                    </svg>
                    <span>Clear All</span>
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

    <!-- Payment Flow Modal -->
    <PaymentFlow 
      :show-payment="showPayment"
      :payment-amount="paymentAmount"
      :payment-type="paymentType"
      :due-date="dueDate"
      @close-payment="closePayment"
      @payment-success="onPaymentSuccess"
    />

    <!-- Error Boundary -->
    <ErrorBoundary 
      :error="store.state.transactions?.error || store.state.analytics?.error || store.state.bnpl?.error || store.state.bills?.error"
      @retry="handleRetry"
      @dismiss="handleDismissError"
    />
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useStore } from 'vuex'
// @ts-ignore
import { useRouter } from 'vue-router'
import TransactionTable from '../../components/TransactionTable.vue'
import BnplSection from '../../components/BnplSection.vue'
import PaymentFlow from '../../components/PaymentFlow.vue'
import ErrorBoundary from '../../components/ErrorBoundary.vue'
import { Chart, registerables } from 'chart.js'
import jsPDF from 'jspdf'

// Register Chart.js components
Chart.register(...registerables)

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
const categoryChart = ref<HTMLCanvasElement | null>(null)
const trendsChart = ref<HTMLCanvasElement | null>(null)
const categoryChartInstance = ref<Chart | null>(null)
const trendsChartInstance = ref<Chart | null>(null)
const showPayment = ref(false)
const paymentAmount = ref(0)
const paymentType = ref('')
const dueDate = ref('')

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

const showStatusDropdown = ref(false)
const showCardDropdown = ref(false)
const showCategoryDropdown = ref(false)

// Computed properties
const transactions = computed(() => store.state?.transactions?.transactions || [])
const cards = computed(() => store.state?.cards?.cards || [])
const bill = computed(() => store.state?.bills?.bill || null)
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
  thisMonth: 0,
  lastMonth: 0,
  change: 0,
  categoryBreakdown: [],
  topMerchants: [],
  monthlyData: []
})

const categoryBreakdown = computed(() => analytics.value.categoryBreakdown || [])


const summary = computed(() => ({
  totalTransactions: transactions.value.length || 0,
  totalSpent: transactions.value.reduce((sum: number, t: Transaction) => sum + t.amount, 0) || 0,
  bnplTransactions: transactions.value.filter((t: Transaction) => t.isBnpl).length || 0,
  avgTransaction: transactions.value.length > 0 
    ? transactions.value.reduce((sum: number, t: Transaction) => sum + t.amount, 0) / transactions.value.length 
    : 0
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
  showStatusDropdown.value = false
  showCardDropdown.value = false
  showCategoryDropdown.value = false
}

const toggleStatusDropdown = () => {
  showStatusDropdown.value = !showStatusDropdown.value
  showCardDropdown.value = false
  showCategoryDropdown.value = false
}

const selectStatus = (status: string) => {
  filters.value.status = status
  showStatusDropdown.value = false
}

const getStatusDisplayText = () => {
  if (!filters.value.status) return 'Select status'
  const statusMap: Record<string, string> = {
    completed: 'Completed',
    pending: 'Pending',
    failed: 'Failed'
  }
  return statusMap[filters.value.status] || 'Select status'
}

const toggleBnpl = () => {
  filters.value.bnplOnly = !filters.value.bnplOnly
}

const toggleCardDropdown = () => {
  showCardDropdown.value = !showCardDropdown.value
  showCategoryDropdown.value = false
  showStatusDropdown.value = false
}

const toggleCategoryDropdown = () => {
  showCategoryDropdown.value = !showCategoryDropdown.value
  showCardDropdown.value = false
  showStatusDropdown.value = false
}

const selectCard = (cardId: string) => {
  filters.value.cardId = cardId
  showCardDropdown.value = false
}

const selectCategory = (category: string) => {
  filters.value.category = category
  showCategoryDropdown.value = false
}

const getCardDisplayText = () => {
  if (!filters.value.cardId) return 'Select card'
  const selectedCard = cards.value.find((card: any) => card.id === filters.value.cardId)
  return selectedCard ? `${selectedCard.cardType} ****${selectedCard.lastFour}` : 'Select card'
}

const getCategoryDisplayText = () => {
  if (!filters.value.category) return 'Select category'
  const categoryMap: Record<string, string> = {
    shopping: '🛒 Shopping',
    food: '🍔 Food & Dining',
    travel: '✈️ Travel',
    entertainment: '🎬 Entertainment',
    other: '📦 Other'
  }
  return categoryMap[filters.value.category] || 'Select category'
}

const exportTransactions = () => {
  exportTransactionsPDF()
}

const exportTransactionsPDF = () => {
  const doc = new jsPDF()
  
  // Add header
  doc.setFontSize(20)
  doc.setTextColor(11, 37, 64) // #0b2540
  doc.text('Transaction History Report', 14, 22)
  
  // Add date
  doc.setFontSize(10)
  doc.setTextColor(100, 100, 100)
  doc.text(`Generated on: ${new Date().toLocaleDateString('en-IN')}`, 14, 30)
  
  // Add filter information
  let filterText = 'All Transactions'
  if (filters.value.cardId || filters.value.category || filters.value.status || filters.value.bnplOnly) {
    const filterParts = []
    if (filters.value.cardId) {
      const selectedCard = cards.value.find((card: any) => card.id === filters.value.cardId)
      filterParts.push(`Card: ${selectedCard ? selectedCard.cardType : 'Selected'}`)
    }
    if (filters.value.category) {
      filterParts.push(`Category: ${filters.value.category}`)
    }
    if (filters.value.status) {
      filterParts.push(`Status: ${filters.value.status}`)
    }
    if (filters.value.bnplOnly) {
      filterParts.push('BNPL Only')
    }
    filterText = `Filtered: ${filterParts.join(', ')}`
  }
  
  doc.text(filterText, 14, 38)
  
  // Create a simple table without autoTable
  let yPosition = 50
  const pageWidth = doc.internal.pageSize.width
  const margin = 14
  const colWidths = [30, 50, 50, 30, 25, 40, 20]
  const colPositions = [margin]
  
  // Calculate column positions with padding
  for (let i = 1; i < colWidths.length; i++) {
    colPositions.push(colPositions[i-1] + colWidths[i-1] + 4) // Add 4 units padding between columns
  }
  
  
  // Ensure table fits within page width
  const totalTableWidth = colPositions[colPositions.length - 1] + colWidths[colWidths.length - 1] - margin
  if (totalTableWidth > pageWidth - 2 * margin) {
    // Adjust column widths if table is too wide
    const scaleFactor = (pageWidth - 2 * margin) / totalTableWidth
    for (let i = 0; i < colWidths.length; i++) {
      colWidths[i] = Math.floor(colWidths[i] * scaleFactor)
    }
    // Recalculate positions
    colPositions.length = 1
    for (let i = 1; i < colWidths.length; i++) {
      colPositions.push(colPositions[i-1] + colWidths[i-1] + 4)
    }
  }
  
  // Add table headers
  doc.setFontSize(8)
  doc.setFont('helvetica', 'bold')
  doc.setTextColor(11, 37, 64)
  
  const headers = ['Date', 'Merchant', 'Amount', 'Category', 'Status', 'Card', 'Type']
  headers.forEach((header, index) => {
    // Ensure text doesn't exceed column width
    let displayText = header
    const maxWidth = colWidths[index] - 2 // Leave 2 units margin
    const textWidth = doc.getTextWidth(header)
    
    if (textWidth > maxWidth) {
      // Truncate text if too long
      while (doc.getTextWidth(displayText + '...') > maxWidth && displayText.length > 0) {
        displayText = displayText.slice(0, -1)
      }
      displayText += '...'
    }
    
    if (index === 2) { // Amount column header - right align
      const finalTextWidth = doc.getTextWidth(displayText)
      const xPosition = colPositions[index] + colWidths[index] - finalTextWidth
      doc.text(displayText, xPosition, yPosition)
    } else {
      doc.text(displayText, colPositions[index], yPosition)
    }
  })
  
  yPosition += 8
  
  // Add table rows
  doc.setFont('helvetica', 'normal')
  doc.setTextColor(0, 0, 0)
  
  filteredTransactions.value.forEach((transaction: Transaction, index: number) => {
    // Check if we need a new page
    if (yPosition > doc.internal.pageSize.height - 20) {
      doc.addPage()
      yPosition = 20
    }
    
    // Alternate row background
    if (index % 2 === 0) {
      doc.setFillColor(249, 250, 251)
      doc.rect(margin, yPosition - 4, pageWidth - 2 * margin, 8, 'F')
    }
    
    // Format amount properly
    const amount = typeof transaction.amount === 'number' ? transaction.amount : parseFloat(transaction.amount) || 0
    const formattedAmount = `Rs. ${amount.toLocaleString('en-IN')}`
    
    
    const rowData = [
      new Date(transaction.createdAt).toLocaleDateString('en-IN'),
      transaction.merchant.length > 15 ? transaction.merchant.substring(0, 15) + '...' : transaction.merchant,
      formattedAmount,
      transaction.category.charAt(0).toUpperCase() + transaction.category.slice(1),
      transaction.status.charAt(0).toUpperCase() + transaction.status.slice(1),
      `${transaction.cardType} ****${transaction.lastFour}`,
      transaction.isBnpl ? 'BNPL' : 'Regular'
    ]
    
    rowData.forEach((cell, cellIndex) => {
      if (cellIndex === 2) { // Amount column - right align
        // Ensure font is set for amount
        doc.setFont('helvetica', 'normal')
        doc.setFontSize(8)
        
        // Simple right alignment for amount
        const textWidth = doc.getTextWidth(cell)
        const xPosition = colPositions[cellIndex] + colWidths[cellIndex] - textWidth - 2
        
        
        doc.text(cell, xPosition, yPosition)
      } else {
        // For other columns, normal truncation
        let displayText = cell
        const maxWidth = colWidths[cellIndex] - 2 // Leave 2 units margin
        const textWidth = doc.getTextWidth(cell)
        
        if (textWidth > maxWidth) {
          // Truncate text if too long
          while (doc.getTextWidth(displayText + '...') > maxWidth && displayText.length > 0) {
            displayText = displayText.slice(0, -1)
          }
          displayText += '...'
        }
        
        doc.text(displayText, colPositions[cellIndex], yPosition)
      }
    })
    
    yPosition += 8
  })
  
  // Add summary
  yPosition += 10
  doc.setFontSize(10)
  doc.setTextColor(11, 37, 64)
  doc.text(`Total Transactions: ${filteredTransactions.value.length}`, margin, yPosition)
  
  // Add total amount
  const totalAmount = filteredTransactions.value.reduce((sum: number, transaction: Transaction) => sum + Number(transaction.amount), 0)
  doc.text(`Total Amount: Rs. ${totalAmount.toLocaleString('en-IN')}`, margin, yPosition + 10)
  
  // Add footer
  doc.setFontSize(8)
  doc.setTextColor(100, 100, 100)
  doc.text('Generated by Credit Card Management Portal', margin, doc.internal.pageSize.height - 10)
  
  // Save the PDF
  doc.save(`transactions_${new Date().toISOString().split('T')[0]}.pdf`)
}

const handleEmiPayment = async (planId: string, amount: number) => {
  try {
    const userId = 1 // TODO: Get from auth store
    await store.dispatch('bnpl/processEmiPayment', { planId, amount, userId })
    
    // Show success message
    paymentAmount.value = amount
    paymentType.value = 'BNPL EMI Payment'
    dueDate.value = new Date().toISOString()
    showPayment.value = true
  } catch (error) {
    console.error('Error processing BNPL payment:', error)
    // Error will be handled by ErrorBoundary
  }
}

const handleBillPayment = () => {
  paymentAmount.value = 34948
  paymentType.value = 'Credit Card Bill Payment'
  dueDate.value = '2024-11-08'
  showPayment.value = true
}

const closePayment = () => {
  showPayment.value = false
}

const onPaymentSuccess = (transactionId: string, amount: number) => {
  console.log('Payment successful:', transactionId, amount)
  setTimeout(() => {
    showPayment.value = false
  }, 2000) 
}

const handleRetry = async () => {
  const userId = 7 // TODO: Get from auth store
  await fetchTransactions()
  store.dispatch('analytics/fetchAnalytics', userId)
  store.dispatch('bnpl/fetchActivePlans', userId)
  await fetchBill()
}

const handleDismissError = () => {
  store.commit('transactions/SET_ERROR', null)
  store.commit('analytics/SET_ERROR', null)
  store.commit('bnpl/SET_ERROR', null)
  store.commit('bills/SET_ERROR', null)
}

const fetchTransactions = async () => {
  loading.value = true
  try {
    await store.dispatch('transactions/fetchTransactions', 7) 
  } catch (error) {
    console.error('Error fetching transactions:', error)
  } finally {
    loading.value = false
  }
}

const fetchBill = async () => {
  try {
    // Use the first card for now, or you can make this dynamic
    const firstCard = cards.value[0]
    if (firstCard) {
      await store.dispatch('bills/fetchBill', { userId: 7, cardId: firstCard.id })
    } else {
      // If no cards, try with a default card ID
      await store.dispatch('bills/fetchBill', { userId: 7, cardId: 4 })
    }
  } catch (error) {
    console.error('Error fetching bill:', error)
  }
}

// Chart initialization methods
const initCategoryChart = () => {
  if (!categoryChart.value) return
  
  const ctx = categoryChart.value.getContext('2d')
  if (!ctx) return

  // Destroy existing chart if it exists
  if (categoryChartInstance.value) {
    categoryChartInstance.value.destroy()
  }

  // Only create chart if we have data
  if (categoryBreakdown.value.length === 0) {
    console.log('No category data available for chart')
    return
  }

  console.log('Creating category chart with data:', categoryBreakdown.value)

  categoryChartInstance.value = new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: categoryBreakdown.value.map((cat: any) => cat.name),
      datasets: [{
        data: categoryBreakdown.value.map((cat: any) => cat.percentage),
        backgroundColor: [
          '#ffd60a',
          '#f97316', 
          '#0b2540', 
          '#111827', 
          '#6b7280' 
        ],
        borderWidth: 0
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '60%',
      plugins: {
        legend: {
          display: false
        }
      }
    }
  })
}

const initTrendsChart = () => {
  if (!trendsChart.value) return
  
  const ctx = trendsChart.value.getContext('2d')
  if (!ctx) return

  // Destroy existing chart if it exists
  if (trendsChartInstance.value) {
    trendsChartInstance.value.destroy()
  }

  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
  
  // Get the last 6 months of data from analytics
  const monthlyData = analytics.value.monthlyData || []
  const currentDate = new Date()
  const last6MonthsData = []
  const last6MonthsLabels = []
  
  // Generate last 6 months
  for (let i = 5; i >= 0; i--) {
    const date = new Date(currentDate.getFullYear(), currentDate.getMonth() - i, 1)
    const monthIndex = date.getMonth()
    const year = date.getFullYear()
    
    last6MonthsLabels.push(months[monthIndex])
    
    // Find matching data from backend
    const matchingData = monthlyData.find((item: any) => 
      item.year === year && item.month === monthIndex + 1
    )
    last6MonthsData.push(matchingData ? matchingData.amount / 1000 : 0)
  }
  
  console.log('Trends chart data:', { last6MonthsLabels, last6MonthsData, monthlyData })
  
  trendsChartInstance.value = new Chart(ctx, {
    type: 'line',
    data: {
      labels: last6MonthsLabels,
      datasets: [{
        label: 'Spending',
        data: last6MonthsData,
        borderColor: '#ffd60a',
        backgroundColor: 'rgba(255, 214, 10, 0.1)',
        borderWidth: 3,
        fill: true,
        tension: 0.4,
        pointBackgroundColor: '#0b2540',
        pointBorderColor: '#ffd60a',
        pointBorderWidth: 2,
        pointRadius: 6
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          grid: {
            color: 'rgba(0, 0, 0, 0.05)'
          },
          ticks: {
            callback: function(value) {
              return '₹' + value + 'K'
            }
          }
        },
        x: {
          grid: {
            display: false
          }
        }
      }
    }
  })
}

// Watchers to re-initialize charts when data changes
watch(categoryBreakdown, () => {
  if (categoryBreakdown.value.length > 0) {
    nextTick(() => {
      initCategoryChart()
    })
  }
}, { deep: true })

watch(() => analytics.value.monthlyData, () => {
  if (analytics.value.monthlyData && analytics.value.monthlyData.length > 0) {
    nextTick(() => {
      initTrendsChart()
    })
  }
}, { deep: true })

onMounted(async () => {
  const userId = 7 // TODO: Get from auth store
  await fetchTransactions()
  await store.dispatch('cards/fetchCards', userId)
  store.dispatch('merchants/fetchMerchants')
  store.dispatch('bnpl/fetchActivePlans', userId)
  store.dispatch('analytics/fetchAnalytics', userId)
  
  // Fetch bill data after cards are loaded
  await fetchBill()
  
  // Initialize charts after DOM is ready
  await nextTick()
  initCategoryChart()
  initTrendsChart()
})
</script>

