<template>
  <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8 font-['Inter',system-ui,-apple-system,sans-serif]">
    <!-- Header Section -->
    <div class="mb-8 sm:mb-12">
      <h1 class="text-2xl sm:text-3xl lg:text-4xl font-bold text-gray-900 mb-2 sm:mb-4">Admin Dashboard</h1>
      <p class="text-base sm:text-lg text-gray-600">Welcome back, Admin {{ adminName }}! Manage credit card applications and user accounts.</p>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="flex flex-col items-center justify-center py-12">
      <div class="w-8 h-8 border-4 border-gray-200 border-t-blue-600 rounded-full animate-spin mb-4"></div>
      <p class="text-gray-600">Loading dashboard data...</p>
    </div>

    <!-- Dashboard Content -->
    <div v-else class="space-y-8">
      <!-- Summary Cards Row -->
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6">
        <div class="bg-white rounded-lg border border-gray-200 p-4 sm:p-6 transition-all duration-200 hover:shadow-md">
          <div class="flex justify-between items-start mb-4">
            <h3 class="text-sm font-medium text-gray-600">Total Applications</h3>
            <div class="w-6 h-6 text-gray-400">
              <svg class="w-full h-full" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
              </svg>
            </div>
          </div>
          <div class="text-2xl sm:text-3xl font-bold text-gray-900 mb-1">{{ totalApplications }}</div>
          <div class="text-sm text-gray-500">All applications</div>
        </div>

        <div class="bg-white rounded-lg border border-gray-200 p-4 sm:p-6 transition-all duration-200 hover:shadow-md">
          <div class="flex justify-between items-start mb-4">
            <h3 class="text-sm font-medium text-gray-600">Pending Applications</h3>
            <div class="w-6 h-6 text-gray-400">
              <svg class="w-full h-full" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
            </div>
          </div>
          <div class="text-2xl sm:text-3xl font-bold text-gray-900 mb-1">{{ pendingApplications }}</div>
          <div class="text-sm text-gray-500">Require review</div>
        </div>

        <div class="bg-white rounded-lg border border-gray-200 p-4 sm:p-6 transition-all duration-200 hover:shadow-md">
          <div class="flex justify-between items-start mb-4">
            <h3 class="text-sm font-medium text-gray-600">Rejected Applications</h3>
            <div class="w-6 h-6 text-gray-400">
              <svg class="w-full h-full" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </div>
          </div>
          <div class="text-2xl sm:text-3xl font-bold text-gray-900 mb-1">{{ rejectedApplications }}</div>
          <div class="text-sm text-gray-500">Declined applications</div>
        </div>

        <div class="bg-white rounded-lg border border-gray-200 p-4 sm:p-6 transition-all duration-200 hover:shadow-md">
          <div class="flex justify-between items-start mb-4">
            <h3 class="text-sm font-medium text-gray-600">Accepted Applications</h3>
            <div class="w-6 h-6 text-gray-400">
              <svg class="w-full h-full" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
              </svg>
            </div>
          </div>
          <div class="text-2xl sm:text-3xl font-bold text-gray-900 mb-1">{{ acceptedApplications }}</div>
          <div class="text-sm text-gray-500">Approved applications</div>
        </div>
      </div>

      <!-- Detail Sections Row -->
      <div class="grid grid-cols-1 lg:grid-cols-1 gap-6">
        <!-- Recent Applications Section -->
        <div class="bg-white rounded-lg border border-gray-200 p-4 sm:p-6">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Recent Applications</h2>
          <div class="space-y-4">
            <div 
              v-for="application in recentApplications" 
              :key="application.id"
              class="flex justify-between items-center py-3 border-b border-gray-100 last:border-b-0"
            >
              <div class="flex-1">
                <h4 class="text-sm font-medium text-gray-900">{{ application.cardNetworkType }} Application</h4>
                <p class="text-sm text-gray-500">₹{{ application.requestedLimit.toLocaleString() }} limit</p>
              </div>
              <span 
                class="px-2 py-1 rounded-full text-xs font-medium uppercase tracking-wide"
                :class="{
                  'bg-gray-800 text-white': application.status === APPLICATION_STATUS.PENDING,
                  'bg-green-100 text-green-800': application.status === APPLICATION_STATUS.ACCEPTED,
                  'bg-red-100 text-red-800': application.status === APPLICATION_STATUS.REJECTED
                }"
              >
                {{ APPLICATION_STATUS_LABELS[application.status as keyof typeof APPLICATION_STATUS_LABELS] || application.status }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, computed, onMounted } from "vue"
import { useStore } from 'vuex'
import { APPLICATION_STATUS, APPLICATION_STATUS_LABELS } from '../constants/constants'
import type { CardApplication } from '../types/cardApplication'

const store = useStore()

// Reactive data
const loading = ref(true)

// Computed properties
const applications = computed(() => store.getters['applications/applications'] || [])
const user = computed(() => store.getters['auth/user'])
const adminName = computed(() => {
  if (user.value && user.value.name) {
    return user.value.name
  }
  const email = store.getters['auth/userRole'] ? 'Admin' : 'User'
  return email
})

const totalApplications = computed(() => applications.value.length)
const pendingApplications = computed(() => 
  applications.value.filter((app: CardApplication) => app.status === APPLICATION_STATUS.PENDING).length
)
const rejectedApplications = computed(() => 
  applications.value.filter((app: CardApplication) => app.status === APPLICATION_STATUS.REJECTED).length
)
const acceptedApplications = computed(() => 
  applications.value.filter((app: CardApplication) => app.status === APPLICATION_STATUS.ACCEPTED).length
)

const recentApplications = computed(() => {
  return applications.value
    .sort((a: CardApplication, b: CardApplication) => new Date(b.applicationDate).getTime() - new Date(a.applicationDate).getTime())
    .slice(0, 2)
})

// Methods
const loadApplications = async () => {
  try {
    loading.value = true
    await store.dispatch('applications/fetchApplications')
  } catch (error) {
    console.error('Error loading applications:', error)
  } finally {
    loading.value = false
  }
}

// Load applications on component mount
onMounted(() => {
  loadApplications()
})
</script> 