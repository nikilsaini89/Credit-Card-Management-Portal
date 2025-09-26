<template>
  <div class="space-y-3 sm:space-y-4 overflow-x-hidden">
    <!-- BNPL Summary Card -->
    <div class="bg-gradient-to-br from-white to-purple-50 rounded-xl sm:rounded-2xl shadow-lg border border-purple-100 p-3 sm:p-4 hover:shadow-xl transition-all duration-300">
      <div class="flex items-center justify-between mb-4">
        <div class="flex items-center">
          <div class="h-8 w-8 rounded-lg bg-gradient-to-br from-purple-500 to-blue-500 flex items-center justify-center mr-3">
            <svg class="h-5 w-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
            </svg>
          </div>
          <h3 class="text-lg font-bold text-gray-900">BNPL Overview</h3>
        </div>
        <div class="text-right">
          <div class="text-2xl font-bold text-purple-600">{{ activePlansCount }}</div>
          <div class="text-xs text-gray-600">Active Plans</div>
        </div>
      </div>
      
      <div class="space-y-2 sm:space-y-3">
        <div class="flex items-center justify-between p-2 sm:p-3 bg-white rounded-lg shadow-sm">
          <div class="flex items-center min-w-0 flex-1">
            <div class="w-3 h-3 bg-red-500 rounded-full mr-2 sm:mr-3 flex-shrink-0"></div>
            <span class="text-xs sm:text-sm font-medium text-gray-700">Outstanding</span>
          </div>
          <div class="text-right flex-shrink-0 ml-2">
            <div :class="getAmountSize(outstandingBalance)" class="font-bold text-red-600 text-xs sm:text-sm">{{ formatCurrency(outstandingBalance) }}</div>
          </div>
        </div>
        
        <div class="flex items-center justify-between p-2 sm:p-3 bg-white rounded-lg shadow-sm">
          <div class="flex items-center min-w-0 flex-1">
            <div class="w-3 h-3 bg-green-500 rounded-full mr-2 sm:mr-3 flex-shrink-0"></div>
            <span class="text-xs sm:text-sm font-medium text-gray-700">Total Paid</span>
          </div>
          <div class="text-right flex-shrink-0 ml-2">
            <div :class="getAmountSize(totalPaid)" class="font-bold text-green-600 text-xs sm:text-sm">{{ formatCurrency(totalPaid) }}</div>
          </div>
        </div>
        
        <div class="flex items-center justify-between p-2 sm:p-3 bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg border border-blue-200">
          <div class="flex items-center min-w-0 flex-1">
            <div class="w-3 h-3 bg-blue-500 rounded-full mr-2 sm:mr-3 flex-shrink-0"></div>
            <span class="text-xs sm:text-sm font-medium text-gray-700">Progress</span>
          </div>
          <div class="text-right flex-shrink-0 ml-2">
            <div class="text-sm sm:text-lg font-bold text-blue-600">{{ Math.round((totalPaid / (totalPaid + outstandingBalance)) * 100) }}%</div>
          </div>
        </div>
      </div>
    </div>

    <!-- Active BNPL Plans -->
    <div v-if="bnplPlans.length > 0" class="bg-white rounded-xl sm:rounded-2xl shadow-lg border border-gray-100 overflow-hidden">
      <div class="px-3 sm:px-4 py-2 sm:py-3 bg-gradient-to-r from-gray-50 to-gray-100 border-b border-gray-200">
        <div class="flex items-center justify-between">
          <div class="flex items-center">
            <div class="h-5 w-5 sm:h-6 sm:w-6 rounded-lg bg-gradient-to-br from-purple-500 to-blue-500 flex items-center justify-center mr-2">
              <svg class="h-3 w-3 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
              </svg>
            </div>
            <h3 class="text-base sm:text-lg font-bold text-gray-900">Active Plans</h3>
          </div>
          <span class="text-xs sm:text-sm font-semibold text-gray-600">{{ bnplPlans.length }} plans</span>
        </div>
      </div>
      
      <div class="divide-y divide-gray-100">
        <div v-for="(plan, index) in bnplPlans" :key="plan.id" class="p-3 sm:p-4 hover:bg-gradient-to-r hover:from-blue-50 hover:to-purple-50 transition-all duration-200 border-l-4" :class="getPlanBorderColor(index)">
          <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-3 sm:mb-4 space-y-2 sm:space-y-0">
            <div class="flex items-center min-w-0 flex-1">
              <div class="h-8 w-8 sm:h-10 sm:w-10 rounded-lg flex items-center justify-center mr-2 sm:mr-3 flex-shrink-0" :class="getPlanIconBg(index)">
                <svg class="h-4 w-4 sm:h-5 sm:w-5" :class="getPlanIconColor(index)" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
                </svg>
              </div>
              <div class="min-w-0 flex-1">
                <div class="text-sm sm:text-base font-semibold text-gray-900 flex flex-col sm:flex-row sm:items-center">
                  <span class="truncate">{{ plan.merchant }}</span>
                  <span class="text-xs font-medium px-2 py-1 rounded-full mt-1 sm:mt-0 sm:ml-2 w-fit" :class="getPlanBadgeBg(index)">{{ getPlanBadgeText(index) }}</span>
                </div>
                <div class="text-xs text-gray-600">{{ plan.tenureMonths }} installments • Started {{ formatDate(plan.startDate) }}</div>
              </div>
            </div>
            <span class="inline-flex items-center px-2 sm:px-3 py-1 rounded-full text-xs font-semibold bg-gradient-to-r from-blue-100 to-purple-100 text-blue-800 border border-blue-200 w-fit">
              Active
            </span>
          </div>

          <!-- Financial Breakdown -->
          <div class="space-y-1 sm:space-y-2 mb-3 sm:mb-4">
            <div class="flex items-center justify-between p-2 sm:p-3 bg-gray-50 rounded-lg">
              <div class="flex items-center min-w-0 flex-1">
                <div class="w-3 h-3 bg-gray-500 rounded-full mr-2 sm:mr-3 flex-shrink-0"></div>
                <span class="text-xs sm:text-sm font-medium text-gray-700">Total</span>
              </div>
              <div class="text-right flex-shrink-0 ml-2">
                <div :class="getAmountSize(plan.totalAmount)" class="font-bold text-gray-900 text-xs sm:text-sm">{{ formatCurrency(plan.totalAmount) }}</div>
              </div>
            </div>
            
            <div class="flex items-center justify-between p-2 sm:p-3 bg-green-50 rounded-lg">
              <div class="flex items-center min-w-0 flex-1">
                <div class="w-3 h-3 bg-green-500 rounded-full mr-2 sm:mr-3 flex-shrink-0"></div>
                <span class="text-xs sm:text-sm font-medium text-gray-700">Paid</span>
              </div>
              <div class="text-right flex-shrink-0 ml-2">
                <div :class="getAmountSize(plan.paidAmount)" class="font-bold text-green-600 text-xs sm:text-sm">{{ formatCurrency(plan.paidAmount) }}</div>
              </div>
            </div>
            
            <div class="flex items-center justify-between p-2 sm:p-3 bg-red-50 rounded-lg">
              <div class="flex items-center min-w-0 flex-1">
                <div class="w-3 h-3 bg-red-500 rounded-full mr-2 sm:mr-3 flex-shrink-0"></div>
                <span class="text-xs sm:text-sm font-medium text-gray-700">Remaining</span>
              </div>
              <div class="text-right flex-shrink-0 ml-2">
                <div :class="getAmountSize(plan.remainingAmount)" class="font-bold text-red-600 text-xs sm:text-sm">{{ formatCurrency(plan.remainingAmount) }}</div>
              </div>
            </div>
            
            <div class="flex items-center justify-between p-2 sm:p-3 bg-blue-50 rounded-lg">
              <div class="flex items-center min-w-0 flex-1">
                <div class="w-3 h-3 bg-blue-500 rounded-full mr-2 sm:mr-3 flex-shrink-0"></div>
                <span class="text-xs sm:text-sm font-medium text-gray-700">EMI</span>
              </div>
              <div class="text-right flex-shrink-0 ml-2">
                <div :class="getAmountSize(plan.monthlyEmi)" class="font-bold text-blue-600 text-xs sm:text-sm">{{ formatCurrency(plan.monthlyEmi) }}</div>
              </div>
            </div>
          </div>

          <!-- Progress and Next Payment -->
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-3 mb-3 sm:mb-4">
            <div>
              <div class="flex justify-between text-xs font-semibold text-gray-700 mb-1">
                <span>Progress</span>
                <span>{{ plan.paidInstallments }}/{{ plan.tenureMonths }}</span>
              </div>
              <div class="w-full bg-gray-200 rounded-full h-2">
                <div 
                  class="bg-gradient-to-r from-blue-500 to-purple-500 h-2 rounded-full transition-all duration-500"
                  :style="{ width: `${(plan.paidInstallments / plan.tenureMonths) * 100}%` }"
                ></div>
              </div>
            </div>
            <div class="text-center p-2 bg-gradient-to-r from-blue-50 to-purple-50 rounded-lg border border-blue-200">
              <div class="text-xs font-semibold text-gray-700">Next Due</div>
              <div class="text-sm font-bold text-blue-600">{{ formatDate(plan.nextDueDate) }}</div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="flex">
            <Button 
              variant="primary"
              size="sm"
              full-width
              @click="payNow(plan)"
              class="text-xs sm:text-sm"
            >
              Pay Now
            </Button>
          </div>
        </div>
      </div>
    </div>

    <!-- No BNPL Plans -->
    <div v-else class="bg-white rounded-lg shadow-sm border p-6 text-center">
      <svg class="mx-auto h-12 w-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10"></path>
      </svg>
      <h3 class="mt-2 text-sm font-medium text-gray-900">No BNPL plans</h3>
      <p class="mt-1 text-sm text-gray-500">You don't have any active BNPL plans.</p>
    </div>

    <!-- BNPL Insights -->
    <div class="bg-blue-50 rounded-lg p-4">
      <h4 class="text-sm font-medium text-blue-900 mb-2">BNPL Insights</h4>
      <p class="text-sm text-blue-700">Your BNPL usage increased 15% vs last month.</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Button from './ui/Button.vue'

