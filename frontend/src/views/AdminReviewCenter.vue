<template>
  <div class="admin-review-center">
    <!-- Header Section -->
    <div class="header-section">
      <h1 class="main-title">Admin Review Center</h1>
      <p class="subtitle">Review and approve applications, limit changes, and flagged transactions</p>
    </div>

    <!-- Card Applications Section -->
    <div class="applications-section">
      <h2 class="section-title">Card Applications</h2>
      
      <div class="applications-grid">
        <!-- Application Card 1: VISA Application #201 (PENDING) -->
        <div class="application-card">
          <div class="card-header">
            <h3 class="card-title">VISA Application #201</h3>
            <span class="status-badge status-pending">PENDING</span>
          </div>
          
          <div class="card-details">
            <div class="detail-item">
              <span class="detail-label">Requested Limit</span>
              <span class="detail-value">₹50,000</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Application Date</span>
              <span class="detail-value">10/09/2025</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">User ID</span>
              <span class="detail-value">#1</span>
            </div>
          </div>
          
          <div class="card-actions">
            <button class="action-btn approve-btn" @click="handleApprove(201)">
              <svg class="btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
              </svg>
              Approve
            </button>
            <button class="action-btn reject-btn" @click="handleReject(201)">
              <svg class="btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
              Reject
            </button>
          </div>
        </div>

        <!-- Application Card 2: AMEX Application #202 (APPROVED) -->
        <div class="application-card">
          <div class="card-header">
            <h3 class="card-title">AMEX Application #202</h3>
            <span class="status-badge status-approved">APPROVED</span>
          </div>
          
          <div class="card-details">
            <div class="detail-item">
              <span class="detail-label">Requested Limit</span>
              <span class="detail-value">₹80,000</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Application Date</span>
              <span class="detail-value">08/09/2025</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">User ID</span>
              <span class="detail-value">#3</span>
            </div>
          </div>
          
          <div class="reviewer-info">
            <span class="reviewer-text">Reviewed by Admin Ayesha on 12/09/2025</span>
          </div>
        </div>

        <!-- Additional Application Cards -->
        <div class="application-card">
          <div class="card-header">
            <h3 class="card-title">MASTERCARD Application #203</h3>
            <span class="status-badge status-pending">PENDING</span>
          </div>
          
          <div class="card-details">
            <div class="detail-item">
              <span class="detail-label">Requested Limit</span>
              <span class="detail-value">₹25,000</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Application Date</span>
              <span class="detail-value">15/09/2025</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">User ID</span>
              <span class="detail-value">#5</span>
            </div>
          </div>
          
          <div class="card-actions">
            <button class="action-btn approve-btn" @click="handleApprove(203)">
              <svg class="btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7"></path>
              </svg>
              Approve
            </button>
            <button class="action-btn reject-btn" @click="handleReject(203)">
              <svg class="btn-icon" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
              </svg>
              Reject
            </button>
          </div>
        </div>

        <div class="application-card">
          <div class="card-header">
            <h3 class="card-title">VISA Application #204</h3>
            <span class="status-badge status-rejected">REJECTED</span>
          </div>
          
          <div class="card-details">
            <div class="detail-item">
              <span class="detail-label">Requested Limit</span>
              <span class="detail-value">₹1,00,000</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">Application Date</span>
              <span class="detail-value">05/09/2025</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">User ID</span>
              <span class="detail-value">#2</span>
            </div>
          </div>
          
          <div class="reviewer-info">
            <span class="reviewer-text">Rejected by Admin John on 07/09/2025</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from "vue"

// Reactive data
const applications = ref([
  {
    id: 201,
    type: "VISA",
    status: "PENDING",
    requestedLimit: "₹50,000",
    applicationDate: "10/09/2025",
    userId: "#1"
  },
  {
    id: 202,
    type: "AMEX",
    status: "APPROVED",
    requestedLimit: "₹80,000",
    applicationDate: "08/09/2025",
    userId: "#3",
    reviewer: "Admin Ayesha",
    reviewDate: "12/09/2025"
  }
])

// Methods
const handleApprove = (applicationId: number) => {
  console.log(`Approving application ${applicationId}`)
  // TODO: Implement API call to approve application
}

const handleReject = (applicationId: number) => {
  console.log(`Rejecting application ${applicationId}`)
  // TODO: Implement API call to reject application
}
</script>

<style scoped>
@reference "tailwindcss";

.admin-review-center {
  @apply mx-auto;
  font-family: "Inter", system-ui, -apple-system, sans-serif;
}

/* Header Section */
.header-section {
  @apply mb-6;
}

.main-title {
  @apply text-4xl font-bold text-gray-900 mb-4;
}

.subtitle {
  @apply text-lg text-gray-600;
}

/* Applications Section */
.applications-section {
  @apply space-y-8;
}

.section-title {
  @apply text-2xl font-semibold text-gray-900 mb-6;
}

.applications-grid {
  @apply space-y-6;
}

/* Application Cards */
.application-card {
  @apply bg-white rounded-lg shadow-sm border border-gray-200 p-6 transition-all duration-200 hover:shadow-md;
}

.card-header {
  @apply flex justify-between items-start mb-6;
}

.card-title {
  @apply text-xl font-semibold text-gray-900;
}

.status-badge {
  @apply px-3 py-1 rounded-full text-xs font-medium uppercase tracking-wide;
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
  @apply grid grid-cols-3 gap-6 mb-6;
}

.detail-item {
  @apply flex flex-col space-y-1;
}

.detail-label {
  @apply text-sm text-gray-500;
}

.detail-value {
  @apply text-lg font-semibold text-gray-900;
}

/* Card Actions */
.card-actions {
  @apply flex items-center space-x-3;
}

.action-btn {
  @apply flex items-center px-4 py-2 rounded-md font-medium transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2;
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

/* Responsive Design */
@media (max-width: 768px) {
  .card-details {
    @apply grid-cols-1 gap-4;
  }
  
  .card-actions {
    @apply flex-col space-x-0 space-y-2;
  }
  
  .action-btn {
    @apply w-full justify-center;
  }
}
</style>