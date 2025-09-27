<template>
  <div class="admin-review-center">
    <!-- Header Section -->
    <div class="header-section">
      <h1 class="main-title">Admin Review Center</h1>
      <p class="subtitle">Review and approve applications, limit changes, and flagged transactions</p>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <p class="loading-text">Loading applications...</p>
    </div>

    <!-- Card Applications Section -->
    <div v-else class="applications-section">
      <h2 class="section-title">Card Applications</h2>
      
      <div v-if="applications.length === 0" class="empty-state">
        <p class="empty-text">No applications found</p>
      </div>
      
      <div v-else class="applications-grid">
        <div 
          v-for="application in applications" 
          :key="application.id"
          class="application-card"
        >
          <div class="card-header">
            <h3 class="card-title">{{ application.cardNetworkType }} Application #{{ application.id }}</h3>
            <span 
              class="status-badge"
              :class="{
                'status-pending': application.status === 'PENDING',
                'status-approved': application.status === 'ACCEPTED',
                'status-rejected': application.status === 'REJECTED'
              }"
            >
              {{ application.status }}
            </span>
          </div>
          
          <div class="card-details">
            <div class="detail-item">
              <span class="detail-label">Requested Limit</span>
              <span class="detail-value">₹{{ application.requestedLimit.toLocaleString() }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Application Date</span>
              <span class="detail-value">{{ formatDate(application.applicationDate) }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">User</span>
              <span class="detail-value">{{ application.userName }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Card Type</span>
              <span class="detail-value">{{ application.cardTypeName }}</span>
            </div>
          </div>
          
          <!-- Actions for pending applications -->
          <div v-if="application.status === 'PENDING'" class="card-actions">
            <button 
              class="action-btn approve-btn" 
              @click="handleApprove(application.id)"
              :disabled="processing"
            >
              <svg class="btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
              </svg>
              Approve
            </button>
            <button 
              class="action-btn reject-btn" 
              @click="handleReject(application.id)"
              :disabled="processing"
            >
              <svg class="btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
              Reject
            </button>
          </div>
          
          <!-- Reviewer info for processed applications -->
          <div v-else class="reviewer-info">
            <span class="reviewer-text">
              {{ application.status === 'ACCEPTED' ? 'Approved' : 'Rejected' }} by {{ application.reviewerName }} on {{ formatDate(application.reviewDate!) }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from "vue"
import { getCardApplications, updateApplicationStatus } from '../services/cards-service'
import type { CardApplication } from '../types/cardApplication'

// Reactive data
const applications = ref<CardApplication[]>([])
const loading = ref(true)
const processing = ref(false)

// Methods
const loadApplications = async () => {
  try {
    loading.value = true
    const data = await getCardApplications()
    applications.value = data
  } catch (error) {
    console.error('Error loading applications:', error)
  } finally {
    loading.value = false
  }
}

const handleApprove = async (applicationId: number) => {
  try {
    processing.value = true
    const result = await updateApplicationStatus(applicationId, 'ACCEPTED')
    if (result) {
      // Update the local state
      const index = applications.value.findIndex(app => app.id === applicationId)
      if (index !== -1) {
        applications.value[index] = result
      }
    }
  } catch (error) {
    console.error('Error approving application:', error)
  } finally {
    processing.value = false
  }
}

const handleReject = async (applicationId: number) => {
  try {
    processing.value = true
    const result = await updateApplicationStatus(applicationId, 'REJECTED')
    if (result) {
      // Update the local state
      const index = applications.value.findIndex(app => app.id === applicationId)
      if (index !== -1) {
        applications.value[index] = result
      }
    }
  } catch (error) {
    console.error('Error rejecting application:', error)
  } finally {
    processing.value = false
  }
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('en-IN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

// Load applications on component mount
onMounted(() => {
  loadApplications()
})
</script>

<style scoped>
@reference "tailwindcss";

.admin-review-center {
  @apply max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-6 sm:py-8;
  font-family: "Inter", system-ui, -apple-system, sans-serif;
}

/* Header Section */
.header-section {
  @apply mb-8 sm:mb-12;
}

.main-title {
  @apply text-2xl sm:text-3xl lg:text-4xl font-bold text-gray-900 mb-2 sm:mb-4;
}

.subtitle {
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

/* Applications Section */
.applications-section {
  @apply space-y-6 sm:space-y-8;
}

.section-title {
  @apply text-xl sm:text-2xl font-semibold text-gray-900 mb-4 sm:mb-6;
}

.applications-grid {
  @apply space-y-4 sm:space-y-6;
}

/* Application Cards */
.application-card {
  @apply bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-6 transition-all duration-200 hover:shadow-md;
}

.card-header {
  @apply flex flex-col sm:flex-row sm:justify-between sm:items-start mb-4 sm:mb-6 space-y-2 sm:space-y-0;
}

.card-title {
  @apply text-lg sm:text-xl font-semibold text-gray-900;
}

.status-badge {
  @apply px-3 py-1 rounded-full text-xs font-medium uppercase tracking-wide self-start;
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

/* Card Details */
.card-details {
  @apply grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 sm:gap-6 mb-4 sm:mb-6;
}

.detail-item {
  @apply flex flex-col space-y-1;
}

.detail-label {
  @apply text-sm text-gray-500;
}

.detail-value {
  @apply text-base sm:text-lg font-semibold text-gray-900;
}

/* Card Actions */
.card-actions {
  @apply flex flex-col sm:flex-row items-stretch sm:items-center space-y-2 sm:space-y-0 sm:space-x-3;
}

.action-btn {
  @apply flex items-center justify-center px-4 py-2 rounded-md font-medium transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed;
}

.approve-btn {
  @apply bg-green-600 text-white hover:bg-green-700 focus:ring-green-500;
}

.reject-btn {
  @apply bg-red-600 text-white hover:bg-red-700 focus:ring-red-500;
}

.btn-icon {
  @apply w-4 h-4 mr-2;
}

/* Reviewer Info */
.reviewer-info {
  @apply mt-4;
}

.reviewer-text {
  @apply text-sm text-gray-500;
}

/* Empty State */
.empty-state {
  @apply text-center py-12;
}

.empty-text {
  @apply text-gray-500 text-lg;
}

/* Responsive Design */
@media (max-width: 640px) {
  .card-details {
    @apply grid-cols-1 gap-4;
  }
  
  .card-actions {
    @apply flex-col space-y-2;
  }
  
  .action-btn {
    @apply w-full justify-center;
  }
}

/* Tablet and up */
@media (min-width: 768px) {
  .applications-grid {
    @apply grid grid-cols-1 lg:grid-cols-2 gap-6;
  }
}

/* Large screens */
@media (min-width: 1024px) {
  .applications-grid {
    @apply grid-cols-1;
  }
}
</style>