interface BnplPlan {
  id: string
  merchant: string
  status: string
  tenureMonths: number
  totalAmount: number
  paidAmount: number
  remainingAmount: number
  monthlyEmi: number
  paidInstallments: number
  startDate: string
  nextDueDate: string
}

interface Props {
  bnplPlans: BnplPlan[]
}

const props = defineProps<Props>()

const emit = defineEmits<{
  'pay-emi': [planId: string, amount: number]
}>()

// Computed properties
const activePlansCount = computed(() => {
  return props.bnplPlans.filter(plan => plan.status === 'ACTIVE').length
})

const outstandingBalance = computed(() => {
  return props.bnplPlans
    .filter(plan => plan.status === 'ACTIVE')
    .reduce((sum, plan) => sum + (plan.remainingAmount || 0), 0)
})

const totalPaid = computed(() => {
  return props.bnplPlans
    .reduce((sum, plan) => sum + (plan.paidAmount || 0), 0)
})

// Methods
const formatNumber = (num: number) => {
  return new Intl.NumberFormat('en-IN').format(num)
}

const formatCurrency = (amount: number) => {
  return `₹${formatNumber(amount)}`
}

const getAmountSize = (amount: number) => {
  if (amount >= 10000000) return 'text-sm' // Crores - smallest
  if (amount >= 100000) return 'text-base'   // Lakhs - small
  if (amount >= 1000) return 'text-lg'    // Thousands - medium
  return 'text-lg' // Small amounts - medium
}

