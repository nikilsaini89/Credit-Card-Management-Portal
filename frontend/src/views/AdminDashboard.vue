<template>
  <div class="admin-dashboard">
    <!-- Header Section -->
    <div class="header-section">
      <h1 class="main-title">Admin Dashboard</h1>
      <p class="welcome-message">Welcome back, Admin {{ adminName }}! Manage credit card applications and user accounts.</p>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p class="loading-text">Loading dashboard data...</p>
    </div>

    <!-- Dashboard Content -->
    <div v-else class="dashboard-content">
      <!-- Summary Cards Row -->
      <div class="summary-cards">
        <div class="summary-card">
          <div class="card-header">
            <h3 class="card-title">Total Applications</h3>
            <div class="card-icon">
              <svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"></path>
              </svg>
            </div>
          </div>
          <div class="card-value">{{ totalApplications }}</div>
          <div class="card-subtitle">All applications</div>
        </div>

        <div class="summary-card">
          <div class="card-header">
            <h3 class="card-title">Pending Applications</h3>
            <div class="card-icon">
              <svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
              </svg>
            </div>
          </div>
          <div class="card-value">{{ pendingApplications }}</div>
          <div class="card-subtitle">Require review</div>
        </div>

        <div class="summary-card">
          <div class="card-header">
            <h3 class="card-title">Rejected Applications</h3>
            <div class="card-icon">
              <svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
            </div>
          </div>
          <div class="card-value">{{ rejectedApplications }}</div>
          <div class="card-subtitle">Declined applications</div>
        </div>

        <div class="summary-card">
          <div class="card-header">
            <h3 class="card-title">Accepted Applications</h3>
            <div class="card-icon">
              <svg class="icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
              </svg>
            </div>
          </div>
          <div class="card-value">{{ acceptedApplications }}</div>
          <div class="card-subtitle">Approved applications</div>
        </div>
      </div>

      <!-- Detail Sections Row -->
      <div class="detail-sections">
        <!-- Recent Applications Section -->
        <div class="detail-section">
          <h2 class="section-title">Recent Applications</h2>
          <div class="applications-list">
            <div 
              v-for="application in recentApplications" 
              :key="application.id"
              class="application-item"
            >
              <div class="application-info">
                <h4 class="application-name">{{ application.cardNetworkType }} Application</h4>
                <p class="application-details">₹{{ application.requestedLimit.toLocaleString() }} limit</p>
              </div>
              <span 
                class="status-badge"
                :class="getStatusBadgeClass(application.status)"
              >
                {{ APPLICATION_STATUS_LABELS[application.status] }}
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
const adminName = ref('Ayesha') // This could be fetched from user profile

// Computed properties
const applications = computed(() => store.getters['applications/applications'] || [])
const totalApplications = computed(() => applications.value.length)
const pendingApplications = computed(() => 
  applications.value.filter(app => app.status === APPLICATION_STATUS.PENDING).length
)
const rejectedApplications = computed(() => 
  applications.value.filter(app => app.status === APPLICATION_STATUS.REJECTED).length
)
const acceptedApplications = computed(() => 
  applications.value.filter(app => app.status === APPLICATION_STATUS.ACCEPTED).length
)

const recentApplications = computed(() => {
  return applications.value
    .sort((a, b) => new Date(b.applicationDate).getTime() - new Date(a.applicationDate).getTime())
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

const getStatusBadgeClass = (status: string) => {
  switch (status) {
    case APPLICATION_STATUS.PENDING:
      return 'status-pending'
    case APPLICATION_STATUS.ACCEPTED:
      return 'status-approved'
    case APPLICATION_STATUS.REJECTED:
      return 'status-rejected'
    default:
      return 'status-pending'
  }
}

// Load applications on component mount
onMounted(() => {
  loadApplications()
})
</script>

<style scoped>
@reference "tailwindcss";

.admin-dashboard {
  @apply max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8;
  font-family: "Inter", system-ui, -apple-system, sans-serif;
}

/* Header Section */
.header-section {
  @apply mb-8 sm:mb-12;
}

.main-title {
  @apply text-2xl sm:text-3xl lg:text-4xl font-bold text-gray-900 mb-2 sm:mb-4;
}

.welcome-message {
  @apply text-base sm:text-lg text-gray-600;
}

/* Loading State */
.loading-container {
  @apply flex flex-col items-center justify-center py-12;
}

.loading-spinner {
  @apply w-8 h-8 border-4 border-gray-200 border-t-blue-600 rounded-full animate-spin mb-4;
}

.loading-text {
  @apply text-gray-600;
}

/* Dashboard Content */
.dashboard-content {
  @apply space-y-8;
}

/* Summary Cards */
.summary-cards {
  @apply grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6;
}

.summary-card {
  @apply bg-white rounded-lg border border-gray-200 p-4 sm:p-6 transition-all duration-200 hover:shadow-md;
}

.card-header {
  @apply flex justify-between items-start mb-4;
}

.card-title {
  @apply text-sm font-medium text-gray-600;
}

.card-icon {
  @apply w-6 h-6 text-gray-400;
}

.icon {
  @apply w-full h-full;
}

.card-value {
  @apply text-2xl sm:text-3xl font-bold text-gray-900 mb-1;
}

.card-subtitle {
  @apply text-sm text-gray-500;
}

/* Detail Sections */
.detail-sections {
  @apply grid grid-cols-1 lg:grid-cols-1 gap-6;
}

.detail-section {
  @apply bg-white rounded-lg border border-gray-200 p-4 sm:p-6;
}

.section-title {
  @apply text-lg font-semibold text-gray-900 mb-4;
}

/* Applications List */
.applications-list {
  @apply space-y-4;
}

.application-item {
  @apply flex justify-between items-center py-3 border-b border-gray-100 last:border-b-0;
}

.application-info {
  @apply flex-1;
}

.application-name {
  @apply text-sm font-medium text-gray-900;
}

.application-details {
  @apply text-sm text-gray-500;
}

.status-badge {
  @apply px-2 py-1 rounded-full text-xs font-medium uppercase tracking-wide;
}

.status-pending {
  @apply bg-gray-800 text-white;
}

.status-approved {
  @apply bg-green-100 text-green-800;
}

.status-rejected {
  @apply bg-red-100 text-red-800;
}

/* Responsive Design */
@media (max-width: 640px) {
  .summary-cards {
    @apply grid-cols-1 gap-4;
  }
  
  .card-value {
    @apply text-xl;
  }
}

@media (min-width: 768px) {
  .summary-cards {
    @apply grid-cols-2;
  }
}

@media (min-width: 1024px) {
  .summary-cards {
    @apply grid-cols-4;
  }
}
</style> 