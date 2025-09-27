<template>
  <div class="apply-card">
    <main class="container">

      <section class="hero">
        <div class="hero-header">
          <h1>Apply for Credit Card</h1>
          <RouterLink to="/my-applications" class="btn-history">
            Application History
          </RouterLink>
        </div>
      </section>

      <section class="card-selection">
        <h2>Select Your Card</h2>
        <p class="muted">Choose the credit card that best fits your lifestyle</p>

        <div class="card-options">
          <div
            v-for="card in cards"
            :key="card.id"
            :class="['option-card', { selected: selectedCard?.id === card.id }]"
            @click="selectCard(card)"
          >
            <div class="option-header">
              <span class="icon">{{ card.icon }}</span>
              <span v-if="selectedCard?.id === card.id" class="checkmark">✔</span>
            </div>
            <h3>{{ card.name }}</h3>
            <ul>
              <li v-for="benefit in card.benefits" :key="benefit">⚡ {{ benefit }}</li>
            </ul>
            <p class="limit">Credit Limit ₹{{ card.limitRange }}</p>
            <div class="tags">
              <span v-for="tag in card.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- Application Form -->
      <section v-if="selectedCard" class="application-form">
        <h2>{{ selectedCard.name }} Application</h2>
        <p class="muted">Complete your application details</p>

        <form @submit.prevent="submitApplication">
          <div class="form-row">
            <label>Requested Credit Limit</label>
            <input
              type="number"
              v-model="form.creditLimit"
              :min="selectedCard.minLimit"
              :max="selectedCard.maxLimit"
              required
            />
            <small>Range: ₹{{ selectedCard.minLimit }} – ₹{{ selectedCard.maxLimit }}</small>
          </div>

          <div class="form-row info">
            <h4>Applicant Information</h4>
            <p><strong>Name:</strong> {{ user.name }}</p>
            <p><strong>Email:</strong> {{ user.email }}</p>
            <p><strong>BNPL Eligible:</strong> {{ user.bnplEligible ? "Yes" : "No" }}</p>
          </div>

          <button type="submit" class="submit-btn">Submit Application</button>
        </form>
      </section>
    </main>
  </div>
</template>

<script>
import { getCardTypes } from '../../services/cards-service';
import { applyForCard } from "../../services/cardApplicationService";
import { getUserIdFromToken } from '../../utils/getTokenData';

export default {
  name: "ApplyCardView",
  data() {
    return {
      user: {
        name: "Rahul Verma",
        email: "rahul@example.com",
        bnplEligible: true,
      },
      cards: [],              // will be filled after fetching
      selectedCard: null,
      form: {
        creditLimit: "",
      },
    };
  },

  async created() {
    try {
      const fetchedCards = await getCardTypes();
      console.log("Fetched cards from backend:", fetchedCards);

      // 🔑 Map backend structure to the expected frontend structure
      this.cards = fetchedCards.map(card => ({
        id: card.id,
        name: card.name,
        icon: card.networkType === 'AMEX' ? '⭐' : '💳',   // pick icon by networkType
        benefits: [card.description],                    // use description as benefit for now
        limitRange: `${card.minCardLimit.toLocaleString()} – ${card.maxCardLimit.toLocaleString()}`,
        minLimit: card.minCardLimit,
        maxLimit: card.maxCardLimit,
        tags: [card.networkType],                         // add tags based on network type
      }));

      console.log("Mapped cards for frontend:", this.cards);
    } catch (err) {
      console.error("Error fetching cards:", err);
    }
  },

  methods: {
    selectCard(card) {
      this.selectedCard = card;
      this.form.creditLimit = card.minLimit;
    },
    submitApplication() {
      const cardApplication = {};
      cardApplication.cardTypeId = this.selectedCard.id;
      cardApplication.requestLimit = this.form.creditLimit;
      cardApplication.userId = getUserIdFromToken();
      console.log(cardApplication," = card application data");
      applyForCard(cardApplication);
      this.$router.push("/my-applications");
      alert(
        `Applied for ${this.selectedCard.name} with credit limit ₹${this.form.creditLimit}`
      );
    },
  },
};
</script>


<style scoped>
.apply-card {
  font-family: "Inter", system-ui, Arial;
  background: #f7f7f9;
  min-height: 100vh;
}

.container {
  max-width: 1180px;
  margin: 28px auto;
  padding: 0 20px;
}

.hero h1 {
  font-size: 28px;
  margin-bottom: 6px;
}

.subtitle,
.muted {
  color: #7b7b85;
  margin-bottom: 20px;
}

.card-options {
  display: flex;
  gap: 20px;
  margin-top: 20px;
}

.option-card {
  flex: 1;
  background: #fff;
  padding: 20px;
  border-radius: 12px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border 0.2s;
}

.option-card.selected {
  border-color: #1f6feb;
}

.option-card h3 {
  margin: 10px 0;
}

.option-card ul {
  margin: 0;
  padding: 0;
  list-style: none;
}

.limit {
  margin: 10px 0;
  font-weight: 600;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag {
  background: #f0f4f8;
  padding: 4px 8px;
  border-radius: 8px;
  font-size: 12px;
}

.checkmark {
  color: #1f6feb;
  font-weight: bold;
}

.application-form {
  margin-top: 40px;
  background: #fff;
  padding: 20px;
  border-radius: 12px;
}

.form-row {
  margin-bottom: 16px;
}

.form-row input {
  display: block;
  padding: 10px;
  width: 100%;
  margin-top: 6px;
  border-radius: 6px;
  border: 1px solid #ddd;
}

.form-row small {
  color: #7b7b85;
}

.submit-btn {
  background: #1f6feb;
  color: #fff;
  border: none;
  padding: 12px 18px;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
}
/* Container for title and button */
.hero-header {
  display: flex;
  justify-content: space-between; /* places items on opposite ends */
  align-items: center;
  margin-bottom: 10px;
}

/* History Button */
.btn-history {
  background: #ffd60a;              /* bright yellow accent */
  color: #0b2540;                    /* dark text for contrast */
  font-weight: 600;
  padding: 10px 18px;
  font-size: 15px;
  border-radius: 8px;
  border: 2px solid #ffd60a;
  text-decoration: none;
  transition: all 0.2s ease;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
  cursor: pointer;
}

/* Hover and focus styles */
.btn-history:hover {
  background: #e5cd56;
  border-color: #e5cd56;
  box-shadow: 0 6px 14px rgba(0, 0, 0, 0.18);
}

.btn-history:focus {
  outline: 2px solid rgba(31, 111, 235, 0.3);
  outline-offset: 3px;
}

</style>