const getPlanBorderColor = (index: number) => {
  const colors = ['border-blue-500', 'border-green-500', 'border-purple-500', 'border-orange-500', 'border-pink-500', 'border-indigo-500']
  return colors[index % colors.length]
}

const getPlanIconBg = (index: number) => {
  const colors = ['bg-gradient-to-br from-blue-100 to-blue-200', 'bg-gradient-to-br from-green-100 to-green-200', 'bg-gradient-to-br from-purple-100 to-purple-200', 'bg-gradient-to-br from-orange-100 to-orange-200', 'bg-gradient-to-br from-pink-100 to-pink-200', 'bg-gradient-to-br from-indigo-100 to-indigo-200']
  return colors[index % colors.length]
}

const getPlanIconColor = (index: number) => {
  const colors = ['text-blue-600', 'text-green-600', 'text-purple-600', 'text-orange-600', 'text-pink-600', 'text-indigo-600']
  return colors[index % colors.length]
}

const getPlanBadgeBg = (index: number) => {
  const colors = ['bg-blue-100 text-blue-800', 'bg-green-100 text-green-800', 'bg-purple-100 text-purple-800', 'bg-orange-100 text-orange-800', 'bg-pink-100 text-pink-800', 'bg-indigo-100 text-indigo-800']
  return colors[index % colors.length]
}

const getPlanBadgeText = (index: number) => {
  const texts = ['Premium', 'Standard', 'Elite', 'Gold', 'Platinum', 'Diamond']
  return texts[index % texts.length]
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('en-IN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const viewSchedule = (plan: BnplPlan) => {
  console.log('View schedule for plan:', plan)
  // Emit event or open modal
}

const payNow = (plan: BnplPlan) => {
  emit('pay-emi', plan.id, plan.monthlyEmi)
}
</script>
