<template>
  <div class="applications">
    <main class="container">
      <!-- Header -->
      <section class="header">
        <div>
          <h1>My Applications</h1>
          <p class="subtitle">Track your credit card application status</p>
        </div>
        <RouterLink to="/apply-card" class="btn-primary">+ Apply New Card</RouterLink>
      </section>

      <!-- Application List -->
      <section class="application-list">
        <div
          v-for="app in applications"
          :key="app.id"
          class="application-card"
        >
          <div class="card-header">
            <div class="title">
              <span class="icon">💳</span>
              <!-- use backend cardTypeName -->
              <h3>{{ app.cardTypeName }}</h3>
              <span class="status-pill">{{ app.status }}</span>
            </div>
            <div class="applied-date">
              <span>📅 Applied: {{ app.appliedDate }}</span>
            </div>
          </div>

          <div class="card-body">
            <p><strong>Application ID:</strong> #{{ app.id }}</p>
            <p><strong>Card Network:</strong> {{ app.cardNetworkType }}</p>
            <p><strong>Requested Credit Limit:</strong> ₹{{ app.requestedLimit.toLocaleString() }}</p>

            <p>
              <strong>Status:</strong>
              <span class="status-pill">{{ app.status }}</span>
              {{ app.statusText }}
            </p>

            <div class="timeline">
              <p>• Application submitted on {{ app.appliedDate }}</p>
              <p v-if="app.reviewerName">
                • Reviewed by <strong>{{ app.reviewerName }}</strong>
                on {{ app.reviewDate }}
              </p>
              <p class="muted">
                • {{ app.statusText === 'Under Review'
                    ? 'Under review – typically takes 2-3 business days'
                    : 'Final decision reached' }}
              </p>
            </div>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script>
import { fetchApplications } from "../../services/cardApplicationService";

// Map backend status → user-friendly text
const STATUS_TEXT_MAP = {
  PENDING: "Under Review",
  ACCEPTED: "Approved",
  REJECTED: "Rejected",
};

export default {
  name: "MyApplicationsView",
  data() {
    return {
      applications: [],
    };
  },

  async created() {
    try {
      const fetched = await fetchApplications();
      console.log("Fetched raw apps:", fetched);

      // Map backend → frontend
      this.applications = fetched.map(app => ({
        id: app.id,
        cardTypeName: app.cardTypeName,
        cardNetworkType: app.cardNetworkType,
        requestedLimit: app.requestedLimit,
        status: app.status,
        statusText: STATUS_TEXT_MAP[app.status] || "Unknown",
        appliedDate: this.formatDate(app.applicationDate),
        reviewerName: app.reviewerName || null,
        reviewDate: app.reviewDate ? this.formatDate(app.reviewDate) : null,
      }));

      console.log("Mapped applications:", this.applications);
    } catch (err) {
      console.error("Error fetching applications:", err);
    }
  },

  methods: {
    formatDate(dateString) {
      if (!dateString) return "";
      const date = new Date(dateString);
      return date.toLocaleDateString("en-GB", {
        day: "2-digit",
        month: "long",
        year: "numeric",
      });
    },
  },
};
</script>

<style scoped>
/* existing styles remain unchanged */
.applications {
  font-family: "Inter", system-ui, Arial;
  background: #f7f7f9;
  min-height: 100vh;
}
.container {
  max-width: 1180px;
  margin: 28px auto;
  padding: 0 20px;
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
}
.subtitle {
  color: #7b7b85;
}
.btn-primary {
  background: #0b0d18;
  color: #fff;
  text-decoration: none;
  padding: 10px 18px;
  border-radius: 8px;
  font-weight: 600;
}
.application-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.application-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.title {
  display: flex;
  align-items: center;
  gap: 10px;
}
.status-pill {
  background: #0b0d18;
  color: #fff;
  padding: 2px 10px;
  font-size: 12px;
  border-radius: 12px;
}
.timeline {
  margin-top: 12px;
}
.timeline .muted {
  color: #7b7b85;
}
.applied-date {
  color: #7b7b85;
  font-size: 14px;
}
</style>
